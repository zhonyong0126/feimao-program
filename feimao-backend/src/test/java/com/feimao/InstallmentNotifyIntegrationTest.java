package com.feimao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.feimao.controller.order.NotifyController;
import com.feimao.service.InsuranceTaskService;
import com.feimao.service.SmsNotificationService;
import com.feimao.service.WechatPayService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class InstallmentNotifyIntegrationTest {
  @Autowired JdbcTemplate jdbc;
  @Autowired NotifyController notify;
  @MockBean WechatPayService pay;
  @MockBean InsuranceTaskService insuranceTasks;
  @MockBean SmsNotificationService notifications;

  @Test
  void leaseCallbackPersistsRefundBalanceCouponAndPlatformIncomeOnce() {
    long userId =
        jdbc.queryForObject(
            "select id from ymcx_user where status='normal' order by id limit 1", Long.class);
    long merchantId =
        jdbc.queryForObject(
            "select id from ymcx_merchant where is_delete=1 order by id limit 1", Long.class);
    long now = System.currentTimeMillis() / 1000;
    String number = "LN" + System.nanoTime();
    jdbc.update(
        "insert into ymcx_user_coupon(user_id,price,reduceprice,state,endtime,createtime,updatetime) "
            + "values(?,100.00,10.00,1,?,?,?)",
        userId,
        now + 3600,
        now,
        now);
    long couponId =
        jdbc.queryForObject(
            "select id from ymcx_user_coupon where user_id=? and createtime=? order by id desc limit 1",
            Long.class,
            userId,
            now);
    jdbc.update(
        "insert into ymcx_order(user_id,merchant_id,number,type,lease_type,state,price,"
            + "discounts_price,coupon_id,last_all_money,is_delete,createtime,updatetime) "
            + "values(?,?,?,1,2,1,100.00,10.00,?,0,1,?,?)",
        userId,
        merchantId,
        number,
        couponId,
        now,
        now);
    when(pay.parse(anyString()))
        .thenAnswer(
            invocation -> {
              Map<String, String> callback = callback(number, "9000", "LEASE-TX");
              callback.put("cash_fee", "8000");
              return callback;
            });
    when(pay.verify(anyMap())).thenReturn(true);
    when(pay.successXml()).thenReturn("SUCCESS");
    when(pay.failXml(anyString())).thenReturn("FAIL");

    assertEquals("SUCCESS", notify.orderNotify("lease payment"));
    assertEquals("SUCCESS", notify.orderNotify("duplicate lease payment"));
    Map<String, Object> order =
        jdbc.queryForMap(
            "select id,state,pay_price,last_all_money,paytime from ymcx_order where number=?", number);
    assertEquals(2, ((Number) order.get("state")).intValue());
    assertEquals(new BigDecimal("80.00"), order.get("pay_price"));
    assertEquals(new BigDecimal("80.00"), order.get("last_all_money"));
    assertEquals(callbackEpoch(), ((Number) order.get("paytime")).longValue());
    assertEquals(
        2,
        jdbc.queryForObject(
            "select state from ymcx_user_coupon where id=?", Integer.class, couponId));
    assertEquals(
        1,
        jdbc.queryForObject(
            "select count(*) from ymcx_platform_log where order_id=? and user_id=? and type=1 "
                + "and user_type=1 and amount=80.00 and is_delete=1",
            Integer.class,
            order.get("id"),
            userId));
  }

  @Test
  void callbackChecksAmountIsIdempotentAndCompletesParentAfterLastBill() {
    long userId =
        jdbc.queryForObject(
            "select id from ymcx_user where status='normal' order by id limit 1", Long.class);
    long merchantId =
        jdbc.queryForObject(
            "select id from ymcx_merchant where is_delete=1 order by id limit 1", Long.class);
    long now = System.currentTimeMillis() / 1000;
    String number = "IN" + System.nanoTime();
    jdbc.update(
        "insert into ymcx_order(user_id,merchant_id,number,type,lease_type,lease_duration,state,"
            + "pay_price,is_delete,createtime,updatetime) values(?,?,?,1,4,2,2,5.00,1,?,?)",
        userId,
        merchantId,
        number,
        now,
        now);
    jdbc.update(
        "insert into ymcx_order_child(user_id,number,child_number,num,price,state,createtime,updatetime) "
            + "values(?,?,?,1,10.00,1,?,?),(?,?,?,2,20.00,1,?,?),(?,?,?,3,30.00,1,?,?)",
        userId,
        number,
        number + "-1",
        now,
        now,
        userId,
        number,
        number + "-2",
        now,
        now,
        userId,
        number,
        number + "-3",
        now,
        now);
    when(pay.parse(anyString())).thenAnswer(invocation -> callback(number + "-1", "999", "TX-1"));
    when(pay.verify(anyMap())).thenReturn(true);
    when(pay.successXml()).thenReturn("SUCCESS");
    when(pay.failXml(anyString())).thenReturn("FAIL");

    assertEquals("FAIL", notify.installmentNotify("wrong amount"));
    assertEquals(
        0,
        jdbc.queryForObject(
            "select count(*) from ymcx_order_child where number=? and state=2",
            Integer.class,
            number));
    assertEquals(
        new BigDecimal("5.00"),
        jdbc.queryForObject(
            "select pay_price from ymcx_order where number=?", BigDecimal.class, number));

    when(pay.parse(anyString())).thenAnswer(invocation -> callback(number + "-1", "1000", "TX-1"));
    assertEquals("SUCCESS", notify.installmentNotify("first payment"));
    assertEquals("SUCCESS", notify.installmentNotify("duplicate first payment"));
    Map<String, Object> parent =
        jdbc.queryForMap("select state,pay_price from ymcx_order where number=?", number);
    assertEquals(2, ((Number) parent.get("state")).intValue());
    assertEquals(new BigDecimal("15.00"), parent.get("pay_price"));

    when(pay.parse(anyString())).thenAnswer(invocation -> callback(number + "-2", "2000", "TX-2"));
    assertEquals("SUCCESS", notify.installmentNotify("last payment"));
    parent = jdbc.queryForMap("select state,pay_price from ymcx_order where number=?", number);
    assertEquals(4, ((Number) parent.get("state")).intValue());
    assertEquals(new BigDecimal("35.00"), parent.get("pay_price"));
    assertEquals(
        2,
        jdbc.queryForObject(
            "select count(*) from ymcx_order_child where number=? and state=2",
            Integer.class,
            number));
    assertEquals(
        2,
        jdbc.queryForObject(
            "select count(*) from ymcx_order_child where number=? and paytime=?",
            Integer.class,
            number,
            callbackEpoch()));
  }

  private static Map<String, String> callback(
      String childNumber, String totalFee, String transactionId) {
    Map<String, String> callback = new HashMap<>();
    callback.put("return_code", "SUCCESS");
    callback.put("result_code", "SUCCESS");
    callback.put("out_trade_no", childNumber);
    callback.put("total_fee", totalFee);
    callback.put("transaction_id", transactionId);
    callback.put("time_end", "20260906123456");
    return callback;
  }

  private static long callbackEpoch() {
    return LocalDateTime.of(2026, 9, 6, 12, 34, 56)
        .atZone(ZoneId.systemDefault())
        .toEpochSecond();
  }
}
