package com.feimao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.feimao.common.BusinessResponse;
import com.feimao.controller.admin.BusinessAdminController;
import com.feimao.controller.admin.OrderFinanceAdminController;
import com.feimao.service.AdminSessionService;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Transactional
class LegacyRecycleParityIntegrationTest {
  @Autowired JdbcTemplate jdbc;
  @Autowired AdminSessionService sessions;
  @Autowired BusinessAdminController business;
  @Autowired OrderFinanceAdminController orders;
  @Autowired RequestMappingHandlerMapping mappings;

  @Test
  void orderGuestSupportsLegacyRecycleRestoreAndDestroy() {
    String token = adminToken();
    long orderId =
        jdbc.queryForObject("select id from ymcx_order order by id limit 1", Long.class);
    long userId =
        jdbc.queryForObject("select user_id from ymcx_order where id=?", Long.class, orderId);
    String name = "回收同行人" + System.nanoTime();
    long now = System.currentTimeMillis() / 1000;
    jdbc.update(
        "insert into ymcx_order_guest(user_id,order_id,name,mobile,idcard,createtime,updatetime,is_delete,is_insure,insure_money) values(?,?,?,?,?,?,?,?,?,?)",
        userId,
        orderId,
        name,
        "138" + "0000" + "0000",
        "440300" + "19900101" + "1234",
        now,
        now,
        1,
        1,
        "1.00");
    long id =
        jdbc.queryForObject(
            "select id from ymcx_order_guest where name=? order by id desc limit 1",
            Long.class,
            name);

    assertEquals(1, orders.deleteOrderGuest(token, id).getCode());
    assertTrue(recycleIds(orders.recycledOrderGuests(token, 1, 100)).contains(id));
    assertEquals(1, orders.restoreOrderGuest(token, id).getCode());
    assertEquals(
        1,
        jdbc.queryForObject(
            "select count(*) from ymcx_order_guest where id=? and is_delete=1 and deletetime is null",
            Integer.class,
            id));
    assertEquals(1, orders.deleteOrderGuest(token, id).getCode());
    assertEquals(1, orders.destroyOrderGuest(token, id).getCode());
    assertEquals(
        0,
        jdbc.queryForObject(
            "select count(*) from ymcx_order_guest where id=?", Integer.class, id));
  }

  @Test
  void merchantAndShopCashSupportLegacyBatchRecycleLifecycle() {
    String token = adminToken();
    long merchantId =
        jdbc.queryForObject("select id from ymcx_merchant order by id limit 1", Long.class);
    long shopId = jdbc.queryForObject("select id from ymcx_shop order by id limit 1", Long.class);
    long merchantCash = insertCash("merchant", merchantId);
    long shopCash = insertCash("shop", shopId);
    Map<String, Object> merchantIds =
        Collections.<String, Object>singletonMap("ids", Arrays.asList(merchantCash));
    Map<String, Object> shopIds =
        Collections.<String, Object>singletonMap("ids", String.valueOf(shopCash));

    assertEquals(1, business.deleteCashRows(token, "merchant", merchantIds).getCode());
    assertEquals(1, business.deleteCashRows(token, "shop", shopIds).getCode());
    assertTrue(recycleIds(business.recycledCashRows(token, "merchant", 1, 100)).contains(merchantCash));
    assertTrue(recycleIds(business.recycledCashRows(token, "shop", 1, 100)).contains(shopCash));
    assertEquals(1, business.restoreCashRows(token, "merchant", merchantIds).getCode());
    assertEquals(1, business.restoreCashRows(token, "shop", shopIds).getCode());
    assertEquals(1, business.deleteCash(token, "merchant", merchantCash).getCode());
    assertEquals(1, business.deleteCash(token, "shop", shopCash).getCode());
    assertEquals(1, business.destroyCashRows(token, "merchant", merchantIds).getCode());
    assertEquals(1, business.destroyCashRows(token, "shop", shopIds).getCode());
  }

  @Test
  void originalAdminAreaUriIsRegistered() {
    assertTrue(
        mappings.getHandlerMethods().keySet().stream()
            .anyMatch(info -> info.getPatternValues().contains("/admin/ajax/area")));
  }

  private String adminToken() {
    long admin =
        jdbc.queryForObject(
            "select id from ymcx_admin where status='normal' order by id limit 1", Long.class);
    return sessions.issue(admin);
  }

  private long insertCash(String role, long ownerId) {
    String table = "merchant".equals(role) ? "ymcx_merchant_cash" : "ymcx_shop_cash";
    String owner = "merchant".equals(role) ? "merchant_id" : "shop_id";
    String number = "RECYCLE-" + role + '-' + System.nanoTime();
    long now = System.currentTimeMillis() / 1000;
    jdbc.update(
        "insert into "
            + table
            + "(out_trade_no,"
            + owner
            + ",amount,status,note,createtime,updatetime,is_delete) values(?,?,?,?,?,?,?,?)",
        number,
        ownerId,
        "1.00",
        1,
        "回收测试",
        now,
        now,
        1);
    return jdbc.queryForObject(
        "select id from " + table + " where out_trade_no=? order by id desc limit 1",
        Long.class,
        number);
  }

  @SuppressWarnings("unchecked")
  private static List<Long> recycleIds(BusinessResponse<?> response) {
    assertEquals(1, response.getCode());
    Map<String, Object> page = (Map<String, Object>) response.getData();
    List<Map<String, Object>> rows = (List<Map<String, Object>>) page.get("data");
    java.util.ArrayList<Long> ids = new java.util.ArrayList<>();
    for (Map<String, Object> row : rows) ids.add(((Number) row.get("id")).longValue());
    return ids;
  }
}
