package com.feimao.controller.admin;

import com.feimao.common.BusinessResponse;
import com.feimao.mapper.AdminLogMapper;
import com.feimao.mapper.UserAccessMapper;
import com.feimao.service.AdminSessionService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Member groups and member-facing permission rules from FastAdmin user/group and user/rule. */
@RestController
@RequestMapping("/api/admin/business/user-access")
public class UserAccessAdminController {
  private final UserAccessMapper access;
  private final AdminLogMapper logs;
  private final AdminSessionService sessions;

  public UserAccessAdminController(UserAccessMapper access, AdminLogMapper logs, AdminSessionService sessions) {
    this.access = access;
    this.logs = logs;
    this.sessions = sessions;
  }

  @GetMapping("/groups")
  public BusinessResponse<?> groups(
      @RequestHeader(value = "token", required = false) String token) {
    if (admin(token) == null) return unauthorized();
    List<Map<String, Object>> groups =
        access.selectGroups();
    for (Map<String, Object> group : groups) group.put("rule_ids", ids(group.get("rules")));
    return BusinessResponse.success(groups);
  }

  @PostMapping("/groups")
  @Transactional
  public BusinessResponse<?> addGroup(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long admin = admin(token);
    if (admin == null) return unauthorized();
    String name = text(body, "name");
    if (name.isEmpty()) return BusinessResponse.error("组名不能为空");
    Set<Long> ruleIds = ids(body == null ? null : body.get("rule_ids"));
    String invalid = validateRules(ruleIds);
    if (invalid != null) return BusinessResponse.error(invalid);
    long now = now();
    access.insertGroup(name, join(ruleIds), status(body), now);
    Long id = access.lastInsertId();
    log(admin, "新增会员组", String.valueOf(id));
    return BusinessResponse.success(Collections.singletonMap("id", id));
  }

  @PutMapping("/groups/{id}")
  public BusinessResponse<?> editGroup(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable long id,
      @RequestBody(required = false) Map<String, Object> body) {
    Long admin = admin(token);
    if (admin == null) return unauthorized();
    if (access.groupCount(id) == 0)
      return BusinessResponse.error("会员组不存在");
    String name = text(body, "name");
    if (name.isEmpty()) return BusinessResponse.error("组名不能为空");
    Set<Long> ruleIds = ids(body == null ? null : body.get("rule_ids"));
    String invalid = validateRules(ruleIds);
    if (invalid != null) return BusinessResponse.error(invalid);
    access.updateGroup(id, name, join(ruleIds), status(body), now());
    log(admin, "编辑会员组", String.valueOf(id));
    return BusinessResponse.success(1);
  }

  @DeleteMapping("/groups/{id}")
  @Transactional
  public BusinessResponse<?> deleteGroup(
      @RequestHeader(value = "token", required = false) String token, @PathVariable long id) {
    Long admin = admin(token);
    if (admin == null) return unauthorized();
    if (access.groupUserCount(id) > 0)
      return BusinessResponse.error("会员组仍有会员，不能删除");
    if (access.deleteGroup(id) == 0)
      return BusinessResponse.error("会员组不存在");
    log(admin, "删除会员组", String.valueOf(id));
    return BusinessResponse.success(1);
  }

  @GetMapping("/rules")
  public BusinessResponse<?> rules(
      @RequestHeader(value = "token", required = false) String token) {
    if (admin(token) == null) return unauthorized();
    List<Map<String, Object>> rows =
        access.selectRules();
    return BusinessResponse.success(tree(rows, 0));
  }

  @PostMapping("/rules")
  @Transactional
  public BusinessResponse<?> addRule(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long admin = admin(token);
    if (admin == null) return unauthorized();
    String name = text(body, "name"), title = text(body, "title");
    long pid = number(body, "pid", 0);
    String error = validateRule(0, pid, name, title);
    if (error != null) return BusinessResponse.error(error);
    long now = now();
    access.insertRule(pid, name, title, text(body, "remark"), number(body, "ismenu", 0),
        number(body, "weigh", 0), status(body), now);
    Long id = access.lastInsertId();
    log(admin, "新增会员规则", String.valueOf(id));
    return BusinessResponse.success(Collections.singletonMap("id", id));
  }

  @PutMapping("/rules/{id}")
  public BusinessResponse<?> editRule(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable long id,
      @RequestBody(required = false) Map<String, Object> body) {
    Long admin = admin(token);
    if (admin == null) return unauthorized();
    if (access.ruleCount(id) == 0)
      return BusinessResponse.error("会员规则不存在");
    long pid = number(body, "pid", 0);
    if (pid == id || descendants(id).contains(pid)) return BusinessResponse.error("上级规则不能是自身或子规则");
    String name = text(body, "name"), title = text(body, "title");
    String error = validateRule(id, pid, name, title);
    if (error != null) return BusinessResponse.error(error);
    access.updateRule(id, pid, name, title, text(body, "remark"), number(body, "ismenu", 0),
        number(body, "weigh", 0), status(body), now());
    log(admin, "编辑会员规则", String.valueOf(id));
    return BusinessResponse.success(1);
  }

  @DeleteMapping("/rules/{id}")
  @Transactional
  public BusinessResponse<?> deleteRule(
      @RequestHeader(value = "token", required = false) String token, @PathVariable long id) {
    Long admin = admin(token);
    if (admin == null) return unauthorized();
    Set<Long> deleted = descendants(id);
    deleted.add(id);
    if (access.ruleCount(id) == 0)
      return BusinessResponse.error("会员规则不存在");
    access.deleteRules(deleted);
    for (Map<String, Object> group : access.selectGroupRules()) {
      Set<Long> retained = ids(group.get("rules"));
      if (retained.removeAll(deleted))
        access.updateGroupRules(((Number) group.get("id")).longValue(), join(retained), now());
    }
    log(admin, "删除会员规则", join(deleted));
    return BusinessResponse.success(1);
  }

  private String validateRule(long id, long pid, String name, String title) {
    if (name.isEmpty() || title.isEmpty()) return "规则名称和标题不能为空";
    if (pid > 0 && access.ruleCount(pid) == 0)
      return "上级规则不存在";
    if (access.ruleNameCount(name, id) > 0)
      return "规则名称已存在";
    return null;
  }

  private String validateRules(Set<Long> ids) {
    for (Long id : ids)
      if (access.ruleCount(id) == 0) return "会员规则不存在";
    return null;
  }

  private Set<Long> descendants(long parent) {
    Set<Long> result = new LinkedHashSet<>();
    Set<Long> pending = new LinkedHashSet<>();
    pending.add(parent);
    while (!pending.isEmpty()) {
      List<Long> children = access.selectChildren(pending);
      pending.clear();
      for (Long child : children) if (result.add(child)) pending.add(child);
    }
    return result;
  }

  private static List<Map<String, Object>> tree(List<Map<String, Object>> rows, long parent) {
    List<Map<String, Object>> result = new ArrayList<>();
    for (Map<String, Object> source : rows) {
      if (((Number) source.get("pid")).longValue() != parent) continue;
      Map<String, Object> node = new LinkedHashMap<>(source);
      node.put("children", tree(rows, ((Number) source.get("id")).longValue()));
      result.add(node);
    }
    return result;
  }

  private Long admin(String token) {
    return sessions.adminId(token);
  }

  private void log(long admin, String title, String content) {
    logs.insertLog(admin, "/api/admin/business/user-access", title, content, now());
  }

  private static Set<Long> ids(Object value) {
    Set<Long> result = new LinkedHashSet<>();
    if (value == null) return result;
    Iterable<?> values = value instanceof Iterable ? (Iterable<?>) value : split(String.valueOf(value));
    for (Object item : values) {
      try {
        result.add(Long.parseLong(String.valueOf(item).trim()));
      } catch (NumberFormatException ignored) {
        // Ignore malformed IDs from historical group records.
      }
    }
    return result;
  }

  private static List<String> split(String value) {
    if (value.trim().isEmpty()) return Collections.emptyList();
    return java.util.Arrays.asList(value.split(","));
  }

  private static String join(Set<Long> ids) {
    return ids.stream().map(String::valueOf).collect(Collectors.joining(","));
  }

  private static long number(Map<String, Object> body, String key, long fallback) {
    try {
      return Long.parseLong(text(body, key));
    } catch (NumberFormatException ignored) {
      return fallback;
    }
  }

  private static String text(Map<String, Object> body, String key) {
    return body == null || body.get(key) == null ? "" : String.valueOf(body.get(key)).trim();
  }

  private static String status(Map<String, Object> body) {
    return "hidden".equals(text(body, "status")) ? "hidden" : "normal";
  }

  private static long now() {
    return System.currentTimeMillis() / 1000;
  }

  private static BusinessResponse<?> unauthorized() {
    return BusinessResponse.error("管理员未登录");
  }
}
