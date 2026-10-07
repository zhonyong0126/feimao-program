package com.feimao.controller.shop;

import com.feimao.common.BusinessResponse;
import com.feimao.mapper.ShopWorkspaceMapper;
import com.feimao.service.RoleSessionService;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shop")
@CrossOrigin
public class ShopController {
  private final ShopWorkspaceMapper shops;
  private final RoleSessionService sessions;
  private final SecureRandom random = new SecureRandom();

  public ShopController(ShopWorkspaceMapper shops, RoleSessionService sessions) {
    this.shops = shops;
    this.sessions = sessions;
  }

  @PostMapping("/login")
  public BusinessResponse<?> login(@RequestBody(required = false) Map<String, Object> b) {
    String username = inputText(b, "username"), password = inputText(b, "password");
    if (username.isEmpty() || password.isEmpty()) return BusinessResponse.error("请输入账号密码");
    Map<String, Object> s = shops.selectLogin(username);
    if (s == null || s.isEmpty()) return BusinessResponse.error("酒店账号不存在");
    if (intOf(s.get("status")) != 1) return BusinessResponse.error("酒店账号已禁用");
    if (!text(s, "password").equals(hash(password, text(s, "salt"))))
      return BusinessResponse.error("密码错误");
    String token = sessions.issue(longOf(s.get("id")), "shop");
    Map<String, Object> u = new LinkedHashMap<>();
    for (String k : Arrays.asList("id", "avatar", "name", "mobile")) u.put(k, s.get(k));
    u.put("token", token);
    u.put("user_id", s.get("id"));
    u.put("role_type", 3);
    return BusinessResponse.success(
        "Logged in successful", Collections.singletonMap("userInfo", u));
  }

  @PostMapping("/logout")
  public BusinessResponse<?> logout(@RequestHeader(value = "token", required = false) String token) {
    sessions.revoke(token);
    return BusinessResponse.success("退出成功", null);
  }

  @PostMapping("/edit")
  public BusinessResponse<?> edit(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> b) {
    Long id = shop(token);
    if (id == null) return unauthorized();
    int type = integer(b, "type", 0);
    if (type != 1 && type != 2) return BusinessResponse.error("请求错误");
    if (type == 1) {
      Map<String, Object> s = shops.selectProfile(id);
      s.put("image", split(text(s, "image")));
      return BusinessResponse.success("操作成功!", s);
    }
    Map<String, Object> fields = new LinkedHashMap<>();
    for (String k :
        Arrays.asList("name", "nickname", "mobile", "address", "avatar", "image", "intro"))
      if (b != null && b.containsKey(k)) fields.put(k, listText(b.get(k)));
    if (fields.isEmpty()) return BusinessResponse.error("没有可修改内容");
    return shops.updateProfile(id, fields, now()) > 0
        ? BusinessResponse.success("操作成功!", 1)
        : BusinessResponse.error("操作失败!", 0);
  }

  @PostMapping("/cashList")
  public BusinessResponse<?> cashList(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> b) {
    Long id = shop(token);
    if (id == null) return unauthorized();
    long page = positiveLong(b, "page", 1), size = positiveLong(b, "list_rows", 10);
    List<Map<String, Object>> rows =
        shops.selectCashRows(id, size, offset(page, size));
    times(rows);
    return BusinessResponse.success("操作成功!", page(rows, shops.cashCount(id), page, size));
  }

  @PostMapping("/cash")
  @Transactional
  public BusinessResponse<?> cash(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> b) {
    Long id = shop(token);
    if (id == null) return unauthorized();
    Object rawAmount = b == null ? null : b.get("amount");
    if (!numeric(rawAmount) || money(rawAmount).signum() == 0)
      return BusinessResponse.error("金额错误");
    BigDecimal amount = money(rawAmount);
    if (amount.compareTo(BigDecimal.ONE) < 0) return BusinessResponse.error("最低1元起提");
    if (amount.compareTo(BigDecimal.valueOf(500)) > 0) return BusinessResponse.error("最高500元");
    BigDecimal before = shops.lockBalance(id);
    if (amount.compareTo(before) > 0) return BusinessResponse.error("余额不足");
    BigDecimal after = before.subtract(amount);
    String number = uniqueCashNumber();
    long current = now();
    shops.insertCash(number, id, amount, current);
    Long cashId = shops.selectCashId(number);
    shops.updateBalance(id, after, current);
    shops.insertCashLog(id, amount, before, after, current, cashId);
    return BusinessResponse.success("操作成功!", null);
  }

  @PostMapping("/index")
  public BusinessResponse<?> index(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> b) {
    Long id = shop(token);
    if (id == null) return unauthorized();
    int type = integer(b, "type", 1);
    long current = positiveLong(b, "page", 1), size = positiveLong(b, "list_rows", 10);
    if (type != 1 && type != 2) return BusinessResponse.error("请求错误");
    Map<String, Object> s =
        shops.selectDashboard(id);
    List<Map<String, Object>> rows;
    int total;
    if (type == 1) {
      rows =
          shops.selectOrders(id, size, offset(current, size));
      total = shops.orderCount(id);
    } else {
      rows =
          shops.selectIncome(id, size, offset(current, size));
      total = shops.incomeCount(id);
    }
    times(rows);
    Map<String, Object> user = new LinkedHashMap<>(s);
    user.remove("money");
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("user", user);
    result.put("money", s.get("money"));
    result.put("list", page(rows, total, current, size));
    return BusinessResponse.success("操作成功!", result);
  }

  private Long shop(String token) {
    Long id = sessions.subject(token, "shop");
    return id != null && shops.activeCount(id) > 0 ? id : null;
  }

  private static BusinessResponse<?> unauthorized() {
    return BusinessResponse.error("请先登录");
  }

  private static long now() {
    return System.currentTimeMillis() / 1000;
  }

  private String number() {
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < 6; i++) b.append(random.nextInt(10));
    b.append(now());
    for (int i = 0; i < 6; i++) b.append(random.nextInt(10));
    return b.toString();
  }

  private String uniqueCashNumber() {
    String value;
    do {
      value = number();
    } while (shops.cashNumberCount(value) > 0);
    return value;
  }

  private static String hash(String p, String salt) {
    String first = DigestUtils.md5DigestAsHex(p.getBytes(StandardCharsets.UTF_8));
    return DigestUtils.md5DigestAsHex((first + salt).getBytes(StandardCharsets.UTF_8));
  }

  private static String text(Map<String, Object> b, String k) {
    return b == null || b.get(k) == null ? "" : b.get(k).toString();
  }

  private static String inputText(Map<String, Object> b, String k) {
    return text(b, k).trim();
  }

  private static int integer(Map<String, Object> b, String k, int d) {
    try {
      return b == null || b.get(k) == null ? d : Integer.parseInt(b.get(k).toString());
    } catch (Exception e) {
      return d;
    }
  }

  /** ThinkPHP casts the requested page to a positive integer but keeps large list_rows values. */
  private static long positiveLong(Map<String, Object> b, String k, long d) {
    try {
      long value = b == null || b.get(k) == null ? d : Long.parseLong(b.get(k).toString().trim());
      return value > 0 ? value : d;
    } catch (Exception e) {
      return d;
    }
  }

  private static long offset(long page, long size) {
    try {
      return Math.multiplyExact(page - 1, size);
    } catch (ArithmeticException ignored) {
      return Long.MAX_VALUE;
    }
  }

  private static int intOf(Object v) {
    return v == null ? 0 : ((Number) v).intValue();
  }

  private static long longOf(Object v) {
    return ((Number) v).longValue();
  }

  private static BigDecimal money(Object v) {
    try {
      return v == null ? BigDecimal.ZERO : new BigDecimal(v.toString().trim());
    } catch (Exception e) {
      return BigDecimal.ZERO;
    }
  }

  private static boolean numeric(Object value) {
    if (value == null || value.toString().trim().isEmpty()) return false;
    try {
      new BigDecimal(value.toString().trim());
      return true;
    } catch (NumberFormatException exception) {
      return false;
    }
  }

  private static String listText(Object v) {
    if (v instanceof Collection) {
      StringBuilder b = new StringBuilder();
      for (Object x : (Collection<?>) v) {
        if (b.length() > 0) b.append(',');
        b.append(x == null ? "" : x.toString().trim());
      }
      return b.toString();
    }
    return v == null ? "" : v.toString().trim();
  }

  private static List<String> split(String s) {
    return s.isEmpty() ? Collections.emptyList() : Arrays.asList(s.split(","));
  }

  private static void times(List<Map<String, Object>> rows) {
    DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    for (Map<String, Object> r : rows)
      if (r.get("createtime") != null)
        r.put(
            "createtime",
            Instant.ofEpochSecond(((Number) r.get("createtime")).longValue())
                .atZone(ZoneId.systemDefault())
                .format(f));
  }

  private static Map<String, Object> page(
      List<Map<String, Object>> rows, int total, long requestedPage, long size) {
    long lastPage = total == 0 ? 0 : ((long) total + size - 1) / size;
    long currentPage = requestedPage > lastPage ? (lastPage > 0 ? lastPage : 1) : requestedPage;
    Map<String, Object> p = new LinkedHashMap<>();
    p.put("total", total);
    p.put("per_page", size);
    p.put("current_page", currentPage);
    p.put("last_page", lastPage);
    p.put("data", rows);
    return p;
  }
}
