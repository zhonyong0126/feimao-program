package com.feimao.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * The original API accepts its token from the token header, request parameters, or the token
 * cookie. Promote the legacy fallbacks to a header so existing controller signatures see the
 * same value. JSON request bodies are replayed after inspection.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class LegacyTokenRequestFilter extends OncePerRequestFilter {
  private final ObjectMapper json;

  public LegacyTokenRequestFilter(ObjectMapper json) {
    this.json = json;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return !request.getRequestURI().startsWith("/api/")
        || (request.getHeader("token") != null && !request.getHeader("token").trim().isEmpty());
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String token = trim(request.getParameter("token"));
    if (token.isEmpty() && request.getCookies() != null) {
      for (Cookie cookie : request.getCookies()) {
        if ("token".equals(cookie.getName())) {
          token = trim(cookie.getValue());
          break;
        }
      }
    }

    byte[] body = null;
    String contentType = request.getContentType();
    boolean jsonBody = contentType != null
        && contentType.toLowerCase(Locale.ROOT).startsWith(MediaType.APPLICATION_JSON_VALUE);
    if (token.isEmpty() && jsonBody) {
      body = readAll(request.getInputStream());
      if (body.length > 0) {
        try {
          Map<String, Object> values =
              json.readValue(body, new TypeReference<Map<String, Object>>() {});
          token = trim(values.get("token"));
        } catch (Exception ignored) {
          // Let Spring report malformed JSON after the untouched bytes are replayed.
        }
      }
    }

    HttpServletRequest next = request;
    if (!token.isEmpty() || body != null) next = new TokenRequest(request, token, body);
    chain.doFilter(next, response);
  }

  private static byte[] readAll(ServletInputStream input) throws IOException {
    java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
    byte[] buffer = new byte[4096];
    int count;
    while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
    return output.toByteArray();
  }

  private static String trim(Object value) {
    return value == null ? "" : value.toString().trim();
  }

  private static final class TokenRequest extends HttpServletRequestWrapper {
    private final String token;
    private final byte[] body;

    private TokenRequest(HttpServletRequest request, String token, byte[] body) {
      super(request);
      this.token = token;
      this.body = body;
    }

    @Override
    public String getHeader(String name) {
      return "token".equalsIgnoreCase(name) && !token.isEmpty() ? token : super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
      if ("token".equalsIgnoreCase(name) && !token.isEmpty())
        return Collections.enumeration(Collections.singletonList(token));
      return super.getHeaders(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
      LinkedHashSet<String> names = new LinkedHashSet<>();
      Enumeration<String> original = super.getHeaderNames();
      if (original != null) while (original.hasMoreElements()) names.add(original.nextElement());
      if (!token.isEmpty()) names.add("token");
      return Collections.enumeration(names);
    }

    @Override
    public int getContentLength() {
      return body == null ? super.getContentLength() : body.length;
    }

    @Override
    public long getContentLengthLong() {
      return body == null ? super.getContentLengthLong() : body.length;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
      if (body == null) return super.getInputStream();
      ByteArrayInputStream input = new ByteArrayInputStream(body);
      return new ServletInputStream() {
        @Override public boolean isFinished() { return input.available() == 0; }
        @Override public boolean isReady() { return true; }
        @Override public void setReadListener(ReadListener listener) { }
        @Override public int read() { return input.read(); }
      };
    }

    @Override
    public java.io.BufferedReader getReader() throws IOException {
      if (body == null) return super.getReader();
      return new java.io.BufferedReader(
          new java.io.InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
    }
  }
}
