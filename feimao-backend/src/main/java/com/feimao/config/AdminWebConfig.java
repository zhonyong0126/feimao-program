package com.feimao.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AdminWebConfig implements WebMvcConfigurer {
  private final AdminPermissionInterceptor permissions;
  private final RoleJwtInterceptor roleJwt;
  private final AdminViewSessionInterceptor adminViews;
  private final LegacyApiAuthInterceptor legacyApiAuth;

  public AdminWebConfig(
      AdminPermissionInterceptor permissions,
      RoleJwtInterceptor roleJwt,
      AdminViewSessionInterceptor adminViews,
      LegacyApiAuthInterceptor legacyApiAuth) {
    this.permissions = permissions;
    this.roleJwt = roleJwt;
    this.adminViews = adminViews;
    this.legacyApiAuth = legacyApiAuth;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(permissions).addPathPatterns("/api/admin/**");
    registry
        .addInterceptor(roleJwt)
        .addPathPatterns("/api/driver/**", "/api/staff/**");
    registry
        .addInterceptor(legacyApiAuth)
        .addPathPatterns(
            "/api/user/**",
            "/api/token/**",
            "/api/store/**",
            "/api/shop/**",
            "/api/sign/**",
            "/api/order/**",
            "/api/merchant/leasePreceptDetails");
    registry.addInterceptor(adminViews).addPathPatterns("/admin", "/admin/**");
  }
}
