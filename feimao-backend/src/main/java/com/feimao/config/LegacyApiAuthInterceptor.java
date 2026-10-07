package com.feimao.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.feimao.common.BusinessResponse;
import com.feimao.service.RoleSessionService;
import com.feimao.service.UserTokenService;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/** Restores the authentication boundary implemented by the original API base controller. */
@Component
public class LegacyApiAuthInterceptor implements HandlerInterceptor {
  private final UserTokenService userTokens;
  private final RoleSessionService roleSessions;
  private final JdbcTemplate jdbc;
  private final ObjectMapper json;

  public LegacyApiAuthInterceptor(
      UserTokenService userTokens,
      RoleSessionService roleSessions,
      JdbcTemplate jdbc,
      ObjectMapper json) {
    this.userTokens = userTokens;
    this.roleSessions = roleSessions;
    this.jdbc = jdbc;
    this.json = json;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws IOException {
    String role = protectedRole(normalize(request.getRequestURI()));
    if (role == null) return true;
    String token = request.getHeader("token");
    boolean authenticated;
    if ("user".equals(role)) {
      Long id = userTokens.userId(token);
      authenticated = id != null && id > 0;
    } else {
      Long id = roleSessions.subject(token, role);
      authenticated = id != null && activeRole(role, id);
    }
    return authenticated || reject(response);
  }

  static String protectedRole(String path) {
    if (path.startsWith("/api/user/"))
      return path.equals("/api/user/login") ? null : "user";
    if (path.startsWith("/api/token/")) return "user";
    if (path.startsWith("/api/store/"))
      return path.equals("/api/store/login") ? null : "store";
    if (path.startsWith("/api/shop/"))
      return path.equals("/api/shop/login") || path.equals("/api/shop/logout") ? null : "shop";
    if (path.startsWith("/api/sign/"))
      return path.equals("/api/sign/couponlist") || path.equals("/api/sign/redeemcoupon")
          ? null : "user";
    if (path.startsWith("/api/order/"))
      return path.equals("/api/order/insuretest")
              || path.equals("/api/order/insurelisttest")
              || path.equals("/api/order/insurecancel")
          ? null : "user";
    if (path.equals("/api/merchant/leasepreceptdetails")) return "user";
    return null;
  }

  private boolean activeRole(String role, long id) {
    String table = "shop".equals(role) ? "ymcx_shop" : "ymcx_merchant";
    Integer count =
        jdbc.queryForObject(
            "select count(*) from " + table
                + " where id=? and is_delete=1 and deletetime is null and status=1",
            Integer.class,
            id);
    return count != null && count > 0;
  }

  private boolean reject(HttpServletResponse response) throws IOException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    json.writeValue(
        response.getWriter(),
        new BusinessResponse<Object>(
            401, "请登录后操作", System.currentTimeMillis() / 1000, null));
    return false;
  }

  private static String normalize(String uri) {
    String value = uri == null ? "" : uri.toLowerCase(Locale.ROOT);
    while (value.length() > 1 && value.endsWith("/")) value = value.substring(0, value.length() - 1);
    return value;
  }
}
