package com.feimao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.feimao.config.AdminPermissionInterceptor;
import com.feimao.service.AdminPermissionService;
import com.feimao.service.AdminSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@ExtendWith(MockitoExtension.class)
class AdminPermissionInterceptorTest {
  @Mock AdminSessionService sessions;
  @Mock AdminPermissionService permissions;

  private AdminPermissionInterceptor interceptor;

  @BeforeEach
  void setUp() {
    interceptor = new AdminPermissionInterceptor(sessions, permissions, new ObjectMapper());
  }

  @Test
  void unauthenticatedAdminRequestIsRejected() throws Exception {
    MockHttpServletRequest request = request("GET", "/api/admin/business/dashboard");
    MockHttpServletResponse response = new MockHttpServletResponse();
    when(sessions.adminId(null)).thenReturn(null);

    assertFalse(interceptor.preHandle(request, response, new Object()));
    assertEquals(403, response.getStatus());
    assertTrue(response.getContentType().startsWith("application/json"));
  }

  @Test
  void unauthenticatedDeliveryAdminRequestIsRejected() throws Exception {
    MockHttpServletRequest request = request("GET", "/api/admin/orders");
    MockHttpServletResponse response = new MockHttpServletResponse();
    when(sessions.adminId(null)).thenReturn(null);

    assertFalse(interceptor.preHandle(request, response, new Object()));
    assertEquals(403, response.getStatus());
  }

  @Test
  void authenticatedDeliveryAdminKeepsExistingAdminLevelAuthorization() throws Exception {
    MockHttpServletRequest request = request("GET", "/api/admin/orders");
    request.addHeader("token", "admin-token");
    MockHttpServletResponse response = new MockHttpServletResponse();
    when(sessions.adminId("admin-token")).thenReturn(7L);

    assertTrue(interceptor.preHandle(request, response, new Object()));
    verifyNoInteractions(permissions);
  }

  @Test
  void oldOrderPermissionRuleControlsNewRestRoute() throws Exception {
    MockHttpServletRequest request = request("POST", "/api/admin/business/orders/100/confirm-return");
    request.addHeader("token", "admin-token");
    MockHttpServletResponse response = new MockHttpServletResponse();
    when(sessions.adminId("admin-token")).thenReturn(7L);
    when(permissions.allowed(7L, "order/confirmCar")).thenReturn(true);

    assertTrue(interceptor.preHandle(request, response, new Object()));
    verify(permissions).allowed(7L, "order/confirmCar");
  }

  @Test
  void orderDeleteUsesHistoricalDeletePermission() throws Exception {
    MockHttpServletRequest request = request("DELETE", "/api/admin/business/orders/100");
    request.addHeader("token", "admin-token");
    MockHttpServletResponse response = new MockHttpServletResponse();
    when(sessions.adminId("admin-token")).thenReturn(7L);
    when(permissions.allowed(7L, "order/del")).thenReturn(true);

    assertTrue(interceptor.preHandle(request, response, new Object()));
    verify(permissions).allowed(7L, "order/del");
  }

  @Test
  void unmappedAdminRouteIsDeniedByDefault() throws Exception {
    MockHttpServletRequest request = request("GET", "/api/admin/business/not-migrated");
    request.addHeader("token", "admin-token");
    MockHttpServletResponse response = new MockHttpServletResponse();
    when(sessions.adminId("admin-token")).thenReturn(7L);

    assertFalse(interceptor.preHandle(request, response, new Object()));
    assertEquals(403, response.getStatus());
  }

  @Test
  void brandSortUsesHistoricalDedicatedPermission() throws Exception {
    MockHttpServletRequest request = request("POST", "/api/admin/business/resources/brand/10/sort");
    request.addHeader("token", "admin-token");
    MockHttpServletResponse response = new MockHttpServletResponse();
    when(sessions.adminId("admin-token")).thenReturn(7L);
    when(permissions.allowed(7L, "brand/sort")).thenReturn(true);

    assertTrue(interceptor.preHandle(request, response, new Object()));
    verify(permissions).allowed(7L, "brand/sort");
  }

  @Test
  void resourceDragOrderUsesHistoricalAjaxPermission() throws Exception {
    MockHttpServletRequest request = request("PUT", "/api/admin/business/resources/brand/order");
    request.addHeader("token", "admin-token");
    MockHttpServletResponse response = new MockHttpServletResponse();
    when(sessions.adminId("admin-token")).thenReturn(7L);
    when(permissions.allowed(7L, "ajax/weigh")).thenReturn(true);

    assertTrue(interceptor.preHandle(request, response, new Object()));
    verify(permissions).allowed(7L, "ajax/weigh");
  }

  @Test
  void attachmentClassifyUsesHistoricalEditPermission() throws Exception {
    MockHttpServletRequest request = request("PUT", "/api/admin/business/system/attachments/classify");
    request.addHeader("token", "admin-token");
    MockHttpServletResponse response = new MockHttpServletResponse();
    when(sessions.adminId("admin-token")).thenReturn(7L);
    when(permissions.allowed(7L, "general/attachment/edit")).thenReturn(true);

    assertTrue(interceptor.preHandle(request, response, new Object()));
    verify(permissions).allowed(7L, "general/attachment/edit");
  }

  @Test
  void attachmentEditUsesHistoricalEditPermission() throws Exception {
    MockHttpServletRequest request =
        request("PUT", "/api/admin/business/system/attachments/42");
    request.addHeader("token", "admin-token");
    MockHttpServletResponse response = new MockHttpServletResponse();
    when(sessions.adminId("admin-token")).thenReturn(7L);
    when(permissions.allowed(7L, "general/attachment/edit")).thenReturn(true);

    assertTrue(interceptor.preHandle(request, response, new Object()));
    verify(permissions).allowed(7L, "general/attachment/edit");
  }

  @Test
  void maintainOptionsUsesHistoricalDedicatedPermission() throws Exception {
    MockHttpServletRequest request =
        request("GET", "/api/admin/business/merchant-services/maintain-options");
    request.addHeader("token", "admin-token");
    MockHttpServletResponse response = new MockHttpServletResponse();
    when(sessions.adminId("admin-token")).thenReturn(7L);
    when(permissions.allowed(7L, "merchantmaintain/maintainList")).thenReturn(true);

    assertTrue(interceptor.preHandle(request, response, new Object()));
    verify(permissions).allowed(7L, "merchantmaintain/maintainList");
  }

  @Test
  void areaCascadeUsesHistoricalAjaxPermission() throws Exception {
    MockHttpServletRequest request = request("GET", "/api/admin/business/addresses/areas");
    request.addHeader("token", "admin-token");
    MockHttpServletResponse response = new MockHttpServletResponse();
    when(sessions.adminId("admin-token")).thenReturn(7L);
    when(permissions.allowed(7L, "ajax/area")).thenReturn(true);

    assertTrue(interceptor.preHandle(request, response, new Object()));
    verify(permissions).allowed(7L, "ajax/area");
  }

  @Test
  void historicalConfigHelpersRequireLoginButNoFineGrainedPermission() throws Exception {
    String[] methods = {"POST", "GET", "POST", "POST"};
    String[] paths = {
      "/api/admin/business/system/config/check",
      "/api/admin/business/system/config/rulelist",
      "/api/admin/business/system/config/selectpage",
      "/api/admin/business/system/config/get_fields_list"
    };
    when(sessions.adminId("admin-token")).thenReturn(7L);
    for (int index = 0; index < paths.length; index++) {
      MockHttpServletRequest request = request(methods[index], paths[index]);
      request.addHeader("token", "admin-token");
      assertTrue(
          interceptor.preHandle(request, new MockHttpServletResponse(), new Object()),
          paths[index]);
    }
    verifyNoInteractions(permissions);
  }

  @Test
  void historicalConfigTableAndMailHelpersStillRequireTheirOwnRules() throws Exception {
    String[] paths = {
      "/api/admin/business/system/config/get_table_list",
      "/api/admin/business/system/config/emailtest"
    };
    String[] rules = {"general/config/get_table_list", "general/config/emailtest"};
    when(sessions.adminId("admin-token")).thenReturn(7L);
    for (int index = 0; index < paths.length; index++) {
      MockHttpServletRequest request = request("POST", paths[index]);
      request.addHeader("token", "admin-token");
      when(permissions.allowed(7L, rules[index])).thenReturn(true);
      assertTrue(interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
      verify(permissions).allowed(7L, rules[index]);
    }
  }

  @Test
  void installmentWritesUseDedicatedHistoricalPermissions() throws Exception {
    String[] methods = {"GET", "POST", "PUT", "DELETE"};
    String[] rules = {"orderchild/index", "orderchild/add", "orderchild/edit", "orderchild/del"};
    for (int i = 0; i < methods.length; i++) {
      String uri =
          "/api/admin/business/installments"
              + (("PUT".equals(methods[i]) || "DELETE".equals(methods[i])) ? "/10" : "");
      MockHttpServletRequest request = request(methods[i], uri);
      request.addHeader("token", "admin-token");
      MockHttpServletResponse response = new MockHttpServletResponse();
      when(sessions.adminId("admin-token")).thenReturn(7L);
      when(permissions.allowed(7L, rules[i])).thenReturn(true);

      assertTrue(interceptor.preHandle(request, response, new Object()));
      verify(permissions).allowed(7L, rules[i]);
    }
  }

  @Test
  void vehicleCatalogRecycleRoutesUseHistoricalPermissions() throws Exception {
    String[] methods = {"GET", "POST", "DELETE", "GET", "POST", "DELETE"};
    String[] paths = {
      "/api/admin/business/resources/brand/recycle",
      "/api/admin/business/resources/brand/restore",
      "/api/admin/business/resources/brand/destroy/10",
      "/api/admin/business/resources/platform-car/recycle",
      "/api/admin/business/resources/platform-car/restore/10",
      "/api/admin/business/resources/platform-car/destroy"
    };
    String[] rules = {
      "brand/recyclebin",
      "brand/restore",
      "brand/destroy",
      "platform_car/recyclebin",
      "platform_car/restore",
      "platform_car/destroy"
    };
    when(sessions.adminId("admin-token")).thenReturn(7L);
    for (int index = 0; index < paths.length; index++) {
      MockHttpServletRequest request = request(methods[index], paths[index]);
      request.addHeader("token", "admin-token");
      when(permissions.allowed(7L, rules[index])).thenReturn(true);
      assertTrue(interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
      verify(permissions).allowed(7L, rules[index]);
    }
  }

  @Test
  void merchantServiceRecycleRoutesUseHistoricalPermissions() throws Exception {
    String[] methods = {"GET", "POST", "DELETE", "GET", "POST", "DELETE"};
    String[] paths = {
      "/api/admin/business/merchant-services/cars/recycle",
      "/api/admin/business/merchant-services/cars/restore/10",
      "/api/admin/business/merchant-services/cars/destroy",
      "/api/admin/business/merchant-services/maintains/recycle",
      "/api/admin/business/merchant-services/maintains/restore",
      "/api/admin/business/merchant-services/maintains/destroy/10"
    };
    String[] rules = {
      "merchantcar/recyclebin",
      "merchantcar/restore",
      "merchantcar/destroy",
      "merchantmaintain/recyclebin",
      "merchantmaintain/restore",
      "merchantmaintain/destroy"
    };
    when(sessions.adminId("admin-token")).thenReturn(7L);
    for (int index = 0; index < paths.length; index++) {
      MockHttpServletRequest request = request(methods[index], paths[index]);
      request.addHeader("token", "admin-token");
      when(permissions.allowed(7L, rules[index])).thenReturn(true);
      assertTrue(interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
      verify(permissions).allowed(7L, rules[index]);
    }
  }

  @Test
  void platformAndSystemFinanceRoutesUseTheirExactHistoricalActions() throws Exception {
    String[] methods = {
      "GET", "POST", "PUT", "DELETE", "GET", "POST", "DELETE",
      "GET", "POST", "PUT", "DELETE", "GET", "POST", "DELETE"
    };
    String[] paths = {
      "/api/admin/business/finance/system",
      "/api/admin/business/finance/system",
      "/api/admin/business/finance/system/10",
      "/api/admin/business/finance/system/10",
      "/api/admin/business/finance/system/recycle",
      "/api/admin/business/finance/system/restore/10",
      "/api/admin/business/finance/system/destroy/10",
      "/api/admin/business/finance/platform",
      "/api/admin/business/finance/platform",
      "/api/admin/business/finance/platform/10",
      "/api/admin/business/finance/platform/10",
      "/api/admin/business/finance/platform/recycle",
      "/api/admin/business/finance/platform/restore/10",
      "/api/admin/business/finance/platform/destroy/10"
    };
    String[] rules = {
      "system_money_log/index", "system_money_log/add", "system_money_log/edit",
      "system_money_log/del", "system_money_log/recyclebin", "system_money_log/restore",
      "system_money_log/destroy", "platform_log/index", "platform_log/add",
      "platform_log/edit", "platform_log/del", "platform_log/recyclebin",
      "platform_log/restore", "platform_log/destroy"
    };
    when(sessions.adminId("admin-token")).thenReturn(7L);
    for (int index = 0; index < paths.length; index++) {
      MockHttpServletRequest request = request(methods[index], paths[index]);
      request.addHeader("token", "admin-token");
      when(permissions.allowed(7L, rules[index])).thenReturn(true);
      assertTrue(interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
      verify(permissions).allowed(7L, rules[index]);
    }
  }

  private static MockHttpServletRequest request(String method, String uri) {
    MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
    request.setRequestURI(uri);
    return request;
  }
}
