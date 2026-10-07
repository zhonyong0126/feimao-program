package com.feimao.controller.order;

import com.feimao.mapper.PaymentNotifyMapper;
import com.feimao.service.InsuranceTaskService;
import com.feimao.service.SmsNotificationService;
import com.feimao.service.WechatPayService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notify")
public class NotifyController {
  private final PaymentNotifyMapper payments;
  private final WechatPayService pay;
  private final InsuranceTaskService insuranceTasks;
  private final SmsNotificationService notifications;

  public NotifyController(
      PaymentNotifyMapper payments,
      WechatPayService pay,
      InsuranceTaskService insuranceTasks,
      SmsNotificationService notifications) {
    this.payments = payments;
    this.pay = pay;
    this.insuranceTasks = insuranceTasks;
    this.notifications = notifications;
  }

  @PostMapping(
      value = {"/leaseCarNotify", "/buyCarNotify", "/maintainNotify"},
      produces = MediaType.APPLICATION_XML_VALUE)
  @Transactional
  public String orderNotify(@RequestBody String xml) {
    try {
      Map<String, String> callback = pay.parse(xml);
      if (!pay.verify(callback)
          || !"SUCCESS".equals(callback.get("return_code"))
          || !"SUCCESS".equals(callback.get("result_code"))) return pay.failXml("签名或支付状态错误");
      String number = callback.get("out_trade_no");
      Map<String, Object> order = payments.lockOrder(number);
      // The legacy WeChat callback acknowledges deleted/missing and duplicate orders so that
      // WeChat does not keep retrying a callback that cannot be applied.
      if (order == null || order.isEmpty()) return pay.successXml();
      if (((Number) order.get("state")).intValue() != 1) return pay.successXml();
      long expected =
          money(order.get("price"))
              .subtract(money(order.get("discounts_price")))
              .multiply(BigDecimal.valueOf(100))
              .setScale(0, RoundingMode.HALF_UP)
              .longValue();
      long total = cents(callback, "total_fee", -1);
      if (expected != total) return pay.failXml("支付金额不一致");
      long paid = cents(callback, "cash_fee", total);
      if (paid < 0) return pay.failXml("支付金额错误");
      long now = System.currentTimeMillis() / 1000;
      long paidAt = callbackTime(callback, now);
      long coupon =
          order.get("coupon_id") == null ? 0 : ((Number) order.get("coupon_id")).longValue();
      if (coupon > 0
          && payments.useCoupon(
                  coupon, ((Number) order.get("user_id")).longValue(), now)
              != 1) return pay.failXml("优惠券不可用");
      if (payments.markOrderPaid(
              ((Number) order.get("id")).longValue(),
              BigDecimal.valueOf(paid, 2),
              callback.get("transaction_id"),
              xml,
              paidAt,
              now)
          != 1) return rollback("修改订单失败");
      if (((Number) order.get("type")).intValue() == 1) {
        payments.insertLeasePaymentLog(
            ((Number) order.get("user_id")).longValue(),
            ((Number) order.get("id")).longValue(),
            BigDecimal.valueOf(paid, 2),
            now);
        TransactionSynchronizationManager.registerSynchronization(
            new TransactionSynchronization() {
              @Override
              public void afterCommit() {
                insuranceTasks.synchronizePoliciesNow();
                notifications.notifyMerchantOrderPaid(((Number) order.get("id")).longValue());
              }
            });
      }
      return pay.successXml();
    } catch (Exception e) {
      return rollback(e.getMessage());
    }
  }

  @PostMapping(
      value = "/installmentNotify",
      produces = MediaType.APPLICATION_XML_VALUE)
  @Transactional
  public String installmentNotify(@RequestBody String xml) {
    try {
      Map<String, String> callback = pay.parse(xml);
      if (!pay.verify(callback)
          || !"SUCCESS".equals(callback.get("return_code"))
          || !"SUCCESS".equals(callback.get("result_code"))) return pay.failXml("签名或支付状态错误");
      Map<String, Object> child = payments.lockInstallment(callback.get("out_trade_no"));
      if (child == null || child.isEmpty()) return pay.successXml();
      if (((Number) child.get("state")).intValue() != 1) return pay.successXml();
      String parentNumber = String.valueOf(child.get("number"));
      Map<String, Object> parent = payments.lockInstallmentParent(parentNumber);
      if (parent == null || parent.isEmpty()) return pay.successXml();
      long total = cents(callback, "total_fee", -1),
          expected =
              money(child.get("price"))
                  .multiply(BigDecimal.valueOf(100))
                  .setScale(0, RoundingMode.HALF_UP)
                  .longValue();
      if (total != expected) return pay.failXml("支付金额不一致");
      long paid = cents(callback, "cash_fee", total);
      if (paid < 0) return pay.failXml("支付金额错误");
      long now = System.currentTimeMillis() / 1000;
      long paidAt = callbackTime(callback, now);
      if (payments.markInstallmentPaid(
              ((Number) child.get("id")).longValue(),
              BigDecimal.valueOf(paid, 2),
              callback.get("transaction_id"),
              paidAt,
              now)
          != 1) return rollback("修改分期记录失败");
      if (payments.addInstallmentPayment(
              parentNumber,
              BigDecimal.valueOf(paid, 2),
              ((Number) child.get("num")).intValue(),
              now)
          != 1) return rollback("修改主订单失败");
      return pay.successXml();
    } catch (Exception e) {
      return rollback(e.getMessage());
    }
  }

  private String rollback(String message) {
    if (TransactionSynchronizationManager.isActualTransactionActive())
      TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
    return pay.failXml(message);
  }

  private static long callbackTime(Map<String, String> callback, long fallback) {
    String value = callback.get("time_end");
    if (value == null || value.trim().isEmpty()) return fallback;
    try {
      return LocalDateTime.parse(value.trim(), DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
          .atZone(ZoneId.systemDefault())
          .toEpochSecond();
    } catch (DateTimeParseException ignored) {
      return fallback;
    }
  }

  private static BigDecimal money(Object value) {
    return value == null ? BigDecimal.ZERO : new BigDecimal(value.toString());
  }

  private static long cents(Map<String, String> callback, String key, long fallback) {
    try {
      String value = callback.get(key);
      return value == null || value.trim().isEmpty() ? fallback : Long.parseLong(value.trim());
    } catch (NumberFormatException exception) {
      return fallback;
    }
  }
}
