package com.feimao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.feimao.common.BusinessResponse;
import com.feimao.config.LegacyFormRequestFilter;
import com.feimao.controller.admin.SystemAdminController;
import com.feimao.mapper.AdminLogMapper;
import com.feimao.mapper.SystemAdminMapper;
import com.feimao.service.AdminSessionService;
import com.feimao.service.VerificationCodeService;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.DigestUtils;

class AdminProfileContractTest {
  private static final String TOKEN = "profile-token";
  private final AtomicBoolean tokenActive = new AtomicBoolean(true);
  private final ObjectMapper json = new ObjectMapper();
  private SystemAdminMapper system;
  private AdminSessionService sessions;
  private SystemAdminController controller;
  private MockMvc mvc;

  @BeforeEach
  void setUp() {
    system = mock(SystemAdminMapper.class);
    AdminLogMapper logs = mock(AdminLogMapper.class);
    sessions = mock(AdminSessionService.class);
    when(sessions.adminId(TOKEN)).thenAnswer(invocation -> tokenActive.get() ? 7L : null);
    org.mockito.Mockito.doAnswer(
            invocation -> {
              tokenActive.set(false);
              return null;
            })
        .when(sessions)
        .revoke(TOKEN);
    controller =
        new SystemAdminController(
            system,
            logs,
            sessions,
            mock(VerificationCodeService.class),
            mock(JdbcTemplate.class),
            json,
            "./uploads");
    mvc =
        MockMvcBuilders.standaloneSetup(controller)
            .addFilters(new LegacyFormRequestFilter(json))
            .build();
  }

  @Test
  void acceptsFlatNestedAndLegacyFormPayloadsWithTheLegacyWhitelist() throws Exception {
    Map<String, Object> flat = new LinkedHashMap<>();
    flat.put("email", "flat@example.com");
    flat.put("nickname", "扁平JSON");
    flat.put("mobile", "199" + "0000" + "0000");
    flat.put("username", "must-not-change");
    flat.put("avatar", "");
    flat.put("password", "");
    putJson(flat).andExpect(jsonPath("$.code").value(1));

    Map<String, Object> row = new LinkedHashMap<>();
    row.put("email", "row@example.com");
    row.put("nickname", "row JSON");
    Map<String, Object> rowEnvelope = new LinkedHashMap<>();
    rowEnvelope.put("row", row);
    putJson(rowEnvelope).andExpect(jsonPath("$.code").value(1));

    Map<String, Object> a = new LinkedHashMap<>();
    a.put("email", "a@example.com");
    a.put("nickname", "a JSON");
    Map<String, Object> aEnvelope = new LinkedHashMap<>();
    aEnvelope.put("a", a);
    putJson(aEnvelope).andExpect(jsonPath("$.code").value(1));

    mvc.perform(
            post("/api/admin/business/system/profile")
                .header("token", TOKEN)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("row[email]", "form-row@example.com")
                .param("row[nickname]", "row 表单")
                .param("row[mobile]", "188" + "0000" + "0000")
                .param("row[avatar]", ""))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(1));

    mvc.perform(
            post("/api/admin/business/system/profile")
                .header("token", TOKEN)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("a[email]", "form-a@example.com")
                .param("a[nickname]", "a 表单"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(1));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Map<String, Object>> values = ArgumentCaptor.forClass(Map.class);
    verify(system, times(5)).updateAdminProfile(eq(7L), values.capture(), anyLong());
    List<Map<String, Object>> updates = values.getAllValues();
    assertUpdate(updates.get(0), "flat@example.com", "扁平JSON");
    assertUpdate(updates.get(1), "row@example.com", "row JSON");
    assertUpdate(updates.get(2), "a@example.com", "a JSON");
    assertUpdate(updates.get(3), "form-row@example.com", "row 表单");
    assertUpdate(updates.get(4), "form-a@example.com", "a 表单");
    for (Map<String, Object> update : updates) {
      assertFalse(update.containsKey("mobile"));
      assertFalse(update.containsKey("username"));
    }
    assertFalse(updates.get(0).containsKey("avatar"));
    assertFalse(updates.get(0).containsKey("password"));
    verify(sessions, never()).revoke(TOKEN);
  }

  @Test
  void requiresEmailBeforeSavingAnyOtherField() throws Exception {
    Map<String, Object> missingEmail = new LinkedHashMap<>();
    missingEmail.put("nickname", "should-not-save");
    putJson(missingEmail)
        .andExpect(jsonPath("$.code").value(0))
        .andExpect(jsonPath("$.msg").value("请输入正确的Email地址"));

    Map<String, Object> emptyEmail = new LinkedHashMap<>();
    emptyEmail.put("email", "");
    emptyEmail.put("nickname", "should-not-save-either");
    putJson(emptyEmail)
        .andExpect(jsonPath("$.code").value(0))
        .andExpect(jsonPath("$.msg").value("请输入正确的Email地址"));

    verify(system, never()).updateAdminProfile(eq(7L), org.mockito.ArgumentMatchers.anyMap(), anyLong());
  }

  @Test
  void ignoresEmptyPasswordButRevokesTheTokenAfterARealPasswordChange() throws Exception {
    Map<String, Object> emptyPassword = new LinkedHashMap<>();
    emptyPassword.put("email", "empty-password@example.com");
    emptyPassword.put("password", "");
    putJson(emptyPassword).andExpect(jsonPath("$.code").value(1));
    assertTrue(tokenActive.get());
    verify(sessions, never()).revoke(TOKEN);

    Map<String, Object> invalidPassword = new LinkedHashMap<>();
    invalidPassword.put("email", "invalid-password@example.com");
    invalidPassword.put("password", "abc\n123");
    putJson(invalidPassword)
        .andExpect(jsonPath("$.code").value(0))
        .andExpect(
            jsonPath("$.msg").value("密码长度必须在6-30位之间，不能包含空格"));
    assertTrue(tokenActive.get());

    String password = "NewProfile123";
    mvc.perform(
            post("/api/admin/business/system/profile")
                .header("token", TOKEN)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("a[email]", "password@example.com")
                .param("a[password]", password))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(1));

    assertFalse(tokenActive.get());
    verify(sessions).revoke(TOKEN);
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Map<String, Object>> values = ArgumentCaptor.forClass(Map.class);
    verify(system, times(2)).updateAdminProfile(eq(7L), values.capture(), anyLong());
    Map<String, Object> passwordUpdate = values.getAllValues().get(1);
    String salt = String.valueOf(passwordUpdate.get("salt"));
    assertEquals(md5(md5(password) + salt), passwordUpdate.get("password"));

    BusinessResponse<?> afterPasswordChange = controller.profile(TOKEN, 1, 10);
    assertEquals(0, afterPasswordChange.getCode());
    assertEquals("管理员未登录", afterPasswordChange.getMsg());
    assertNull(afterPasswordChange.getData());
  }

  private ResultActions putJson(Map<String, Object> body) throws Exception {
    return mvc.perform(
        put("/api/admin/business/system/profile")
            .header("token", TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsBytes(body)));
  }

  private static void assertUpdate(
      Map<String, Object> values, String email, String nickname) {
    assertEquals(email, values.get("email"));
    assertEquals(nickname, values.get("nickname"));
  }

  private static String md5(String value) {
    return DigestUtils.md5DigestAsHex(value.getBytes(StandardCharsets.UTF_8));
  }
}
