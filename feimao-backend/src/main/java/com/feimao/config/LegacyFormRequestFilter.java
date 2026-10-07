package com.feimao.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import javax.servlet.FilterChain;
import javax.servlet.ReadListener;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Preserves the original ThinkPHP API contract: API clients may submit ordinary
 * application/x-www-form-urlencoded POST bodies as well as JSON bodies.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class LegacyFormRequestFilter extends OncePerRequestFilter {
  private final ObjectMapper objectMapper;

  public LegacyFormRequestFilter(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String contentType = request.getContentType();
    return !request.getRequestURI().startsWith("/api/")
        || !"POST".equalsIgnoreCase(request.getMethod())
        || contentType == null
        || !contentType.toLowerCase().startsWith(MediaType.APPLICATION_FORM_URLENCODED_VALUE);
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    Map<String, Object> body = new LinkedHashMap<>();
    request.getParameterMap().forEach((key, values) -> {
      if (values == null || values.length == 0) body.put(key, "");
      else if (values.length == 1) body.put(key, values[0]);
      else body.put(key, values);
    });
    byte[] json = objectMapper.writeValueAsBytes(body);
    chain.doFilter(new JsonBodyRequest(request, json), response);
  }

  private static final class JsonBodyRequest extends HttpServletRequestWrapper {
    private final byte[] body;

    private JsonBodyRequest(HttpServletRequest request, byte[] body) {
      super(request);
      this.body = body;
    }

    @Override
    public String getContentType() {
      return MediaType.APPLICATION_JSON_VALUE;
    }

    @Override
    public String getHeader(String name) {
      return "Content-Type".equalsIgnoreCase(name)
          ? MediaType.APPLICATION_JSON_VALUE
          : super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
      return "Content-Type".equalsIgnoreCase(name)
          ? Collections.enumeration(Collections.singletonList(MediaType.APPLICATION_JSON_VALUE))
          : super.getHeaders(name);
    }

    @Override
    public int getContentLength() {
      return body.length;
    }

    @Override
    public long getContentLengthLong() {
      return body.length;
    }

    @Override
    public ServletInputStream getInputStream() {
      ByteArrayInputStream input = new ByteArrayInputStream(body);
      return new ServletInputStream() {
        @Override public boolean isFinished() { return input.available() == 0; }
        @Override public boolean isReady() { return true; }
        @Override public void setReadListener(ReadListener listener) { }
        @Override public int read() { return input.read(); }
      };
    }

    @Override
    public java.io.BufferedReader getReader() {
      return new java.io.BufferedReader(
          new java.io.InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
    }
  }
}
