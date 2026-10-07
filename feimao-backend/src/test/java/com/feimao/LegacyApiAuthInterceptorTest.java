package com.feimao;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.feimao.config.LegacyApiAuthInterceptor;
import com.feimao.service.RoleSessionService;
import com.feimao.service.UserTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@ExtendWith(MockitoExtension.class)
class LegacyApiAuthInterceptorTest {
  @Mock UserTokenService userTokens;
  @Mock RoleSessionService roleSessions;
  @Mock JdbcTemplate jdbc;
  private LegacyApiAuthInterceptor interceptor;

  @BeforeEach
  void setUp() {
    interceptor =
        new LegacyApiAuthInterceptor(userTokens, roleSessions, jdbc, new ObjectMapper());
  }

  @Test
  void originalPublicActionsStayPublic() throws Exception {
    assertTrue(check("/api/user/login", null).allowed);
    assertTrue(check("/api/store/login", null).allowed);
    assertTrue(check("/api/shop/logout", null).allowed);
    assertTrue(check("/api/sign/couponList", null).allowed);
    assertTrue(check("/api/sign/redeemCoupon", null).allowed);
    assertTrue(check("/api/order/insureTest", null).allowed);
    assertTrue(check("/api/merchant/details", null).allowed);
  }

  @Test
  void protectedUserActionUsesOriginal401Envelope() throws Exception {
    Check result = check("/api/user/info", null);
    assertFalse(result.allowed);
    assertEquals(401, result.response.getStatus());
    @SuppressWarnings("unchecked")
    java.util.Map<String, Object> body =
        new ObjectMapper().readValue(result.response.getContentAsByteArray(), java.util.Map.class);
    assertEquals(401, ((Number) body.get("code")).intValue());
    assertEquals("请登录后操作", body.get("msg"));
    assertNull(body.get("data"));
  }

  @Test
  void activeUserAndBusinessRoleTokensPass() throws Exception {
    when(userTokens.userId("user-token")).thenReturn(7L);
    assertTrue(check("/api/order/list", "user-token").allowed);

    when(roleSessions.subject("shop-token", "shop")).thenReturn(3L);
    when(jdbc.queryForObject(anyString(), eq(Integer.class), eq(3L))).thenReturn(1);
    assertTrue(check("/api/shop/index", "shop-token").allowed);
  }

  @Test
  void disabledBusinessRoleIsRejected() throws Exception {
    when(roleSessions.subject("store-token", "store")).thenReturn(8L);
    when(jdbc.queryForObject(anyString(), eq(Integer.class), eq(8L))).thenReturn(0);
    assertFalse(check("/api/store/logout", "store-token").allowed);
  }

  private Check check(String path, String token) throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("POST", path);
    request.setRequestURI(path);
    if (token != null) request.addHeader("token", token);
    MockHttpServletResponse response = new MockHttpServletResponse();
    return new Check(interceptor.preHandle(request, response, new Object()), response);
  }

  private static final class Check {
    final boolean allowed;
    final MockHttpServletResponse response;

    Check(boolean allowed, MockHttpServletResponse response) {
      this.allowed = allowed;
      this.response = response;
    }
  }
}
