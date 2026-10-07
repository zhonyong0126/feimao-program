package com.feimao.controller.admin;

import com.feimao.common.BusinessResponse;
import com.feimao.mapper.AdminLogMapper;
import com.feimao.mapper.PartnerAdminMapper;
import com.feimao.service.AdminSessionService;
import com.feimao.service.MiniProgramCodeService;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

/** Full administrative lifecycle for merchants and referral shops. */
@RestController
@RequestMapping("/api/admin/business/partners")
public class PartnerAdminController {
  private static final List<String> COMMON_FIELDS =
      Arrays.asList("name", "nickname", "mobile", "address", "avatar", "image", "intro", "status");
  private static final List<String> MERCHANT_FIELDS =
      Arrays.asList(
          "business",
          "administrative",
          "car_num",
          "min_price",
          "pick_type",
          "lng",
          "lat",
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
          "service_money5");
  private static final List<String> SHOP_FIELDS = Arrays.asList("commission_shop_rate");

  private final PartnerAdminMapper partners;
  private final AdminLogMapper adminLogs;
  private final AdminSessionService sessions;
  private final MiniProgramCodeService codes;
  private final SecureRandom random = new SecureRandom();

  public PartnerAdminController(
      PartnerAdminMapper partners,
      AdminLogMapper adminLogs,
      AdminSessionService sessions,
      MiniProgramCodeService codes) {
    this.partners = partners;
    this.adminLogs = adminLogs;
    this.sessions = sessions;
    this.codes = codes;
  }

  @GetMapping("/{role}")
  public BusinessResponse<?> list(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable String role,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int limit,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Integer status,
      @RequestParam(required = false) Integer examineStatus) {
    if (admin(token) == null) return unauthorized();
    String table = table(role);
    if (table == null) return BusinessResponse.error("类型错误");
    page = Math.max(page, 1);
    limit = Math.min(Math.max(limit, 1), 100);
    String search = keyword == null ? "" : keyword.trim();
    boolean merchant = "merchant".equals(role);
    int total = merchant ? partners.merchantCount(search, status, examineStatus) : partners.shopCount(search, status, examineStatus);
    List<Map<String, Object>> rows = merchant
        ? partners.merchants(search, status, examineStatus, limit, (page - 1) * limit)
        : partners.shops(search, status, examineStatus, limit, (page - 1) * limit);
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("total", total);
    result.put("per_page", limit);
    result.put("current_page", page);
    result.put("last_page", Math.max(1, (total + limit - 1) / limit));
    result.put("data", rows);
    return BusinessResponse.success(result);
  }

  public BusinessResponse<?> list(
      String token, String role, int page, int limit, String keyword, Integer status) {
    return list(token, role, page, limit, keyword, status, null);
  }

  @GetMapping("/user-options")
  public BusinessResponse<?> userOptions(
      @RequestHeader(value = "token", required = false) String token,
      @RequestParam String role) {
    if (admin(token) == null) return unauthorized();
    if (table(role) == null) return BusinessResponse.error("类型错误");
    return BusinessResponse.success(
        "merchant".equals(role) ? partners.merchantUserOptions() : partners.shopUserOptions());
  }

  @GetMapping("/{role}/{id}")
  public BusinessResponse<?> detail(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable String role,
      @PathVariable long id) {
    if (admin(token) == null) return unauthorized();
    String table = table(role);
    if (table == null) return BusinessResponse.error("类型错误");
    Map<String, Object> row = "merchant".equals(role) ? partners.merchant(id) : partners.shop(id);
    if (row == null || row.isEmpty()) return BusinessResponse.error("记录不存在");
    row.remove("password");
    row.remove("salt");
    if ("merchant".equals(role))
      row.put(
          "point",
          String.valueOf(row.get("lng") == null ? "" : row.get("lng"))
              + ","
              + String.valueOf(row.get("lat") == null ? "" : row.get("lat")));
    row.put("images", split(row.get("image")));
    return BusinessResponse.success(row);
  }

  @PostMapping("/{role}")
  @Transactional
  public BusinessResponse<?> add(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable String role,
      @RequestBody(required = false) Map<String, Object> body) {
    Long admin = admin(token);
    if (admin == null) return unauthorized();
    String table = table(role);
    if (table == null) return BusinessResponse.error("类型错误");
    long userId = number(body, "user_id", 0);
    String error = validate(role, body, userId, 0);
    if (error != null) return BusinessResponse.error(error);
    String salt = random(6), password = text(body, "password");
    long now = now();
    Map<String, Object> values = addValues(body, userId, salt, hash(password, salt));
    if ("merchant".equals(role)) partners.insertMerchant(values, now);
    else partners.insertShop(values, now);
    long id = partners.lastInsertId();
    if ("merchant".equals(role)) partners.bindMerchant(userId, id, now);
    else partners.bindShop(userId, id, now);
    afterCommit(() -> codes.generateAsync(role, id));
    log(admin, "新增" + label(role), String.valueOf(id));
    return BusinessResponse.success(java.util.Collections.singletonMap("id", id));
  }

  @PutMapping("/{role}/{id}")
  @Transactional
  public BusinessResponse<?> edit(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable String role,
      @PathVariable long id,
      @RequestBody(required = false) Map<String, Object> body) {
    Long admin = admin(token);
    if (admin == null) return unauthorized();
    String table = table(role);
    if (table == null) return BusinessResponse.error("类型错误");
    Map<String, Object> metadata =
        "merchant".equals(role) ? partners.merchantMetadata(id) : partners.shopMetadata(id);
    if (metadata == null || metadata.isEmpty()) return BusinessResponse.error("记录不存在");
    long oldUser = ((Number) metadata.get("user_id")).longValue();
    long userId =
        body != null && body.containsKey("user_id") ? number(body, "user_id", 0) : oldUser;
    String error = validate(role, body, userId, id);
    if (error != null) return BusinessResponse.error(error);
    Map<String, Object> fields = new LinkedHashMap<>();
    for (String field : COMMON_FIELDS)
      if (body != null && body.containsKey(field))
        fields.put(field, "image".equals(field) ? listText(body.get(field)) : body.get(field));
    for (String field : "merchant".equals(role) ? MERCHANT_FIELDS : SHOP_FIELDS)
      if (body != null && body.containsKey(field)) fields.put(field, body.get(field));
    if ("merchant".equals(role) && body != null && body.containsKey("point"))
      putPoint(fields, body.get("point"));
    if (userId != oldUser) fields.put("user_id", userId);
    if (body != null && body.containsKey("username"))
      fields.put("username", text(body, "username"));
    if (!text(body, "password").isEmpty()) {
      String salt = random(6);
      fields.put("salt", salt);
      fields.put("password", hash(text(body, "password"), salt));
    }
    if (fields.isEmpty()) return BusinessResponse.error("没有可修改内容");
    if ("merchant".equals(role)) partners.updateMerchant(id, fields, now());
    else partners.updateShop(id, fields, now());
    if (userId != oldUser) {
      if ("merchant".equals(role)) {
        partners.unbindMerchant(oldUser, id, now());
        partners.bindMerchant(userId, id, now());
      } else {
        partners.unbindShop(oldUser, id, now());
        partners.bindShop(userId, id, now());
      }
    }
    if ("merchant".equals(role) && (fields.containsKey("lng") || fields.containsKey("lat")))
      partners.syncMerchantCarCoordinates(id);
    if (metadata.get("qrimage") == null || String.valueOf(metadata.get("qrimage")).isEmpty())
      afterCommit(() -> codes.generateAsync(role, id));
    log(admin, "编辑" + label(role), String.valueOf(id));
    return BusinessResponse.success(1);
  }

  @DeleteMapping("/{role}/{id}")
  @Transactional
  public BusinessResponse<?> delete(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable String role,
      @PathVariable long id) {
    Long admin = admin(token);
    if (admin == null) return unauthorized();
    String table = table(role);
    if (table == null) return BusinessResponse.error("类型错误");
    Map<String, Object> metadata =
        "merchant".equals(role) ? partners.merchantMetadata(id) : partners.shopMetadata(id);
    if (metadata == null || metadata.isEmpty()) return BusinessResponse.error("记录不存在");
    if ("merchant".equals(role) && partners.activeMerchantCarCount(id) > 0)
      return BusinessResponse.error("门店下有车辆存在");
    long now = now(), userId = ((Number) metadata.get("user_id")).longValue();
    if ("merchant".equals(role)) {
      partners.deleteMerchant(id, now);
      partners.unbindMerchant(userId, id, now);
    } else {
      partners.deleteShop(id, now);
      partners.unbindShop(userId, id, now);
    }
    log(admin, "删除" + label(role), String.valueOf(id));
    return BusinessResponse.success(1);
  }

  @DeleteMapping("/{role}")
  @Transactional
  public BusinessResponse<?> deleteBatch(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable String role,
      @RequestBody(required = false) Map<String, Object> body) {
    Long admin = admin(token);
    if (admin == null) return unauthorized();
    if (table(role) == null) return BusinessResponse.error("类型错误");
    List<Long> ids = ids(body == null ? null : body.get("ids"));
    if (ids.isEmpty()) return BusinessResponse.error("请选择要删除的记录");
    List<Map<String, Object>> records = new ArrayList<>();
    for (Long id : ids) {
      Map<String, Object> metadata =
          "merchant".equals(role) ? partners.merchantMetadata(id) : partners.shopMetadata(id);
      if (metadata == null || metadata.isEmpty()) continue;
      if ("merchant".equals(role) && partners.activeMerchantCarCount(id) > 0)
        return BusinessResponse.error("门店下有车辆存在");
      metadata.put("id", id);
      records.add(metadata);
    }
    if (records.isEmpty()) return BusinessResponse.error("记录不存在");
    long current = now();
    for (Map<String, Object> record : records) {
      long id = ((Number) record.get("id")).longValue();
      long userId = ((Number) record.get("user_id")).longValue();
      if ("merchant".equals(role)) {
        partners.deleteMerchant(id, current);
        partners.unbindMerchant(userId, id, current);
      } else {
        partners.deleteShop(id, current);
        partners.unbindShop(userId, id, current);
      }
    }
    log(admin, "批量删除" + label(role), ids.toString());
    return BusinessResponse.success(1);
  }

  @GetMapping("/{role}/recycle")
  public BusinessResponse<?> recycle(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable String role,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int limit) {
    if (admin(token) == null) return unauthorized();
    if (table(role) == null) return BusinessResponse.error("类型错误");
    page = Math.max(page, 1);
    limit = Math.min(Math.max(limit, 1), 100);
    boolean merchant = "merchant".equals(role);
    int total = merchant ? partners.recycledMerchantCount() : partners.recycledShopCount();
    List<Map<String, Object>> rows = merchant
        ? partners.recycledMerchants(limit, (page - 1) * limit)
        : partners.recycledShops(limit, (page - 1) * limit);
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("total", total);
    result.put("per_page", limit);
    result.put("current_page", page);
    result.put("last_page", Math.max(1, (total + limit - 1) / limit));
    result.put("data", rows);
    return BusinessResponse.success(result);
  }

  @PostMapping("/{role}/restore/{id}")
  @Transactional
  public BusinessResponse<?> restore(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable String role,
      @PathVariable long id) {
    Long admin = admin(token);
    if (admin == null) return unauthorized();
    if (table(role) == null) return BusinessResponse.error("类型错误");
    int changed = "merchant".equals(role) ? partners.restoreMerchant(id, now()) : partners.restoreShop(id, now());
    if (changed != 1) return BusinessResponse.error("回收记录不存在");
    log(admin, "恢复" + label(role), String.valueOf(id));
    return BusinessResponse.success(1);
  }

  @DeleteMapping("/{role}/destroy/{id}")
  @Transactional
  public BusinessResponse<?> destroy(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable String role,
      @PathVariable long id) {
    Long admin = admin(token);
    if (admin == null) return unauthorized();
    if (table(role) == null) return BusinessResponse.error("类型错误");
    int changed = "merchant".equals(role) ? partners.destroyMerchant(id) : partners.destroyShop(id);
    if (changed != 1) return BusinessResponse.error("回收记录不存在");
    log(admin, "彻底删除" + label(role), String.valueOf(id));
    return BusinessResponse.success(1);
  }

  private static Map<String, Object> addValues(
      Map<String, Object> body, long userId, String salt, String password) {
    Map<String, Object> values = new LinkedHashMap<>();
    values.put("user_id", userId);
    values.put("username", text(body, "username"));
    values.put("salt", salt);
    values.put("password", password);
    for (String field :
        Arrays.asList(
            "name",
            "nickname",
            "mobile",
            "address",
            "avatar",
            "intro",
            "business",
            "administrative",
            "lng",
            "lat")) values.put(field, text(body, field));
    values.put("image", listText(body == null ? null : body.get("image")));
    values.put("status", number(body, "status", 1));
    values.put("pick_type", number(body, "pick_type", 1));
    values.put("car_num", number(body, "car_num", 0));
    values.put("min_price", decimal(body, "min_price"));
    putPoint(values, body == null ? null : body.get("point"));
    for (String field :
        Arrays.asList(
            "commission_normal_rate",
            "commission_merchant_rate",
            "commission_shop_rate",
            "service_distance1",
            "service_distance2",
            "service_distance3",
            "service_distance4")) values.put(field, number(body, field, 0));
    for (String field :
        Arrays.asList(
            "service_money1",
            "service_money2",
            "service_money3",
            "service_money4",
            "service_money5")) values.put(field, decimal(body, field));
    return values;
  }

  private static void putPoint(Map<String, Object> values, Object rawPoint) {
    String point = rawPoint == null ? "" : String.valueOf(rawPoint).trim();
    if (point.isEmpty()) return;
    String[] coordinates = point.split(",", -1);
    if (coordinates.length != 2) return;
    values.put("lng", coordinates[0].trim());
    values.put("lat", coordinates[1].trim());
  }

  private String validate(String role, Map<String, Object> b, long userId, long id) {
    if (userId <= 0 || partners.userCount(userId) == 0)
      return "用户不存在";
    if (text(b, "name").isEmpty() && id == 0) return label(role) + "名称不能为空";
    String username = text(b, "username");
    if (id == 0 && (username.isEmpty() || text(b, "password").isEmpty())) return "登录账号和密码不能为空";
    if (!username.isEmpty()
        && ("merchant".equals(role) ? partners.merchantUsernameCount(username,id) : partners.shopUsernameCount(username,id)) > 0) return "登录账号已存在";
    if (("merchant".equals(role) ? partners.otherMerchantBinding(id,userId) : partners.otherShopBinding(id,userId)) > 0)
      return "用户已绑定其他" + label(role);
    if ("merchant".equals(role)
        && (id == 0 || (b != null && b.containsKey("point")))
        && !validPoint(text(b, "point"))) return "请选择经纬度";
    if ("merchant".equals(role) && number(b, "pick_type", 1) != 1)
      for (String f :
          Arrays.asList(
              "service_distance1",
              "service_distance2",
              "service_distance3",
              "service_distance4",
              "service_money1",
              "service_money2",
              "service_money3",
              "service_money4",
              "service_money5")) if (decimal(b, f).signum() <= 0) return "请设置完善取车公里和取车金额";
    return null;
  }

  private static boolean validPoint(String point) {
    String[] coordinates = point.split(",", -1);
    if (coordinates.length != 2) return false;
    try {
      double lng = Double.parseDouble(coordinates[0].trim());
      double lat = Double.parseDouble(coordinates[1].trim());
      return lng >= -180 && lng <= 180 && lat >= -90 && lat <= 90;
    } catch (NumberFormatException ignored) {
      return false;
    }
  }

  private Long admin(String t) {
    return sessions.adminId(t);
  }

  private static String table(String r) {
    return "merchant".equals(r) ? "ymcx_merchant" : "shop".equals(r) ? "ymcx_shop" : null;
  }

  private static String label(String r) {
    return "merchant".equals(r) ? "门店" : "酒店";
  }

  private void log(long a, String t, String c) {
    adminLogs.insertLog(a, "/api/admin/business/partners", t, c, now());
  }

  private static void afterCommit(Runnable r) {
    if (TransactionSynchronizationManager.isSynchronizationActive())
      TransactionSynchronizationManager.registerSynchronization(
          new TransactionSynchronization() {
            @Override
            public void afterCommit() {
              r.run();
            }
          });
    else r.run();
  }

  private String random(int n) {
    String c = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    StringBuilder s = new StringBuilder();
    for (int i = 0; i < n; i++) s.append(c.charAt(random.nextInt(c.length())));
    return s.toString();
  }

  private static String hash(String p, String s) {
    String f = DigestUtils.md5DigestAsHex(p.getBytes(StandardCharsets.UTF_8));
    return DigestUtils.md5DigestAsHex((f + s).getBytes(StandardCharsets.UTF_8));
  }

  private static String text(Map<String, Object> b, String k) {
    return b == null || b.get(k) == null ? "" : String.valueOf(b.get(k)).trim();
  }

  private static long number(Map<String, Object> b, String k, long d) {
    try {
      return Long.parseLong(text(b, k));
    } catch (Exception e) {
      return d;
    }
  }

  private static java.math.BigDecimal decimal(Map<String, Object> b, String k) {
    try {
      return new java.math.BigDecimal(text(b, k));
    } catch (Exception e) {
      return java.math.BigDecimal.ZERO;
    }
  }

  private static String listText(Object v) {
    if (v instanceof Iterable) {
      List<String> x = new ArrayList<>();
      for (Object i : (Iterable<?>) v) x.add(String.valueOf(i));
      return String.join(",", x);
    }
    return v == null ? "" : String.valueOf(v);
  }

  private static List<Long> ids(Object value) {
    List<Long> result = new ArrayList<>();
    if (value instanceof Iterable) {
      for (Object item : (Iterable<?>) value) {
        try {
          long id = Long.parseLong(String.valueOf(item));
          if (id > 0) result.add(id);
        } catch (NumberFormatException ignored) {
        }
      }
    } else if (value != null) {
      for (String item : String.valueOf(value).split(",")) {
        try {
          long id = Long.parseLong(item.trim());
          if (id > 0) result.add(id);
        } catch (NumberFormatException ignored) {
        }
      }
    }
    return result;
  }

  private static List<String> split(Object v) {
    String s = v == null ? "" : String.valueOf(v);
    return s.isEmpty() ? java.util.Collections.emptyList() : Arrays.asList(s.split(","));
  }

  private static long now() {
    return System.currentTimeMillis() / 1000;
  }

  private static BusinessResponse<?> unauthorized() {
    return BusinessResponse.error("管理员未登录");
  }
}
