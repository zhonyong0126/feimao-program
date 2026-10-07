package com.feimao.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class WechatLoginService {
  private final RestTemplate http;
  private final String appId;
  private final String secret;

  public WechatLoginService(
      RestTemplateBuilder builder,
      @Value("${business.wechat.app-id:}") String appId,
      @Value("${business.wechat.secret:}") String secret) {
    this.http = builder.build();
    this.appId = appId;
    this.secret = secret;
  }

  public String exchangeCode(String code) {
    if (appId.isEmpty() || secret.isEmpty()) {
      throw new IllegalStateException("微信登录未配置，请设置 WECHAT_APP_ID 和 WECHAT_APP_SECRET");
    }
    String url =
        UriComponentsBuilder.fromHttpUrl("https://api.weixin.qq.com/sns/jscode2session")
            .queryParam("appid", appId)
            .queryParam("secret", secret)
            .queryParam("js_code", code)
            .queryParam("grant_type", "authorization_code")
            .toUriString();
    try {
      JsonNode response = http.getForObject(url, JsonNode.class);
      if (response == null || !response.hasNonNull("openid")) {
        String message =
            response != null && response.has("errmsg") ? response.get("errmsg").asText() : "微信登录失败";
        throw new IllegalArgumentException(message);
      }
      return response.get("openid").asText();
    } catch (RestClientException ex) {
      throw new IllegalStateException("无法连接微信登录服务", ex);
    }
  }
}
