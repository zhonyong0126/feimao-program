package com.feimao;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.feimao.config.LegacyTokenRequestFilter;
import java.nio.charset.StandardCharsets;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.*;

class LegacyTokenRequestFilterTest {
  private final LegacyTokenRequestFilter filter =
      new LegacyTokenRequestFilter(new ObjectMapper());

  @Test
  void promotesQueryTokenToHeader() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/user/info");
    request.setRequestURI("/api/user/info");
    request.addParameter("token", "query-token");
    Capture capture = run(request);
    assertEquals("query-token", capture.request.getHeader("token"));
  }

  @Test
  void promotesJsonTokenAndReplaysBody() throws Exception {
    String body = "{\"token\":\"json-token\",\"type\":1}";
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/user/info");
    request.setRequestURI("/api/user/info");
    request.setContentType("application/json;charset=UTF-8");
    request.setContent(body.getBytes(StandardCharsets.UTF_8));
    Capture capture = run(request);
    assertEquals("json-token", capture.request.getHeader("token"));
    assertEquals(
        body,
        org.springframework.util.StreamUtils.copyToString(
            capture.request.getInputStream(), StandardCharsets.UTF_8));
  }

  @Test
  void explicitHeaderWinsOverFallbacks() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/user/info");
    request.setRequestURI("/api/user/info");
    request.addHeader("token", "header-token");
    request.addParameter("token", "query-token");
    Capture capture = run(request);
    assertEquals("header-token", capture.request.getHeader("token"));
  }

  private Capture run(MockHttpServletRequest request) throws Exception {
    Capture capture = new Capture();
    FilterChain chain =
        (next, response) -> capture.request = (HttpServletRequest) next;
    filter.doFilter(request, new MockHttpServletResponse(), chain);
    return capture;
  }

  private static final class Capture {
    HttpServletRequest request;
  }
}
