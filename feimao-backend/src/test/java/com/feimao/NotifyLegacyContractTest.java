package com.feimao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.feimao.controller.order.NotifyController;
import com.feimao.mapper.PaymentNotifyMapper;
import com.feimao.service.InsuranceTaskService;
import com.feimao.service.SmsNotificationService;
import com.feimao.service.WechatPayService;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NotifyLegacyContractTest {
  private PaymentNotifyMapper payments;
  private WechatPayService pay;
  private NotifyController controller;
  private Map<String, String> callback;

  @BeforeEach
  void setUp() {
    payments = mock(PaymentNotifyMapper.class);
    pay = mock(WechatPayService.class);
    controller =
        new NotifyController(
            payments,
            pay,
            mock(InsuranceTaskService.class),
            mock(SmsNotificationService.class));
    callback = new LinkedHashMap<>();
    callback.put("return_code", "SUCCESS");
    callback.put("result_code", "SUCCESS");
    callback.put("out_trade_no", "UNKNOWN");
    callback.put("total_fee", "2000");
    callback.put("cash_fee", "2000");
    callback.put("transaction_id", "TX1");
    callback.put("time_end", "20260906123456");
    when(pay.parse(anyString())).thenReturn(callback);
    when(pay.verify(callback)).thenReturn(true);
    when(pay.successXml()).thenReturn("SUCCESS");
    when(pay.failXml(anyString())).thenReturn("FAIL");
  }

  @Test
  void missingOrderAndInstallmentCallbacksAreAcknowledged() {
    assertEquals("SUCCESS", controller.orderNotify("order callback"));
    assertEquals("SUCCESS", controller.installmentNotify("installment callback"));
    verify(pay, never()).failXml("订单不存在");
    verify(pay, never()).failXml("分期记录不存在");
  }

  @Test
  void missingInstallmentParentIsAcknowledgedWithoutMarkingChildPaid() {
    Map<String, Object> child = installment(1);
    when(payments.lockInstallment("UNKNOWN")).thenReturn(child);

    assertEquals("SUCCESS", controller.installmentNotify("installment callback"));

    verify(payments).lockInstallmentParent("PARENT");
    verify(payments, never())
        .markInstallmentPaid(anyLong(), any(BigDecimal.class), anyString(), anyLong(), anyLong());
  }

  @Test
  void installmentCompletesParentWhenCurrentChildNumEqualsLeaseDurationRule() {
    Map<String, Object> child = installment(1);
    when(payments.lockInstallment("UNKNOWN")).thenReturn(child);
    Map<String, Object> parent = new LinkedHashMap<>();
    parent.put("id", 9L);
    parent.put("lease_duration", "2");
    when(payments.lockInstallmentParent("PARENT")).thenReturn(parent);
    when(payments.markInstallmentPaid(
            eq(7L), any(BigDecimal.class), eq("TX1"), anyLong(), anyLong()))
        .thenReturn(1);
    when(payments.addInstallmentPayment(
            eq("PARENT"), any(BigDecimal.class), eq(2), anyLong()))
        .thenReturn(1);

    assertEquals("SUCCESS", controller.installmentNotify("installment callback"));

    verify(payments)
        .addInstallmentPayment(
            eq("PARENT"),
            argThat(amount -> amount.compareTo(new BigDecimal("20.00")) == 0),
            eq(2),
            anyLong());
  }

  @Test
  void alreadyPaidOrderAndInstallmentCallbacksAreAcknowledged() {
    Map<String, Object> order = new LinkedHashMap<>();
    order.put("state", 2);
    when(payments.lockOrder("UNKNOWN")).thenReturn(order);
    Map<String, Object> child = installment(2);
    when(payments.lockInstallment("UNKNOWN")).thenReturn(child);

    assertEquals("SUCCESS", controller.orderNotify("order callback"));
    assertEquals("SUCCESS", controller.installmentNotify("installment callback"));
    verify(payments, never()).lockInstallmentParent(anyString());
  }

  private static Map<String, Object> installment(int state) {
    Map<String, Object> child = new LinkedHashMap<>();
    child.put("id", 7L);
    child.put("number", "PARENT");
    child.put("num", 2);
    child.put("price", new BigDecimal("20.00"));
    child.put("state", state);
    return child;
  }
}
