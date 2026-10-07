package com.feimao;

import static org.junit.jupiter.api.Assertions.*;

import com.feimao.common.BusinessResponse;
import com.feimao.controller.shop.ShopController;
import com.feimao.service.RoleSessionService;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class ShopContractIntegrationTest {
  @Autowired JdbcTemplate jdbc;
  @Autowired ShopController shops;
  @Autowired RoleSessionService sessions;

  @Test
  void profileAndCashResponsesKeepLegacyContract() {
    Map<String, Object> shop =
        jdbc.queryForMap(
            "select id,money from ymcx_shop order by id limit 1");
    long shopId = ((Number) shop.get("id")).longValue();
    jdbc.update(
        "update ymcx_shop set is_delete=1,deletetime=null,examine_status=2,status=1 where id=?",
        shopId);
    String token = sessions.issue(shopId, "shop");

    @SuppressWarnings("unchecked")
    Map<String, Object> profile =
        (Map<String, Object>)
            shops.edit(token, Collections.singletonMap("type", 1)).getData();
    assertTrue(profile.containsKey("money"));
    assertFalse(profile.containsKey("password"));
    assertTrue(profile.get("image") instanceof java.util.List);

    Map<String, Object> invalid = new LinkedHashMap<>();
    invalid.put("amount", "not-a-number");
    assertEquals("金额错误", shops.cash(token, invalid).getMsg());
    invalid.put("amount", 0);
    assertEquals("金额错误", shops.cash(token, invalid).getMsg());

    if (new java.math.BigDecimal(shop.get("money").toString()).compareTo(java.math.BigDecimal.ONE) >= 0) {
      BusinessResponse<?> success = shops.cash(token, Collections.singletonMap("amount", 1));
      assertEquals(1, success.getCode());
      assertNull(success.getData());
    }
  }

  @Test
  void paginationKeepsThinkPhpShapeAndAcceptsLegacyLargePageSize() {
    Map<String, Object> shop = jdbc.queryForMap("select id from ymcx_shop order by id limit 1");
    long shopId = ((Number) shop.get("id")).longValue();
    jdbc.update(
        "update ymcx_shop set is_delete=1,deletetime=null,examine_status=2,status=1 where id=?",
        shopId);
    String token = sessions.issue(shopId, "shop");

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("page", 9999999999L);
    body.put("list_rows", 9999999999L);
    @SuppressWarnings("unchecked")
    Map<String, Object> result = (Map<String, Object>) shops.cashList(token, body).getData();
    assertEquals(9999999999L, ((Number) result.get("per_page")).longValue());
    long lastPage = ((Number) result.get("last_page")).longValue();
    assertEquals(lastPage == 0 ? 1 : lastPage, ((Number) result.get("current_page")).longValue());
  }
}
