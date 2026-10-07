package com.feimao.controller.user;

import com.feimao.common.BusinessResponse;
import com.feimao.mapper.UserProfileMapper;
import com.feimao.service.MiniProgramCodeService;
import com.feimao.service.UserTokenService;
import com.feimao.service.WechatLoginService;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@CrossOrigin
public class UserController {
  private final UserProfileMapper users;
  private final UserTokenService tokens;
  private final WechatLoginService wechat;
  private final MiniProgramCodeService miniProgramCodes;

  public UserController(
      UserProfileMapper users,
      UserTokenService tokens,
      WechatLoginService wechat,
      MiniProgramCodeService miniProgramCodes) {
    this.users = users;
    this.tokens = tokens;
    this.wechat = wechat;
    this.miniProgramCodes = miniProgramCodes;
  }

  @PostMapping("/login")
  @Transactional
  public BusinessResponse<?> login(@RequestBody(required = false) Map<String, Object> body) {
    String code = text(body, "code");
    if (code.isEmpty() || text(body, "encryptedData").isEmpty() || text(body, "iv").isEmpty())
      return BusinessResponse.error("Logo fail");
    final String openid;
    try {
      openid = wechat.exchangeCode(code);
    } catch (IllegalArgumentException | IllegalStateException ex) {
      return BusinessResponse.error(ex.getMessage());
    }
    Long existingUserId = users.userIdByOpenid(openid);
    long now = now();
    long userId;
    if (existingUserId == null) {
      Map<String, Object> inserted = new LinkedHashMap<>();
      users.insertWechatUser(inserted, openid, now);
      userId = ((Number) inserted.get("id")).longValue();
      users.initializeNickname(userId, String.format("用户%06d", userId));
    } else {
      userId = existingUserId;
      users.updateLoginTime(userId, now);
    }
    String token = tokens.issueLogin(userId);
    Map<String, Object> userInfo = users.loginUser(userId);
    userInfo.put("token", token);
    userInfo.put("role_type", 1);
    return BusinessResponse.success("登录成功", Collections.singletonMap("userInfo", userInfo));
  }

  @PostMapping("/info")
  public BusinessResponse<?> info(@RequestHeader(value = "token", required = false) String token) {
    Long userId = tokens.userId(token);
    if (userId == null) return unauthorized();
    return BusinessResponse.success("操作成功!", users.userInfo(userId));
  }

  @PostMapping("/logout")
  public BusinessResponse<?> logout(@RequestHeader(value = "token", required = false) String token) {
    tokens.revoke(token);
    return BusinessResponse.success("退出成功", null);
  }

  @PostMapping("/edit")
  public BusinessResponse<?> edit(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long userId = tokens.userId(token);
    if (userId == null) return unauthorized();
    Map<String, String> fields = new LinkedHashMap<>();
    for (String field : Arrays.asList("nickname", "avatar", "mobile"))
      if (body != null && body.containsKey(field)) fields.put(field, text(body, field));
    int changed = users.updateProfile(userId, fields, now());
    return changed > 0
        ? BusinessResponse.success("操作成功!", changed)
        : BusinessResponse.error("操作失败!", changed);
  }

  @PostMapping("/addressList")
  public BusinessResponse<?> addressList(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long userId = tokens.userId(token);
    if (userId == null) return unauthorized();
    long page = positiveLong(body, "page", 1), size = positiveLong(body, "list_rows", 10);
    List<Map<String, Object>> rows = users.addresses(userId, size, (page - 1) * size);
    return BusinessResponse.success("操作成功!", page(rows, users.addressCount(userId), page, size));
  }

  @PostMapping("/addressAdd")
  public BusinessResponse<?> addressAdd(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long userId = tokens.userId(token);
    if (userId == null) return unauthorized();
    String name = text(body, "name"),
        mobile = text(body, "mobile"),
        address = text(body, "address"),
        administrative = text(body, "administrative");
    String lngText = text(body, "lng"), latText = text(body, "lat");
    double lng = number(body, "lng", 0), lat = number(body, "lat", 0);
    if (name.isEmpty()) return BusinessResponse.error("联系人不得为空");
    if (mobile.isEmpty()) return BusinessResponse.error("手机号不得为空");
    if (!mobile.matches("^1[3-9]\\d{9}$")) return BusinessResponse.error("手机号格式错误");
    if (administrative.isEmpty()) return BusinessResponse.error("行政区不得为空");
    if (address.isEmpty()) return BusinessResponse.error("详细地址不得为空");
    if (lngText.isEmpty() || latText.isEmpty()) return BusinessResponse.error("经纬度不得为空");
    int changed =
        users.insertAddress(
            userId,
            name,
            administrative,
            address,
            mobile,
            lng,
            lat,
            now());
    return BusinessResponse.success("操作成功!", changed);
  }

  @PostMapping("/addressEdit")
  public BusinessResponse<?> addressEdit(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long userId = tokens.userId(token);
    if (userId == null) return unauthorized();
    long addressId = (long) number(body, "address_id", 0);
    if (addressId <= 0) return BusinessResponse.error("请求错误");
    if (users.addressExists(addressId, userId) == 0) return BusinessResponse.error("地址不存在");
    Map<String, Object> fields = new LinkedHashMap<>();
    for (String field : Arrays.asList("name", "administrative", "address", "mobile", "lng", "lat"))
      if (body != null && body.containsKey(field)) fields.put(field, text(body, field));
    int changed = users.updateAddress(addressId, userId, fields, now());
    return changed > 0
        ? BusinessResponse.success("操作成功!", changed)
        : BusinessResponse.error("操作失败!", "");
  }

  @PostMapping("/addressDel")
  public BusinessResponse<?> addressDel(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long userId = tokens.userId(token);
    if (userId == null) return unauthorized();
    long addressId = (long) number(body, "address_id", 0);
    if (addressId <= 0) return BusinessResponse.error("Address id not");
    if (users.addressExists(addressId, userId) == 0)
      return BusinessResponse.error("地址不存在", "");
    int changed = users.deleteAddress(addressId, userId);
    return changed == 0
        ? BusinessResponse.error("操作失败!", "")
        : BusinessResponse.success("操作成功!", changed);
  }

  @PostMapping("/merchantApply")
  @Transactional
  public BusinessResponse<?> merchantApply(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long userId = tokens.userId(token);
    if (userId == null) return unauthorized();
    int type = positiveInt(body, "type", 0);
    if (type != 1 && type != 2) return BusinessResponse.error("参数错误");
    Map<String, Object> user = users.lockApplicationUser("merchant", userId);
    int status = ((Number) user.get("status")).intValue();
    long merchantId = ((Number) user.get("partner_id")).longValue();
    if (type == 1) {
      Map<String, Object> result = new LinkedHashMap<>();
      result.put("merchant_status", status);
      Map<String, Object> merchant =
          status != 1 && merchantId > 0
              ? users.applicationDetail("merchant", merchantId, userId)
              : null;
      result.put("merchant", merchant == null ? Collections.emptyList() : merchant);
      return BusinessResponse.success("操作成功!", result);
    }
    String name = text(body, "name"),
        mobile = text(body, "mobile"),
        administrative = text(body, "administrative"),
        address = text(body, "address");
    if (status == 3) return BusinessResponse.error("你的申请已审核通过");
    if (name.isEmpty()) return BusinessResponse.error("门店名称不得为空");
    if (mobile.isEmpty()) return BusinessResponse.error("联系电话不得为空");
    if (!mobile.matches("^1[3-9]\\d{9}$")) return BusinessResponse.error("联系电话格式错误");
    if (administrative.isEmpty()) return BusinessResponse.error("行政区不得为空");
    if (address.isEmpty()) return BusinessResponse.error("地址不得为空");
    if (status == 1) {
      String avatar = config("avatar", "/assets/img/avatar.png");
      Map<String, Object> values = applicationValues(body, userId, now());
      values.put("avatar", avatar);
      users.insertMerchantApplication(values);
      long id = ((Number) values.get("id")).longValue();
      users.bindApplication("merchant", userId, id, now());
      generateCodeAfterCommit("merchant", id);
      return BusinessResponse.success("操作成功", 1);
    }
    Map<String, Object> application = users.applicationState("merchant", merchantId, userId);
    if (application == null || application.isEmpty()) return BusinessResponse.error("申请记录不存在");
    if (((Number) application.get("examine_status")).intValue() == 2)
      return BusinessResponse.error("您的申请已通过");
    users.resubmitMerchant(merchantId, applicationValues(body, userId, now()));
    users.markApplicationPending("merchant", userId, now());
    return BusinessResponse.success("操作成功", 1);
  }

  @PostMapping("/shopApply")
  @Transactional
  public BusinessResponse<?> shopApply(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long userId = tokens.userId(token);
    if (userId == null) return unauthorized();
    int type = positiveInt(body, "type", 0);
    if (type != 1 && type != 2) return BusinessResponse.error("参数错误");
    Map<String, Object> user = users.lockApplicationUser("shop", userId);
    int status = ((Number) user.get("status")).intValue();
    long shopId = ((Number) user.get("partner_id")).longValue();
    if (type == 1) {
      Map<String, Object> result = new LinkedHashMap<>();
      result.put("shop_status", status);
      Map<String, Object> shop =
          status != 1 && shopId > 0 ? users.applicationDetail("shop", shopId, userId) : null;
      if (shop == null) result.put("shop", Collections.emptyList());
      else {
        shop.put("image", split(text(shop, "image")));
        result.put("shop", shop);
      }
      return BusinessResponse.success("操作成功!", result);
    }
    String name = text(body, "name"),
        mobile = text(body, "mobile"),
        address = text(body, "address"),
        image = listText(body == null ? null : body.get("image"));
    if (status == 3) return BusinessResponse.error("你的申请已审核通过");
    if (mobile.isEmpty()) return BusinessResponse.error("联系电话不得为空");
    if (!mobile.matches("^1[3-9]\\d{9}$")) return BusinessResponse.error("联系电话格式错误");
    if (address.isEmpty()) return BusinessResponse.error("门店地址不得为空");
    if (text(body, "lng").isEmpty() || text(body, "lat").isEmpty())
      return BusinessResponse.error("经纬度不得为空");
    String avatar = image.isEmpty() ? "" : image.split(",")[0];
    if (status == 1) {
      Map<String, Object> values = applicationValues(body, userId, now());
      values.put("image", image);
      values.put("avatar", avatar);
      users.insertShopApplication(values);
      long id = ((Number) values.get("id")).longValue();
      users.bindApplication("shop", userId, id, now());
      generateCodeAfterCommit("shop", id);
      return BusinessResponse.success("操作成功", 1);
    }
    Map<String, Object> application = users.applicationState("shop", shopId, userId);
    if (application == null || application.isEmpty()) return BusinessResponse.error("申请记录不存在");
    if (((Number) application.get("examine_status")).intValue() == 2)
      return BusinessResponse.error("您的申请已通过");
    Map<String, Object> values = applicationValues(body, userId, now());
    values.put("image", image);
    values.put("avatar", avatar);
    users.resubmitShop(shopId, values);
    users.markApplicationPending("shop", userId, now());
    return BusinessResponse.success("操作成功", 1);
  }

  private static Map<String, Object> applicationValues(
      Map<String, Object> body, long userId, long now) {
    Map<String, Object> values = new LinkedHashMap<>();
    values.put("user_id", userId);
    for (String field :
        Arrays.asList(
            "name", "nickname", "mobile", "administrative", "address", "intro", "lat", "lng"))
      values.put(field, text(body, field));
    values.put("now", now);
    return values;
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

  @PostMapping("/couponList")
  public BusinessResponse<?> couponList(
      @RequestHeader(value = "token", required = false) String token) {
    Long userId = tokens.userId(token);
    if (userId == null) return unauthorized();
    return BusinessResponse.success("操作成功!", users.coupons(userId));
  }

  @PostMapping("/authenticationInfo")
  public BusinessResponse<?> authenticationInfo(
      @RequestHeader(value = "token", required = false) String token) {
    return authInfo(token, false);
  }

  @PostMapping("/staffAuthenticationInfo")
  public BusinessResponse<?> staffAuthenticationInfo(
      @RequestHeader(value = "token", required = false) String token) {
    return authInfo(token, true);
  }

  @PostMapping("/authentication")
  public BusinessResponse<?> authentication(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long userId = tokens.userId(token);
    if (userId == null) return unauthorized();
    if (text(body, "authentication_mobile").isEmpty()
        || text(body, "authentication_name").isEmpty()
        || text(body, "authentication_card").isEmpty()) return BusinessResponse.error("请完善认证信息");
    Integer status = users.authenticationStatus(userId, false);
    if (status != null && status == 2) return BusinessResponse.error("认证审核中");
    if (status != null && status == 3) return BusinessResponse.error("已通过认证");
    int changed =
        users.submitAuthentication(
            userId,
            text(body, "authentication_mobile"),
            text(body, "authentication_name"),
            text(body, "authentication_card"),
            now());
    return changed == 0 ? BusinessResponse.error("操作失败!", "") : BusinessResponse.success("操作成功!", changed);
  }

  @PostMapping("/staffAuthentication")
  public BusinessResponse<?> staffAuthentication(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long userId = tokens.userId(token);
    if (userId == null) return unauthorized();
    if (text(body, "staff_mobile").isEmpty()
        || text(body, "staff_name").isEmpty()
        || text(body, "staff_work").isEmpty()) return BusinessResponse.error("请完善认证信息");
    Integer status = users.authenticationStatus(userId, true);
    if (status != null && status == 2) return BusinessResponse.error("认证审核中");
    if (status != null && status == 3) return BusinessResponse.error("已通过认证");
    int changed =
        users.submitStaffAuthentication(
            userId,
            text(body, "staff_mobile"),
            text(body, "staff_name"),
            text(body, "staff_work"),
            now());
    return changed == 0 ? BusinessResponse.error("操作失败!", "") : BusinessResponse.success("操作成功!", changed);
  }

  @PostMapping("/malfunction")
  public BusinessResponse<?> malfunction(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long userId = tokens.userId(token);
    if (userId == null) return unauthorized();
    String number = text(body, "number"),
        image = text(body, "image"),
        content = text(body, "content");
    if (number.isEmpty() || image.isEmpty() || content.isEmpty())
      return BusinessResponse.error("请完善故障信息!");
    if (users.malfunctionOrderCount(number) == 0)
      return BusinessResponse.error("订单编号不存在，请输入正确的订单号！");
    int changed = users.insertMalfunction(userId, number, image, content, now());
    return BusinessResponse.success("操作成功!", changed);
  }

  private BusinessResponse<?> authInfo(String token, boolean staff) {
    Long userId = tokens.userId(token);
    if (userId == null) return unauthorized();
    return BusinessResponse.success("操作成功!", users.authInfo(userId, staff));
  }

  private static BusinessResponse<?> unauthorized() {
    return BusinessResponse.error("请先登录");
  }

  private static long now() {
    return System.currentTimeMillis() / 1000;
  }

  private static String text(Map<String, Object> body, String key) {
    return body == null || body.get(key) == null ? "" : body.get(key).toString().trim();
  }

  private static double number(Map<String, Object> body, String key, double fallback) {
    try {
      return body == null || body.get(key) == null
          ? fallback
          : Double.parseDouble(body.get(key).toString());
    } catch (NumberFormatException ignored) {
      return fallback;
    }
  }

  private static int positiveInt(Map<String, Object> body, String key, int fallback) {
    int value = (int) number(body, key, fallback);
    return value > 0 ? value : fallback;
  }

  private static long positiveLong(Map<String, Object> body, String key, long fallback) {
    try {
      long value = body == null || body.get(key) == null
          ? fallback
          : Long.parseLong(body.get(key).toString());
      return value > 0 ? value : fallback;
    } catch (NumberFormatException ignored) {
      return fallback;
    }
  }

  private String config(String name, String fallback) {
    String value = users.config(name);
    return value == null ? fallback : value;
  }

  private static String listText(Object value) {
    if (value instanceof Iterable) {
      StringBuilder b = new StringBuilder();
      for (Object v : (Iterable<?>) value) {
        if (v == null) continue;
        if (b.length() > 0) b.append(',');
        b.append(v);
      }
      return b.toString();
    }
    return value == null ? "" : value.toString();
  }

  private static List<String> split(String value) {
    return value == null || value.isEmpty()
        ? Collections.emptyList()
        : Arrays.asList(value.split(","));
  }

  private static Map<String, Object> page(
      List<Map<String, Object>> rows, int total, long current, long size) {
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("total", total);
    result.put("per_page", size);
    result.put("current_page", current);
    result.put("last_page", total == 0 ? 0 : (total + size - 1) / size);
    result.put("data", rows);
    return result;
  }
}
