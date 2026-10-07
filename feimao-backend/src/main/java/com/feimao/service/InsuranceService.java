package com.feimao.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feimao.mapper.InsuranceMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class InsuranceService {
  private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private final InsuranceMapper policies;
  private final ObjectMapper json;
  private final RestTemplate http;
  private final String insureUrl;
  private final String queryUrl;
  private final String cancelUrl;
  private final String token;
  private final String probeUserName;
  private final String probeCertNo;
  private final String probeMobile;
  private final String probeUnlockTime;
  private final String probeProvince;
  private final String probeCity;
  private final String probePromoteId;
  private final String probeOrderSn;
  private final Map<String, Object> provider = new LinkedHashMap<>();

  public InsuranceService(
      InsuranceMapper policies,
      ObjectMapper json,
      RestTemplateBuilder builder,
      @Value("${business.insurance.insure-url:}") String insureUrl,
      @Value("${business.insurance.query-url:}") String queryUrl,
      @Value("${business.insurance.cancel-url:}") String cancelUrl,
      @Value("${business.insurance.token:}") String token,
      @Value("${business.insurance.product-code:}") String productCode,
      @Value("${business.insurance.applicant-name:}") String applicantName,
      @Value("${business.insurance.applicant-num:}") String applicantNum,
      @Value("${business.insurance.address:}") String address,
      @Value("${business.insurance.link-man:}") String linkMan,
      @Value("${business.insurance.link-mobile:}") String linkMobile,
      @Value("${business.insurance.probe-user-name:}") String probeUserName,
      @Value("${business.insurance.probe-cert-no:}") String probeCertNo,
      @Value("${business.insurance.probe-mobile:}") String probeMobile,
      @Value("${business.insurance.probe-unlock-time:}") String probeUnlockTime,
      @Value("${business.insurance.probe-province:}") String probeProvince,
      @Value("${business.insurance.probe-city:}") String probeCity,
      @Value("${business.insurance.probe-promote-id:}") String probePromoteId,
      @Value("${business.insurance.probe-order-sn:}") String probeOrderSn) {
    this.policies = policies;
    this.json = json;
    this.http =
        builder
            .setConnectTimeout(Duration.ofSeconds(5))
            .setReadTimeout(Duration.ofSeconds(15))
            .build();
    this.insureUrl = insureUrl;
    this.queryUrl = queryUrl;
    this.cancelUrl = cancelUrl;
    this.token = token;
    this.probeUserName = probeUserName;
    this.probeCertNo = probeCertNo;
    this.probeMobile = probeMobile;
    this.probeUnlockTime = probeUnlockTime;
    this.probeProvince = probeProvince;
    this.probeCity = probeCity;
    this.probePromoteId = probePromoteId;
    this.probeOrderSn = probeOrderSn;
    provider.put("product_code", productCode);
    provider.put("applicant_name", applicantName);
    provider.put("applicant_num", applicantNum);
    provider.put("address", address);
    provider.put("link_man", linkMan);
    provider.put("link_mobile", linkMobile);
  }

  public boolean canInsure() {
    return !insureUrl.isEmpty() && !token.isEmpty() && providerReady();
  }

  public boolean canCancel() {
    return !cancelUrl.isEmpty() && !token.isEmpty() && providerReady();
  }

  public Map<String, Object> queryGuestPolicy(long guestId) {
    if (queryUrl.isEmpty() || token.isEmpty() || !providerReady())
      throw new IllegalStateException("保险查询服务未配置");
    String policyNumber = policies.selectPolicyNumber(guestId);
    if (text(policyNumber).isEmpty()) throw new IllegalArgumentException("保单不存在");
    try {
      Map<String, Object> body = new LinkedHashMap<>();
      body.put("biz_order_sn", policyNumber);
      body.putAll(provider);
      JsonNode response = post(queryUrl, body);
      if (!success(response))
        throw new IllegalStateException(response.path("msg").asText("保单查询失败"));
      JsonNode record = response.path("data").path("record");
      JsonNode item = record.isArray() ? record.path(0) : record;
      @SuppressWarnings("unchecked")
      Map<String, Object> result = json.convertValue(item, LinkedHashMap.class);
      result.remove("promote_id");
      result.remove("policy_num");
      result.remove("note");
      result.remove("policy_url");
      result.remove("product_code");
      Object certType = result.get("policy_user_certtype");
      if (certType != null)
        result.put("policy_user_certtype", "1".equals(String.valueOf(certType)) ? "身份证" : "护照");
      Object sex = result.get("policy_user_sex");
      if (sex != null) result.put("policy_user_sex", "1".equals(String.valueOf(sex)) ? "男" : "女");
      Object status = result.get("status");
      if (status != null) result.put("status", policyStatus(String.valueOf(status)));
      return result;
    } catch (IllegalStateException e) {
      throw e;
    } catch (Exception e) {
      throw new IllegalStateException("保单查询失败：" + e.getMessage(), e);
    }
  }

  public int insurePendingGuests() {
    if (!canInsure()) return 0;
    List<Map<String, Object>> rows = policies.selectPendingInsureGuests();
    int success = 0;
    for (Map<String, Object> row : rows) if (insure(row)) success++;
    return success;
  }

  public int cancelPendingGuests() {
    if (!canCancel()) return 0;
    List<Map<String, Object>> rows = policies.selectPendingCancelGuests(now());
    int success = 0;
    for (Map<String, Object> row : rows) if (cancel(row)) success++;
    return success;
  }

  /** Cancels effective policies during the legacy administrator order-cancellation transaction. */
  public int cancelOrderGuests(long orderId, long current) {
    List<Map<String, Object>> rows = policies.selectOrderGuestsToCancel(orderId, current);
    int success = 0;
    for (Map<String, Object> row : rows) if (cancel(row)) success++;
    return success;
  }

  /** Preserves the three public insurance-provider probes shipped by the legacy API. */
  public Map<String, Object> legacyProbe(String action) {
    String url = "insure".equals(action) ? insureUrl : "query".equals(action) ? queryUrl : cancelUrl;
    if (url.isEmpty() || token.isEmpty() || !providerReady())
      throw new IllegalStateException("保险服务未配置");
    Map<String, Object> body = new LinkedHashMap<>();
    if ("insure".equals(action)) {
      if (probeUserName.isEmpty()
          || probeCertNo.isEmpty()
          || probeMobile.isEmpty()
          || probeUnlockTime.isEmpty())
        throw new IllegalStateException("保险测试投保参数未配置");
      body.put("policy_user_name", probeUserName);
      body.put("policy_user_certno", probeCertNo);
      body.put("policy_user_certtype", 2);
      body.put("policy_user_mobile", probeMobile);
      body.put("policy_user_sex", gender(probeCertNo));
      body.put("unlock_time", probeUnlockTime);
      body.put("policy_user_birthday", birthday(probeCertNo));
      body.put("province", probeProvince);
      body.put("city", probeCity);
      body.put("promote_id", probePromoteId);
      body.put("guarantee", 1);
      body.put("biz_order_sn", guestNumber());
    } else {
      if (probeOrderSn.isEmpty()) throw new IllegalStateException("保险测试订单号未配置");
      body.put("biz_order_sn", probeOrderSn);
    }
    body.putAll(provider);
    try {
      Map<String, Object> result = new LinkedHashMap<>();
      result.put("requestBody", body);
      result.put("response", json.convertValue(post(url, body), Object.class));
      return result;
    } catch (Exception exception) {
      throw new IllegalStateException("保险接口调用失败：" + exception.getMessage(), exception);
    }
  }

  private boolean insure(Map<String, Object> row) {
    long id = number(row.get("id"));
    try {
      String idcard = text(row.get("idcard"));
      Map<String, Object> body = new LinkedHashMap<>();
      body.put("policy_user_name", text(row.get("name")));
      body.put("policy_user_certno", idcard);
      body.put("policy_user_certtype", 1);
      body.put("policy_user_mobile", text(row.get("mobile")));
      body.put("policy_user_sex", gender(idcard));
      body.put("unlock_time", format(number(row.get("starttime"))));
      body.put("policy_user_birthday", birthday(idcard));
      String address =
          text(row.get("pick_address")).isEmpty()
              ? text(row.get("administrative")) + text(row.get("merchant_address"))
              : text(row.get("pick_address"));
      body.put("province", province(address));
      body.put("city", city(address));
      body.put("promote_id", "xichang");
      body.put("guarantee", guarantee(row));
      body.put("biz_order_sn", guestNumber());
      body.putAll(provider);
      JsonNode response = post(insureUrl, body);
      long current = now();
      if (success(response)) {
        JsonNode item = response.path("succ_data");
        if (item.isArray()) item = item.path(0);
        policies.markInsured(
            id,
            item.path("biz_order_sn").asText(text(body.get("biz_order_sn"))),
            item.path("num_id").asText(""),
            row.get("starttime"),
            row.get("endtime"),
            response.toString(),
            current);
        return true;
      }
      saveResponse(id, response.toString());
    } catch (Exception e) {
      saveResponse(id, error(e));
    }
    return false;
  }

  private boolean cancel(Map<String, Object> row) {
    long id = number(row.get("id"));
    try {
      Map<String, Object> body = new LinkedHashMap<>();
      body.put("biz_order_sn", text(row.get("biz_order_sn")));
      body.putAll(provider);
      JsonNode response = post(cancelUrl, body);
      long current = now();
      if (success(response)) {
        policies.markCancelled(id, response.toString(), current);
        return true;
      }
      saveResponse(id, response.toString());
    } catch (Exception e) {
      saveResponse(id, error(e));
    }
    return false;
  }

  private JsonNode post(String url, Map<String, Object> body) throws JsonProcessingException {
    long timestamp = now();
    String nonce = UUID.randomUUID().toString();
    List<String> parts =
        new ArrayList<>(
            Arrays.asList(
                token, String.valueOf(timestamp), nonce, md5(json.writeValueAsString(body))));
    Collections.sort(parts);
    Map<String, Object> head = new LinkedHashMap<>();
    head.put("timestamp", timestamp);
    head.put("nonce", nonce);
    head.put("sign", md5(String.join("", parts)));
    Map<String, Object> request = new LinkedHashMap<>();
    request.put("requestBody", body);
    request.put("requestHead", head);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    String response = http.postForObject(url, new HttpEntity<>(request, headers), String.class);
    return json.readTree(response == null ? "{}" : response);
  }

  private boolean providerReady() {
    for (Object value : provider.values()) if (text(value).isEmpty()) return false;
    return true;
  }

  private void saveResponse(long id, String response) {
    policies.saveResponse(id, response, now());
  }

  private String error(Exception e) {
    try {
      Map<String, Object> value = new LinkedHashMap<>();
      value.put("code", "LOCAL_ERROR");
      value.put("message", e.getMessage());
      return json.writeValueAsString(value);
    } catch (JsonProcessingException ignored) {
      return "{\"code\":\"LOCAL_ERROR\"}";
    }
  }

  private static boolean success(JsonNode response) {
    return "1000".equals(response.path("code").asText());
  }

  private static String policyStatus(String status) {
    if ("6".equals(status)) return "投保成功";
    if ("10".equals(status)) return "承保失败";
    if ("11".equals(status)) return "已出单";
    return "保障注销";
  }

  private static long guarantee(Map<String, Object> row) {
    int leaseType = (int) number(row.get("lease_type"));
    if (leaseType == 1) return 1;
    if (leaseType == 2) {
      try {
        return Math.max(1, Long.parseLong(text(row.get("lease_duration"))));
      } catch (NumberFormatException ignored) {
        return 1;
      }
    }
    return 30;
  }

  private static String birthday(String idcard) {
    if (idcard.length() != 18) return "";
    return idcard.substring(6, 10)
        + "-"
        + idcard.substring(10, 12)
        + "-"
        + idcard.substring(12, 14);
  }

  private static int gender(String idcard) {
    if (idcard.length() != 18 || !Character.isDigit(idcard.charAt(16))) return 0;
    return (idcard.charAt(16) - '0') % 2 == 1 ? 1 : 2;
  }

  private static String province(String address) {
    int at = address.indexOf('省');
    if (at >= 0) return address.substring(0, at + 1);
    for (String city : Arrays.asList("北京市", "上海市", "天津市", "重庆市"))
      if (address.startsWith(city)) return city;
    return "";
  }

  private static String city(String address) {
    String province = province(address);
    String rest = address.substring(Math.min(province.length(), address.length()));
    int at = rest.indexOf('市');
    return at < 0 ? "" : rest.substring(0, at + 1);
  }

  private String guestNumber() {
    for (int i = 0; i < 10; i++) {
      String value =
          String.valueOf(System.currentTimeMillis())
              + String.format("%06d", (int) (Math.random() * 1000000));
      if (policies.policyNumberCount(value) == 0) return value;
    }
    return UUID.randomUUID().toString().replace("-", "");
  }

  private static String format(long epoch) {
    return Instant.ofEpochSecond(epoch).atZone(ZoneId.systemDefault()).format(TIME);
  }

  private static String md5(String value) {
    try {
      byte[] digest =
          MessageDigest.getInstance("MD5").digest(value.getBytes(StandardCharsets.UTF_8));
      StringBuilder result = new StringBuilder();
      for (byte item : digest) result.append(String.format("%02x", item & 0xff));
      return result.toString();
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private static long number(Object value) {
    return value == null ? 0 : Long.parseLong(value.toString());
  }

  private static String text(Object value) {
    return value == null ? "" : String.valueOf(value);
  }

  private static long now() {
    return System.currentTimeMillis() / 1000;
  }
}
