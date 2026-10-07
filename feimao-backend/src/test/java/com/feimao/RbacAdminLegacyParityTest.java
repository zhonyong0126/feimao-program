package com.feimao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.feimao.common.BusinessResponse;
import com.feimao.controller.admin.RbacAdminController;
import com.feimao.mapper.AdminLogMapper;
import com.feimao.mapper.RbacAdminMapper;
import com.feimao.service.AdminSessionService;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RbacAdminLegacyParityTest {
  @Mock private RbacAdminMapper mapper;
  @Mock private AdminLogMapper logs;
  @Mock private AdminSessionService sessions;

  private RbacAdminController controller;

  @BeforeEach
  void setUp() {
    controller = new RbacAdminController(mapper, logs, sessions);
  }

  @Test
  @SuppressWarnings("unchecked")
  void ordinaryAdministratorContextContainsOnlyOwnedActionsAndChildGroups() {
    when(sessions.adminId("token")).thenReturn(7L);
    when(mapper.groups()).thenReturn(Arrays.asList(group(2, 0, "当前组", "40,41,43"), group(3, 2, "下级组", "40")));
    when(mapper.currentAdminGroupIds(7L)).thenReturn(Collections.singletonList(2L));
    when(mapper.adminIdsInGroups(anyList())).thenReturn(Arrays.asList(7L, 8L));
    when(mapper.allowedRuleNames(eq(7L), anyList()))
        .thenReturn(Arrays.asList("auth/admin/add", "auth/admin/del"));

    BusinessResponse<?> response = controller.context("token", "admins");

    assertEquals(1, response.getCode());
    Map<String, Object> data = (Map<String, Object>) response.getData();
    Map<String, Boolean> permissions = (Map<String, Boolean>) data.get("permissions");
    assertTrue(permissions.get("add"));
    assertFalse(permissions.get("edit"));
    assertTrue(permissions.get("delete"));
    List<Map<String, Object>> groups = (List<Map<String, Object>>) data.get("groups");
    assertEquals(Collections.singletonList(3L), Collections.singletonList(((Number) groups.get(0).get("id")).longValue()));
  }

  @Test
  void batchAdministratorDeleteProtectsCurrentAndSuperAdministrator() {
    when(sessions.adminId("token")).thenReturn(7L);
    when(mapper.groups()).thenReturn(Collections.singletonList(group(2, 0, "当前组", "*")));
    when(mapper.currentAdminGroupIds(7L)).thenReturn(Collections.singletonList(2L));
    when(mapper.adminIdsInGroups(anyList())).thenReturn(Arrays.asList(7L, 8L));
    when(mapper.deleteAdmin(8L)).thenReturn(1);
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("ids", Arrays.asList(1, 7, 8));

    BusinessResponse<?> response = controller.deleteAdmins("token", body);

    assertEquals(1, response.getCode());
    assertEquals(1, response.getData());
    verify(mapper).deleteAdmin(8L);
    verify(mapper, never()).deleteAdmin(1L);
    verify(mapper, never()).deleteAdmin(7L);
  }

  @Test
  void deletingRuleAlsoDeletesDescendantsAndPrunesGroupRuleLists() {
    when(sessions.adminId("token")).thenReturn(1L);
    when(mapper.rules()).thenReturn(Arrays.asList(rule(10, 0, "父级"), rule(11, 10, "子级"), rule(12, 0, "保留")));
    when(mapper.deleteRule(11L)).thenReturn(1);
    when(mapper.deleteRule(10L)).thenReturn(1);
    Map<String, Object> group = new LinkedHashMap<>();
    group.put("id", 2L);
    group.put("rules", "10,11,12");
    when(mapper.groupRules()).thenReturn(Collections.singletonList(group));
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("ids", Collections.singletonList(10));

    BusinessResponse<?> response = controller.deleteRules("token", body);

    assertEquals(1, response.getCode());
    assertEquals(2, response.getData());
    verify(mapper).deleteRule(11L);
    verify(mapper).deleteRule(10L);
    verify(mapper).updateGroupRules(eq(2L), eq("12"), org.mockito.ArgumentMatchers.anyLong());
  }

  @Test
  void migratedPagesExposeExactlyTheLegacyVisibleControls() throws Exception {
    String script = resource("static/admin/js/access.js");
    String admins = script.substring(script.indexOf("admins:"), script.indexOf("'admin-groups':"));
    assertTrue(admins.contains("['groups_text', '所属组别']"));
    assertTrue(admins.contains("['logintime', '最后登录']"));
    assertTrue(admins.contains("['group_ids', '所属组别', 'group-multi']"));
    assertFalse(admins.contains("email"));
    assertFalse(admins.contains("merchant_id"));
    assertFalse(admins.contains("avatar"));

    String groups = script.substring(script.indexOf("'admin-groups':"), script.indexOf("'admin-rules':"));
    assertTrue(groups.contains("['pid', '父级']"));
    assertTrue(groups.contains("['rule_ids', '权限', 'rule-tree']"));
    assertFalse(groups.contains("member_count"));

    String rules = script.substring(script.indexOf("'admin-rules':"), script.indexOf("logs:"));
    assertTrue(rules.contains("['url', '链接', 'text', true]"));
    assertTrue(rules.contains("['menutype', '打开方式', 'menu-type', true]"));
    assertTrue(rules.contains("['extend', '扩展属性', 'textarea', true]"));
    assertFalse(rules.contains("['type'"));
    assertTrue(script.contains("toggle-menu"));
    assertTrue(script.contains("config.url + '/context'"));

    String template = resource("templates/admin/access/list.ftl");
    assertTrue(template.contains("id=\"bulkRbacDelete\""));
    assertTrue(template.contains("id=\"toggleAllRules\""));
    assertTrue(template.contains("data-permission=\"add\""));
    assertTrue(template.contains("/admin/js/access.js?v=20261006-5"));
  }

  private Map<String, Object> group(long id, long pid, String name, String rules) {
    Map<String, Object> row = new LinkedHashMap<>();
    row.put("id", id);
    row.put("pid", pid);
    row.put("name", name);
    row.put("rules", rules);
    row.put("status", "normal");
    return row;
  }

  private Map<String, Object> rule(long id, long pid, String title) {
    Map<String, Object> row = new LinkedHashMap<>();
    row.put("id", id);
    row.put("pid", pid);
    row.put("title", title);
    row.put("name", "test/" + id);
    row.put("weigh", 0);
    return row;
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
