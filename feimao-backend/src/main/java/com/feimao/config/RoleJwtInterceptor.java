package com.feimao.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.feimao.common.Result;
import com.feimao.utils.JwtUtils;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RoleJwtInterceptor implements HandlerInterceptor {
  private final JwtUtils jwt;
  private final ObjectMapper json;
  private final JdbcTemplate jdbc;
  private final StringRedisTemplate redis;

  public RoleJwtInterceptor(
      JwtUtils jwt, ObjectMapper json, JdbcTemplate jdbc, StringRedisTemplate redis) {
    this.jwt = jwt;
    this.json = json;
    this.jdbc = jdbc;
    this.redis = redis;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws IOException {
    String path = request.getRequestURI();
    if (path.equals("/api/driver/login/wx") || path.equals("/api/staff/login")) return true;
    String expected = path.startsWith("/api/driver/") ? "driver" : "staff";
    String authorization = request.getHeader("Authorization");
    if (authorization == null || !authorization.startsWith("Bearer ")) {
      return reject(response, "请先登录");
    }
    String token = authorization.substring(7).trim();
    if (token.isEmpty() || !jwt.validateToken(token)) {
      return reject(response, "登录状态无效");
    }
    Long userId = jwt.getUserId(token);
    if (userId == null || !expected.equals(jwt.getUserType(token))) {
      return reject(response, "登录状态无效");
    }
    String table = expected.equals("driver") ? "fm_driver" : "fm_staff";
    Integer enabled =
        jdbc.queryForObject(
            "select count(*) from " + table + " where id=? and deleted=0 and status=1",
            Integer.class,
            userId);
    if (enabled == null || enabled == 0) {
      return reject(response, "账号已被禁用");
    }
    String activeToken = redis.opsForValue().get(expected + ":token:" + userId);
    if (!token.equals(activeToken)) {
      return reject(response, "登录状态已失效，请重新登录");
    }
    return true;
  }

  private boolean reject(HttpServletResponse response, String message) throws IOException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setCharacterEncoding("UTF-8");
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    json.writeValue(response.getWriter(), Result.error(401, message));
    return false;
  }
}
