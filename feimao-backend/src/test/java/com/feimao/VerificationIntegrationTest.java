package com.feimao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.feimao.controller.common.ValidateController;
import com.feimao.controller.common.VerificationController;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class VerificationIntegrationTest {
  @Autowired JdbcTemplate jdbc;
  @Autowired VerificationController verification;
  @Autowired ValidateController validate;

  @Test
  void smsCodeUsesHistoricalTableAndAccountRules() {
    String mobile =
        jdbc.queryForObject(
            "select mobile from ymcx_user where mobile<>'' and status='normal' order by id limit 1",
            String.class);
    long now = System.currentTimeMillis() / 1000;
    jdbc.update(
        "insert into ymcx_sms(event,mobile,code,times,ip,createtime) values('resetpwd',?,'654321',0,'127.0.0.1',?)",
        mobile,
        now);
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("mobile", mobile);
    body.put("event", "resetpwd");
    body.put("captcha", "654321");
    assertEquals(1, verification.checkSms(body).getCode());
  }

  @Test
  void missingProviderDoesNotPersistFakeEmailCode() {
    String email = "verification-" + System.nanoTime() + "@example.com";
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("email", email);
    body.put("event", "register");
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteAddr("127.0.0.1");
    assertEquals(0, verification.sendEmail(body, request).getCode());
    assertEquals(
        0,
        jdbc.queryForObject("select count(*) from ymcx_ems where email=?", Integer.class, email));
  }

  @Test
  void accountAndCodeValidationEndpointsPreserveTheirRules() {
    long userId =
        jdbc.queryForObject(
            "select id from ymcx_user where mobile<>'' and status='normal' order by id limit 1",
            Long.class);
    String suffix = String.valueOf(System.nanoTime());
    jdbc.update(
        "update ymcx_user set username=?,nickname=?,email=? where id=?",
        "validate" + suffix,
        "校验用户" + suffix,
        "validate-" + suffix + "@example.com",
        userId);
    Map<String, Object> user =
        jdbc.queryForMap(
            "select id,username,nickname,mobile,email from ymcx_user where id=?", userId);
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("mobile", user.get("mobile"));
    assertEquals(0, validate.checkMobileAvailable(body).getCode());
    assertEquals(1, validate.checkMobileExist(body).getCode());
    body.put("id", user.get("id"));
    assertEquals(1, validate.checkMobileAvailable(body).getCode());

    body.clear();
    body.put("email", user.get("email"));
    assertEquals(0, validate.checkEmailAvailable(body).getCode());
    assertEquals(1, validate.checkEmailExist(body).getCode());
    body.put("id", user.get("id"));
    assertEquals(1, validate.checkEmailAvailable(body).getCode());

    body.clear();
    body.put("username", user.get("username"));
    assertEquals(0, validate.checkUsernameAvailable(body).getCode());
    body.put("nickname", user.get("nickname"));
    assertEquals(0, validate.checkNicknameAvailable(body).getCode());

    long now = System.currentTimeMillis() / 1000;
    jdbc.update(
        "insert into ymcx_sms(event,mobile,code,times,ip,createtime) values('resetpwd',?,'112233',0,'127.0.0.1',?)",
        user.get("mobile"),
        now);
    body.clear();
    body.put("mobile", user.get("mobile"));
    body.put("event", "resetpwd");
    body.put("captcha", "112233");
    assertEquals(1, validate.checkSmsCorrect(body).getCode());
  }
}
