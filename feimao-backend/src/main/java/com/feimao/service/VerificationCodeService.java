package com.feimao.service;

import com.feimao.mapper.VerificationCodeMapper;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;
import javax.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class VerificationCodeService {
  private final VerificationCodeMapper codes;
  private final JavaMailSender mail;
  private final RestTemplate http;
  private final String smsUrl;
  private final String smsAppCode;
  private final String smsTemplate;
  private final String mailFrom;

  public VerificationCodeService(
      VerificationCodeMapper codes,
      JavaMailSender mail,
      RestTemplateBuilder builder,
      @Value("${business.sms.url:}") String smsUrl,
      @Value("${business.sms.app-code:}") String smsAppCode,
      @Value("${business.sms.verification-template-id:}") String smsTemplate,
      @Value("${business.mail-from:}") String mailFrom) {
    this.codes = codes;
    this.mail = mail;
    this.http =
        builder
            .setConnectTimeout(Duration.ofSeconds(5))
            .setReadTimeout(Duration.ofSeconds(15))
            .build();
    this.smsUrl = smsUrl;
    this.smsAppCode = smsAppCode;
    this.smsTemplate = smsTemplate;
    this.mailFrom = mailFrom;
  }

  public void sendSms(String mobile, String event, String ip) {
    validateAccount("mobile", mobile, event);
    limit("ymcx_sms", "mobile", mobile, event, ip, true);
    if (smsUrl.isEmpty() || smsAppCode.isEmpty() || smsTemplate.isEmpty())
      throw new IllegalStateException("请在后台插件管理安装短信验证插件");
    String code = code();
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    headers.set("Authorization", "APPCODE " + smsAppCode);
    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("templateId", smsTemplate);
    form.add("receive", mobile);
    form.add("tag", code);
    http.postForObject(smsUrl, new HttpEntity<>(form, headers), String.class);
    codes.insertSms(mobile, event, code, ip, now());
  }

  public void sendEmail(String email, String event, String ip) {
    validateAccount("email", email, event);
    limit("ymcx_ems", "email", email, event, ip, false);
    if (mailFrom.isEmpty()) throw new IllegalStateException("发送失败");
    String code = code();
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(mailFrom);
    message.setTo(email);
    message.setSubject("邮箱验证码");
    message.setText("您的验证码是：" + code + "，5分钟内有效。请勿泄露给他人。");
    mail.send(message);
    codes.insertEmail(email, event, code, ip, now());
  }

  public void sendTestEmail(String email) {
    if (email == null
        || !email.trim().matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
      throw new IllegalArgumentException("邮箱格式错误");
    if (mailFrom.isEmpty()) throw new IllegalStateException("邮件服务未配置");
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(mailFrom);
    message.setTo(email.trim());
    message.setSubject("租售业务后台测试邮件");
    message.setText("这是一封来自租售业务后台的 SMTP 配置测试邮件。收到此邮件表示邮件服务配置有效。");
    mail.send(message);
  }

  /** Send the FastAdmin configuration test with the SMTP values submitted by that form. */
  public void sendTestEmail(String email, Map<String, Object> settings, String siteName) {
    String mailType = setting(settings, "mail_type");
    if (!("1".equals(mailType) || "2".equals(mailType)))
      throw new IllegalStateException("邮件功能已关闭");

    JavaMailSenderImpl sender = new JavaMailSenderImpl();
    sender.setHost(setting(settings, "mail_smtp_host"));
    try {
      sender.setPort(Integer.parseInt(setting(settings, "mail_smtp_port")));
    } catch (NumberFormatException exception) {
      throw new IllegalStateException(exception.getMessage(), exception);
    }
    String from = setting(settings, "mail_from");
    sender.setUsername(from);
    sender.setPassword(setting(settings, "mail_smtp_pass"));
    sender.setDefaultEncoding("UTF-8");
    Properties properties = sender.getJavaMailProperties();
    properties.setProperty("mail.smtp.auth", "true");
    properties.setProperty("mail.smtp.connectiontimeout", "5000");
    properties.setProperty("mail.smtp.timeout", "15000");
    properties.setProperty("mail.smtp.writetimeout", "15000");
    String verifyType = setting(settings, "mail_verify_type");
    if ("1".equals(verifyType)) properties.setProperty("mail.smtp.starttls.enable", "true");
    if ("2".equals(verifyType)) properties.setProperty("mail.smtp.ssl.enable", "true");

    try {
      MimeMessage message = sender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
      helper.setFrom(from, setting(settings, "mail_smtp_user"));
      helper.setTo(email.trim());
      helper.setSubject("这是一封来自" + Objects.toString(siteName, "") + "的邮件");
      helper.setText(
          "<div style=\"min-height:550px; padding: 100px 55px 200px;\">"
              + "这是一封来自"
              + Objects.toString(siteName, "")
              + "的校验邮件,用于校验邮件配置是否正常!"
              + "</div>",
          true);
      sender.send(message);
    } catch (Exception exception) {
      String message = exception.getMessage();
      Throwable cause = exception.getCause();
      while ((message == null || message.trim().isEmpty()) && cause != null) {
        message = cause.getMessage();
        cause = cause.getCause();
      }
      throw new IllegalStateException(
          message == null || message.trim().isEmpty() ? "测试邮件发送失败" : message,
          exception);
    }
  }

  private static String setting(Map<String, Object> settings, String name) {
    return settings == null ? "" : Objects.toString(settings.get(name), "").trim();
  }

  public boolean checkSms(String mobile, String event, String code) {
    validateAccount("mobile", mobile, event);
    return checkSmsCode(mobile, event, code);
  }

  public boolean checkEmail(String email, String event, String code) {
    validateAccount("email", email, event);
    return checkEmailCode(email, event, code);
  }

  /** Raw check used by api/validate, whose legacy action did not apply account-event rules. */
  public boolean checkSmsCode(String mobile, String event, String code) {
    return check(codes.latestSms(mobile, event), code, true, mobile, event);
  }

  /** Raw check used by api/validate, whose legacy action did not apply account-event rules. */
  public boolean checkEmailCode(String email, String event, String code) {
    return check(codes.latestEmail(email, event), code, false, email, event);
  }

  private boolean check(
      Map<String, Object> row, String code, boolean sms, String target, String event) {
    if (row == null || row.isEmpty()) return false;
    long id = ((Number) row.get("id")).longValue();
    int times = ((Number) row.get("times")).intValue();
    long created = ((Number) row.get("createtime")).longValue();
    if (now() - created > 120) {
      if (sms) codes.deleteSms(target, event);
      else codes.deleteEmail(target, event);
      return false;
    }
    if (times > 10) return false;
    boolean correct = String.valueOf(row.get("code")).equals(code);
    if (!correct) {
      if (sms) codes.incrementSms(id);
      else codes.incrementEmail(id);
    }
    return correct;
  }

  private void limit(
      String table, String field, String target, String event, String ip, boolean ipLimit) {
    int recent =
        "ymcx_sms".equals(table)
            ? codes.recentSms(target, event, now() - 60)
            : codes.recentEmail(target, event, now() - 60);
    if (recent > 0) throw new IllegalStateException("发送频繁");
    if (ipLimit) {
      int hourly = codes.recentSmsByIp(ip, now() - 3600);
      if (hourly >= 5) throw new IllegalStateException("发送频繁");
    }
  }

  private void validateAccount(String field, String target, String event) {
    boolean occupied =
        ("mobile".equals(field) ? codes.mobileAccountCount(target) : codes.emailAccountCount(target))
            > 0;
    if ("register".equals(event) && occupied) throw new IllegalArgumentException("已被注册");
    if (("changemobile".equals(event) || "changeemail".equals(event)) && occupied)
      throw new IllegalArgumentException("已被占用");
    if (("changepwd".equals(event) || "resetpwd".equals(event)) && !occupied)
      throw new IllegalArgumentException("未注册");
  }

  private static String code() {
    return String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
  }

  private static long now() {
    return System.currentTimeMillis() / 1000;
  }
}
