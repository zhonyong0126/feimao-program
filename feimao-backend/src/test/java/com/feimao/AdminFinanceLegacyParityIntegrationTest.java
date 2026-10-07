package com.feimao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.feimao.common.BusinessResponse;
import com.feimao.controller.admin.OrderFinanceAdminController;
import com.feimao.service.AdminSessionService;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class AdminFinanceLegacyParityIntegrationTest {
  @Autowired JdbcTemplate jdbc;
  @Autowired AdminSessionService sessions;
  @Autowired OrderFinanceAdminController controller;

  @Test
  void platformLedgerUsesLegacyAssociationsChineseTypesAndFourSummaries() {
    String token = adminToken();
    long now = System.currentTimeMillis() / 1000;
    long userId =
        jdbc.queryForObject(
            "select id from ymcx_user where status='normal' order by id limit 1", Long.class);
    long merchantId =
        jdbc.queryForObject(
            "select id from ymcx_merchant where is_delete=1 and deletetime is null order by id limit 1",
            Long.class);
    long shopId =
        jdbc.queryForObject(
            "select id from ymcx_shop order by id limit 1", Long.class);
    String orderNumber = uniqueNumber("P");
    long orderId = insertOrder(userId, merchantId, orderNumber, now);
    String merchantCashNumber = uniqueNumber("M");
    String shopCashNumber = uniqueNumber("S");
    long merchantCashId = insertCash("merchant", merchantId, merchantCashNumber, now);
    long shopCashId = insertCash("shop", shopId, shopCashNumber, now);

    insertPlatform(userId, "10.00", 1, orderId, now, 1);
    insertPlatform(userId, "2.00", 2, orderId, now, 1);
    insertPlatform(merchantId, "3.00", 3, merchantCashId, now, 2);
    insertPlatform(shopId, "4.00", 4, shopCashId, now, 3);
    insertPlatform(userId, "5.00", 5, orderId, now, 1);

    Map<String, Object> page =
        data(
            controller.finance(
                token,
                "platform",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                now,
                now,
                1,
                20));
    assertEquals(5, ((Number) page.get("total")).intValue());
    assertMoney("24.00", page.get("money"));
    assertMoney("10.00", page.get("in_money"));
    assertMoney("14.00", page.get("out_money"));
    assertMoney("7.00", page.get("cash_money"));

    List<Map<String, Object>> rows = rows(page);
    Map<Integer, Map<String, Object>> byType =
        rows.stream()
            .collect(
                Collectors.toMap(
                    row -> ((Number) row.get("type")).intValue(), row -> row));
    assertEquals("租车订单支付", byType.get(1).get("type_text"));
    assertEquals("租车押金退款", byType.get(2).get("type_text"));
    assertEquals("商家余额提现", byType.get(3).get("type_text"));
    assertEquals("酒店余额提现", byType.get(4).get("type_text"));
    assertEquals("取消订单退款", byType.get(5).get("type_text"));
    assertEquals("用户", byType.get(1).get("user_type_text"));
    assertEquals("门店", byType.get(3).get("user_type_text"));
    assertEquals("酒店", byType.get(4).get("user_type_text"));
    assertEquals(orderNumber, byType.get(1).get("out_trade_no"));
    assertEquals(merchantCashNumber, byType.get(3).get("out_trade_no"));
    assertEquals(shopCashNumber, byType.get(4).get("out_trade_no"));
    assertFalse(String.valueOf(byType.get(1).get("nickname")).isEmpty());
    assertFalse(String.valueOf(byType.get(3).get("nickname")).isEmpty());
    assertFalse(String.valueOf(byType.get(4).get("nickname")).isEmpty());
  }

  @Test
  void systemLedgerUsesLegacyChineseTypesOrderNumberAndConfiguredTotal() {
    String token = adminToken();
    long now = System.currentTimeMillis() / 1000;
    long userId = jdbc.queryForObject("select id from ymcx_user order by id limit 1", Long.class);
    long merchantId = jdbc.queryForObject("select id from ymcx_merchant order by id limit 1", Long.class);
    String orderNumber = uniqueNumber("Y");
    long orderId = insertOrder(userId, merchantId, orderNumber, now);
    jdbc.update("update ymcx_config set value='123.45' where name='system_money'");
    insertSystem("1.00", 1, orderId, now);
    insertSystem("2.00", 2, orderId, now);
    insertSystem("3.00", 3, orderId, now);

    Map<String, Object> page =
        data(
            controller.finance(
                token,
                "system",
                null,
                null,
                null,
                null,
                orderNumber,
                null,
                null,
                now,
                now,
                1,
                20));
    assertEquals(3, ((Number) page.get("total")).intValue());
    assertMoney("6.00", page.get("amount"));
    assertMoney("123.45", page.get("system_money"));
    Map<Integer, String> types =
        rows(page).stream()
            .collect(
                Collectors.toMap(
                    row -> ((Number) row.get("type")).intValue(),
                    row -> String.valueOf(row.get("type_text"))));
    assertEquals("正常下单平台获取", types.get(1));
    assertEquals("扫门店码平台获取", types.get(2));
    assertEquals("扫酒店码平台获取", types.get(3));
    assertTrue(rows(page).stream().allMatch(row -> orderNumber.equals(row.get("order_number"))));
  }

  @Test
  void platformAndSystemLedgersSupportLegacyBatchRecycleLifecycle() {
    String token = adminToken();
    long now = System.currentTimeMillis() / 1000;
    long userId = jdbc.queryForObject("select id from ymcx_user order by id limit 1", Long.class);
    long platformId = insertPlatform(userId, "1.00", 1, 0, now, 1);
    long systemId = insertSystem("1.00", 1, 0, now);

    Map<String, Object> platformIds =
        Collections.<String, Object>singletonMap("ids", Arrays.asList(platformId));
    Map<String, Object> systemIds =
        Collections.<String, Object>singletonMap("ids", String.valueOf(systemId));
    assertEquals(1, controller.deleteFinances(token, "platform", platformIds).getCode());
    assertEquals(1, controller.deleteFinances(token, "system", systemIds).getCode());
    assertTrue(recycleIds(controller.recycledFinance(token, "platform", 1, 100)).contains(platformId));
    assertTrue(recycleIds(controller.recycledFinance(token, "system", 1, 100)).contains(systemId));

    assertEquals(1, controller.restoreFinances(token, "platform", platformIds).getCode());
    assertEquals(1, controller.restoreFinances(token, "system", systemIds).getCode());
    assertEquals(1, controller.deleteFinance(token, "platform", platformId).getCode());
    assertEquals(1, controller.deleteFinance(token, "system", systemId).getCode());
    assertEquals(1, controller.destroyFinances(token, "platform", platformIds).getCode());
    assertEquals(1, controller.destroyFinances(token, "system", systemIds).getCode());
    assertEquals(
        0,
        jdbc.queryForObject(
            "select count(*) from ymcx_platform_log where id=?", Integer.class, platformId));
    assertEquals(
        0,
        jdbc.queryForObject(
            "select count(*) from ymcx_system_money_log where id=?", Integer.class, systemId));
  }

  @Test
  void fundPageExposesOnlyLegacyLedgerActionsAndChineseMappings() throws Exception {
    String template =
        new String(
            Files.readAllBytes(Paths.get("src/main/resources/templates/admin/fund/list.ftl")),
            StandardCharsets.UTF_8);
    String script =
        new String(
            Files.readAllBytes(Paths.get("src/main/resources/static/admin/js/fund.js")),
            StandardCharsets.UTF_8);
    assertTrue(template.contains("section == 'finance-platform' || section == 'finance-system'"));
    assertTrue(template.contains("bulkDeleteFinanceButton"));
    assertTrue(template.contains("bulkRestoreFundButton"));
    assertTrue(template.contains("bulkDestroyFundButton"));
    assertTrue(template.contains("moneyValue"));
    assertTrue(template.contains("inMoneyValue"));
    assertTrue(template.contains("outMoneyValue"));
    assertTrue(template.contains("cashMoneyValue"));
    assertTrue(template.contains("systemMoneyValue"));
    assertTrue(script.contains("1: '租车订单支付'"));
    assertTrue(script.contains("5: '取消订单退款'"));
    assertTrue(script.contains("3: '扫酒店码平台获取'"));
    assertTrue(script.contains("platformUserTypes = { 1: '用户', 2: '门店', 3: '酒店' }"));
    assertTrue(script.contains("if (legacyLedger)"));
    assertTrue(script.contains("business-detail"));
  }

  private String adminToken() {
    long admin =
        jdbc.queryForObject(
            "select id from ymcx_admin where status='normal' order by id limit 1", Long.class);
    return sessions.issue(admin);
  }

  private long insertOrder(long userId, long merchantId, String number, long now) {
    jdbc.update(
        "insert into ymcx_order(user_id,merchant_id,number,type,state,is_delete,createtime,updatetime) values(?,?,?,1,1,1,?,?)",
        userId,
        merchantId,
        number,
        now,
        now);
    return jdbc.queryForObject(
        "select id from ymcx_order where number=? order by id desc limit 1", Long.class, number);
  }

  private long insertCash(String role, long ownerId, String number, long now) {
    String table = "merchant".equals(role) ? "ymcx_merchant_cash" : "ymcx_shop_cash";
    String owner = "merchant".equals(role) ? "merchant_id" : "shop_id";
    jdbc.update(
        "insert into "
            + table
            + "(out_trade_no,"
            + owner
            + ",amount,status,note,createtime,updatetime,is_delete) values(?,?,?,?,?,?,?,1)",
        number,
        ownerId,
        "1.00",
        1,
        "资金兼容测试",
        now,
        now);
    return jdbc.queryForObject(
        "select id from " + table + " where out_trade_no=? order by id desc limit 1",
        Long.class,
        number);
  }

  private long insertPlatform(
      long ownerId, String amount, int type, long orderId, long now, int userType) {
    jdbc.update(
        "insert into ymcx_platform_log(user_id,amount,type,log_note,order_id,createtime,is_delete,user_type) values(?,?,?,?,?,?,1,?)",
        ownerId,
        amount,
        type,
        "资金兼容测试",
        String.valueOf(orderId),
        now,
        userType);
    return jdbc.queryForObject("select last_insert_id()", Long.class);
  }

  private long insertSystem(String amount, int type, long orderId, long now) {
    jdbc.update(
        "insert into ymcx_system_money_log(money,`before`,`after`,type,memo,createtime,updatetime,is_delete,order_id) values(?,0,?,?,'资金兼容测试',?,?,1,?)",
        amount,
        amount,
        type,
        now,
        now,
        orderId);
    return jdbc.queryForObject("select last_insert_id()", Long.class);
  }

  private static String uniqueNumber(String prefix) {
    return prefix + Long.toUnsignedString(System.nanoTime());
  }

  private static void assertMoney(String expected, Object actual) {
    assertEquals(0, new BigDecimal(expected).compareTo(new BigDecimal(String.valueOf(actual))));
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> data(BusinessResponse<?> response) {
    assertEquals(1, response.getCode(), response.getMsg());
    return (Map<String, Object>) response.getData();
  }

  @SuppressWarnings("unchecked")
  private static List<Map<String, Object>> rows(Map<String, Object> page) {
    return (List<Map<String, Object>>) page.get("data");
  }

  private static List<Long> recycleIds(BusinessResponse<?> response) {
    return rows(data(response)).stream()
        .map(row -> ((Number) row.get("id")).longValue())
        .collect(Collectors.toList());
  }
}
