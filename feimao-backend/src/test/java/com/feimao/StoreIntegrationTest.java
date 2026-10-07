package com.feimao;

import static org.junit.jupiter.api.Assertions.*;

import com.feimao.common.BusinessResponse;
import com.feimao.controller.store.StoreController;
import com.feimao.service.RoleSessionService;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class StoreIntegrationTest {
  @Autowired JdbcTemplate jdbc;
  @Autowired StoreController stores;
  @Autowired RoleSessionService sessions;

  @Test
  void carStockAndDamagedReturnRulesPersistAtomically() {
    Map<String, Object> car =
        jdbc.queryForMap(
            "select mc.id,mc.merchant_id,mc.platform_car_id,mc.total_stock,mc.stock from ymcx_merchant_car mc join ymcx_merchant m on m.id=mc.merchant_id where mc.is_delete=1 and mc.deletetime is null and m.is_delete=1 and m.deletetime is null and m.status=1 and exists(select 1 from ymcx_order o where o.merchant_id=mc.merchant_id and o.is_delete=1) order by mc.id limit 1");
    long merchant = ((Number) car.get("merchant_id")).longValue();
    String token = sessions.issue(merchant, "store");
    int total = ((Number) car.get("total_stock")).intValue();
    Map<String, Object> edit = new LinkedHashMap<>();
    edit.put("type", 3);
    edit.put("id", car.get("id"));
    edit.put("platform_car_id", car.get("platform_car_id"));
    edit.put("total_stock", total + 1);
    edit.put("cash_price", "1.00");
    edit.put("month_price", "10.00");
    edit.put("day_price", "2.00");
    edit.put("hour_price", "1.00");
    edit.put("images", "car.jpg");
    edit.put("intro", "detail.jpg");
    assertEquals(1, stores.carEdit(token, edit).getCode());
    Integer changed =
        jdbc.queryForObject(
            "select total_stock from ymcx_merchant_car where id=?", Integer.class, car.get("id"));
    assertEquals(total + 1, changed);

    Map<String, Object> order =
        jdbc.queryForMap(
            "select id,number from ymcx_order where merchant_id=? and is_delete=1 order by id limit 1",
            merchant);
    jdbc.update(
        "update ymcx_order set state=4,pick_state=1,cash_price=10 where id=?", order.get("id"));
    Map<String, Object> back = new LinkedHashMap<>();
    back.put("order_number", order.get("number"));
    back.put("return_type", 2);
    back.put("refund_cash_price", "5.00");
    BusinessResponse<?> response = stores.backCar(token, back);
    assertEquals(1, response.getCode());
    Map<String, Object> after =
        jdbc.queryForMap(
            "select state,pick_state,return_type,refund_cash_price from ymcx_order where id=?",
            order.get("id"));
    assertEquals(4, ((Number) after.get("state")).intValue());
    assertEquals(2, ((Number) after.get("pick_state")).intValue());
    assertEquals(2, ((Number) after.get("return_type")).intValue());
  }
}
