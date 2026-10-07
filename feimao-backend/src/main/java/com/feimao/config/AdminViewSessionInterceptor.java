package com.feimao.config;

import com.feimao.service.AdminSessionService;
import java.io.IOException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminViewSessionInterceptor implements HandlerInterceptor {
  private final AdminSessionService sessions;

  public AdminViewSessionInterceptor(AdminSessionService sessions) {
    this.sessions = sessions;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws IOException {
    String path = request.getRequestURI();
    if (path.equals("/admin/login")
        || path.equals("/admin/ajax/lang")
        || path.equals("/admin/ajax/lang/")) return true;
    String token = cookie(request, "admin_token");
    if (sessions.adminId(token) != null) return true;
    response.sendRedirect("/admin/login");
    return false;
  }

  private static String cookie(HttpServletRequest request, String name) {
    if (request.getCookies() == null) return null;
    for (Cookie cookie : request.getCookies())
      if (name.equals(cookie.getName()))
        return cookie.getValue();
    return null;
  }
}
