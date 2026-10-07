package com.feimao;

import static org.junit.jupiter.api.Assertions.*;

import com.feimao.controller.user.UserController;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class UserApplyIntegrationTest {
  @Autowired JdbcTemplate jdbc;
  @Autowired UserController users;

  @Test
  void merchantAndShopApplicationsBindToUserAtomically() {
    Map<String, Object> u =
        jdbc.queryForMap(
            "select id from ymcx_user where merchant_status=1 and shop_status=1 and status='normal' order by id limit 1");
    long userId = ((Number) u.get("id")).longValue(), now = System.currentTimeMillis() / 1000;
    String token = "integrationUserApplyRollbackOnly000001";
    jdbc.update(
        "insert into ymcx_user_token(token,user_id,createtime,expiretime) values(?,?,?,?) on duplicate key update user_id=values(user_id),expiretime=values(expiretime)",
        token,
        userId,
        now,
        now + 600);
    Map<String, Object> merchant = new LinkedHashMap<>();
    merchant.put("type", 2);
    merchant.put("name", "回滚测试门店");
    merchant.put("mobile", "138" + "0000" + "0000");
    merchant.put("administrative", "测试区");
    merchant.put("address", "测试地址");
    merchant.put("intro", "测试");
    merchant.put("lat", "22.5");
    merchant.put("lng", "114.0");
    assertEquals(1, users.merchantApply(token, merchant).getCode());
    Map<String, Object> shop = new LinkedHashMap<>();
    shop.put("type", 2);
    shop.put("name", "回滚测试酒店");
    shop.put("nickname", "测试人");
    shop.put("mobile", "138" + "0000" + "0001");
    shop.put("address", "测试地址");
    shop.put("image", Arrays.asList("/test/a.png", "/test/b.png"));
    shop.put("intro", "测试");
    shop.put("lat", "22.5");
    shop.put("lng", "114.0");
    assertEquals(1, users.shopApply(token, shop).getCode());
    @SuppressWarnings("unchecked")
    Map<String, Object> merchantInfo =
        (Map<String, Object>)
            users.merchantApply(token, Collections.singletonMap("type", 1)).getData();
    @SuppressWarnings("unchecked")
    Map<String, Object> shopInfo =
        (Map<String, Object>) users.shopApply(token, Collections.singletonMap("type", 1)).getData();
    @SuppressWarnings("unchecked")
    Map<String, Object> merchantDetail = (Map<String, Object>) merchantInfo.get("merchant");
    @SuppressWarnings("unchecked")
    Map<String, Object> shopDetail = (Map<String, Object>) shopInfo.get("shop");
    assertEquals(
        0,
        new java.math.BigDecimal("114.0")
            .compareTo(new java.math.BigDecimal(merchantDetail.get("lng").toString())));
    assertEquals(
        0,
        new java.math.BigDecimal("22.5")
            .compareTo(new java.math.BigDecimal(merchantDetail.get("lat").toString())));
    assertEquals(Arrays.asList("/test/a.png", "/test/b.png"), shopDetail.get("image"));
    Map<String, Object> after =
        jdbc.queryForMap(
            "select merchant_status,merchant_id,shop_status,shop_id from ymcx_user where id=?",
            userId);
    assertEquals(2, ((Number) after.get("merchant_status")).intValue());
    assertTrue(((Number) after.get("merchant_id")).longValue() > 0);
    assertEquals(2, ((Number) after.get("shop_status")).intValue());
    assertTrue(((Number) after.get("shop_id")).longValue() > 0);
    assertEquals(
        "",
        jdbc.queryForObject(
            "select coalesce(qrimage,'') from ymcx_merchant where id=?",
            String.class,
            after.get("merchant_id")));
    assertEquals(
        "",
        jdbc.queryForObject(
            "select coalesce(qrimage,'') from ymcx_shop where id=?",
            String.class,
            after.get("shop_id")));

    long merchantId = ((Number) after.get("merchant_id")).longValue();
    long shopId = ((Number) after.get("shop_id")).longValue();
    jdbc.update("update ymcx_merchant set examine_status=3,examine_note='资料不清晰' where id=?", merchantId);
    jdbc.update("update ymcx_shop set examine_status=3,examine_note='照片不清晰' where id=?", shopId);
    jdbc.update("update ymcx_user set merchant_status=4,shop_status=4 where id=?", userId);
    merchant.put("name", "重提测试门店");
    merchant.put("address", "重提门店地址");
    shop.put("name", "重提测试酒店");
    shop.put("address", "重提酒店地址");
    shop.put("image", Collections.singletonList("/test/resubmit.png"));
    assertEquals(1, users.merchantApply(token, merchant).getCode());
    assertEquals(1, users.shopApply(token, shop).getCode());
    assertEquals(
        1,
        jdbc.queryForObject(
            "select count(*) from ymcx_merchant where id=? and user_id=? and name='重提测试门店' and address='重提门店地址' and examine_status=1 and examine_note='资料不清晰'",
            Integer.class,
            merchantId,
            userId));
    assertEquals(
        1,
        jdbc.queryForObject(
            "select count(*) from ymcx_shop where id=? and user_id=? and name='重提测试酒店' and address='重提酒店地址' and image='/test/resubmit.png' and examine_status=1 and examine_note='照片不清晰'",
            Integer.class,
            shopId,
            userId));
    Map<String, Object> resubmittedUser =
        jdbc.queryForMap("select merchant_status,shop_status from ymcx_user where id=?", userId);
    assertEquals(2, ((Number) resubmittedUser.get("merchant_status")).intValue());
    assertEquals(2, ((Number) resubmittedUser.get("shop_status")).intValue());
    assertEquals(
        1,
        jdbc.queryForObject(
            "select count(*) from ymcx_merchant where user_id=? and is_delete=1",
            Integer.class,
            userId));
    assertEquals(
        1,
        jdbc.queryForObject(
            "select count(*) from ymcx_shop where user_id=? and is_delete=1",
            Integer.class,
            userId));
  }
}
