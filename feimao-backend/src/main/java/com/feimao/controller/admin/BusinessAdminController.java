package com.feimao.controller.admin;

import com.feimao.common.BusinessResponse;
import com.feimao.mapper.AdminLogMapper;
import com.feimao.mapper.BusinessAdminMapper;
import com.feimao.service.AdminSessionService;
import com.feimao.service.ImageCaptchaService;
import com.feimao.service.MiniProgramCodeService;
import com.feimao.service.WechatTransferService;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/business")
@CrossOrigin
public class BusinessAdminController {
  private final BusinessAdminMapper businessAdmin;
  private final AdminLogMapper adminLogs;
  private final AdminSessionService sessions;
  private final MiniProgramCodeService miniProgramCodes;
  private final ImageCaptchaService captchas;
  private final boolean captchaEnabled;
  private final WechatTransferService transfers;
  private final SecureRandom random = new SecureRandom();

  public BusinessAdminController(
      BusinessAdminMapper businessAdmin,
      AdminLogMapper adminLogs,
      AdminSessionService sessions,
      MiniProgramCodeService miniProgramCodes,
      WechatTransferService transfers,
      ImageCaptchaService captchas,
      @org.springframework.beans.factory.annotation.Value("${business.admin.captcha-enabled:false}")
          boolean captchaEnabled) {
    this.businessAdmin = businessAdmin;
    this.adminLogs = adminLogs;
    this.sessions = sessions;
    this.miniProgramCodes = miniProgramCodes;
    this.transfers = transfers;
    this.captchas = captchas;
    this.captchaEnabled = captchaEnabled;
  }

  @PostMapping("/login")
  public BusinessResponse<?> login(@RequestBody(required = false) Map<String, Object> b) {
    String username = text(b, "username"), password = text(b, "password");
    if (username.isEmpty() || password.isEmpty()) return BusinessResponse.error("请输入账号密码");
    if (captchaEnabled && !captchas.verify(text(b, "captcha_id"), text(b, "captcha")))
      return BusinessResponse.error("验证码不正确");
    Map<String, Object> a = businessAdmin.loginAdmin(username);
    if (a == null || a.isEmpty()) return BusinessResponse.error("管理员不存在");
    if (!"normal".equals(a.get("status"))) return BusinessResponse.error("管理员已禁用");
    if (!text(a, "password").equals(hash(password, text(a, "salt")))) {
      businessAdmin.loginFailed(longOf(a.get("id")));
      return BusinessResponse.error("密码错误");
    }
    long now = now(), id = longOf(a.get("id"));
    String token = sessions.issue(id);
    businessAdmin.loginSucceeded(id, token, now);
    Map<String, Object> info = new LinkedHashMap<>();
    for (String k : Arrays.asList("id", "username", "nickname", "avatar")) info.put(k, a.get(k));
    info.put("token", token);
    return BusinessResponse.success(info);
  }

  @PostMapping("/logout")
  public BusinessResponse<?> logout(@RequestHeader(value = "token", required = false) String token) {
    sessions.revoke(token);
    return BusinessResponse.success(null);
  }

  @GetMapping("/dashboard")
  public BusinessResponse<?> dashboard(
      @RequestHeader(value = "token", required = false) String token) {
    if (admin(token) == null) return unauthorized();
    long now = now();
    ZoneId zone = ZoneId.of("Asia/Shanghai");
    long today = LocalDate.now(zone).atStartOfDay(zone).toEpochSecond();
    Map<String, Object> d =
        new LinkedHashMap<>(
            businessAdmin.dashboard(
                today, now - 3 * 86400, now - 7 * 86400, now - 30 * 86400));
    for (String key :
        Arrays.asList(
            "users",
            "orders",
            "pending_merchants",
            "pending_shops",
            "pending_merchant_cash",
            "pending_shop_cash",
            "pending_auth",
            "pending_staff_auth",
            "totaluser",
            "totaladmin",
            "totalcategory",
            "dbtablenums",
            "todayusersignup",
            "todayuserlogin",
            "sevendau",
            "thirtydau",
            "threednu",
            "sevendnu",
            "attachmentnums",
            "picturenums"))
      if (d.get(key) instanceof Number) d.put(key, ((Number) d.get(key)).intValue());
    List<String> labels = new ArrayList<>();
    List<Integer> signups = new ArrayList<>();
    DateTimeFormatter dayFormat = DateTimeFormatter.ISO_LOCAL_DATE;
    for (int offset = 6; offset >= 0; offset--) {
      LocalDate day = LocalDate.now(zone).minusDays(offset);
      long start = day.atStartOfDay(zone).toEpochSecond();
      long end = day.plusDays(1).atStartOfDay(zone).toEpochSecond();
      labels.add(day.format(dayFormat));
      signups.add(businessAdmin.signupCount(start, end));
    }
    d.put("signup_labels", labels);
    d.put("signup_counts", signups);
    return BusinessResponse.success(d);
  }

  @GetMapping("/applications/{role}")
  public BusinessResponse<?> applications(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable String role,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int limit,
      @RequestParam(required = false) Integer status) {
    if (admin(token) == null) return unauthorized();
    if (!role.equals("merchant") && !role.equals("shop")) return BusinessResponse.error("类型错误");
    List<Map<String, Object>> rows =
        businessAdmin.applications(role, status, limit, (page - 1) * limit);
    for (Map<String, Object> row : rows) removeSensitive(row);
    return BusinessResponse.success(
        page(rows, businessAdmin.applicationCount(role, status), page, limit));
  }

  @PostMapping("/applications/{role}/examine")
  @Transactional
  public BusinessResponse<?> examineApplication(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable String role,
      @RequestBody(required = false) Map<String, Object> b) {
    Long admin = admin(token);
    if (admin == null) return unauthorized();
    if (!role.equals("merchant") && !role.equals("shop")) return BusinessResponse.error("类型错误");
    long id = longValue(b, "id", 0);
    int type = intValue(b, "type", 0);
    if (id <= 0 || (type != 2 && type != 3)) return BusinessResponse.error("参数错误");
    Map<String, Object> row = businessAdmin.lockApplication(role, id);
    if (row == null || row.isEmpty()) return BusinessResponse.error("申请不存在");
    if (intOf(row.get("examine_status")) != 1) return BusinessResponse.error("申请已审核");
    long now = now();
    if (type == 2) {
      String username = text(b, "username"), password = text(b, "password");
      if (username.isEmpty() || password.isEmpty()) return BusinessResponse.error("通过审核时必须设置登录账号和密码");
      List<String> settingColumns =
          role.equals("merchant")
              ? Arrays.asList(
                  "commission_normal_rate",
                  "commission_merchant_rate",
                  "commission_shop_rate",
                  "service_distance1",
                  "service_distance2",
                  "service_distance3",
                  "service_distance4",
                  "service_money1",
                  "service_money2",
                  "service_money3",
                  "service_money4",
                  "service_money5")
              : Collections.singletonList("commission_shop_rate");
      Map<String, Integer> settings = new LinkedHashMap<>();
      for (String column : settingColumns) {
        if (b == null || !b.containsKey(column)) return BusinessResponse.error("请填写完整的佣金和服务费配置");
        int value = intValue(b, column, -1);
        if (value < 0) return BusinessResponse.error("佣金和服务费配置不能小于0");
        settings.put(column, value);
      }
      if (role.equals("merchant")
          && !(settings.get("service_distance1") < settings.get("service_distance2")
              && settings.get("service_distance2") < settings.get("service_distance3")
              && settings.get("service_distance3") < settings.get("service_distance4")))
        return BusinessResponse.error("服务距离必须逐级递增");
      if (businessAdmin.applicationUsernameCount(role, username, id) > 0)
        return BusinessResponse.error("登录账号已存在");
      String salt = random(6);
      if ("merchant".equals(role))
        businessAdmin.approveMerchant(
            id, username, salt, hash(password, salt), settings, admin, now);
      else
        businessAdmin.approveShop(
            id, username, salt, hash(password, salt), settings, admin, now);
      businessAdmin.updateApplicationUser(role, longOf(row.get("user_id")), 3, now);
      generateCodeAfterCommit(role, id);
    } else {
      String note = text(b, "note");
      businessAdmin.rejectApplication(role, id, note, admin, now);
      businessAdmin.updateApplicationUser(role, longOf(row.get("user_id")), 4, now);
    }
    log(admin, "审核" + (role.equals("merchant") ? "门店" : "酒店") + "申请", String.valueOf(id));
    return BusinessResponse.success(1);
  }

  private void generateCodeAfterCommit(String role, long id) {
    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronization() {
          @Override
          public void afterCommit() {
            miniProgramCodes.generateAsync(role, id);
          }
        });
  }

  @GetMapping("/user-auth/{kind}")
  public BusinessResponse<?> authList(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable String kind,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int limit) {
    if (admin(token) == null) return unauthorized();
    if (!kind.equals("identity") && !kind.equals("staff")) return BusinessResponse.error("类型错误");
    page = Math.max(page, 1);
    limit = Math.min(Math.max(limit, 1), 100);
    List<Map<String, Object>> rows =
        businessAdmin.authApplications(kind, limit, (page - 1) * limit);
    return BusinessResponse.success(
        page(rows, businessAdmin.authApplicationCount(kind), page, limit));
  }

  @PostMapping("/user-auth/{kind}/examine")
  @Transactional
  public BusinessResponse<?> examineAuth(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable String kind,
      @RequestBody(required = false) Map<String, Object> b) {
    Long admin = admin(token);
    if (admin == null) return unauthorized();
    if (!kind.equals("identity") && !kind.equals("staff")) return BusinessResponse.error("类型错误");
    long id = longValue(b, "id", 0);
    int approved = intValue(b, "approved", 0);
    int target = approved == 1 ? 3 : 4;
    int changed = businessAdmin.examineAuth(kind, id, target, now());
    if (changed == 0) return BusinessResponse.error("认证记录不存在或已审核");
    log(admin, "审核用户" + (kind.equals("identity") ? "实名" : "员工") + "认证", String.valueOf(id));
    return BusinessResponse.success(1);
  }

  @GetMapping("/cash/{role}")
  public BusinessResponse<?> cashList(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable String role,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int limit,
      @RequestParam(required = false) Integer status,
      @RequestParam(required = false) String keyword) {
    if (admin(token) == null) return unauthorized();
    boolean merchant = role.equals("merchant");
    if (!merchant && !role.equals("shop")) return BusinessResponse.error("类型错误");
    String search = keyword == null ? "" : keyword.trim();
    int total = businessAdmin.cashCount(role, status, search);
    BigDecimal paid = businessAdmin.paidCash(role, status, search);
    List<Map<String, Object>> rows =
        businessAdmin.cashRows(role, status, search, limit, (page - 1) * limit);
    Map<String, Object> result = page(rows, total, page, limit);
    result.put("extend", java.util.Collections.singletonMap("money", paid));
    return BusinessResponse.success(result);
  }

  public BusinessResponse<?> cashList(String token, String role, int page, int limit) {
    return cashList(token, role, page, limit, null, null);
  }

  @PostMapping("/cash/{role}")
  @Transactional
  public BusinessResponse<?> addCash(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable String role,
      @RequestBody(required = false) Map<String, Object> body) {
    Long admin = admin(token);
    if (admin == null) return unauthorized();
    if (!role.equals("merchant") && !role.equals("shop")) return BusinessResponse.error("类型错误");
    long ownerId = longValue(body, role + "_id", 0);
    BigDecimal amount = money(body == null ? null : body.get("amount"));
    int status = intValue(body, "status", 1);
    if (businessAdmin.cashOwnerExists(role, ownerId) == 0) return BusinessResponse.error("申请方不存在");
    if (amount.signum() < 0 || amount.compareTo(new BigDecimal("99999999.99")) > 0)
      return BusinessResponse.error("提现金额不正确");
    if (status < 1 || status > 3) return BusinessResponse.error("提现状态不正确");
    long now = now();
    businessAdmin.insertCash(
        role,
        text(body, "out_trade_no"),
        ownerId,
        amount,
        status,
        text(body, "note"),
        now,
        longValue(body, "pay_time", now),
        text(body, "pay_number"),
        money(body == null ? null : body.get("pay_money")));
    log(admin, "新增" + (role.equals("merchant") ? "门店" : "酒店") + "提现记录", String.valueOf(ownerId));
    return BusinessResponse.success(1);
  }

  @GetMapping("/cash/{role}/{id}")
  public BusinessResponse<?> cashDetail(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable String role,
      @PathVariable long id) {
    if (admin(token) == null) return unauthorized();
    boolean merchant = role.equals("merchant");
    if (!merchant && !role.equals("shop")) return BusinessResponse.error("类型错误");
    Map<String, Object> row = businessAdmin.cashDetail(role, id);
    return row == null || row.isEmpty()
        ? BusinessResponse.error("提现记录不存在")
        : BusinessResponse.success(row);
  }

  @DeleteMapping("/cash/{role}/{id}")
  @Transactional
  public BusinessResponse<?> deleteCash(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable String role,
      @PathVariable long id) {
    Long admin = admin(token);
    if (admin == null) return unauthorized();
    boolean merchant = role.equals("merchant");
    if (!merchant && !role.equals("shop")) return BusinessResponse.error("类型错误");
    if (businessAdmin.deleteCash(role, id, now()) == 0) return BusinessResponse.error("提现记录不存在");
    log(admin, "删除" + (merchant ? "门店" : "酒店") + "提现记录", String.valueOf(id));
    return BusinessResponse.success(1);
  }

  @DeleteMapping("/cash/{role}")
  @Transactional
  public BusinessResponse<?> deleteCashRows(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable String role,
      @RequestBody(required = false) Map<String, Object> body) {
    Long admin = admin(token);
    if (admin == null) return unauthorized();
    boolean merchant = role.equals("merchant");
    if (!merchant && !role.equals("shop")) return BusinessResponse.error("类型错误");
    List<Long> ids = idList(body == null ? null : body.get("ids"));
    if (ids.isEmpty()) return BusinessResponse.error("请选择要删除的提现记录");
    int changed = businessAdmin.deleteCashRows(role, ids, now());
    if (changed == 0) return BusinessResponse.error("提现记录不存在");
    log(admin, "批量删除" + (merchant ? "门店" : "酒店") + "提现记录", ids.toString());
    return BusinessResponse.success(changed);
  }

  @GetMapping("/cash/{role}/recycle")
  public BusinessResponse<?> recycledCashRows(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable String role,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int limit) {
    if (admin(token) == null) return unauthorized();
    if (!role.equals("merchant") && !role.equals("shop"))
      return BusinessResponse.error("类型错误");
    page = Math.max(page, 1);
    limit = Math.min(Math.max(limit, 1), 100);
    return BusinessResponse.success(
        page(
            businessAdmin.recycledCashRows(role, limit, (page - 1) * limit),
            businessAdmin.recycledCashCount(role),
            page,
            limit));
  }

  @PostMapping("/cash/{role}/restore/{id}")
  @Transactional
  public BusinessResponse<?> restoreCash(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable String role,
      @PathVariable long id) {
    Long admin = admin(token);
    if (admin == null) return unauthorized();
    boolean merchant = role.equals("merchant");
    if (!merchant && !role.equals("shop")) return BusinessResponse.error("类型错误");
    if (businessAdmin.restoreCash(role, id, now()) != 1)
      return BusinessResponse.error("回收提现记录不存在");
    log(admin, "恢复" + (merchant ? "门店" : "酒店") + "提现记录", String.valueOf(id));
    return BusinessResponse.success(1);
  }

  @DeleteMapping("/cash/{role}/destroy/{id}")
  @Transactional
  public BusinessResponse<?> destroyCash(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable String role,
      @PathVariable long id) {
    Long admin = admin(token);
    if (admin == null) return unauthorized();
    boolean merchant = role.equals("merchant");
    if (!merchant && !role.equals("shop")) return BusinessResponse.error("类型错误");
    if (businessAdmin.destroyCash(role, id) != 1)
      return BusinessResponse.error("回收提现记录不存在");
    log(admin, "彻底删除" + (merchant ? "门店" : "酒店") + "提现记录", String.valueOf(id));
    return BusinessResponse.success(1);
  }

  @PostMapping("/cash/{role}/restore")
  @Transactional
  public BusinessResponse<?> restoreCashRows(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable String role,
      @RequestBody(required = false) Map<String, Object> body) {
    Long admin = admin(token);
    if (admin == null) return unauthorized();
    boolean merchant = role.equals("merchant");
    if (!merchant && !role.equals("shop")) return BusinessResponse.error("类型错误");
    List<Long> ids = idList(body == null ? null : body.get("ids"));
    int changed = businessAdmin.restoreCashRows(role, ids, now());
    if (changed == 0) return BusinessResponse.error("回收站中没有可恢复的提现记录");
    log(
        admin,
        "批量恢复" + (merchant ? "门店" : "酒店") + "提现记录",
        ids.isEmpty() ? "全部" : ids.toString());
    return BusinessResponse.success(changed);
  }

  @DeleteMapping("/cash/{role}/destroy")
  @Transactional
  public BusinessResponse<?> destroyCashRows(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable String role,
      @RequestBody(required = false) Map<String, Object> body) {
    Long admin = admin(token);
    if (admin == null) return unauthorized();
    boolean merchant = role.equals("merchant");
    if (!merchant && !role.equals("shop")) return BusinessResponse.error("类型错误");
    List<Long> ids = idList(body == null ? null : body.get("ids"));
    int changed = businessAdmin.destroyCashRows(role, ids);
    if (changed == 0) return BusinessResponse.error("回收站中没有可彻底删除的提现记录");
    log(
        admin,
        "批量彻底删除" + (merchant ? "门店" : "酒店") + "提现记录",
        ids.isEmpty() ? "全部" : ids.toString());
    return BusinessResponse.success(changed);
  }

  @PostMapping("/cash/{role}/reject")
  @Transactional
  public BusinessResponse<?> rejectCash(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable String role,
      @RequestBody(required = false) Map<String, Object> b) {
    Long admin = admin(token);
    if (admin == null) return unauthorized();
    boolean merchant = role.equals("merchant");
    if (!merchant && !role.equals("shop")) return BusinessResponse.error("类型错误");
    String idField = merchant ? "merchant_id" : "shop_id";
    long id = longValue(b, "id", 0), now = now();
    Map<String, Object> c = businessAdmin.lockCash(role, id);
    if (c == null || c.isEmpty()) return BusinessResponse.error("提现记录不存在或已审核");
    long ownerId = longOf(c.get(idField));
    Map<String, Object> o = businessAdmin.lockOwnerMoney(role, ownerId);
    BigDecimal amount = money(c.get("amount")),
        before = money(o.get("money")),
        after = before.add(amount);
    businessAdmin.rejectCash(role, id, text(b, "note"), admin, now);
    businessAdmin.updateOwnerMoney(role, ownerId, after, now);
    businessAdmin.insertRefundLog(
        role,
        ownerId,
        amount,
        before,
        after,
        (merchant ? "门店" : "酒店") + "提现审核拒绝返还",
        now,
        id);
    log(admin, "拒绝" + (merchant ? "门店" : "酒店") + "提现", String.valueOf(id));
    return BusinessResponse.success(1);
  }

  @PostMapping("/cash/{role}/approve")
  @Transactional
  public BusinessResponse<?> approveCash(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable String role,
      @RequestBody(required = false) Map<String, Object> body) {
    Long admin = admin(token);
    if (admin == null) return unauthorized();
    boolean merchant = role.equals("merchant");
    if (!merchant && !role.equals("shop")) return BusinessResponse.error("类型错误");
    String idField = merchant ? "merchant_id" : "shop_id";
    long id = longValue(body, "id", 0);
    Map<String, Object> cashRow = businessAdmin.lockCashForTransfer(role, id);
    if (cashRow == null || cashRow.isEmpty()) return BusinessResponse.error("提现记录不存在或已审核");
    BigDecimal amount = money(cashRow.get("amount"));
    String batchId;
    try {
      batchId =
          transfers.transfer(
              text(cashRow, "openid"),
              text(cashRow, "out_trade_no"),
              amount.multiply(BigDecimal.valueOf(100)).longValueExact(),
              merchant ? "门店提现" : "酒店提现");
    } catch (ArithmeticException | IllegalArgumentException | IllegalStateException exception) {
      return BusinessResponse.error(exception.getMessage());
    }
    long current = now();
    int changed =
        businessAdmin.approveCash(
            role, id, batchId, amount, admin, text(body, "note"), current);
    if (changed == 0) throw new IllegalStateException("提现状态已变化");
    businessAdmin.insertPlatformWithdrawal(
        longOf(cashRow.get(idField)),
        amount,
        merchant ? 3 : 4,
        (merchant ? "门店" : "酒店") + "佣金提现",
        id,
        current,
        merchant ? 2 : 3);
    log(admin, "通过" + (merchant ? "门店" : "酒店") + "提现", String.valueOf(id));
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("batch_id", batchId);
    result.put("amount", amount);
    return BusinessResponse.success(result);
  }

  private Long admin(String token) {
    return sessions.adminId(token);
  }

  private static BusinessResponse<?> unauthorized() {
    return BusinessResponse.error("管理员未登录");
  }

  private void log(long admin, String title, String content) {
    adminLogs.insertLog(admin, "/api/admin/business", title, content, now());
  }

  private static String hash(String password, String salt) {
    String first = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));
    return DigestUtils.md5DigestAsHex((first + salt).getBytes(StandardCharsets.UTF_8));
  }

  private String random(int n) {
    String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789", s = "";
    for (int i = 0; i < n; i++) s += chars.charAt(random.nextInt(chars.length()));
    return s;
  }

  private static long now() {
    return System.currentTimeMillis() / 1000;
  }

  private static void removeSensitive(Map<String, Object> row) {
    if (row == null) return;
    for (String key : Arrays.asList("password", "salt", "token", "openid")) row.remove(key);
  }

  private static long longOf(Object v) {
    return ((Number) v).longValue();
  }

  private static long longValue(Map<String, Object> b, String k, long d) {
    try {
      return b == null || b.get(k) == null ? d : Long.parseLong(b.get(k).toString());
    } catch (Exception e) {
      return d;
    }
  }

  private static int intValue(Map<String, Object> b, String k, int d) {
    try {
      return b == null || b.get(k) == null ? d : Integer.parseInt(b.get(k).toString());
    } catch (Exception e) {
      return d;
    }
  }

  private static int intOf(Object v) {
    return v == null ? 0 : ((Number) v).intValue();
  }

  private static String text(Map<String, Object> b, String k) {
    return b == null || b.get(k) == null ? "" : b.get(k).toString();
  }

  private static BigDecimal money(Object v) {
    return v == null ? BigDecimal.ZERO : new BigDecimal(v.toString());
  }

  private static List<Long> idList(Object value) {
    LinkedHashSet<Long> ids = new LinkedHashSet<>();
    if (value instanceof Collection) {
      for (Object item : (Collection<?>) value) addId(ids, item);
    } else if (value != null) {
      for (String item : value.toString().split(",")) addId(ids, item);
    }
    return new ArrayList<>(ids);
  }

  private static void addId(Set<Long> ids, Object value) {
    try {
      long id = Long.parseLong(String.valueOf(value).trim());
      if (id > 0) ids.add(id);
    } catch (RuntimeException ignored) {
      // 旧批量操作会忽略无法转换为主键的值。
    }
  }

  private static Map<String, Object> page(List<Map<String, Object>> r, int t, int c, int s) {
    Map<String, Object> p = new LinkedHashMap<>();
    p.put("total", t);
    p.put("per_page", s);
    p.put("current_page", c);
    p.put("last_page", Math.max(1, (t + s - 1) / s));
    p.put("data", r);
    return p;
  }
}
