package com.feimao;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.feimao.config.AdminPermissionInterceptor;
import com.feimao.controller.admin.AddressAdminController;
import com.feimao.controller.common.CaptchaController;
import com.feimao.mapper.AdminLogMapper;
import com.feimao.mapper.BusinessAddressMapper;
import com.feimao.service.AdminPermissionService;
import com.feimao.service.AdminSessionService;
import com.feimao.service.BusinessAddressService;
import com.feimao.service.BusinessUserService;
import com.feimao.service.ImageCaptchaService;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class LegacyAreaCaptchaContractTest {
  private static final String TOKEN = "legacy-area-token";
  private BusinessAddressMapper addresses;
  private AdminSessionService sessions;
  private MockMvc areaMvc;

  @BeforeEach
  void setUpAreaController() {
    addresses = mock(BusinessAddressMapper.class);
    sessions = mock(AdminSessionService.class);
    AdminPermissionService permissions = mock(AdminPermissionService.class);
    when(sessions.adminId(nullable(String.class))).thenReturn(null);
    when(sessions.adminId(TOKEN)).thenReturn(7L);
    when(permissions.allowed(7L, "ajax/area")).thenReturn(true);
    Map<String, Object> legacyRow = new LinkedHashMap<>();
    legacyRow.put("value", 11L);
    legacyRow.put("name", "测试地区");
    when(addresses.selectLegacyAreas(anyString(), anyInt()))
        .thenReturn(Collections.singletonList(legacyRow));

    AddressAdminController controller =
        new AddressAdminController(
            addresses,
            mock(BusinessAddressService.class),
            mock(BusinessUserService.class),
            mock(AdminLogMapper.class),
            sessions);
    areaMvc =
        MockMvcBuilders.standaloneSetup(controller)
            .addInterceptors(
                new AdminPermissionInterceptor(sessions, permissions, new ObjectMapper()))
            .build();
  }

  @Test
  void legacyAreaUsesFastAdminDefaultsAndNestedRowPrecedence() throws Exception {
    areaMvc
        .perform(get("/api/admin/ajax/area").header("token", TOKEN))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(1))
        .andExpect(jsonPath("$.msg").value(""))
        .andExpect(jsonPath("$.data[0].value").value(11))
        .andExpect(jsonPath("$.data[0].name").value("测试地区"))
        .andExpect(jsonPath("$.data[0].id").doesNotExist())
        .andExpect(jsonPath("$.data[0].pid").doesNotExist());

    areaMvc.perform(
        get("/api/admin/ajax/area").header("token", TOKEN).param("province", "11"));
    areaMvc.perform(
        get("/api/admin/ajax/area")
            .header("token", TOKEN)
            .param("province", "11")
            .param("city", "1101"));
    areaMvc.perform(
        get("/api/admin/ajax/area")
            .header("token", TOKEN)
            .param("province", "99")
            .param("city", "9901")
            .param("row[province]", "22")
            .param("row[city]", "2201"));
    areaMvc.perform(
        get("/api/admin/ajax/area").header("token", TOKEN).param("city", "1101"));

    InOrder order = inOrder(addresses);
    order.verify(addresses).selectLegacyAreas("0", 1);
    order.verify(addresses).selectLegacyAreas("11", 2);
    order.verify(addresses).selectLegacyAreas("1101", 3);
    order.verify(addresses).selectLegacyAreas("2201", 3);
    order.verify(addresses).selectLegacyAreas("0", 1);
  }

  @Test
  void currentAreaEndpointKeepsItsExtendedProjectionAndPermission() throws Exception {
    Map<String, Object> currentRow = new LinkedHashMap<>();
    currentRow.put("value", 11L);
    currentRow.put("id", 11L);
    currentRow.put("pid", 0L);
    currentRow.put("name", "测试省");
    currentRow.put("level", 1);
    when(addresses.selectAreas(0, 1, ""))
        .thenReturn(Collections.singletonList(currentRow));

    areaMvc
        .perform(get("/api/admin/business/addresses/areas").header("token", TOKEN))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(1))
        .andExpect(jsonPath("$.data[0].id").value(11))
        .andExpect(jsonPath("$.data[0].pid").value(0))
        .andExpect(jsonPath("$.data[0].level").value(1));

    verify(addresses).selectAreas(0, 1, "");
  }

  @Test
  void legacyAreaStillRequiresAnAdministratorSession() throws Exception {
    assertNull(sessions.adminId("expired-token"));
    areaMvc
        .perform(get("/api/admin/ajax/area").header("token", "expired-token"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value(0))
        .andExpect(jsonPath("$.msg").value("管理员未登录"));
  }

  @Test
  void captchaSupportsEmptyAndExplicitIdentifiers() throws Exception {
    ImageCaptchaService captchas = mock(ImageCaptchaService.class);
    byte[] image = new byte[] {(byte) 137, 80, 78, 71};
    when(captchas.create("default")).thenReturn(image);
    when(captchas.create("login-id")).thenReturn(image);
    MockMvc captchaMvc =
        MockMvcBuilders.standaloneSetup(new CaptchaController(captchas)).build();

    captchaMvc
        .perform(get("/api/common/captcha"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.IMAGE_PNG))
        .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
        .andExpect(header().string(HttpHeaders.PRAGMA, "no-cache"))
        .andExpect(content().bytes(image));
    captchaMvc
        .perform(get("/api/common/captcha/login-id"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.IMAGE_PNG))
        .andExpect(content().bytes(image));

    verify(captchas).create("default");
    verify(captchas).create("login-id");
  }
}
