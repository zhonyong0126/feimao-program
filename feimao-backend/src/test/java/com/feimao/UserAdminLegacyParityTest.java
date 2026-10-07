package com.feimao;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.feimao.common.BusinessResponse;
import com.feimao.controller.admin.UserAdminController;
import com.feimao.entity.BusinessUser;
import com.feimao.mapper.AdminLogMapper;
import com.feimao.mapper.UserAdminMapper;
import com.feimao.service.AdminSessionService;
import com.feimao.service.BusinessUserService;
import com.feimao.service.UserTokenService;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAdminLegacyParityTest {
  @Mock private UserAdminMapper mapper;
  @Mock private BusinessUserService users;
  @Mock private UserTokenService tokens;
  @Mock private AdminLogMapper logs;
  @Mock private AdminSessionService sessions;

  private UserAdminController controller;

  @BeforeEach
  void setUp() {
    controller = new UserAdminController(mapper, users, tokens, logs, sessions);
  }

  @Test
  void listPassesEveryLegacyMemberFilterToTheMapper() {
    when(sessions.adminId("admin-token")).thenReturn(7L);
    when(mapper.countRows(12L, "小明", "138", 100L, 200L, "hidden")).thenReturn(1);
    when(mapper.selectRows(12L, "小明", "138", 100L, 200L, "hidden", 20, 20))
        .thenReturn(Collections.singletonList(Collections.<String, Object>singletonMap("id", 12L)));

    BusinessResponse<?> response =
        controller.list("admin-token", 12L, " 小明 ", " 138 ", 100L, 200L, "hidden", 2, 20);

    assertEquals(1, response.getCode());
    Map<?, ?> data = (Map<?, ?>) response.getData();
    assertEquals(1, data.get("total"));
    assertEquals(2, data.get("page"));
    verify(mapper).countRows(12L, "小明", "138", 100L, 200L, "hidden");
    verify(mapper).selectRows(12L, "小明", "138", 100L, 200L, "hidden", 20, 20);
  }

  @Test
  void batchDeleteAndStatusUseDeduplicatedPositiveMemberIds() {
    when(sessions.adminId("admin-token")).thenReturn(7L);
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("ids", Arrays.<Object>asList(3, "4", 3, "无效", 0));
    when(mapper.deleteRows(Arrays.asList(3L, 4L))).thenReturn(2);

    BusinessResponse<?> deleted = controller.deleteMany("admin-token", body);

    assertEquals(1, deleted.getCode());
    assertEquals(2, deleted.getData());
    verify(tokens).revokeUser(3L);
    verify(tokens).revokeUser(4L);
    verify(mapper).deleteRows(Arrays.asList(3L, 4L));

    body.put("status", "hidden");
    when(mapper.updateStatuses(org.mockito.ArgumentMatchers.eq(Arrays.asList(3L, 4L)),
        org.mockito.ArgumentMatchers.eq("hidden"), org.mockito.ArgumentMatchers.anyLong()))
        .thenReturn(2);

    BusinessResponse<?> hidden = controller.updateStatuses("admin-token", body);

    assertEquals(1, hidden.getCode());
    assertEquals(2, hidden.getData());
  }

  @Test
  @SuppressWarnings({"rawtypes", "unchecked"})
  void editAcceptsOnlyFieldsEnabledByTheLegacyEditForm() {
    when(sessions.adminId("admin-token")).thenReturn(7L);
    when(mapper.lockUser(9L)).thenReturn(Collections.<String, Object>singletonMap("id", 9L));
    Map<String, Object> unsupported = new LinkedHashMap<>();
    unsupported.put("username", "not-editable");
    unsupported.put("money", "999.00");
    assertEquals(0, controller.edit("admin-token", 9L, unsupported).getCode());

    Map<String, Object> allowed = new LinkedHashMap<>();
    allowed.put("avatar", "/uploads/member.png");
    allowed.put("nickname", "会员九");
    allowed.put("mobile", "138" + "0000" + "0009");
    allowed.put("jointime", "2026-10-06T08:30:00");
    allowed.put("status", "normal");
    when(users.update(any(UpdateWrapper.class))).thenReturn(true);

    assertEquals(1, controller.edit("admin-token", 9L, allowed).getCode());

    ArgumentCaptor<UpdateWrapper<BusinessUser>> updateCaptor =
        (ArgumentCaptor) ArgumentCaptor.forClass(UpdateWrapper.class);
    verify(users).update(updateCaptor.capture());
    String sqlSet = updateCaptor.getValue().getSqlSet();
    assertTrue(sqlSet.contains("avatar="));
    assertTrue(sqlSet.contains("nickname="));
    assertTrue(sqlSet.contains("mobile="));
    assertTrue(sqlSet.contains("jointime="));
    assertTrue(sqlSet.contains("status="));
    assertFalse(sqlSet.contains("username="));
    assertFalse(sqlSet.contains("money="));
  }

  @Test
  void memberPageContainsOnlyTheLegacyListAndEditControls() throws Exception {
    String script = resource("static/admin/js/access.js");
    String memberConfig = script.substring(script.indexOf("users:"), script.indexOf("'user-groups':"));
    assertTrue(memberConfig.contains("['avatar', '头像'"));
    assertTrue(memberConfig.contains("['nickname', '昵称']"));
    assertTrue(memberConfig.contains("['mobile', '手机号']"));
    assertTrue(memberConfig.contains("['jointime', '注册时间'"));
    assertTrue(memberConfig.contains("['status', '状态'"));
    assertFalse(memberConfig.contains("username"));
    assertFalse(memberConfig.contains("password"));
    assertFalse(memberConfig.contains("money"));
    assertTrue(script.contains("name=\"jointimeFrom\""));
    assertTrue(script.contains("name=\"jointimeTo\""));
    assertTrue(script.contains("bulkMemberNormal"));
    assertTrue(script.contains("bulkMemberHidden"));

    String template = resource("templates/admin/access/list.ftl");
    assertTrue(template.contains("<#if section == 'users'>"));
    assertTrue(template.contains("bulkMemberDelete"));
    assertTrue(template.contains("<#if section != 'users' && section != 'admins'"));
    assertTrue(template.contains("/admin/js/access.js?v=20261006-5"));
  }

  private String resource(String name) throws Exception {
    InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
    if (input == null) throw new IllegalStateException("Missing test resource: " + name);
    try (InputStream stream = input; ByteArrayOutputStream output = new ByteArrayOutputStream()) {
      byte[] buffer = new byte[4096];
      int read;
      while ((read = stream.read(buffer)) != -1) output.write(buffer, 0, read);
      return new String(output.toByteArray(), StandardCharsets.UTF_8);
    }
  }
}
