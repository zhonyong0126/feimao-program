package com.feimao.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.feimao.common.BusinessResponse;
import com.feimao.entity.BusinessUser;
import com.feimao.mapper.AdminLogMapper;
import com.feimao.mapper.UserAdminMapper;
import com.feimao.service.AdminSessionService;
import com.feimao.service.BusinessUserService;
import com.feimao.service.UserTokenService;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

/** Administrative member management migrated from FastAdmin user/user. */
@RestController
@RequestMapping("/api/admin/business/users")
public class UserAdminController {
  private static final List<String> EDITABLE =
      Arrays.asList(
          "avatar",
          "nickname",
          "mobile",
          "jointime",
          "status");

  private final UserAdminMapper userAdmin;
  private final BusinessUserService users;
  private final UserTokenService tokens;
  private final AdminLogMapper logs;
  private final AdminSessionService sessions;
  private final SecureRandom random = new SecureRandom();

  public UserAdminController(UserAdminMapper userAdmin, BusinessUserService users,
      UserTokenService tokens, AdminLogMapper logs, AdminSessionService sessions) {
    this.userAdmin = userAdmin;
    this.users = users;
    this.tokens = tokens;
    this.logs = logs;
    this.sessions = sessions;
  }

  @GetMapping
  public BusinessResponse<?> list(
      @RequestHeader(value = "token", required = false) String token,
      @RequestParam(required = false) Long id,
      @RequestParam(required = false) String nickname,
      @RequestParam(required = false) String mobile,
      @RequestParam(required = false) Long jointimeFrom,
      @RequestParam(required = false) Long jointimeTo,
      @RequestParam(required = false) String status,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int limit) {
    if (admin(token) == null) return unauthorized();
    page = Math.max(page, 1);
    limit = Math.min(Math.max(limit, 1), 100);
    String nicknameSearch = nickname == null ? "" : nickname.trim();
    String mobileSearch = mobile == null ? "" : mobile.trim();
    String state = status == null ? "" : status.trim();
    if (!state.isEmpty() && !"normal".equals(state) && !"hidden".equals(state))
      return BusinessResponse.error("状态参数错误");
    if (jointimeFrom != null && jointimeTo != null && jointimeFrom > jointimeTo)
      return BusinessResponse.error("注册时间范围错误");
    int total =
        userAdmin.countRows(id, nicknameSearch, mobileSearch, jointimeFrom, jointimeTo, state);
    List<Map<String, Object>> rows =
        userAdmin.selectRows(
            id,
            nicknameSearch,
            mobileSearch,
            jointimeFrom,
            jointimeTo,
            state,
            limit,
            (page - 1) * limit);
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("rows", rows);
    result.put("total", total);
    result.put("page", page);
    result.put("limit", limit);
    return BusinessResponse.success(result);
  }

  @PostMapping
  public BusinessResponse<?> add(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long admin = admin(token);
    if (admin == null) return unauthorized();
    String username = text(body, "username");
    String nickname = text(body, "nickname");
    String mobile = text(body, "mobile");
    String email = text(body, "email");
    String duplicate = duplicate(username, nickname, mobile, email, 0);
    if (duplicate != null) return BusinessResponse.error(duplicate);
    BigDecimal money = body != null && body.containsKey("money") ? decimal(body.get("money")) : BigDecimal.ZERO;
    Integer score = body != null && body.containsKey("score") ? nonNegativeInteger(body.get("score")) : 0;
    if (money == null) return BusinessResponse.error("余额格式错误");
    if (score == null) return BusinessResponse.error("积分格式错误");
    String password = text(body, "password"), salt = password.isEmpty() ? "" : random(10);
    long now = now();
    BusinessUser user = new BusinessUser();
    user.setGroupId((long) integer(body, "group_id", 0));
    user.setUsername(username);
    user.setNickname(nickname);
    user.setPassword(password.isEmpty() ? "" : hash(password, salt));
    user.setSalt(salt);
    user.setEmail(email);
    user.setMobile(mobile);
    user.setAvatar(text(body, "avatar"));
    user.setLevel(integer(body, "level", 1));
    user.setGender(integer(body, "gender", 0));
    String birthday = text(body, "birthday");
    if (!birthday.isEmpty()) {
      try {
        user.setBirthday(LocalDate.parse(birthday));
      } catch (DateTimeParseException ignored) {
        return BusinessResponse.error("生日格式错误");
      }
    }
    user.setBio(text(body, "bio"));
    user.setMoney(money);
    user.setScore(score);
    Long joinTime = body != null && body.containsKey("jointime") ? epoch(body.get("jointime")) : now;
    if (joinTime == null) return BusinessResponse.error("注册时间格式错误");
    user.setJointime(joinTime);
    user.setCreatetime(now);
    user.setUpdatetime(now);
    user.setStatus(status(body));
    user.setMerchantStatus(1);
    user.setMerchantId(0L);
    user.setShopStatus(1);
    user.setShopId(0L);
    users.save(user);
    log(admin, "新增会员", String.valueOf(user.getId()));
    return BusinessResponse.success(java.util.Collections.singletonMap("id", user.getId()));
  }

  @PutMapping("/{id}")
  @org.springframework.transaction.annotation.Transactional
  public BusinessResponse<?> edit(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable long id,
      @RequestBody(required = false) Map<String, Object> body) {
    Long admin = admin(token);
    if (admin == null) return unauthorized();
    Map<String, Object> original = userAdmin.lockUser(id);
    if (original == null || original.isEmpty()) return BusinessResponse.error("会员不存在");
    String duplicate =
        duplicate("", text(body, "nickname"), text(body, "mobile"), "", id);
    if (duplicate != null) return BusinessResponse.error(duplicate);
    Map<String, Object> fields = new LinkedHashMap<>();
    for (String field : EDITABLE) {
      if (body != null && body.containsKey(field)) fields.put(field, body.get(field));
    }
    if (fields.containsKey("nickname")) {
      String nickname = text(body, "nickname");
      if (nickname.isEmpty()) return BusinessResponse.error("昵称不能为空");
      fields.put("nickname", nickname);
    }
    if (fields.containsKey("avatar")) fields.put("avatar", text(body, "avatar"));
    if (fields.containsKey("mobile")) fields.put("mobile", text(body, "mobile"));
    if (fields.containsKey("jointime")) {
      Long joinTime = epoch(fields.get("jointime"));
      if (joinTime == null) return BusinessResponse.error("注册时间格式错误");
      fields.put("jointime", joinTime);
    }
    if (fields.containsKey("status")) {
      String status = text(body, "status");
      if (!"normal".equals(status) && !"hidden".equals(status))
        return BusinessResponse.error("状态参数错误");
      fields.put("status", status);
    }
    if (fields.isEmpty()) return BusinessResponse.error("没有可修改内容");
    UpdateWrapper<BusinessUser> update = new UpdateWrapper<>();
    for (Map.Entry<String, Object> field : fields.entrySet()) {
      update.set(field.getKey(), field.getValue());
    }
    update.set("updatetime", now()).eq("id", id);
    users.update(update);
    log(admin, "编辑会员", String.valueOf(id));
    return BusinessResponse.success(1);
  }

  @PutMapping("/status")
  @org.springframework.transaction.annotation.Transactional
  public BusinessResponse<?> updateStatuses(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long admin = admin(token);
    if (admin == null) return unauthorized();
    List<Long> ids = idList(body == null ? null : body.get("ids"));
    if (ids.isEmpty()) return BusinessResponse.error("请选择要修改的会员");
    String status = text(body, "status");
    if (!"normal".equals(status) && !"hidden".equals(status))
      return BusinessResponse.error("状态参数错误");
    int changed = userAdmin.updateStatuses(ids, status, now());
    if (changed == 0) return BusinessResponse.error("会员不存在或状态未变化");
    log(admin, "批量修改会员状态", ids + ":" + status);
    return BusinessResponse.success(changed);
  }

  @DeleteMapping("/{id}")
  @org.springframework.transaction.annotation.Transactional
  public BusinessResponse<?> delete(
      @RequestHeader(value = "token", required = false) String token, @PathVariable long id) {
    Long admin = admin(token);
    if (admin == null) return unauthorized();
    if (users.getById(id) == null)
      return BusinessResponse.error("会员不存在");
    tokens.revokeUser(id);
    users.removeById(id);
    log(admin, "删除会员", String.valueOf(id));
    return BusinessResponse.success(1);
  }

  @DeleteMapping
  @org.springframework.transaction.annotation.Transactional
  public BusinessResponse<?> deleteMany(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long admin = admin(token);
    if (admin == null) return unauthorized();
    List<Long> ids = idList(body == null ? null : body.get("ids"));
    if (ids.isEmpty()) return BusinessResponse.error("请选择要删除的会员");
    for (Long id : ids) tokens.revokeUser(id);
    int changed = userAdmin.deleteRows(ids);
    if (changed == 0) return BusinessResponse.error("会员不存在");
    log(admin, "批量删除会员", ids.toString());
    return BusinessResponse.success(changed);
  }

  private String duplicate(
      String username, String nickname, String mobile, String email, long excludedId) {
    if (!username.isEmpty()
        && duplicateCount("username", username, excludedId) > 0) return "用户名已经被占用";
    if (!nickname.isEmpty()
        && duplicateCount("nickname", nickname, excludedId) > 0) return "昵称已经被占用";
    if (!mobile.isEmpty()
        && duplicateCount("mobile", mobile, excludedId) > 0)
      return "该手机号已经占用";
    if (!email.isEmpty()
        && duplicateCount("email", email, excludedId) > 0)
      return "邮箱已经被占用";
    return null;
  }

  private long duplicateCount(String field, String value, long excludedId) {
    return users.count(new QueryWrapper<BusinessUser>().eq(field, value).ne("id", excludedId));
  }

  private Long admin(String token) {
    return sessions.adminId(token);
  }

  private void log(long admin, String title, String content) {
    logs.insertLog(admin, "/api/admin/business/users", title, content, now());
  }

  private static String status(Map<String, Object> body) {
    String status = text(body, "status");
    return "hidden".equals(status) ? "hidden" : "normal";
  }

  private static int integer(Map<String, Object> body, String key, int fallback) {
    try {
      return Integer.parseInt(text(body, key));
    } catch (NumberFormatException ignored) {
      return fallback;
    }
  }

  private static BigDecimal decimal(Object value) {
    try {
      BigDecimal result = new BigDecimal(String.valueOf(value)).setScale(2);
      return result.signum() < 0 ? null : result;
    } catch (Exception ignored) {
      return null;
    }
  }

  private static Integer nonNegativeInteger(Object value) {
    try {
      int result = Integer.parseInt(String.valueOf(value));
      return result < 0 ? null : result;
    } catch (Exception ignored) {
      return null;
    }
  }

  private static Long epoch(Object value) {
    if (value == null) return null;
    String text = String.valueOf(value).trim();
    try {
      if (text.matches("^[0-9]+$")) return Long.parseLong(text);
      return LocalDateTime.parse(text).atZone(ZoneId.systemDefault()).toEpochSecond();
    } catch (Exception ignored) {
      return null;
    }
  }

  private static List<Long> idList(Object value) {
    List<Long> ids = new ArrayList<>();
    if (value instanceof Iterable) {
      for (Object item : (Iterable<?>) value) addId(ids, item);
    } else if (value != null) {
      for (String item : String.valueOf(value).split(",")) addId(ids, item);
    }
    return ids;
  }

  private static void addId(List<Long> ids, Object value) {
    try {
      long id = Long.parseLong(String.valueOf(value).trim());
      if (id > 0 && !ids.contains(id)) ids.add(id);
    } catch (NumberFormatException ignored) {
      // FastAdmin's batch action ignores values that cannot be converted to a primary key.
    }
  }

  private static String text(Map<String, Object> body, String key) {
    return body == null || body.get(key) == null ? "" : String.valueOf(body.get(key)).trim();
  }

  private String random(int length) {
    String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    StringBuilder value = new StringBuilder(length);
    for (int i = 0; i < length; i++) value.append(chars.charAt(random.nextInt(chars.length())));
    return value.toString();
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
}
