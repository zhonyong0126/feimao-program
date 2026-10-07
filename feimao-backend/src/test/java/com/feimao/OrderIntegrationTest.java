package com.feimao;

import static org.junit.jupiter.api.Assertions.*;

import com.feimao.common.BusinessResponse;
import com.feimao.controller.order.OrderController;
import com.feimao.service.OrderTaskService;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class OrderIntegrationTest {
  @Autowired JdbcTemplate jdbc;
  @Autowired OrderController orders;
  @Autowired OrderTaskService orderTasks;

  @Test
  void installmentPaymentMatchesLegacyUnrestrictedSelectionAndRenumbersEachAttempt() {
    long userId =
        jdbc.queryForObject(
            "select id from ymcx_user where status='normal' order by id limit 1", Long.class);
    long merchantId =
        jdbc.queryForObject(
            "select id from ymcx_merchant where is_delete=1 order by id limit 1", Long.class);
    long now = System.currentTimeMillis() / 1000;
    String token = "integrationInstallmentPaymentToken0001";
    String number = "IF" + System.nanoTime();
    jdbc.update(
        "insert into ymcx_user_token(token,user_id,createtime,expiretime) values(?,?,?,?) "
            + "on duplicate key update user_id=values(user_id),expiretime=values(expiretime)",
        token,
        userId,
        now,
        now + 600);
    jdbc.update(
        "insert into ymcx_order(user_id,merchant_id,number,type,lease_type,state,is_delete,createtime,updatetime) "
            + "values(?,?,?,1,4,2,1,?,?)",
        userId,
        merchantId,
        number,
        now,
        now);
    jdbc.update(
        "insert into ymcx_order_child(user_id,number,num,price,state,createtime,updatetime) "
            + "values(?,?,1,10.00,1,?,?),(?,?,2,20.00,1,?,?)",
        userId,
        number,
        now,
        now,
        userId,
        number,
        now,
        now);
    List<Map<String, Object>> children =
        jdbc.queryForList(
            "select id,num from ymcx_order_child where number=? order by num", number);
    Map<String, Object> secondBody = new LinkedHashMap<>();
    secondBody.put("order_number", number);
    secondBody.put("should_id", children.get(1).get("id"));
    BusinessResponse<?> skipped = orders.installment(token, secondBody);
    assertEquals(0, skipped.getCode());
    assertNotNull(
        jdbc.queryForObject(
            "select child_number from ymcx_order_child where id=?",
            String.class,
            children.get(1).get("id")));

    Map<String, Object> firstBody = new LinkedHashMap<>();
    firstBody.put("order_number", number);
    firstBody.put("should_id", children.get(0).get("id"));
    assertEquals(0, orders.installment(token, firstBody).getCode());
    String firstPaymentNumber =
        jdbc.queryForObject(
            "select child_number from ymcx_order_child where id=?",
            String.class,
            children.get(0).get("id"));
    assertNotNull(firstPaymentNumber);
    assertFalse(firstPaymentNumber.isEmpty());
    assertEquals(0, orders.installment(token, firstBody).getCode());
    assertNotEquals(
        firstPaymentNumber,
        jdbc.queryForObject(
            "select child_number from ymcx_order_child where id=?",
            String.class,
            children.get(0).get("id")));
  }

  @Test
  void expiredOrderTaskMatchesLegacyStockRestorationForEveryReferencedCar() {
    Map<String, Object> car =
        jdbc.queryForMap(
            "select mc.id,mc.merchant_id,mc.stock from ymcx_merchant_car mc "
                + "where mc.is_delete=1 and mc.deletetime is null and mc.stock>0 order by mc.id limit 1");
    long userId =
        jdbc.queryForObject(
            "select id from ymcx_user where status='normal' order by id limit 1", Long.class);
    long oldTime = System.currentTimeMillis() / 1000 - 1900;
    String leaseNumber = "TL" + System.nanoTime();
    String buyNumber = "TB" + System.nanoTime();
    String insert =
        "insert into ymcx_order(user_id,merchant_id,merchant_car_id,number,type,state,pick_state,"
            + "cash_state,source_type,source_id,lease_money,cash_price,pay_price,last_all_money,"
            + "is_delete,createtime,updatetime) values(?,?,?,?,?,1,1,1,1,0,0,0,0,0,1,?,?)";
    jdbc.update(
        insert,
        userId,
        car.get("merchant_id"),
        car.get("id"),
        leaseNumber,
        1,
        oldTime,
        oldTime);
    jdbc.update(
        insert,
        userId,
        car.get("merchant_id"),
        car.get("id"),
        buyNumber,
        2,
        oldTime,
        oldTime);
    jdbc.update("update ymcx_merchant_car set stock=stock-1 where id=?", car.get("id"));

    orderTasks.cancelUnpaidOrders();

    assertEquals(
        2,
        jdbc.queryForObject(
            "select count(*) from ymcx_order where number in (?,?) and state=6",
            Integer.class,
            leaseNumber,
            buyNumber));
    assertEquals(
        ((Number) car.get("stock")).intValue() + 1,
        jdbc.queryForObject(
            "select stock from ymcx_merchant_car where id=?", Integer.class, car.get("id")));
    orderTasks.cancelUnpaidOrders();
    assertEquals(
        ((Number) car.get("stock")).intValue() + 1,
        jdbc.queryForObject(
            "select stock from ymcx_merchant_car where id=?", Integer.class, car.get("id")));
  }

  @Test
  void paidLeaseTaskStartsOnlyExpiredLeaseOrders() {
    long userId =
        jdbc.queryForObject(
            "select id from ymcx_user where status='normal' order by id limit 1", Long.class);
    long merchantId =
        jdbc.queryForObject(
            "select id from ymcx_merchant where is_delete=1 order by id limit 1", Long.class);
    long now = System.currentTimeMillis() / 1000;
    String expiredLease = "SL" + System.nanoTime();
    String recentLease = "SR" + System.nanoTime();
    String expiredBuy = "SB" + System.nanoTime();
    String sql =
        "insert into ymcx_order(user_id,merchant_id,number,type,state,paytime,is_delete,createtime,updatetime) values(?,?,?,?,2,?,1,?,?)";
    jdbc.update(sql, userId, merchantId, expiredLease, 1, now - 120, now - 180, now - 180);
    jdbc.update(sql, userId, merchantId, recentLease, 1, now - 20, now - 30, now - 30);
    jdbc.update(sql, userId, merchantId, expiredBuy, 2, now - 120, now - 180, now - 180);

    orderTasks.startPaidLeaseOrders();

    assertEquals(
        3,
        jdbc.queryForObject(
            "select state from ymcx_order where number=?", Integer.class, expiredLease));
    assertEquals(
        2,
        jdbc.queryForObject(
            "select state from ymcx_order where number=?", Integer.class, recentLease));
    assertEquals(
        2,
        jdbc.queryForObject(
            "select state from ymcx_order where number=?", Integer.class, expiredBuy));
  }

  @Test
  @Transactional(propagation = org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
  void leaseOrderRollsBackGuestsAndStockWhenLegacyWechatPaymentFails() {
    Map<String, Object> user =
        jdbc.queryForMap("select id from ymcx_user where status='normal' order by id limit 1");
    Map<String, Object> car =
        jdbc.queryForMap(
            "select mc.id,mc.stock from ymcx_merchant_car mc join ymcx_merchant m on m.id=mc.merchant_id where mc.status=1 and mc.is_delete=1 and mc.stock>0 and m.status=1 and m.is_delete=1 and m.examine_status=2 order by mc.id limit 1");
    String token = "integrationLeaseOrderTokenRollbackOnly000001";
    long now = System.currentTimeMillis() / 1000;
    jdbc.update(
        "insert into ymcx_user_token(token,user_id,createtime,expiretime) values(?,?,?,?) on duplicate key update user_id=values(user_id),expiretime=values(expiretime)",
        token,
        user.get("id"),
        now,
        now + 600);
    Map<String, Object> guest = new LinkedHashMap<>();
    guest.put("name", "测试乘客");
    guest.put("mobile", "138" + "0000" + "0001");
    guest.put("idcard", "110101" + "19900101" + "1234");
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("merchant_car_id", car.get("id"));
    body.put("pick_type", 1);
    body.put("lease_type", 1);
    body.put("pick_single", 1);
    body.put("starttime", "2026-08-23 09:00");
    body.put("endtime", "2026-08-23 12:00");
    body.put("take_back_time", "2026-08-23 12:30");
    body.put("name", "测试承租人");
    body.put("mobile", "138" + "0000" + "0000");
    body.put("idcard", "110101" + "19900101" + "123X");
    body.put("guest", Collections.singletonList(guest));
    body.put("source_type", 1);
    body.put("mobile", "12345");
    assertEquals(0, orders.leaseCarPay(token, body).getCode());
    body.put("mobile", "138" + "0000" + "0000");
    int orderCountBefore =
        jdbc.queryForObject("select count(*) from ymcx_order where user_id=?", Integer.class, user.get("id"));
    BusinessResponse<?> response = orders.leaseCarPay(token, body);
    assertEquals(0, response.getCode());
    assertEquals("支付失败重新支付!", response.getMsg());
    assertNull(response.getData());
    assertEquals(
        orderCountBefore,
        jdbc.queryForObject("select count(*) from ymcx_order where user_id=?", Integer.class, user.get("id")));
    assertEquals(
        ((Number) car.get("stock")).intValue(),
        jdbc.queryForObject(
            "select stock from ymcx_merchant_car where id=?", Integer.class, car.get("id")));
  }

  @Test
  void simpleOrderKeepsLegacyPendingRowsAndAllowsUnusedCouponReuse() {
    long userId =
        jdbc.queryForObject(
            "select id from ymcx_user where status='normal' order by id limit 1", Long.class);
    Map<String, Object> car =
        jdbc.queryForMap(
            "select mc.id,mc.price,p.image from ymcx_merchant_car mc "
                + "join ymcx_platform_car p on p.id=mc.platform_car_id "
                + "where mc.status=1 and mc.is_delete=1 and mc.deletetime is null "
                + "and p.deletetime is null order by mc.id limit 1");
    jdbc.update("update ymcx_merchant_car set price=10.00 where id=?", car.get("id"));
    long now = System.currentTimeMillis() / 1000;
    String token = "integrationSimpleOrderCouponToken000001";
    jdbc.update(
        "insert into ymcx_user_token(token,user_id,createtime,expiretime) values(?,?,?,?) "
            + "on duplicate key update user_id=values(user_id),expiretime=values(expiretime)",
        token,
        userId,
        now,
        now + 600);
    jdbc.update(
        "insert into ymcx_user_coupon(user_id,price,reduceprice,state,endtime,createtime,updatetime) "
            + "values(?,?,?,?,?,?,?)",
        userId,
        "0.01",
        "0.01",
        1,
        now + 3600,
        now,
        now);
    long couponId =
        jdbc.queryForObject(
            "select id from ymcx_user_coupon where user_id=? and price=0.01 and reduceprice=0.01 "
                + "and createtime=? order by id desc limit 1",
            Long.class,
            userId,
            now);
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("merchant_car_id", car.get("id"));
    body.put("coupon_id", couponId);
    body.put("num", 1);
    body.put("take_back_time", "2026-09-07 10:00");
    body.put("name", "优惠券测试用户");
    body.put("mobile", "138" + "0000" + "0000");
    body.put("pick_type", 1);
    body.put("service_image", car.get("image"));

    BusinessResponse<?> first = orders.buyCarPay(token, body);
    assertEquals(0, first.getCode(), "without payment credentials the pending order is retained");
    assertEquals("支付失败重新支付!", first.getMsg());
    assertNull(first.getData());
    String number =
        jdbc.queryForObject(
            "select number from ymcx_order where user_id=? and coupon_id=? order by id desc limit 1",
            String.class,
            userId,
            couponId);
    Map<String, Object> order =
        jdbc.queryForMap(
            "select coupon_id,discounts_price,state from ymcx_order where number=?", number);
    assertEquals(couponId, ((Number) order.get("coupon_id")).longValue());
    assertEquals("0.01", order.get("discounts_price").toString());
    assertEquals(1, ((Number) order.get("state")).intValue());
    assertEquals(
        1,
        jdbc.queryForObject(
            "select state from ymcx_user_coupon where id=?", Integer.class, couponId));

    Map<String, Object> listBody = new LinkedHashMap<>();
    listBody.put("type", 0);
    listBody.put("state", 1);
    listBody.put("page", 1);
    listBody.put("list_rows", 1000);
    @SuppressWarnings("unchecked")
    Map<String, Object> allPage = (Map<String, Object>) orders.list(token, listBody).getData();
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> allOrders =
        (List<Map<String, Object>>) allPage.get("data");
    Map<String, Object> listedOrder =
        allOrders.stream()
            .filter(row -> number.equals(row.get("number")))
            .findFirst()
            .orElseThrow(AssertionError::new);
    assertFalse(
        listedOrder.containsKey("discounts_price"),
        "the legacy order list did not publish the discount field");
    assertEquals(1000, ((Number) allPage.get("per_page")).intValue());
    listBody.put("type", 4);
    assertEquals(1, orders.list(token, listBody).getCode());

    BusinessResponse<?> second = orders.buyCarPay(token, body);
    assertEquals(0, second.getCode());
    assertEquals("支付失败重新支付!", second.getMsg());
    assertEquals(
        2,
        jdbc.queryForObject(
            "select count(*) from ymcx_order where coupon_id=? and state=1 and is_delete=1",
            Integer.class,
            couponId));

    body.put("coupon_id", 0);
    body.put("pick_type", 3);
    body.put("pick_address", "");
    BusinessResponse<?> missingAddress = orders.buyCarPay(token, body);
    assertEquals(0, missingAddress.getCode());
    assertEquals("请选择取车地址!", missingAddress.getMsg());
    body.put("pick_type", 1);
    body.put("mobile", "12345");
    BusinessResponse<?> legacyNonEmptyMobile = orders.buyCarPay(token, body);
    assertEquals(0, legacyNonEmptyMobile.getCode());
    assertNotEquals("手机号格式错误", legacyNonEmptyMobile.getMsg());
  }
}
