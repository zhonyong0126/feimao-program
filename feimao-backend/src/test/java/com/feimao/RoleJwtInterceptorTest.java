package com.feimao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.feimao.config.RoleJwtInterceptor;
import com.feimao.utils.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class RoleJwtInterceptorTest {
  @Mock JwtUtils jwt;
  @Mock JdbcTemplate jdbc;
  @Mock StringRedisTemplate redis;
  @Mock ValueOperations<String, String> values;
  private RoleJwtInterceptor interceptor;

  @BeforeEach
  void setUp() {
    interceptor = new RoleJwtInterceptor(jwt, new ObjectMapper(), jdbc, redis);
  }

  @Test
  void loginRoutesRemainPublic() throws Exception {
    assertTrue(check("/api/driver/login/wx", null).allowed);
    assertTrue(check("/api/staff/login", null).allowed);
  }

  @Test
  void missingTokenIsRejectedAsUnauthorized() throws Exception {
    Check check = check("/api/driver/orders/my", null);
    assertFalse(check.allowed);
    assertEquals(401, check.response.getStatus());
  }

  @Test
  void driverTokenCanAccessDriverApi() throws Exception {
    when(jwt.validateToken("driver-token")).thenReturn(true);
    when(jwt.getUserId("driver-token")).thenReturn(9L);
    when(jwt.getUserType("driver-token")).thenReturn("driver");
    when(jdbc.queryForObject(anyString(), eq(Integer.class), eq(9L))).thenReturn(1);
    when(redis.opsForValue()).thenReturn(values);
    when(values.get("driver:token:9")).thenReturn("driver-token");
    assertTrue(check("/api/driver/orders/my", "Bearer driver-token").allowed);
  }

  @Test
  void driverTokenCannotAccessStaffApi() throws Exception {
    when(jwt.validateToken("driver-token")).thenReturn(true);
    when(jwt.getUserId("driver-token")).thenReturn(9L);
    when(jwt.getUserType("driver-token")).thenReturn("driver");
    Check check = check("/api/staff/orders", "Bearer driver-token");
    assertFalse(check.allowed);
    assertEquals(401, check.response.getStatus());
  }

  @Test
  void disabledDriverTokenIsRejectedImmediately() throws Exception {
    when(jwt.validateToken("driver-token")).thenReturn(true);
    when(jwt.getUserId("driver-token")).thenReturn(9L);
    when(jwt.getUserType("driver-token")).thenReturn("driver");
    when(jdbc.queryForObject(anyString(), eq(Integer.class), eq(9L))).thenReturn(0);
    Check check = check("/api/driver/orders/my", "Bearer driver-token");
    assertFalse(check.allowed);
    assertEquals(401, check.response.getStatus());
  }

  @Test
  void replacedOrLoggedOutTokenIsRejected() throws Exception {
    when(jwt.validateToken("driver-token")).thenReturn(true);
    when(jwt.getUserId("driver-token")).thenReturn(9L);
    when(jwt.getUserType("driver-token")).thenReturn("driver");
    when(jdbc.queryForObject(anyString(), eq(Integer.class), eq(9L))).thenReturn(1);
    when(redis.opsForValue()).thenReturn(values);
    when(values.get("driver:token:9")).thenReturn("new-token");
    Check check = check("/api/driver/orders/my", "Bearer driver-token");
    assertFalse(check.allowed);
    assertEquals(401, check.response.getStatus());
  }

  private Check check(String path, String authorization) throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
    request.setRequestURI(path);
    if (authorization != null) request.addHeader("Authorization", authorization);
    MockHttpServletResponse response = new MockHttpServletResponse();
    return new Check(interceptor.preHandle(request, response, new Object()), response);
  }

  private static class Check {
    final boolean allowed;
    final MockHttpServletResponse response;

    Check(boolean allowed, MockHttpServletResponse response) {
      this.allowed = allowed;
      this.response = response;
    }
  }
}
