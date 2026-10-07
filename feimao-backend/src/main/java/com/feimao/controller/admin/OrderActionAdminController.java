package com.feimao.controller.admin;

import com.feimao.common.BusinessResponse;
import com.feimao.controller.store.StoreController;
import com.feimao.mapper.AdminLogMapper;
import com.feimao.mapper.OrderActionMapper;
import com.feimao.service.AdminSessionService;
import com.feimao.service.InsuranceService;
import com.feimao.service.WechatPayService;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/business/orders")
public class OrderActionAdminController {
  private final OrderActionMapper orders;
  private final AdminLogMapper logs;
  private final AdminSessionService sessions;
  private final StoreController storeController;
  private final WechatPayService pay;
  private final InsuranceService insurance;

  public OrderActionAdminController(
      OrderActionMapper orders,
      AdminLogMapper logs,
      AdminSessionService sessions,
      StoreController storeController,
      WechatPayService pay,
      InsuranceService insurance) {
    this.orders = orders;
    this.logs = logs;
    this.sessions = sessions;
    this.storeController = storeController;
    this.pay = pay;
    this.insurance = insurance;
  }

  @PostMapping("/{id}/confirm-return")
  @Transactional
  public BusinessResponse<?> confirmReturn(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable long id,
      @RequestBody(required = false) Map<String, Object> body) {
    Long adminId = sessions.adminId(token);
    if (adminId == null) return unauthorized();
    Map<String, Object> order = orders.selectReturnOrder(id);
    if (order == null || order.isEmpty()) return BusinessResponse.error("订单不存在");
    Map<String, Object> request = new LinkedHashMap<>();
    request.put("order_number", order.get("number"));
    request.put("return_type", 1);
    request.put("refund_cash_price", money(body, "refund_cash_price"));
    BusinessResponse<?> result =
        storeController.settleBackCarForAdmin(
            ((Number) order.get("merchant_id")).longValue(), request);
    if (result.getCode() == 1)
      log(
          adminId,
          "/api/admin/business/orders/" + id + "/confirm-return",
          "管理员确认还车",
          String.valueOf(id));
    return result;
  }

  @PostMapping("/{id}/cancel")
  @Transactional
  public BusinessResponse<?> cancel(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable long id,
      @RequestBody(required = false) Map<String, Object> body) {
    Long adminId = sessions.adminId(token);
    if (adminId == null) return unauthorized();
    Map<String, Object> order = orders.lockOrder(id);
    if (order == null || order.isEmpty()) return BusinessResponse.error("订单不存在");
    int state = integer(order.get("state"));
    int type = integer(order.get("type"));
    if (state < 1 || state > 3 || integer(order.get("pick_state")) != 1)
      return BusinessResponse.error("当前订单状态不允许取消");
    BigDecimal refund = money(body, "cancel_refund_price");
    BigDecimal paid = decimal(order.get("pay_price"));
    if (refund.signum() < 0 || refund.compareTo(paid) > 0)
      return BusinessResponse.error("退款金额不能大于订单支付金额：" + paid);
    String refundNumber = "";
    if (state > 1 && refund.signum() > 0) {
      refundNumber = refundNumber();
      try {
        pay.refund(
            String.valueOf(order.get("transaction_id")), refundNumber, cents(paid), cents(refund));
      } catch (IllegalStateException exception) {
        return BusinessResponse.error(exception.getMessage());
      }
      String typeName = type == 1 ? "租车" : type == 2 ? "购车" : "维修";
      orders.insertRefundLog(
          ((Number) order.get("user_id")).longValue(),
          refund,
          id,
          typeName + "取消订单退款",
          now());
    }
    long current = now();
    if (state > 1 && type == 1) insurance.cancelOrderGuests(id, current);
    BigDecimal remaining = decimal(order.get("last_all_money")).subtract(refund);
    if (remaining.signum() < 0) remaining = BigDecimal.ZERO;
    int changed =
        orders.cancelOrder(id, state, adminId, refund, refundNumber, remaining, current);
    if (changed == 0) return BusinessResponse.error("订单状态已变化，请刷新后重试");
    if (type == 1
        && order.get("merchant_car_id") != null
        && ((Number) order.get("merchant_car_id")).longValue() > 0) {
      orders.restoreCarStock(((Number) order.get("merchant_car_id")).longValue(), current);
    }
    log(
        adminId,
        "/api/admin/business/orders/" + id + "/cancel",
        "管理员取消订单",
        String.valueOf(id));
    return BusinessResponse.success(1);
  }

  private void log(long adminId, String path, String title, String content) {
    logs.insertLog(adminId, path, title, content, now());
  }

  private static BigDecimal money(Map<String, Object> body, String field) {
    return decimal(body == null ? null : body.get(field));
  }

  private static BigDecimal decimal(Object value) {
    try {
      return value == null ? BigDecimal.ZERO : new BigDecimal(String.valueOf(value));
    } catch (NumberFormatException ignored) {
      return BigDecimal.valueOf(-1);
    }
  }

  private static int integer(Object value) {
    return value == null ? 0 : ((Number) value).intValue();
  }

  private static long cents(BigDecimal value) {
    return value.movePointRight(2).longValueExact();
  }

  private static String refundNumber() {
    return String.valueOf(System.currentTimeMillis())
        + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
  }

  private static long now() {
    return System.currentTimeMillis() / 1000;
  }

  private static BusinessResponse<?> unauthorized() {
    return BusinessResponse.error("管理员未登录");
  }
}
