package com.feimao.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.feimao.common.BusinessResponse;
import com.feimao.service.AdminPermissionService;
import com.feimao.service.AdminSessionService;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminPermissionInterceptor implements HandlerInterceptor {
  private final AdminSessionService sessions;
  private final AdminPermissionService permissions;
  private final ObjectMapper json;

  public AdminPermissionInterceptor(
      AdminSessionService sessions, AdminPermissionService permissions, ObjectMapper json) {
    this.sessions = sessions;
    this.permissions = permissions;
    this.json = json;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws IOException {
    String path = request.getRequestURI();
    if (path.equals("/api/admin/business/login")
        || path.equals("/api/admin/business/logout")) return true;
    Long adminId = sessions.adminId(request.getHeader("token"));
    if (adminId == null) return reject(response, "管理员未登录");
    // The delivery-management API predates the migrated FastAdmin RBAC data. Keep its existing
    // admin-level authorization model, but require a valid administrator session for every route.
    if (!path.startsWith("/api/admin/business/")) return true;
    String suffix = path.substring("/api/admin/business".length());
    if (suffix.equals("/system/config/check")
        || suffix.equals("/system/config/rulelist")
        || suffix.equals("/system/config/selectpage")
        || suffix.equals("/system/config/get_fields_list")) return true;
    if (suffix.equals("/partners/user-options")) {
      String role = "shop".equals(request.getParameter("role")) ? "shop/index" : "merchant/index";
      return permissions.allowed(adminId, role) || reject(response, "无权限访问");
    }
    String rule = rule(path, request.getMethod());
    if (rule == null || !permissions.allowed(adminId, rule)) return reject(response, "无权限访问");
    return true;
  }

  private boolean reject(HttpServletResponse response, String message) throws IOException {
    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    response.setCharacterEncoding("UTF-8");
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    json.writeValue(response.getWriter(), BusinessResponse.error(message));
    return false;
  }

  private String rule(String path, String method) {
    String suffix = path.substring("/api/admin/business".length());
    if (suffix.equals("/dashboard")) return "dashboard/index";
    if (suffix.startsWith("/rbac/admins")) return action("auth/admin", method);
    if (suffix.startsWith("/rbac/groups")) return action("auth/group", method);
    if (suffix.startsWith("/rbac/rules")) return action("auth/rule", method);
    if (suffix.startsWith("/users")) return action("user/user", method);
    if (suffix.startsWith("/user-access/groups")) return action("user/group", method);
    if (suffix.startsWith("/user-access/rules")) return action("user/rule", method);
    if (suffix.equals("/addresses/areas")) return "ajax/area";
    if (suffix.startsWith("/addresses")) return action("address", method);
    if (suffix.startsWith("/partners/merchant")) {
      if (suffix.contains("/restore/")) return "merchant/restore";
      if (suffix.contains("/destroy/")) return "merchant/destroy";
      if (suffix.endsWith("/recycle")) return "merchant/recyclebin";
      return action("merchant", method);
    }
    if (suffix.startsWith("/partners/shop")) {
      if (suffix.contains("/restore/")) return "shop/restore";
      if (suffix.contains("/destroy/")) return "shop/destroy";
      if (suffix.endsWith("/recycle")) return "shop/recyclebin";
      return action("shop", method);
    }
    if (suffix.equals("/system/config/get_table_list"))
      return "general/config/get_table_list";
    if (suffix.equals("/system/config/emailtest")
        || suffix.equals("/system/config/email-test")) return "general/config/emailtest";
    if (suffix.startsWith("/system/config"))
      return "GET".equals(method) ? "general/config/index" : "general/config/edit";
    if (suffix.equals("/system/attachments/select")) return "general/attachment/select";
    if (suffix.equals("/system/attachments/classify")) return "general/attachment/edit";
    if ("PUT".equals(method) && suffix.matches("/system/attachments/\\d+"))
      return "general/attachment/edit";
    if (suffix.startsWith("/system/attachments"))
      return "GET".equals(method) ? "general/attachment/index" : "general/attachment/del";
    if (suffix.startsWith("/system/profile"))
      return "GET".equals(method) ? "general/profile/index" : "general/profile/update";
    if (suffix.matches("/resources/brand/\\d+/sort")) return "brand/sort";
    if (suffix.matches("/resources/[^/]+/order")) return "ajax/weigh";
    if (suffix.startsWith("/resources/")) {
      String resource = suffix.substring("/resources/".length()).split("/")[0];
      Map<String, String> names = new HashMap<>();
      names.put("platform-car", "platform_car");
      names.put("malfunction", "malfunction");
      names.put("banner", "banner");
      names.put("brand", "brand");
      names.put("maintain", "maintain");
      names.put("coupon", "coupon");
      names.put("category", "category");
      if ("brand".equals(resource) || "platform-car".equals(resource)) {
        String legacy = "brand".equals(resource) ? "brand" : "platform_car";
        String operation =
            suffix.substring(("/resources/" + resource).length());
        if ("/recycle".equals(operation)) return legacy + "/recyclebin";
        if ("/restore".equals(operation) || operation.startsWith("/restore/"))
          return legacy + "/restore";
        if ("/destroy".equals(operation) || operation.startsWith("/destroy/"))
          return legacy + "/destroy";
      }
      if ("maintain".equals(resource)) {
        if (suffix.contains("/restore/")) return "maintain/restore";
        if (suffix.contains("/destroy/")) return "maintain/destroy";
        if (suffix.endsWith("/recycle")) return "maintain/recyclebin";
      }
      return names.containsKey(resource) ? action(names.get(resource), method) : null;
    }
    if (suffix.startsWith("/merchant-services/cars")) {
      String operation = suffix.substring("/merchant-services/cars".length());
      if ("/recycle".equals(operation)) return "merchantcar/recyclebin";
      if ("/restore".equals(operation) || operation.startsWith("/restore/"))
        return "merchantcar/restore";
      if ("/destroy".equals(operation) || operation.startsWith("/destroy/"))
        return "merchantcar/destroy";
      return action("merchantcar", method);
    }
    if (suffix.startsWith("/merchant-services/platform-cars"))
      return action("merchantcar", method);
    if (suffix.startsWith("/merchant-services/maintain-options"))
      return "merchantmaintain/maintainList";
    if (suffix.startsWith("/merchant-services/maintains")) {
      String operation = suffix.substring("/merchant-services/maintains".length());
      if ("/recycle".equals(operation)) return "merchantmaintain/recyclebin";
      if ("/restore".equals(operation) || operation.startsWith("/restore/"))
        return "merchantmaintain/restore";
      if ("/destroy".equals(operation) || operation.startsWith("/destroy/"))
        return "merchantmaintain/destroy";
      return action("merchantmaintain", method);
    }
    if (suffix.matches("/orders/\\d+/confirm-return")) return "order/confirmCar";
    if (suffix.matches("/orders/\\d+/cancel")) return "order/cancel";
    if (suffix.matches("/orders/\\d+"))
      return "DELETE".equals(method) ? "order/del" : "order/detail";
    if (suffix.equals("/orders")) return "DELETE".equals(method) ? "order/del" : "order/index";
    if (suffix.startsWith("/order-guests")) {
      if (suffix.endsWith("/policy")) return "order_guest/insure";
      return action("order_guest", method);
    }
    if (suffix.startsWith("/installments")) return action("orderchild", method);
    if (suffix.startsWith("/finance/merchant")) {
      if (suffix.contains("/restore/")) return "merchant_money_log/restore";
      if (suffix.contains("/destroy/")) return "merchant_money_log/destroy";
      if (suffix.endsWith("/recycle")) return "merchant_money_log/recyclebin";
      return action("merchant_money_log", method);
    }
    if (suffix.startsWith("/finance/shop")) {
      if (suffix.contains("/restore/")) return "shop_money_log/restore";
      if (suffix.contains("/destroy/")) return "shop_money_log/destroy";
      if (suffix.endsWith("/recycle")) return "shop_money_log/recyclebin";
      return action("shop_money_log", method);
    }
    if (suffix.startsWith("/finance/system"))
      return financeRule(suffix, "/finance/system", "system_money_log", method);
    if (suffix.startsWith("/finance/platform"))
      return financeRule(suffix, "/finance/platform", "platform_log", method);
    if (suffix.matches("/operation-logs/\\d+")) return "auth/adminlog/detail";
    if (suffix.startsWith("/operation-logs"))
      return "DELETE".equals(method) ? "auth/adminlog/del" : "auth/adminlog/index";
    if (suffix.startsWith("/user-coupons")) {
      if (suffix.contains("/restore/")) return "user_coupon/restore";
      if (suffix.contains("/destroy/")) return "user_coupon/destroy";
      if (suffix.endsWith("/recycle")) return "user_coupon/recyclebin";
      return action("user_coupon", method);
    }
    if (suffix.startsWith("/sign-logs")) return "sign_log/index";
    if (suffix.startsWith("/applications/merchant"))
      return suffix.endsWith("/examine") ? "merchant/examine" : "merchant/index";
    if (suffix.startsWith("/applications/shop"))
      return suffix.endsWith("/examine") ? "shop/examine" : "shop/index";
    if (suffix.startsWith("/cash/merchant"))
      return suffix.endsWith("/reject") || suffix.endsWith("/approve")
          ? "merchant_cash/examine"
          : action("merchant_cash", method);
    if (suffix.startsWith("/cash/shop"))
      return suffix.endsWith("/reject") || suffix.endsWith("/approve")
          ? "shop_cash/examine"
          : action("shop_cash", method);
    if (suffix.startsWith("/user-auth/")) return "user/user/edit";
    return null;
  }

  private static String action(String prefix, String method) {
    if ("GET".equals(method)) return prefix + "/index";
    if ("POST".equals(method)) return prefix + "/add";
    if ("PUT".equals(method)) return prefix + "/edit";
    if ("DELETE".equals(method)) return prefix + "/del";
    return null;
  }

  private static String financeRule(
      String suffix, String routePrefix, String legacyPrefix, String method) {
    String operation = suffix.substring(routePrefix.length());
    if ("/recycle".equals(operation)) return legacyPrefix + "/recyclebin";
    if ("/restore".equals(operation) || operation.startsWith("/restore/"))
      return legacyPrefix + "/restore";
    if ("/destroy".equals(operation) || operation.startsWith("/destroy/"))
      return legacyPrefix + "/destroy";
    return action(legacyPrefix, method);
  }
}
