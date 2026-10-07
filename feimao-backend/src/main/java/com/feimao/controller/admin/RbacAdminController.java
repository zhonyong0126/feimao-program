package com.feimao.controller.admin;

import com.feimao.common.BusinessResponse;
import com.feimao.mapper.AdminLogMapper;
import com.feimao.mapper.RbacAdminMapper;
import com.feimao.service.AdminSessionService;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/business/rbac")
public class RbacAdminController {
  private static final Pattern USERNAME = Pattern.compile("\\w{3,30}");
  private static final Pattern PASSWORD = Pattern.compile("\\S{6,30}");
  private static final Pattern MOBILE = Pattern.compile("1[3-9]\\d{9}");
  private static final Set<String> MENU_TYPES =
      new LinkedHashSet<>(Arrays.asList("addtabs", "dialog", "ajax", "blank"));

  private final RbacAdminMapper rbac;
  private final AdminLogMapper adminLogs;
  private final AdminSessionService sessions;
  private final SecureRandom random = new SecureRandom();

  public RbacAdminController(
      RbacAdminMapper rbac, AdminLogMapper adminLogs, AdminSessionService sessions) {
    this.rbac = rbac;
    this.adminLogs = adminLogs;
    this.sessions = sessions;
  }

  @GetMapping("/admins")
  public BusinessResponse<?> admins(
      @RequestHeader(value = "token", required = false) String token,
      @RequestParam(required = false) Long id,
      @RequestParam(required = false) String username,
      @RequestParam(required = false) String nickname,
      @RequestParam(required = false) String mobile,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) Long loginFrom,
      @RequestParam(required = false) Long loginTo,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int limit) {
    Long operator = adminId(token);
    if (operator == null) return unauthorized();
    page = Math.max(page, 1);
    limit = Math.min(Math.max(limit, 1), 100);
    Scope scope = scope(operator);
    int unrestricted = operator == 1 ? 1 : 0;
    List<Long> visibleAdmins = new ArrayList<>(scope.visibleAdminIds);
    int total =
        rbac.adminCount(
            id, trim(username), trim(nickname), trim(mobile), trim(status), loginFrom, loginTo,
            unrestricted, visibleAdmins);
    List<Map<String, Object>> rows =
        rbac.admins(
            id, trim(username), trim(nickname), trim(mobile), trim(status), loginFrom, loginTo,
            unrestricted, visibleAdmins, limit, (page - 1) * limit);
    for (Map<String, Object> row : rows) {
      List<Map<String, Object>> groups =
          visibleAdminGroups(number(row.get("id")), scope.groupIds);
      List<String> names = new ArrayList<>();
      List<Long> groupIds = new ArrayList<>();
      for (Map<String, Object> group : groups) {
        groupIds.add(number(group.get("id")));
        names.add(String.valueOf(group.get("name")));
      }
      row.put("groups", join(new LinkedHashSet<>(groupIds)));
      row.put("groups_text", String.join(",", names));
    }
    return BusinessResponse.success(page(rows, total, page, limit));
  }

  /** Kept for direct Java callers while HTTP uses the legacy column filters above. */
  public BusinessResponse<?> admins(String token, String keyword, int page, int limit) {
    return admins(token, null, keyword, keyword, keyword, null, null, null, page, limit);
  }

  @GetMapping("/{section:admins|groups|rules}/context")
  public BusinessResponse<?> context(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable String section) {
    Long operator = adminId(token);
    if (operator == null) return unauthorized();
    if ("rules".equals(section) && operator != 1)
      return BusinessResponse.error("仅超级管理员可以管理后台权限");
    Scope scope = scope(operator);
    String prefix =
        "admins".equals(section)
            ? "auth/admin"
            : "groups".equals(section) ? "auth/group" : "auth/rule";
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("admin_id", operator);
    result.put("super_admin", operator == 1);
    result.put("own_group_ids", new ArrayList<>(scope.ownGroupIds));
    result.put("permissions", actionPermissions(operator, prefix));
    if ("admins".equals(section)) {
      result.put("groups", groupRows(scope.assignableGroupIds));
    } else if ("groups".equals(section)) {
      result.put("groups", groupRows(scope.groupIds));
      result.put("rules", permittedRuleRows(operator));
    } else {
      result.put("rules", rbac.rules());
    }
    return BusinessResponse.success(result);
  }

  @PostMapping("/admins")
  @Transactional
  public BusinessResponse<?> addAdmin(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long operator = adminId(token);
    if (operator == null) return unauthorized();
    String username = value(body, "username");
    String nickname = value(body, "nickname");
    String password = value(body, "password");
    String mobile = value(body, "mobile");
    String error = validateAdmin(username, nickname, password, mobile, 0, true);
    if (error != null) return BusinessResponse.error(error);
    String state = requestedStatus(body);
    if (state == null) return BusinessResponse.error("状态参数错误");
    Set<Long> groups = ids(body == null ? null : body.get("group_ids"));
    error = validateAssignableGroups(operator, groups);
    if (error != null) return BusinessResponse.error(error);

    String salt = random(10);
    long now = now();
    Map<String, Object> values = new LinkedHashMap<>();
    values.put("username", username);
    values.put("nickname", nickname);
    values.put("password", hash(password, salt));
    values.put("salt", salt);
    values.put("avatar", "/assets/img/avatar.png");
    values.put("mobile", mobile);
    values.put("status", state);
    values.put("loginfailure", 0);
    values.put("createtime", now);
    values.put("updatetime", now);
    rbac.insert("admin", values);
    long id = number(values.get("id"));
    replaceGroups(id, groups);
    log(operator, "新增管理员", String.valueOf(id));
    return BusinessResponse.success(Collections.singletonMap("id", id));
  }

  @PutMapping("/admins/{id}")
  @Transactional
  public BusinessResponse<?> updateAdmin(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable long id,
      @RequestBody(required = false) Map<String, Object> body) {
    Long operator = adminId(token);
    if (operator == null) return unauthorized();
    if (!canManageAdmin(operator, id) || rbac.adminExists(id) == 0)
      return BusinessResponse.error("管理员不存在或无权操作");
    Map<String, Object> values = new LinkedHashMap<>();
    String username = body != null && body.containsKey("username") ? value(body, "username") : null;
    String nickname = body != null && body.containsKey("nickname") ? value(body, "nickname") : null;
    String mobile = body != null && body.containsKey("mobile") ? value(body, "mobile") : null;
    String password = body != null && body.containsKey("password") ? value(body, "password") : null;
    if (username != null && !USERNAME.matcher(username).matches())
      return BusinessResponse.error("用户名必须为3至30位字母、数字或下划线");
    if (username != null && rbac.adminUsernameCount(username, id) > 0)
      return BusinessResponse.error("管理员账号已存在");
    if (nickname != null && nickname.isEmpty()) return BusinessResponse.error("昵称不能为空");
    if (mobile != null && !mobile.isEmpty() && !MOBILE.matcher(mobile).matches())
      return BusinessResponse.error("手机号格式错误");
    if (mobile != null && !mobile.isEmpty() && rbac.adminMobileCount(mobile, id) > 0)
      return BusinessResponse.error("手机号已存在");
    if (password != null && !password.isEmpty() && !PASSWORD.matcher(password).matches())
      return BusinessResponse.error("密码必须为6至30位且不能包含空白字符");
    if (username != null) values.put("username", username);
    if (nickname != null) values.put("nickname", nickname);
    if (mobile != null) values.put("mobile", mobile);
    if (body != null && body.containsKey("status")) {
      String state = requestedStatus(body);
      if (state == null) return BusinessResponse.error("状态参数错误");
      values.put("status", state);
    }
    if (password != null && !password.isEmpty()) {
      String salt = random(10);
      values.put("salt", salt);
      values.put("password", hash(password, salt));
    }
    if (body != null && body.containsKey("group_ids")) {
      Set<Long> groups = ids(body.get("group_ids"));
      String error = validateAssignableGroups(operator, groups);
      if (error != null) return BusinessResponse.error(error);
      replaceGroups(id, groups);
    }
    if (values.isEmpty() && (body == null || !body.containsKey("group_ids")))
      return BusinessResponse.error("没有可修改内容");
    if (!values.isEmpty()) {
      values.put("updatetime", now());
      rbac.update("admin", id, values);
    }
    log(operator, "修改管理员", String.valueOf(id));
    return BusinessResponse.success(1);
  }

  @DeleteMapping("/admins/{id}")
  @Transactional
  public BusinessResponse<?> deleteAdmin(
      @RequestHeader(value = "token", required = false) String token, @PathVariable long id) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("ids", Collections.singletonList(id));
    return deleteAdmins(token, body);
  }

  @DeleteMapping("/admins")
  @Transactional
  public BusinessResponse<?> deleteAdmins(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long operator = adminId(token);
    if (operator == null) return unauthorized();
    int deleted = 0;
    for (Long id : ids(body == null ? null : body.get("ids"))) {
      if (id == 1 || id == operator || !canManageAdmin(operator, id)) continue;
      if (rbac.deleteAdmin(id) == 0) continue;
      rbac.deleteAdminGroups(id);
      rbac.deleteAdminSessions(id);
      deleted++;
    }
    if (deleted == 0) return BusinessResponse.error("没有可删除的管理员");
    log(operator, "删除管理员", String.valueOf(deleted));
    return BusinessResponse.success(deleted);
  }

  @GetMapping("/groups")
  public BusinessResponse<?> groups(@RequestHeader(value = "token", required = false) String token) {
    Long operator = adminId(token);
    if (operator == null) return unauthorized();
    return BusinessResponse.success(tree(groupRows(scope(operator).groupIds), 0));
  }

  @PostMapping("/groups")
  @Transactional
  public BusinessResponse<?> addGroup(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long operator = adminId(token);
    if (operator == null) return unauthorized();
    String name = value(body, "name");
    if (name.isEmpty()) return BusinessResponse.error("权限组名称不能为空");
    long pid = longValue(body, "pid", 0);
    if (pid <= 0 || !scope(operator).groupIds.contains(pid))
      return BusinessResponse.error("父级权限组不存在或超出权限范围");
    String state = requestedStatus(body);
    if (state == null) return BusinessResponse.error("状态参数错误");
    Set<Long> requested = ids(body == null ? null : body.get("rule_ids"));
    String ruleError = validateRules(requested);
    if (ruleError != null) return BusinessResponse.error(ruleError);
    Set<Long> rules = restrictedRules(operator, pid, requested);
    Map<String, Object> values = new LinkedHashMap<>();
    values.put("pid", pid);
    values.put("name", name);
    values.put("rules", join(rules));
    values.put("status", state);
    values.put("createtime", now());
    values.put("updatetime", now());
    rbac.insert("group", values);
    long id = number(values.get("id"));
    log(operator, "新增权限组", String.valueOf(id));
    return BusinessResponse.success(Collections.singletonMap("id", id));
  }

  @PutMapping("/groups/{id}")
  @Transactional
  public BusinessResponse<?> updateGroup(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable long id,
      @RequestBody(required = false) Map<String, Object> body) {
    Long operator = adminId(token);
    if (operator == null) return unauthorized();
    Scope scope = scope(operator);
    if (!scope.groupIds.contains(id) || scope.ownGroupIds.contains(id) || rbac.groupExists(id) == 0)
      return BusinessResponse.error("权限组不存在或无权操作");
    Map<String, Object> values = new LinkedHashMap<>();
    if (body != null && body.containsKey("name")) {
      String name = value(body, "name");
      if (name.isEmpty()) return BusinessResponse.error("权限组名称不能为空");
      values.put("name", name);
    }
    long pid = groupParent(id);
    if (body != null && body.containsKey("pid")) {
      pid = longValue(body, "pid", 0);
      if (pid <= 0 || !scope.groupIds.contains(pid))
        return BusinessResponse.error("父级权限组不存在或超出权限范围");
      if (descendantGroupIds(id, allGroups()).contains(pid))
        return BusinessResponse.error("父级权限组不能是自身或下级");
      values.put("pid", pid);
    }
    if (body != null && body.containsKey("status")) {
      String state = requestedStatus(body);
      if (state == null) return BusinessResponse.error("状态参数错误");
      values.put("status", state);
    }
    Set<Long> rules = null;
    if (body != null && body.containsKey("rule_ids")) {
      Set<Long> requested = ids(body.get("rule_ids"));
      String error = validateRules(requested);
      if (error != null) return BusinessResponse.error(error);
      rules = restrictedRules(operator, pid, requested);
      values.put("rules", join(rules));
    }
    if (values.isEmpty()) return BusinessResponse.error("没有可修改内容");
    values.put("updatetime", now());
    rbac.update("group", id, values);
    if (rules != null) restrictChildGroupRules(id, rules);
    log(operator, "修改权限组", String.valueOf(id));
    return BusinessResponse.success(1);
  }

  @DeleteMapping("/groups/{id}")
  @Transactional
  public BusinessResponse<?> deleteGroup(
      @RequestHeader(value = "token", required = false) String token, @PathVariable long id) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("ids", Collections.singletonList(id));
    return deleteGroups(token, body);
  }

  @DeleteMapping("/groups")
  @Transactional
  public BusinessResponse<?> deleteGroups(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long operator = adminId(token);
    if (operator == null) return unauthorized();
    Scope scope = scope(operator);
    int deleted = 0;
    for (Long id : ids(body == null ? null : body.get("ids"))) {
      if (!scope.groupIds.contains(id) || scope.ownGroupIds.contains(id)) continue;
      if (rbac.groupMemberCount(id) > 0 || rbac.childGroupCount(id) > 0) continue;
      if (rbac.deleteGroup(id) > 0) deleted++;
    }
    if (deleted == 0)
      return BusinessResponse.error("不能删除当前所属、含管理员或含下级的权限组");
    log(operator, "删除权限组", String.valueOf(deleted));
    return BusinessResponse.success(deleted);
  }

  @GetMapping("/rules")
  public BusinessResponse<?> rules(@RequestHeader(value = "token", required = false) String token) {
    Long operator = adminId(token);
    if (operator == null) return unauthorized();
    if (operator != 1) return BusinessResponse.error("仅超级管理员可以管理后台权限");
    return BusinessResponse.success(tree(rbac.rules(), 0));
  }

  @PostMapping("/rules")
  @Transactional
  public BusinessResponse<?> addRule(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long operator = requireSuper(token);
    if (operator == null) return superAdminRequired(token);
    String name = value(body, "name");
    String title = value(body, "title");
    String error = validateRuleForm(body, 0, name, title);
    if (error != null) return BusinessResponse.error(error);
    Map<String, Object> values = ruleValues(body);
    values.put("type", "file");
    values.put("name", name);
    values.put("title", title);
    values.putIfAbsent("pid", 0L);
    values.putIfAbsent("ismenu", 0L);
    values.putIfAbsent("weigh", 0L);
    values.putIfAbsent("status", "normal");
    values.put("createtime", now());
    values.put("updatetime", now());
    rbac.insert("rule", values);
    long id = number(values.get("id"));
    log(operator, "新增权限规则", String.valueOf(id));
    return BusinessResponse.success(Collections.singletonMap("id", id));
  }

  @PutMapping("/rules/{id}")
  @Transactional
  public BusinessResponse<?> updateRule(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable long id,
      @RequestBody(required = false) Map<String, Object> body) {
    Long operator = requireSuper(token);
    if (operator == null) return superAdminRequired(token);
    if (rbac.ruleExists(id) == 0) return BusinessResponse.error("权限规则不存在");
    String name =
        body != null && body.containsKey("name") ? value(body, "name") : currentRuleText(id, "name");
    String title =
        body != null && body.containsKey("title") ? value(body, "title") : currentRuleText(id, "title");
    String error = validateRuleForm(body, id, name, title);
    if (error != null) return BusinessResponse.error(error);
    if (body != null && body.containsKey("pid")) {
      long pid = longValue(body, "pid", 0);
      if (descendantRuleIds(id, rbac.rules()).contains(pid))
        return BusinessResponse.error("上级规则不能是自身或下级");
    }
    Map<String, Object> values = ruleValues(body);
    if (body != null && body.containsKey("name")) values.put("name", name);
    if (body != null && body.containsKey("title")) values.put("title", title);
    if (values.isEmpty()) return BusinessResponse.error("没有可修改内容");
    values.put("updatetime", now());
    rbac.update("rule", id, values);
    log(operator, "修改权限规则", String.valueOf(id));
    return BusinessResponse.success(1);
  }

  @PutMapping("/rules/{id}/menu")
  @Transactional
  public BusinessResponse<?> updateRuleMenu(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable long id,
      @RequestBody(required = false) Map<String, Object> body) {
    Long operator = requireSuper(token);
    if (operator == null) return superAdminRequired(token);
    if (rbac.ruleExists(id) == 0) return BusinessResponse.error("权限规则不存在");
    long ismenu = longValue(body, "ismenu", -1);
    if (ismenu != 0 && ismenu != 1) return BusinessResponse.error("菜单状态参数错误");
    Map<String, Object> values = new LinkedHashMap<>();
    values.put("ismenu", ismenu);
    values.put("updatetime", now());
    rbac.update("rule", id, values);
    log(operator, "修改菜单状态", String.valueOf(id));
    return BusinessResponse.success(1);
  }

  @DeleteMapping("/rules/{id}")
  @Transactional
  public BusinessResponse<?> deleteRule(
      @RequestHeader(value = "token", required = false) String token, @PathVariable long id) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("ids", Collections.singletonList(id));
    return deleteRules(token, body);
  }

  @DeleteMapping("/rules")
  @Transactional
  public BusinessResponse<?> deleteRules(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long operator = requireSuper(token);
    if (operator == null) return superAdminRequired(token);
    List<Map<String, Object>> all = rbac.rules();
    Set<Long> deleteIds = new LinkedHashSet<>();
    for (Long id : ids(body == null ? null : body.get("ids")))
      deleteIds.addAll(descendantRuleIds(id, all));
    if (deleteIds.isEmpty()) return BusinessResponse.error("没有可删除的权限规则");
    List<Long> ordered = new ArrayList<>(deleteIds);
    Collections.reverse(ordered);
    int deleted = 0;
    for (Long id : ordered) if (rbac.deleteRule(id) > 0) deleted++;
    if (deleted == 0) return BusinessResponse.error("没有可删除的权限规则");
    removeRulesFromGroups(deleteIds);
    log(operator, "删除权限规则", String.valueOf(deleted));
    return BusinessResponse.success(deleted);
  }

  private String validateAdmin(
      String username, String nickname, String password, String mobile, long excludeId, boolean adding) {
    if (!USERNAME.matcher(username).matches()) return "用户名必须为3至30位字母、数字或下划线";
    if (nickname.isEmpty()) return "昵称不能为空";
    if ((adding || !password.isEmpty()) && !PASSWORD.matcher(password).matches())
      return "密码必须为6至30位且不能包含空白字符";
    if (!mobile.isEmpty() && !MOBILE.matcher(mobile).matches()) return "手机号格式错误";
    if (rbac.adminUsernameCount(username, excludeId) > 0) return "管理员账号已存在";
    if (!mobile.isEmpty() && rbac.adminMobileCount(mobile, excludeId) > 0) return "手机号已存在";
    return null;
  }

  private String validateRuleForm(
      Map<String, Object> body, long excludeId, String name, String title) {
    if (name.isEmpty() || title.isEmpty()) return "规则名称和标题不能为空";
    if (rbac.ruleNameCount(name, excludeId) > 0) return "规则名称已存在";
    long pid = longValue(body, "pid", excludeId == 0 ? 0 : currentRuleNumber(excludeId, "pid"));
    long ismenu =
        longValue(body, "ismenu", excludeId == 0 ? 0 : currentRuleNumber(excludeId, "ismenu"));
    if (ismenu != 0 && ismenu != 1) return "是否菜单参数错误";
    if (ismenu == 0 && pid == 0) return "非菜单规则必须选择上级";
    if (pid > 0 && rbac.ruleExists(pid) == 0) return "上级规则不存在";
    if (body != null && body.containsKey("status") && requestedStatus(body) == null)
      return "状态参数错误";
    String menuType = value(body, "menutype");
    if (!menuType.isEmpty() && !MENU_TYPES.contains(menuType)) return "菜单打开方式错误";
    return null;
  }

  private Map<String, Object> ruleValues(Map<String, Object> body) {
    Map<String, Object> values = new LinkedHashMap<>();
    if (body == null) return values;
    for (String field : Arrays.asList("icon", "url", "condition", "remark", "menutype", "extend"))
      if (body.containsKey(field)) values.put(field, value(body, field));
    for (String field : Arrays.asList("pid", "ismenu", "weigh"))
      if (body.containsKey(field)) values.put(field, longValue(body, field, 0));
    if (body.containsKey("status")) values.put("status", requestedStatus(body));
    return values;
  }

  private Map<String, Boolean> actionPermissions(long adminId, String prefix) {
    List<String> names = Arrays.asList(prefix + "/add", prefix + "/edit", prefix + "/del");
    Set<String> allowed =
        adminId == 1
            ? new LinkedHashSet<>(names)
            : new LinkedHashSet<>(rbac.allowedRuleNames(adminId, names));
    Map<String, Boolean> permissions = new LinkedHashMap<>();
    permissions.put("add", allowed.contains(prefix + "/add"));
    permissions.put("edit", allowed.contains(prefix + "/edit"));
    permissions.put("delete", allowed.contains(prefix + "/del"));
    return permissions;
  }

  private Scope scope(long adminId) {
    List<Map<String, Object>> groups = allGroups();
    Set<Long> own = new LinkedHashSet<>(rbac.currentAdminGroupIds(adminId));
    Set<Long> visible = new LinkedHashSet<>();
    if (adminId == 1) {
      for (Map<String, Object> group : groups) visible.add(number(group.get("id")));
    } else {
      for (Long root : own) visible.addAll(descendantGroupIds(root, groups));
    }
    visible.remove(4L);
    Set<Long> assignable = new LinkedHashSet<>(visible);
    if (adminId != 1) assignable.removeAll(own);
    Set<Long> visibleAdmins = new LinkedHashSet<>();
    if (!visible.isEmpty()) visibleAdmins.addAll(rbac.adminIdsInGroups(new ArrayList<>(visible)));
    visibleAdmins.add(adminId);
    return new Scope(own, visible, assignable, visibleAdmins);
  }

  private boolean canManageAdmin(long operator, long id) {
    return operator == 1 || scope(operator).visibleAdminIds.contains(id);
  }

  private String validateAssignableGroups(long operator, Set<Long> groups) {
    if (groups.isEmpty()) return "至少选择一个所属组别";
    if (!scope(operator).assignableGroupIds.containsAll(groups))
      return "所属组别不存在或超出权限范围";
    return null;
  }

  private List<Map<String, Object>> visibleAdminGroups(long adminId, Set<Long> allowed) {
    List<Map<String, Object>> result = new ArrayList<>();
    for (Map<String, Object> group : rbac.adminGroups(adminId))
      if (allowed.contains(number(group.get("id")))) result.add(group);
    return result;
  }

  private List<Map<String, Object>> allGroups() {
    List<Map<String, Object>> result = new ArrayList<>();
    for (Map<String, Object> row : rbac.groups())
      if (number(row.get("id")) != 4) result.add(new LinkedHashMap<>(row));
    return result;
  }

  private List<Map<String, Object>> groupRows(Set<Long> allowed) {
    List<Map<String, Object>> result = new ArrayList<>();
    for (Map<String, Object> row : allGroups())
      if (allowed.contains(number(row.get("id")))) result.add(row);
    return result;
  }

  private Set<Long> descendantGroupIds(long root, List<Map<String, Object>> groups) {
    Set<Long> result = new LinkedHashSet<>();
    result.add(root);
    boolean changed;
    do {
      changed = false;
      for (Map<String, Object> group : groups) {
        long id = number(group.get("id"));
        if (result.contains(number(group.get("pid"))) && result.add(id)) changed = true;
      }
    } while (changed);
    return result;
  }

  private long groupParent(long id) {
    for (Map<String, Object> group : allGroups())
      if (number(group.get("id")) == id) return number(group.get("pid"));
    return 0;
  }

  private Set<Long> restrictedRules(long operator, long parentGroupId, Set<Long> requested) {
    Set<Long> result = new LinkedHashSet<>(requested);
    result.retainAll(groupRuleAllowance(parentGroupId));
    result.retainAll(operatorRuleAllowance(operator));
    return result;
  }

  private Set<Long> operatorRuleAllowance(long operator) {
    Set<Long> all = allRuleIds();
    if (operator == 1) return all;
    Set<Long> result = new LinkedHashSet<>();
    Set<Long> own = new LinkedHashSet<>(rbac.currentAdminGroupIds(operator));
    for (Map<String, Object> group : allGroups()) {
      if (!own.contains(number(group.get("id")))) continue;
      String rules = String.valueOf(group.get("rules"));
      if ("*".equals(rules)) return all;
      result.addAll(ids(rules));
    }
    return result;
  }

  private Set<Long> groupRuleAllowance(long groupId) {
    Set<Long> all = allRuleIds();
    for (Map<String, Object> group : allGroups()) {
      if (number(group.get("id")) != groupId) continue;
      String rules = String.valueOf(group.get("rules"));
      return "*".equals(rules) ? all : ids(rules);
    }
    return new LinkedHashSet<>();
  }

  private Set<Long> allRuleIds() {
    Set<Long> result = new LinkedHashSet<>();
    for (Map<String, Object> rule : rbac.rules()) result.add(number(rule.get("id")));
    return result;
  }

  private List<Map<String, Object>> permittedRuleRows(long operator) {
    Set<Long> allowed = operatorRuleAllowance(operator);
    List<Map<String, Object>> result = new ArrayList<>();
    for (Map<String, Object> rule : rbac.rules())
      if (allowed.contains(number(rule.get("id")))) result.add(rule);
    return result;
  }

  private void restrictChildGroupRules(long id, Set<Long> parentRules) {
    Set<Long> descendants = descendantGroupIds(id, allGroups());
    descendants.remove(id);
    for (Map<String, Object> group : rbac.groupRules()) {
      long groupId = number(group.get("id"));
      if (!descendants.contains(groupId)) continue;
      Set<Long> childRules = ids(group.get("rules"));
      childRules.retainAll(parentRules);
      rbac.updateGroupRules(groupId, join(childRules), now());
    }
  }

  private Set<Long> descendantRuleIds(long root, List<Map<String, Object>> rules) {
    Set<Long> result = new LinkedHashSet<>();
    boolean exists = false;
    for (Map<String, Object> rule : rules)
      if (number(rule.get("id")) == root) exists = true;
    if (!exists) return result;
    result.add(root);
    boolean changed;
    do {
      changed = false;
      for (Map<String, Object> rule : rules) {
        long id = number(rule.get("id"));
        if (result.contains(number(rule.get("pid"))) && result.add(id)) changed = true;
      }
    } while (changed);
    return result;
  }

  private void replaceGroups(long adminId, Set<Long> groups) {
    rbac.deleteAdminGroups(adminId);
    for (Long group : groups) rbac.insertAdminGroup(adminId, group);
  }

  private String validateRules(Set<Long> rules) {
    for (Long id : rules) if (rbac.ruleExists(id) == 0) return "权限规则不存在：" + id;
    return null;
  }

  private void removeRulesFromGroups(Set<Long> removed) {
    for (Map<String, Object> group : rbac.groupRules()) {
      String raw = String.valueOf(group.get("rules"));
      if ("*".equals(raw)) continue;
      Set<Long> rules = ids(raw);
      if (rules.removeAll(removed))
        rbac.updateGroupRules(number(group.get("id")), join(rules), now());
    }
  }

  private List<Map<String, Object>> tree(List<Map<String, Object>> rows, long parent) {
    List<Map<String, Object>> result = new ArrayList<>();
    List<Map<String, Object>> sorted = new ArrayList<>(rows);
    Set<Long> rowIds = new LinkedHashSet<>();
    for (Map<String, Object> row : sorted) rowIds.add(number(row.get("id")));
    sorted.sort(
        Comparator.comparingLong((Map<String, Object> row) -> number(row.get("pid")))
            .thenComparing(
                (left, right) -> Long.compare(number(right.get("weigh")), number(left.get("weigh"))))
            .thenComparingLong(row -> number(row.get("id"))));
    for (Map<String, Object> row : sorted) {
      long pid = number(row.get("pid"));
      boolean rootWithHiddenParent = parent == 0 && pid != 0 && !rowIds.contains(pid);
      if (pid != parent && !rootWithHiddenParent) continue;
      Map<String, Object> node = new LinkedHashMap<>(row);
      node.put("children", tree(sorted, number(row.get("id"))));
      result.add(node);
    }
    return result;
  }

  private String currentRuleText(long id, String field) {
    for (Map<String, Object> rule : rbac.rules())
      if (number(rule.get("id")) == id) return String.valueOf(rule.get(field));
    return "";
  }

  private long currentRuleNumber(long id, String field) {
    for (Map<String, Object> rule : rbac.rules())
      if (number(rule.get("id")) == id) return number(rule.get(field));
    return 0;
  }

  private Long requireSuper(String token) {
    Long id = adminId(token);
    return id != null && id == 1 ? id : null;
  }

  private BusinessResponse<?> superAdminRequired(String token) {
    return adminId(token) == null
        ? unauthorized()
        : BusinessResponse.error("仅超级管理员可以管理后台权限");
  }

  private Long adminId(String token) {
    return sessions.adminId(token);
  }

  private void log(long adminId, String title, String content) {
    adminLogs.insertLog(adminId, "/api/admin/business/rbac", title, content, now());
  }

  private String random(int length) {
    final String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    StringBuilder value = new StringBuilder();
    for (int i = 0; i < length; i++) value.append(chars.charAt(random.nextInt(chars.length())));
    return value.toString();
  }

  private static Set<Long> ids(Object value) {
    Set<Long> result = new LinkedHashSet<>();
    if (value == null) return result;
    Iterable<?> values =
        value instanceof Iterable
            ? (Iterable<?>) value
            : Arrays.asList(String.valueOf(value).split(","));
    for (Object item : values) {
      try {
        long id = Long.parseLong(String.valueOf(item).trim());
        if (id > 0) result.add(id);
      } catch (NumberFormatException ignored) {
        // Ignore empty and malformed IDs as the legacy comma-list form did.
      }
    }
    return result;
  }

  private static String join(Set<Long> values) {
    List<String> strings = new ArrayList<>();
    for (Long value : values) strings.add(String.valueOf(value));
    return String.join(",", strings);
  }

  private static String requestedStatus(Map<String, Object> body) {
    if (body == null || !body.containsKey("status")) return "normal";
    String state = value(body, "status");
    return "normal".equals(state) || "hidden".equals(state) ? state : null;
  }

  private static long longValue(Map<String, Object> body, String field, long fallback) {
    try {
      return body == null || body.get(field) == null
          ? fallback
          : Long.parseLong(String.valueOf(body.get(field)));
    } catch (NumberFormatException ignored) {
      return fallback;
    }
  }

  private static long number(Object value) {
    if (value instanceof Number) return ((Number) value).longValue();
    try {
      return value == null ? 0 : Long.parseLong(String.valueOf(value));
    } catch (NumberFormatException ignored) {
      return 0;
    }
  }

  private static String value(Map<String, Object> body, String field) {
    Object value = body == null ? null : body.get(field);
    return value == null ? "" : String.valueOf(value).trim();
  }

  private static String trim(String value) {
    return value == null ? "" : value.trim();
  }

  private static String hash(String password, String salt) {
    String first = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));
    return DigestUtils.md5DigestAsHex((first + salt).getBytes(StandardCharsets.UTF_8));
  }

  private static long now() {
    return System.currentTimeMillis() / 1000;
  }

  private static BusinessResponse<?> unauthorized() {
    return BusinessResponse.error("管理员未登录");
  }

  private static Map<String, Object> page(
      List<Map<String, Object>> rows, int total, int current, int size) {
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("total", total);
    result.put("per_page", size);
    result.put("current_page", current);
    result.put("last_page", Math.max(1, (total + size - 1) / size));
    result.put("data", rows);
    return result;
  }

  private static final class Scope {
    private final Set<Long> ownGroupIds;
    private final Set<Long> groupIds;
    private final Set<Long> assignableGroupIds;
    private final Set<Long> visibleAdminIds;

    private Scope(
        Set<Long> ownGroupIds,
        Set<Long> groupIds,
        Set<Long> assignableGroupIds,
        Set<Long> visibleAdminIds) {
      this.ownGroupIds = ownGroupIds;
      this.groupIds = groupIds;
      this.assignableGroupIds = assignableGroupIds;
      this.visibleAdminIds = visibleAdminIds;
    }
  }
}
