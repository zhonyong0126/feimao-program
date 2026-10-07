package com.feimao.controller.common;

import com.feimao.common.BusinessResponse;
import com.feimao.service.VerificationCodeService;
import java.util.Map;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class VerificationController {
  private static final Pattern MOBILE = Pattern.compile("^1\\d{10}$");
  private final VerificationCodeService codes;

  public VerificationController(VerificationCodeService codes) {
    this.codes = codes;
  }

  @PostMapping("/sms/send")
  public BusinessResponse<?> sendSms(
      @RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
    String mobile = value(body, "mobile");
    if (!MOBILE.matcher(mobile).matches()) return BusinessResponse.error("手机号不正确");
    try {
      codes.sendSms(mobile, event(body), clientIp(request));
      return BusinessResponse.successMessage("发送成功");
    } catch (IllegalArgumentException | IllegalStateException exception) {
      return BusinessResponse.error(exception.getMessage());
    }
  }

  @PostMapping("/sms/check")
  public BusinessResponse<?> checkSms(@RequestBody(required = false) Map<String, Object> body) {
    String mobile = value(body, "mobile");
    if (!MOBILE.matcher(mobile).matches()) return BusinessResponse.error("手机号不正确");
    try {
      return codes.checkSms(mobile, event(body), value(body, "captcha"))
          ? BusinessResponse.successMessage("成功")
          : BusinessResponse.error("验证码不正确");
    } catch (IllegalArgumentException exception) {
      return BusinessResponse.error(exception.getMessage());
    }
  }

  @PostMapping("/ems/send")
  public BusinessResponse<?> sendEmail(
      @RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
    String email = value(body, "email");
    try {
      codes.sendEmail(email, event(body), clientIp(request));
      return BusinessResponse.successMessage("发送成功");
    } catch (IllegalArgumentException | IllegalStateException exception) {
      return BusinessResponse.error(exception.getMessage());
    }
  }

  @PostMapping("/ems/check")
  public BusinessResponse<?> checkEmail(@RequestBody(required = false) Map<String, Object> body) {
    String email = value(body, "email");
    try {
      return codes.checkEmail(email, event(body), value(body, "captcha"))
          ? BusinessResponse.successMessage("成功")
          : BusinessResponse.error("验证码不正确");
    } catch (IllegalArgumentException exception) {
      return BusinessResponse.error(exception.getMessage());
    }
  }

  private static String event(Map<String, Object> body) {
    String event = value(body, "event");
    return event.isEmpty() ? "register" : event;
  }

  private static String value(Map<String, Object> body, String key) {
    return body == null || body.get(key) == null ? "" : String.valueOf(body.get(key)).trim();
  }

  private static String clientIp(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");
    return forwarded == null || forwarded.isEmpty()
        ? request.getRemoteAddr()
        : forwarded.split(",")[0].trim();
  }
}
