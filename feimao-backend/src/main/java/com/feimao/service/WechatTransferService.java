package com.feimao.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Service
public class WechatTransferService {
  private static final String PATH = "/v3/transfer/batches";
  private static final String URL = "https://api.mch.weixin.qq.com" + PATH;

  private final ObjectMapper json;
  private final RestTemplate http;
  private final String appId;
  private final String merchantId;
  private final String serialNo;
  private final String privateKeyPath;

  public WechatTransferService(
      ObjectMapper json,
      RestTemplateBuilder builder,
      @Value("${business.wechat.app-id:}") String appId,
      @Value("${business.wechat-pay.merchant-id:}") String merchantId,
      @Value("${business.wechat-pay.serial-no:}") String serialNo,
      @Value("${business.wechat-pay.private-key-path:}") String privateKeyPath) {
    this.json = json;
    this.http =
        builder
            .setConnectTimeout(Duration.ofSeconds(5))
            .setReadTimeout(Duration.ofSeconds(30))
            .build();
    this.appId = appId;
    this.merchantId = merchantId;
    this.serialNo = serialNo;
    this.privateKeyPath = privateKeyPath;
  }

  public boolean configured() {
    return !appId.isEmpty()
        && !merchantId.isEmpty()
        && !serialNo.isEmpty()
        && !privateKeyPath.isEmpty();
  }

  public String transfer(
      String openid, String tradeNumber, long amountInCents, String description) {
    if (!configured())
      throw new IllegalStateException("微信提现未配置，请设置商户号、证书序列号和 WECHAT_PAY_PRIVATE_KEY_PATH");
    if (openid == null || openid.isEmpty()) throw new IllegalArgumentException("提现用户未绑定微信");
    if (amountInCents <= 0) throw new IllegalArgumentException("提现金额错误");
    try {
      Map<String, Object> detail = new LinkedHashMap<>();
      detail.put("out_detail_no", tradeNumber);
      detail.put("transfer_amount", amountInCents);
      detail.put("transfer_remark", description);
      detail.put("openid", openid);
      Map<String, Object> request = new LinkedHashMap<>();
      request.put("appid", appId);
      request.put("out_batch_no", tradeNumber);
      request.put("batch_name", description);
      request.put("batch_remark", description);
      request.put("total_amount", amountInCents);
      request.put("total_num", 1);
      request.put("transfer_detail_list", java.util.Collections.singletonList(detail));
      String body = json.writeValueAsString(request);
      long timestamp = System.currentTimeMillis() / 1000;
      String nonce = UUID.randomUUID().toString().replace("-", "");
      String authorization = authorization(timestamp, nonce, body);
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));
      headers.set(HttpHeaders.AUTHORIZATION, authorization);
      headers.set(HttpHeaders.USER_AGENT, "feimao-business-migration");
      ResponseEntity<JsonNode> response =
          http.postForEntity(URL, new HttpEntity<>(body, headers), JsonNode.class);
      JsonNode value = response.getBody();
      if (value == null || !value.hasNonNull("batch_id"))
        throw new IllegalStateException("微信未返回转账批次号");
      return value.path("batch_id").asText();
    } catch (HttpStatusCodeException exception) {
      try {
        JsonNode error = json.readTree(exception.getResponseBodyAsString());
        throw new IllegalStateException(error.path("message").asText("微信提现失败"));
      } catch (java.io.IOException ignored) {
        throw new IllegalStateException("微信提现失败");
      }
    } catch (IllegalArgumentException | IllegalStateException exception) {
      throw exception;
    } catch (Exception exception) {
      throw new IllegalStateException("微信提现请求失败", exception);
    }
  }

  private String authorization(long timestamp, String nonce, String body) throws Exception {
    String message = "POST\n" + PATH + "\n" + timestamp + "\n" + nonce + "\n" + body + "\n";
    Signature signer = Signature.getInstance("SHA256withRSA");
    signer.initSign(privateKey());
    signer.update(message.getBytes(StandardCharsets.UTF_8));
    String signature = Base64.getEncoder().encodeToString(signer.sign());
    return "WECHATPAY2-SHA256-RSA2048 mchid=\""
        + merchantId
        + "\",nonce_str=\""
        + nonce
        + "\",timestamp=\""
        + timestamp
        + "\",serial_no=\""
        + serialNo
        + "\",signature=\""
        + signature
        + "\"";
  }

  private PrivateKey privateKey() throws Exception {
    String pem =
        new String(Files.readAllBytes(Paths.get(privateKeyPath)), StandardCharsets.UTF_8)
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replaceAll("\\s", "");
    return KeyFactory.getInstance("RSA")
        .generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(pem)));
  }
}
