package com.feimao.controller.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feimao.common.BusinessResponse;
import com.feimao.service.LegacyAjaxMaintenanceService;
import com.feimao.service.LegacyLanguageCatalog;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** Remaining compatibility actions from the original admin/index Ajax controllers. */
@RestController
public class LegacyAjaxAuxiliaryController {
  private static final long LANGUAGE_CACHE_SECONDS = 30L * 60 * 60 * 24;
  private static final DateTimeFormatter EXPIRES =
      DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
  private final LegacyLanguageCatalog languages;
  private final LegacyAjaxMaintenanceService maintenance;
  private final CommonController common;
  private final ObjectMapper json;
  private final boolean appDebug;

  public LegacyAjaxAuxiliaryController(
      LegacyLanguageCatalog languages,
      LegacyAjaxMaintenanceService maintenance,
      CommonController common,
      ObjectMapper json,
      @Value("${legacy.app-debug:true}") boolean appDebug) {
    this.languages = languages;
    this.maintenance = maintenance;
    this.common = common;
    this.json = json;
    this.appDebug = appDebug;
  }

  @GetMapping(value = {"/admin/ajax/lang", "/ajax/lang", "/index/ajax/lang"})
  public ResponseEntity<byte[]> lang(
      @RequestParam(value = "controllername", required = false) String controllerName,
      @RequestParam(value = "lang", required = false) String language,
      HttpServletRequest request)
      throws JsonProcessingException {
    String module = request.getRequestURI().startsWith("/index/") ? "index" : "admin";
    String body = "define(" + json.writeValueAsString(languages.get(module, controllerName, language)) + ");";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(
        new MediaType("application", "javascript", StandardCharsets.UTF_8));
    if (!appDebug) {
      headers.set(HttpHeaders.CACHE_CONTROL, "public");
      headers.set(HttpHeaders.PRAGMA, "cache");
      headers.set(
          HttpHeaders.EXPIRES,
          EXPIRES.format(ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(LANGUAGE_CACHE_SECONDS)));
    }
    return new ResponseEntity<>(body.getBytes(StandardCharsets.UTF_8), headers, HttpStatus.OK);
  }

  @RequestMapping(
      value = "/admin/ajax/wipecache",
      method = {RequestMethod.GET, RequestMethod.POST})
  public BusinessResponse<?> wipeCache(@RequestParam(value = "type", required = false) String type) {
    try {
      maintenance.wipe(type == null ? null : type.trim());
      return BusinessResponse.success("", null);
    } catch (RuntimeException exception) {
      return BusinessResponse.error(
          exception.getMessage() == null ? "清理缓存失败" : exception.getMessage());
    }
  }

  @PostMapping("/admin/ajax/upload")
  public BusinessResponse<?> adminUpload(
      @RequestParam(value = "file", required = false) MultipartFile file,
      @RequestHeader(value = "token", required = false) String headerToken,
      @RequestParam(value = "token", required = false) String formToken,
      @RequestParam(value = "category", required = false, defaultValue = "") String category,
      @RequestParam(value = "chunkid", required = false) String chunkId,
      HttpServletRequest request) {
    String token = firstNonBlank(headerToken, formToken, cookie(request, "admin_token"));
    return common.upload(file, token, null, category, chunkId, request);
  }

  @PostMapping("/index/ajax/upload")
  public BusinessResponse<?> indexUpload(
      @RequestParam(value = "file", required = false) MultipartFile file,
      @RequestHeader(value = "token", required = false) String headerToken,
      @RequestParam(value = "token", required = false) String formToken,
      @RequestParam(value = "category", required = false, defaultValue = "") String category,
      @RequestParam(value = "chunkid", required = false) String chunkId,
      HttpServletRequest request) {
    return common.upload(file, headerToken, formToken, category, chunkId, request);
  }

  private static String cookie(HttpServletRequest request, String name) {
    if (request.getCookies() == null) return null;
    for (Cookie cookie : request.getCookies())
      if (name.equals(cookie.getName())) return cookie.getValue();
    return null;
  }

  private static String firstNonBlank(String... values) {
    for (String value : values)
      if (value != null && !value.trim().isEmpty()) return value;
    return null;
  }
}
