package com.feimao;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.feimao.common.BusinessResponse;
import com.feimao.config.AdminViewSessionInterceptor;
import com.feimao.controller.common.CommonController;
import com.feimao.controller.common.LegacyAjaxAuxiliaryController;
import com.feimao.service.AdminSessionService;
import com.feimao.service.LegacyAjaxMaintenanceService;
import com.feimao.service.LegacyLanguageCatalog;
import java.util.Collections;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class LegacyAjaxAuxiliaryControllerTest {
  private LegacyAjaxMaintenanceService maintenance;
  private CommonController common;
  private MockMvc mvc;

  @BeforeEach
  void setUp() {
    maintenance = mock(LegacyAjaxMaintenanceService.class);
    common = mock(CommonController.class);
    ObjectMapper json = new ObjectMapper();
    mvc =
        MockMvcBuilders.standaloneSetup(
                new LegacyAjaxAuxiliaryController(
                    new LegacyLanguageCatalog(json), maintenance, common, json, true))
            .build();
  }

  @Test
  void langReturnsFixedDefineJsonpWithExactModuleAndControllerDictionary() throws Exception {
    mvc.perform(
            get("/admin/ajax/lang")
                .param("callback", "evil")
                .param("controllername", "Brand")
                .param("lang", "zh-cn"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.parseMediaType("application/javascript")))
        .andExpect(header().doesNotExist(HttpHeaders.CACHE_CONTROL))
        .andExpect(content().string(containsString("define({")))
        .andExpect(content().string(containsString("\"name\":\"品牌名称\"")))
        .andExpect(content().string(containsString("\"username\":\"账号\"")))
        .andExpect(content().string(org.hamcrest.Matchers.endsWith("});")));

    mvc.perform(get("/index/ajax/lang").param("controllername", "index"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("\"username\":\"用户名\"")));
    mvc.perform(get("/index/ajax/lang").param("lang", "en-us"))
        .andExpect(content().string("define({});"));
  }

  @Test
  void wipeCacheSupportsLegacyGetAndPostAndReturnsSuccess() throws Exception {
    mvc.perform(get("/admin/ajax/wipecache").param("type", " content "))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(1))
        .andExpect(jsonPath("$.msg").value(""));
    verify(maintenance).wipe("content");

    mvc.perform(post("/admin/ajax/wipecache").param("type", "unknown"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(1));
    verify(maintenance).wipe("unknown");
  }

  @Test
  void uploadAliasesForwardLegacyFieldsAndAdminCookieToken() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("file", "a.png", "image/png", new byte[] {1});
    doReturn(BusinessResponse.success("上传成功", Collections.singletonMap("url", "/u")))
        .when(common)
        .upload(any(), any(), any(), any(), any(), any());

    mvc.perform(
            multipart("/admin/ajax/upload")
                .file(file)
                .cookie(new Cookie("admin_token", "cookie-token"))
                .param("category", "image"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(1));
    verify(common)
        .upload(any(), eq("cookie-token"), isNull(), eq("image"), isNull(), any());

    mvc.perform(
            multipart("/index/ajax/upload")
                .file(file)
                .param("token", "form-token")
                .param("chunkid", "chunk"))
        .andExpect(status().isOk());
    verify(common)
        .upload(any(), isNull(), eq("form-token"), eq(""), eq("chunk"), any());
  }

  @Test
  void onlyLangIsPublicWhileAdminUploadAndWipeRequireLogin() throws Exception {
    AdminSessionService sessions = mock(AdminSessionService.class);
    when(sessions.adminId(null)).thenReturn(null);
    AdminViewSessionInterceptor interceptor = new AdminViewSessionInterceptor(sessions);
    assertTrue(check(interceptor, "/admin/ajax/lang"));
    assertFalse(check(interceptor, "/admin/ajax/upload"));
    assertFalse(check(interceptor, "/admin/ajax/wipecache"));
  }

  @Test
  @SuppressWarnings("unchecked")
  void maintenanceMatchesDebugGuardAndProductionBrowserVersionIncrement() {
    JdbcTemplate jdbc = mock(JdbcTemplate.class);
    ObjectProvider<CacheManager> caches = mock(ObjectProvider.class);
    new LegacyAjaxMaintenanceService(jdbc, caches, true).wipe("browser");
    verify(jdbc, never()).query(any(String.class), any(RowMapper.class));

    when(jdbc.query(any(String.class), any(RowMapper.class)))
        .thenReturn(Collections.singletonList("1.9"));
    new LegacyAjaxMaintenanceService(jdbc, caches, false).wipe("browser");
    verify(jdbc).update("update ymcx_config set value=? where name='version'", "1.10");
  }

  private static boolean check(AdminViewSessionInterceptor interceptor, String path)
      throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
    request.setRequestURI(path);
    MockHttpServletResponse response = new MockHttpServletResponse();
    boolean allowed = interceptor.preHandle(request, response, new Object());
    if (!allowed) assertEquals("/admin/login", response.getRedirectedUrl());
    return allowed;
  }
}
