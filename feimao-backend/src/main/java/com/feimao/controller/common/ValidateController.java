package com.feimao.controller.common;

import com.feimao.common.BusinessResponse;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.feimao.entity.BusinessUser;
import com.feimao.service.BusinessUserService;
import com.feimao.service.VerificationCodeService;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Public account and verification-code checks provided by the original API. */
@RestController
@RequestMapping("/api/validate")
public class ValidateController {
  private final BusinessUserService users;
  private final VerificationCodeService codes;

  public ValidateController(BusinessUserService users, VerificationCodeService codes) {
    this.users = users;
    this.codes = codes;
  }

  @PostMapping("/check_email_available")
  public BusinessResponse<?> checkEmailAvailable(
      @RequestBody(required = false) Map<String, Object> body) {
    return available("email", value(body, "email"), id(body), "邮箱已经被占用");
  }

  @PostMapping("/check_username_available")
  public BusinessResponse<?> checkUsernameAvailable(
      @RequestBody(required = false) Map<String, Object> body) {
    return available("username", value(body, "username"), id(body), "用户名已经被占用");
  }

  @PostMapping("/check_nickname_available")
  public BusinessResponse<?> checkNicknameAvailable(
      @RequestBody(required = false) Map<String, Object> body) {
    return available("nickname", value(body, "nickname"), id(body), "昵称已经被占用");
  }

  @PostMapping("/check_mobile_available")
  public BusinessResponse<?> checkMobileAvailable(
      @RequestBody(required = false) Map<String, Object> body) {
    return available("mobile", value(body, "mobile"), id(body), "该手机号已经占用");
  }

  @PostMapping("/check_mobile_exist")
  public BusinessResponse<?> checkMobileExist(
      @RequestBody(required = false) Map<String, Object> body) {
    return exists("mobile", value(body, "mobile"), "手机号不存在");
  }

  @PostMapping("/check_email_exist")
  public BusinessResponse<?> checkEmailExist(
      @RequestBody(required = false) Map<String, Object> body) {
    return exists("email", value(body, "email"), "邮箱不存在");
  }

  @PostMapping("/check_sms_correct")
  public BusinessResponse<?> checkSmsCorrect(
      @RequestBody(required = false) Map<String, Object> body) {
    try {
      return codes.checkSmsCode(value(body, "mobile"), value(body, "event"), value(body, "captcha"))
          ? BusinessResponse.success("", null)
          : BusinessResponse.error("验证码不正确");
    } catch (IllegalArgumentException exception) {
      return BusinessResponse.error(exception.getMessage());
    }
  }

  @PostMapping("/check_ems_correct")
  public BusinessResponse<?> checkEmailCorrect(
      @RequestBody(required = false) Map<String, Object> body) {
    try {
      return codes.checkEmailCode(value(body, "email"), value(body, "event"), value(body, "captcha"))
          ? BusinessResponse.success("", null)
          : BusinessResponse.error("验证码不正确");
    } catch (IllegalArgumentException exception) {
      return BusinessResponse.error(exception.getMessage());
    }
  }

  private BusinessResponse<?> available(String field, String value, long id, String message) {
    long count = users.count(new QueryWrapper<BusinessUser>().eq(field, value).ne("id", id));
    return count > 0 ? BusinessResponse.error(message) : BusinessResponse.success("", null);
  }

  private BusinessResponse<?> exists(String field, String value, String message) {
    long count = users.count(new QueryWrapper<BusinessUser>().eq(field, value));
    return count == 0 ? BusinessResponse.error(message) : BusinessResponse.success("", null);
  }

  private static long id(Map<String, Object> body) {
    try {
      return Long.parseLong(value(body, "id"));
    } catch (NumberFormatException ignored) {
      return 0;
    }
  }

  private static String value(Map<String, Object> body, String key) {
    return body == null || body.get(key) == null ? "" : String.valueOf(body.get(key)).trim();
  }
}
