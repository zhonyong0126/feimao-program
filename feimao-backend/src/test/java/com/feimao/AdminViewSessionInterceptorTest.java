package com.feimao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.feimao.config.AdminViewSessionInterceptor;
import com.feimao.service.AdminSessionService;
import javax.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@ExtendWith(MockitoExtension.class)
class AdminViewSessionInterceptorTest {
  @Mock AdminSessionService sessions;
  private AdminViewSessionInterceptor interceptor;

  @BeforeEach
  void setUp() {
    interceptor = new AdminViewSessionInterceptor(sessions);
  }

  @Test
  void loginPageIsPublic() throws Exception {
    assertTrue(check("/admin/login", null).allowed);
  }

  @Test
  void anonymousAdminPageRedirectsToLogin() throws Exception {
    when(sessions.adminId(null)).thenReturn(null);
    Check result = check("/admin/order/list", null);
    assertFalse(result.allowed);
    assertEquals("/admin/login", result.response.getRedirectedUrl());
  }

  @Test
  void validAdminCookieAllowsServerRenderedPage() throws Exception {
    when(sessions.adminId("admin-token")).thenReturn(7L);
    assertTrue(check("/admin/order/list", "admin-token").allowed);
  }

  private Check check(String path, String token) throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
    request.setRequestURI(path);
    if (token != null) request.setCookies(new Cookie("admin_token", token));
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
