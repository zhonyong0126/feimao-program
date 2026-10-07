package com.feimao.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feimao.mapper.PartnerCodeMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class MiniProgramCodeService {
  private static final Logger LOG = LoggerFactory.getLogger(MiniProgramCodeService.class);

  private final PartnerCodeMapper partners;
  private final ObjectMapper json;
  private final RestTemplate http;
  private final String appId;
  private final String secret;
  private final String envVersion;
  private final Path uploadDir;
  private String accessToken;
  private long tokenExpiresAt;

  public MiniProgramCodeService(
      PartnerCodeMapper partners,
      ObjectMapper json,
      RestTemplateBuilder builder,
      @Value("${business.wechat.app-id:}") String appId,
      @Value("${business.wechat.secret:}") String secret,
      @Value("${business.wechat.code-env-version:release}") String envVersion,
      @Value("${business.upload-dir:./uploads}") String uploadDir) {
    this.partners = partners;
    this.json = json;
    this.http =
        builder
            .setConnectTimeout(Duration.ofSeconds(5))
            .setReadTimeout(Duration.ofSeconds(20))
            .build();
    this.appId = appId;
    this.secret = secret;
    this.envVersion = envVersion;
    this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
  }

  public boolean configured() {
    return !appId.isEmpty() && !secret.isEmpty();
  }

  @Async
  public void generateAsync(String role, long id) {
    try {
      generate(role, id);
    } catch (Exception exception) {
      LOG.warn("小程序码生成失败，role={}, id={}", role, id, exception);
    }
  }

  public String generate(String role, long id) throws IOException {
    if (!configured()) throw new IllegalStateException("微信小程序码未配置");
    boolean merchant = "merchant".equals(role);
    if (!merchant && !"shop".equals(role)) throw new IllegalArgumentException("二维码类型错误");
    int exists = merchant ? partners.merchantCount(id) : partners.shopCount(id);
    if (exists == 0) throw new IllegalArgumentException("二维码主体不存在");
    Map<String, Object> request = new LinkedHashMap<>();
    request.put("scene", id + (merchant ? "-2" : "-3"));
    request.put("page", merchant ? "pages/IndexChild/XQ" : "pages/index/index");
    request.put("env_version", envVersion);
    request.put("check_path", false);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    String url =
        UriComponentsBuilder.fromHttpUrl("https://api.weixin.qq.com/wxa/getwxacodeunlimit")
            .queryParam("access_token", accessToken())
            .toUriString();
    ResponseEntity<byte[]> response =
        http.postForEntity(url, new HttpEntity<>(request, headers), byte[].class);
    byte[] image = response.getBody();
    if (image == null || image.length == 0) throw new IllegalStateException("微信未返回小程序码");
    MediaType type = response.getHeaders().getContentType();
    if (type != null && MediaType.APPLICATION_JSON.isCompatibleWith(type)) {
      JsonNode error = json.readTree(image);
      throw new IllegalStateException(error.path("errmsg").asText("小程序码生成失败"));
    }
    Path directory = uploadDir.resolve(role).normalize();
    if (!directory.startsWith(uploadDir)) throw new IllegalStateException("上传目录错误");
    Files.createDirectories(directory);
    String fileName = "appcode" + id + ".png";
    Path target = directory.resolve(fileName);
    Path temporary = Files.createTempFile(directory, "appcode-", ".tmp");
    try {
      Files.write(temporary, image);
      Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
    } finally {
      Files.deleteIfExists(temporary);
    }
    String publicPath = "/uploads/" + role + "/" + fileName;
    if (merchant) partners.updateMerchantCode(id, publicPath, now());
    else partners.updateShopCode(id, publicPath, now());
    return publicPath;
  }

  @Scheduled(fixedDelayString = "${business.wechat.code-retry-delay-ms:600000}")
  public void retryMissingCodes() {
    if (!configured()) return;
    retry("merchant", partners.missingMerchantCodes());
    retry("shop", partners.missingShopCodes());
  }

  private void retry(String role, List<Long> ids) {
    for (Long id : ids) {
      try {
        generate(role, id);
      } catch (Exception exception) {
        LOG.warn("小程序码重试失败，role={}, id={}", role, id, exception);
      }
    }
  }

  private synchronized String accessToken() {
    long current = now();
    if (accessToken != null && tokenExpiresAt > current + 60) return accessToken;
    String url =
        UriComponentsBuilder.fromHttpUrl("https://api.weixin.qq.com/cgi-bin/token")
            .queryParam("grant_type", "client_credential")
            .queryParam("appid", appId)
            .queryParam("secret", secret)
            .toUriString();
    JsonNode response = http.getForObject(url, JsonNode.class);
    if (response == null || !response.hasNonNull("access_token"))
      throw new IllegalStateException(
          response == null ? "获取微信令牌失败" : response.path("errmsg").asText("获取微信令牌失败"));
    accessToken = response.path("access_token").asText();
    tokenExpiresAt = current + Math.max(300, response.path("expires_in").asLong(7200));
    return accessToken;
  }

  private static long now() {
    return System.currentTimeMillis() / 1000;
  }
}
