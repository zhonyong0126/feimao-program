package com.feimao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.feimao.common.BusinessResponse;
import com.feimao.controller.user.SignController;
import com.feimao.controller.user.UserController;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class UserAuthenticationIntegrationTest {
  @Autowired JdbcTemplate jdbc;
  @Autowired UserController users;
  @Autowired SignController signs;

  private long userId;
  private String token;

  @BeforeEach
  void prepareUserToken() {
    userId =
        jdbc.queryForObject(
            "select id from ymcx_user where status='normal' order by id limit 1", Long.class);
    long now = System.currentTimeMillis() / 1000;
    token = "integrationUserAuthenticationToken0001";
    jdbc.update(
        "insert into ymcx_user_token(token,user_id,createtime,expiretime) values(?,?,?,?) "
            + "on duplicate key update user_id=values(user_id),expiretime=values(expiretime)",
        token,
        userId,
        now,
        now + 600);
  }

  @Test
  void realNameAuthenticationSupportsRejectedResubmissionAndPreventsDuplicatePendingSubmission() {
    jdbc.update("update ymcx_user set authentication=1 where id=?", userId);
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("authentication_mobile", "138" + "0000" + "0000");
    body.put("authentication_name", "认证测试用户");
    body.put("authentication_card", "440300" + "19900101" + "1234");

    assertEquals(0, users.authentication(null, body).getCode());
    assertEquals(1, users.authentication(token, body).getCode());
    assertEquals(0, users.authentication(token, body).getCode());
    assertEquals(2, jdbc.queryForObject("select authentication from ymcx_user where id=?", Integer.class, userId));

    jdbc.update("update ymcx_user set authentication=4 where id=?", userId);
    body.put("authentication_mobile", "139" + "0000" + "0000");
    assertEquals(1, users.authentication(token, body).getCode());
    @SuppressWarnings("unchecked")
    Map<String, Object> info =
        (Map<String, Object>) users.authenticationInfo(token).getData();
    assertEquals("139" + "0000" + "0000", info.get("authentication_mobile"));
  }

  @Test
  void staffAuthenticationPersistsWorkProofAndSupportsRejectedResubmission() {
    jdbc.update("update ymcx_user set staff_authentication=1 where id=?", userId);
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("staff_mobile", "137" + "0000" + "0000");
    body.put("staff_name", "员工测试用户");
    body.put("staff_work", "/uploads/test-work-proof.png");

    BusinessResponse<?> submitted = users.staffAuthentication(token, body);
    assertEquals(1, submitted.getCode());
    assertEquals(0, users.staffAuthentication(token, body).getCode());

    jdbc.update("update ymcx_user set staff_authentication=4 where id=?", userId);
    body.put("staff_work", "/uploads/rejected-work-proof.png");
    assertEquals(1, users.staffAuthentication(token, body).getCode());
    @SuppressWarnings("unchecked")
    Map<String, Object> info =
        (Map<String, Object>) users.staffAuthenticationInfo(token).getData();
    assertEquals("/uploads/rejected-work-proof.png", info.get("staff_work"));
  }

  @Test
  void malfunctionUsesTheLegacyOrderNumberAndStateCheckWithoutOwnershipFiltering() {
    Map<String, Object> order =
        jdbc.queryForMap(
            "select id,number,user_id from ymcx_order where is_delete=1 order by id limit 1");
    long orderUserId = ((Number) order.get("user_id")).longValue();
    jdbc.update("update ymcx_order set state=2 where id=?", order.get("id"));
    jdbc.update("update ymcx_user_token set user_id=? where token=?", orderUserId, token);
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("number", order.get("number"));
    body.put("image", "/uploads/malfunction-test.png");
    body.put("content", "测试故障内容");

    assertEquals(1, users.malfunction(token, body).getCode());
    assertEquals(
        1,
        jdbc.queryForObject(
            "select count(*) from ymcx_malfunction where user_id=? and number=? and image=? and content=?",
            Integer.class,
            orderUserId,
            order.get("number"),
            body.get("image"),
            body.get("content")));

    long anotherUser =
        jdbc.queryForObject(
            "select id from ymcx_user where status='normal' and id<>? order by id limit 1",
            Long.class,
            orderUserId);
    jdbc.update("update ymcx_user_token set user_id=? where token=?", anotherUser, token);
    assertEquals(1, users.malfunction(token, body).getCode());
    body.put("image", "");
    assertEquals(0, users.malfunction(token, body).getCode());
  }

  @Test
  void signInIsIdempotentAndInvalidRewardFallsBackToOne() {
    jdbc.update("delete from ymcx_sign_log where user_id=? and date(createtime)=current_date", userId);
    int before = jdbc.queryForObject("select score from ymcx_user where id=?", Integer.class, userId);
    jdbc.update("update ymcx_config set value='-5' where name='sign_score'");

    BusinessResponse<?> first = signs.sign(token);
    assertEquals(1, first.getCode());
    assertEquals(1, ((Number) first.getData()).intValue());
    assertEquals(0, signs.sign(token).getCode());
    assertEquals(
        before + 1,
        jdbc.queryForObject("select score from ymcx_user where id=?", Integer.class, userId));
    assertEquals(
        1,
        jdbc.queryForObject(
            "select count(*) from ymcx_sign_log where user_id=? and date(createtime)=current_date",
            Integer.class,
            userId));
  }

  @Test
  void couponRedemptionPersistsCouponScoreAndLogAndRejectsInvalidRequests() {
    long now = System.currentTimeMillis() / 1000;
    jdbc.update(
        "insert into ymcx_coupon(score,price,reduceprice,weigh,status,endtime,createtime,updatetime) "
            + "values(?,?,?,?,?,?,?,?)",
        20,
        "80.00",
        "10.00",
        9999,
        "normal",
        now + 3600,
        now,
        now);
    long couponId =
        jdbc.queryForObject(
            "select id from ymcx_coupon where score=20 and price=80.00 and reduceprice=10.00 "
                + "and weigh=9999 order by id desc limit 1",
            Long.class);
    jdbc.update("update ymcx_user set score=50 where id=?", userId);
    int couponCountBefore =
        jdbc.queryForObject(
            "select count(*) from ymcx_user_coupon where user_id=?", Integer.class, userId);
    int logCountBefore =
        jdbc.queryForObject(
            "select count(*) from ymcx_user_score_log where user_id=?", Integer.class, userId);
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("coupon_id", couponId);

    assertEquals(1, signs.redeem(token, body).getCode());
    assertEquals(
        30,
        jdbc.queryForObject("select score from ymcx_user where id=?", Integer.class, userId));
    assertEquals(
        couponCountBefore + 1,
        jdbc.queryForObject(
            "select count(*) from ymcx_user_coupon where user_id=?", Integer.class, userId));
    Map<String, Object> userCoupon =
        jdbc.queryForMap(
            "select price,reduceprice,state,endtime from ymcx_user_coupon "
                + "where user_id=? order by id desc limit 1",
            userId);
    assertEquals("80.00", userCoupon.get("price").toString());
    assertEquals("10.00", userCoupon.get("reduceprice").toString());
    assertEquals(1, ((Number) userCoupon.get("state")).intValue());
    assertEquals(now + 3600, ((Number) userCoupon.get("endtime")).longValue());
    Map<String, Object> scoreLog =
        jdbc.queryForMap(
            "select score,`before`,`after`,memo from ymcx_user_score_log "
                + "where user_id=? order by id desc limit 1",
            userId);
    assertEquals(-20, ((Number) scoreLog.get("score")).intValue());
    // The legacy action reads user.score after saving it, so both fields contain the result.
    assertEquals(30, ((Number) scoreLog.get("before")).intValue());
    assertEquals(30, ((Number) scoreLog.get("after")).intValue());
    assertEquals("兑换满80.00减10.00优惠券", scoreLog.get("memo"));
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> ownedCoupons =
        (List<Map<String, Object>>) users.couponList(token).getData();
    assertEquals(
        1L,
        ownedCoupons.stream()
            .filter(row -> ((Number) row.get("coupon_id")).longValue() > 0)
            .filter(row -> "80.00".equals(row.get("price").toString()))
            .filter(row -> "10.00".equals(row.get("reduceprice").toString()))
            .filter(row -> ((Number) row.get("endtime")).longValue() == now + 3600)
            .count());

    jdbc.update("update ymcx_user set score=5 where id=?", userId);
    assertEquals(0, signs.redeem(token, body).getCode());
    assertEquals(
        couponCountBefore + 1,
        jdbc.queryForObject(
            "select count(*) from ymcx_user_coupon where user_id=?", Integer.class, userId));
    assertEquals(
        logCountBefore + 1,
        jdbc.queryForObject(
            "select count(*) from ymcx_user_score_log where user_id=?", Integer.class, userId));

    jdbc.update("update ymcx_coupon set endtime=? where id=?", now - 1, couponId);
    assertEquals(0, signs.redeem(token, body).getCode());
  }

  @Test
  void addressCrudKeepsLegacyAddValidationAndUnvalidatedPartialEdit() {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("name", "地址测试用户");
    body.put("mobile", "138" + "0000" + "0000");
    body.put("administrative", "广东省-深圳市-龙岗区");
    body.put("address", "测试大道一号");
    body.put("lng", 114.123456);
    body.put("lat", 22.654321);

    assertEquals(1, users.addressAdd(token, body).getCode());
    long addressId =
        jdbc.queryForObject(
            "select id from ymcx_address where user_id=? and name=? order by id desc limit 1",
            Long.class,
            userId,
            body.get("name"));
    body.put("address_id", addressId);
    body.put("address", "测试大道二号");
    body.put("lng", 114.223456);
    assertEquals(1, users.addressEdit(token, body).getCode());
    Map<String, Object> saved =
        jdbc.queryForMap("select address,lng,lat from ymcx_address where id=?", addressId);
    assertEquals("测试大道二号", saved.get("address"));
    assertEquals(114.223456, ((Number) saved.get("lng")).doubleValue(), 0.000001);
    assertEquals(22.654321, ((Number) saved.get("lat")).doubleValue(), 0.000001);

    body.put("mobile", "12345");
    // Legacy addressEdit did not run the Address validator.
    assertEquals(1, users.addressEdit(token, body).getCode());
    body.put("mobile", "138" + "0000" + "0000");
    body.put("lat", 0);
    // ThinkPHP's require rule explicitly accepts numeric/string zero.
    assertEquals(1, users.addressAdd(token, body).getCode());

    long anotherUser =
        jdbc.queryForObject(
            "select id from ymcx_user where status='normal' and id<>? order by id limit 1",
            Long.class,
            userId);
    jdbc.update(
        "insert into ymcx_address(user_id,name,administrative,address,mobile,createtime,updatetime,lng,lat) "
            + "values(?,?,?,?,?,?,?,?,?)",
        anotherUser,
        "其他用户地址",
        "广东省-深圳市-龙岗区",
        "其他大道一号",
        "139" + "0000" + "0000",
        System.currentTimeMillis() / 1000,
        System.currentTimeMillis() / 1000,
        114.1,
        22.6);
    long anotherAddressId =
        jdbc.queryForObject(
            "select id from ymcx_address where user_id=? and name='其他用户地址' order by id desc limit 1",
            Long.class,
            anotherUser);
    body.put("lat", 22.654321);
    body.put("address_id", anotherAddressId);
    assertEquals(0, users.addressEdit(token, body).getCode());
    Map<String, Object> deleteBody = new LinkedHashMap<>();
    deleteBody.put("address_id", anotherAddressId);
    assertEquals(0, users.addressDel(token, deleteBody).getCode());
    deleteBody.put("address_id", addressId);
    assertEquals(1, users.addressDel(token, deleteBody).getCode());
  }
}
