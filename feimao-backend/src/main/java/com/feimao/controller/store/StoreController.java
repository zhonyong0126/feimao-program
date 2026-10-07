package com.feimao.controller.store;

import com.feimao.common.BusinessResponse;
import com.feimao.mapper.StoreWorkspaceMapper;
import com.feimao.service.RoleSessionService;
import com.feimao.service.WechatPayService;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RestController
@RequestMapping("/api/store")
@CrossOrigin
public class StoreController {
  private final StoreWorkspaceMapper workspace;
  private final RoleSessionService sessions;
  private final WechatPayService pay;
  private final SecureRandom random = new SecureRandom();

  public StoreController(
      StoreWorkspaceMapper workspace,
      RoleSessionService sessions,
      WechatPayService pay) {
    this.workspace = workspace;
    this.sessions = sessions;
    this.pay = pay;
  }

  @PostMapping("/login")
  public BusinessResponse<?> login(@RequestBody(required = false) Map<String, Object> body) {
    String username = text(body, "username"), password = text(body, "password");
    if (username.isEmpty() || password.isEmpty()) return BusinessResponse.error("请输入账号密码");
    Map<String, Object> user = workspace.loginMerchant(username);
    if (user == null || user.isEmpty()) return BusinessResponse.error("门店账号不存在");
    if (intOf(user.get("status")) != 1) return BusinessResponse.error("门店账号已禁用");
    if (!text(user, "password").equals(hash(password, text(user, "salt"))))
      return BusinessResponse.error("密码错误");
    String token = sessions.issue(longOf(user.get("id")), "store");
    Map<String, Object> info = new LinkedHashMap<>();
    for (String k : Arrays.asList("id", "avatar", "name", "mobile")) info.put(k, user.get(k));
    info.put("token", token);
    info.put("user_id", user.get("id"));
    info.put("role_type", 2);
    return BusinessResponse.success(
        "Logged in successful", Collections.singletonMap("userInfo", info));
  }

  @PostMapping("/logout")
  public BusinessResponse<?> logout(@RequestHeader(value = "token", required = false) String token) {
    if (store(token) == null) return unauthorized();
    sessions.revoke(token);
    return BusinessResponse.success("退出成功", null);
  }

  @PostMapping("/index")
  public BusinessResponse<?> index(@RequestHeader(value = "token", required = false) String token) {
    Long id = store(token);
    if (id == null) return unauthorized();
    Map<String, Object> user = workspace.merchantHome(id);
    BigDecimal income = workspace.orderIncome(id);
    Map<String, Object> result = new LinkedHashMap<>();
    Map<String, Object> info = new LinkedHashMap<>(user);
    info.remove("money");
    result.put("user", info);
    result.put("order_money", income);
    result.put("user_money", user.get("money"));
    return BusinessResponse.success("操作成功!", result);
  }

  @PostMapping("/moneyLog")
  public BusinessResponse<?> moneyLog(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long id = store(token);
    if (id == null) return unauthorized();
    long page = positiveLong(body, "page", 1), size = positiveLong(body, "list_rows", 10);
    List<Map<String, Object>> rows = workspace.moneyLogs(id, size, offset(page, size));
    formatTimes(rows, "createtime");
    return BusinessResponse.success("操作成功!", page(rows, workspace.moneyLogCount(id), page, size));
  }

  @PostMapping("/orderList")
  public BusinessResponse<?> orderList(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long id = store(token);
    if (id == null) return unauthorized();
    long page = positiveLong(body, "page", 1), size = positiveLong(body, "list_rows", 10);
    int state = intValue(body, "state", 0),
        pick = intValue(body, "pick_type", 0);
    String search = text(body, "search");
    if (state != 0 && !Arrays.asList(2, 3, 4, 5, 6).contains(state))
      return BusinessResponse.error("请求错误");
    if (pick != 0 && !Arrays.asList(1, 3).contains(pick)) return BusinessResponse.error("请求错误");
    List<Map<String, Object>> rows =
        workspace.orders(id, state, pick, search, size, offset(page, size));
    for (Map<String, Object> row : rows) {
      int leaseType = intOf(row.get("lease_type"));
      row.put("lease_duration", text(row, "lease_duration") + (leaseType == 1 ? "小时" : "天"));
      row.put("pick_type", intOf(row.get("pick_type")) == 1 ? "到店取车" : "送车上门");
      Map<Integer, String> leaseNames = new LinkedHashMap<>();
      leaseNames.put(1, "时租");
      leaseNames.put(2, "日租");
      leaseNames.put(3, "月租");
      row.put("lease_type", leaseNames.containsKey(leaseType) ? leaseNames.get(leaseType) : leaseNames);
    }
    return BusinessResponse.success("操作成功!",
        page(rows, workspace.orderCount(id, state, pick, search), page, size));
  }

  @PostMapping("/orderDetail")
  public BusinessResponse<?> orderDetail(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long id = store(token);
    if (id == null) return unauthorized();
    String number = text(body, "order_number");
    if (number.isEmpty()) return BusinessResponse.error("请选择支付订单!");
    Map<String, Object> order = workspace.order(number, id);
    if (order == null || order.isEmpty()) return BusinessResponse.error("订单不存在");
    order.put("merchant_address", text(order, "merchant_administrative") + text(order, "merchant_address"));
    formatTime(order, "starttime", false);
    formatTime(order, "endtime", false);
    formatTime(order, "take_back_time", false);
    formatTime(order, "createtime", true);
    List<Map<String, Object>> guests = workspace.orderGuests(longOf(order.get("id")));
    order.put("guest", guests.size() > 1 ? new ArrayList<>(guests.subList(1, guests.size())) : Collections.emptyList());
    return BusinessResponse.success("操作成功!", order);
  }

  @PostMapping("/backCar")
  @Transactional
  public BusinessResponse<?> backCar(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long merchantId = store(token);
    if (merchantId == null) return unauthorized();
    return settleBackCar(merchantId, body, false);
  }

  public BusinessResponse<?> settleBackCarForAdmin(long merchantId, Map<String, Object> body) {
    return settleBackCar(merchantId, body, true);
  }

  private BusinessResponse<?> settleBackCar(
      long merchantId, Map<String, Object> body, boolean adminConfirm) {
    String number = text(body, "order_number");
    int returnType = intValue(body, "return_type", 0);
    Object rawRefund = body == null ? null : body.get("refund_cash_price");
    if (number.isEmpty() || (returnType != 1 && returnType != 2))
      return BusinessResponse.error("请求错误");
    String amountError = amountError(rawRefund, true);
    if (amountError != null) return BusinessResponse.error(amountError);
    BigDecimal refund = money(rawRefund);
    Map<String, Object> merchant = workspace.lockSettlementMerchant(merchantId);
    Map<String, Object> o = workspace.lockSettlementOrder(merchantId, number);
    if (o == null || o.isEmpty()) return BusinessResponse.error("订单不存在");
    BigDecimal cash = money(o.get("cash_price"));
    if (refund.compareTo(cash) > 0) return BusinessResponse.error("不能大于订单押金金额：" + cash);
    int requiredPickState = adminConfirm ? 2 : 1;
    if (intOf(o.get("state")) != 4)
      return BusinessResponse.error(
          adminConfirm ? "订单状态不允许确认还车" : "订单" + orderState(intOf(o.get("state"))));
    if (intOf(o.get("pick_state")) != requiredPickState)
      return BusinessResponse.error(
          adminConfirm
              ? "订单状态不允许确认还车"
              : "订单" + pickState(intOf(o.get("pick_state"))));
    if (adminConfirm && returnType != 1) return BusinessResponse.error("管理员确认只能完成还车");
    long orderId = longOf(o.get("id"));
    if (returnType == 2) {
      workspace.markAbnormalReturn(orderId, refund, now());
      return BusinessResponse.success("还车成功!", 1);
    }
    String refundNo = null;
    if (cash.signum() > 0 && refund.signum() > 0) {
      refundNo = number();
      try {
        pay.refund(
            String.valueOf(o.get("transaction_id")),
            refundNo,
            cents(money(o.get("pay_price"))),
            cents(refund));
      } catch (IllegalStateException e) {
        return BusinessResponse.error(e.getMessage());
      }
      workspace.insertDepositRefund(longOf(o.get("user_id")), orderId, refund, now());
    }
    BigDecimal last = money(o.get("last_all_money")).subtract(refund).max(BigDecimal.ZERO);
    workspace.completeReturn(orderId, refundNo, refund, last, now());
    workspace.restoreStock(longOf(o.get("merchant_car_id")), now());
    int source = intOf(o.get("source_type"));
    long sourceId = o.get("source_id") == null ? 0 : longOf(o.get("source_id"));
    int rate, moneyType;
    String memo;
    if (source == 2 && sourceId == merchantId) {
      rate = intOf(merchant.get("commission_merchant_rate"));
      moneyType = 2;
      memo = "扫门店码租车下单门店获取佣金";
    } else if (source == 3) {
      rate = intOf(merchant.get("commission_shop_rate"));
      moneyType = 3;
      memo = "扫酒店码租车下单门店获取佣金";
    } else {
      rate = intOf(merchant.get("commission_normal_rate"));
      moneyType = 1;
      memo = "正常租车下单门店获取佣金";
    }
    BigDecimal lease = money(o.get("lease_money")),
        merchantMoney = percent(lease, rate),
        merchantBefore = money(merchant.get("money"));
    if (merchantMoney.signum() > 0) {
      workspace.addMerchantCommission(merchantId, merchantMoney, now());
      workspace.insertMerchantCommission(
          merchantId,
          merchantMoney,
          merchantBefore,
          merchantBefore.add(merchantMoney),
          moneyType,
          memo,
          now(),
          orderId);
    }
    BigDecimal shopMoney = BigDecimal.ZERO;
    if (source == 3 && sourceId > 0) {
      Map<String, Object> s = workspace.lockSettlementShop(sourceId);
      if (s != null && !s.isEmpty()) {
        shopMoney = percent(lease, intOf(s.get("commission_shop_rate")));
        if (shopMoney.signum() > 0) {
          BigDecimal before = money(s.get("money"));
          workspace.addShopCommission(sourceId, shopMoney, now());
          workspace.insertShopCommission(
              sourceId, shopMoney, before, before.add(shopMoney), now(), orderId);
        }
      }
    }
    BigDecimal system = lease.subtract(merchantMoney).subtract(shopMoney);
    if (system.signum() > 0) {
      Map<String, Object> c = workspace.lockSystemMoney();
      if (c != null && !c.isEmpty()) {
        BigDecimal before = money(c.get("value"));
        workspace.updateSystemMoney(
            longOf(c.get("id")), before.add(system).toPlainString());
        workspace.insertSystemCommission(
            system,
            before,
            before.add(system),
            source,
            source == 1 ? "正常下单平台获取" : source == 2 ? "扫门店码下单平台获取" : "扫酒店码下单平台获取",
            now(),
            orderId);
      }
    }
    return BusinessResponse.success("还车成功!", 1);
  }

  @PostMapping("/edit")
  public BusinessResponse<?> edit(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long id = store(token);
    if (id == null) return unauthorized();
    int type = intValue(body, "type", 0);
    if (type != 1 && type != 2) return BusinessResponse.error("请求错误");
    if (type == 1) {
      Map<String, Object> m = workspace.profile(id);
      m.put("image", split(text(m, "image")));
      return BusinessResponse.success("操作成功!", m);
    }
    Map<String, Object> fields = new LinkedHashMap<>();
    for (String key :
        Arrays.asList(
            "name",
            "avatar",
            "nickname",
            "image",
            "mobile",
            "business",
            "administrative",
            "address",
            "intro",
            "pick_type",
            "car_num",
            "min_price",
            "lat",
            "lng",
            "qrimage"))
      if (body != null && body.containsKey(key)) fields.put(key, listText(body.get(key)));
    if (fields.isEmpty()) return BusinessResponse.error("没有可修改内容");
    int changed = workspace.updateProfile(id, fields, now());
    return changed == 0 ? BusinessResponse.error("操作失败!", 0) : BusinessResponse.success("操作成功!", changed);
  }

  @PostMapping("/carList")
  public BusinessResponse<?> carList(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long id = store(token);
    if (id == null) return unauthorized();
    int status = intValue(body, "status", 0);
    if (status != 1 && status != 2) return BusinessResponse.error("请求错误");
    long page = positiveLong(body, "page", 1), size = positiveLong(body, "list_rows", 10);
    List<Map<String, Object>> rows = workspace.cars(id, status, size, offset(page, size));
    for (Map<String, Object> r : rows) {
      r.put("images", split(text(r, "images")));
      r.put("intro", split(text(r, "intro")));
    }
    return BusinessResponse.success("操作成功!", page(rows, workspace.carCount(id, status), page, size));
  }

  @PostMapping("/brandCarList")
  public BusinessResponse<?> brandCars(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long id = store(token);
    if (id == null) return unauthorized();
    long brand = longValue(body, "brand_id", 0);
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("brand_list", workspace.brands());
    result.put("platform_car_list", workspace.platformCars(brand));
    return BusinessResponse.success("操作成功!", result);
  }

  @PostMapping("/carEdit")
  @Transactional
  public BusinessResponse<?> carEdit(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long merchant = store(token);
    if (merchant == null) return unauthorized();
    int type = intValue(body, "type", 0);
    long id = longValue(body, "id", 0), platform = longValue(body, "platform_car_id", 0);
    if (type < 1 || type > 3) return BusinessResponse.error("请求错误");
    if (type == 1) {
      if (legacyEmpty(body == null ? null : body.get("id")))
        return BusinessResponse.error("请选择车辆");
      Map<String, Object> car = workspace.car(id, merchant);
      if (car == null || car.isEmpty()) return BusinessResponse.error("车辆不存在");
      car.put("intro", split(text(car, "intro")));
      car.put("images", split(text(car, "images")));
      car.remove("name");
      car.remove("image");
      return BusinessResponse.success("操作成功!", car);
    }
    String requiredError = merchantCarRequiredError(body, type);
    if (requiredError != null) return BusinessResponse.error(requiredError);
    int total = intValue(body, "total_stock", -1);
    if (workspace.duplicateCar(merchant, platform, type == 3 ? id : 0) > 0)
      return BusinessResponse.error("该门店已有该车辆，无法重复添加");
    String intro = listText(body == null ? null : body.get("intro")),
        images = listText(body == null ? null : body.get("images"));
    BigDecimal cash = money(body == null ? null : body.get("cash_price")),
        month = money(body == null ? null : body.get("month_price")),
        day = money(body == null ? null : body.get("day_price")),
        hour = money(body == null ? null : body.get("hour_price"));
    if (type == 2) {
      workspace.insertCar(
          merchant, platform, intro, images, cash, month, day, hour, total, now());
      return BusinessResponse.success("操作成功!", null);
    }
    if (legacyEmpty(body.get("id"))) return BusinessResponse.error("请选择车辆");
    Map<String, Object> lockedCar = workspace.lockCar(id, merchant);
    if (lockedCar == null || lockedCar.isEmpty()) return BusinessResponse.error("车辆不存在");
    int oldTotal = intOf(lockedCar.get("total_stock")),
        oldStock = intOf(lockedCar.get("stock")),
        rented = oldTotal - oldStock;
    if (total < rented) return BusinessResponse.error("总库存不能少于已租出车辆数");
    int stock = oldStock + (total - oldTotal);
    workspace.updateCar(
        id, merchant, platform, intro, images, cash, month, day, hour, total, stock, now());
    return BusinessResponse.success("操作成功!", null);
  }

  @PostMapping("/carStatus")
  public BusinessResponse<?> carStatus(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long merchant = store(token);
    if (merchant == null) return unauthorized();
    long id = longValue(body, "id", 0);
    int status = intValue(body, "status", 0);
    if (status != 1 && status != 2) return BusinessResponse.error("请求错误");
    int changed = workspace.updateCarStatus(id, merchant, status, now());
    return changed == 0 ? BusinessResponse.error("操作失败!") : BusinessResponse.success("操作成功!", null);
  }

  @PostMapping("/carDel")
  public BusinessResponse<?> carDel(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long merchant = store(token);
    if (merchant == null) return unauthorized();
    long id = longValue(body, "id", 0);
    Integer currentStatus = workspace.carStatus(id, merchant);
    if (currentStatus == null) return BusinessResponse.error("车辆不存在");
    if (intOf(currentStatus) == 1) return BusinessResponse.error("请先下架车辆");
    workspace.deleteCar(id, merchant, now());
    return BusinessResponse.success("操作成功!", null);
  }

  @PostMapping("/cashList")
  public BusinessResponse<?> cashList(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long id = store(token);
    if (id == null) return unauthorized();
    long page = positiveLong(body, "page", 1), size = positiveLong(body, "list_rows", 10);
    List<Map<String, Object>> rows = workspace.cashRows(id, size, offset(page, size));
    formatTimes(rows, "createtime");
    return BusinessResponse.success("操作成功!", page(rows, workspace.cashCount(id), page, size));
  }

  @PostMapping("/cash")
  @Transactional
  public BusinessResponse<?> cash(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long id = store(token);
    if (id == null) return unauthorized();
    Object rawAmount = body == null ? null : body.get("amount");
    if (!numeric(rawAmount) || money(rawAmount).signum() == 0)
      return BusinessResponse.error("金额错误");
    BigDecimal amount = money(rawAmount);
    if (amount.compareTo(BigDecimal.ONE) < 0) return BusinessResponse.error("最低1元起提");
    if (amount.compareTo(BigDecimal.valueOf(500)) > 0) return BusinessResponse.error("最高500元");
    Map<String, Object> merchant = workspace.lockMerchantMoney(id);
    BigDecimal before = money(merchant.get("money"));
    if (amount.compareTo(before) > 0) return BusinessResponse.error("余额不足");
    BigDecimal after = before.subtract(amount);
    String number = number();
    Map<String, Object> values = new LinkedHashMap<>();
    values.put("out_trade_no", number);
    values.put("merchant_id", id);
    values.put("amount", amount);
    values.put("now", now());
    workspace.insertCash(values);
    long cashId = ((Number) values.get("id")).longValue();
    workspace.updateMerchantMoney(id, after, now());
    workspace.insertCashLog(id, amount, before, after, now(), cashId);
    return BusinessResponse.success("操作成功!", null);
  }

  private Long store(String token) {
    Long id = sessions.subject(token, "store");
    return id != null && id > 0 && workspace.activeMerchant(id) != null ? id : null;
  }

  private static BusinessResponse<?> unauthorized() {
    if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes) {
      HttpServletResponse response =
          ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
      if (response != null) response.setStatus(HttpStatus.UNAUTHORIZED.value());
    }
    return new BusinessResponse<>(401, "请登录后操作", now(), null);
  }

  private static String hash(String password, String salt) {
    String first = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));
    return DigestUtils.md5DigestAsHex((first + salt).getBytes(StandardCharsets.UTF_8));
  }

  private static long now() {
    return System.currentTimeMillis() / 1000;
  }

  private String number() {
    for (int attempt = 0; attempt < 10; attempt++) {
      StringBuilder b = new StringBuilder();
      for (int i = 0; i < 6; i++) b.append(random.nextInt(10));
      b.append(now());
      for (int i = 0; i < 6; i++) b.append(random.nextInt(10));
      if (workspace.cashNumberCount(b.toString()) == 0) return b.toString();
    }
    throw new IllegalStateException("提现单号生成失败");
  }

  private static int intValue(Map<String, Object> b, String k, int d) {
    try {
      return b == null || b.get(k) == null ? d : Integer.parseInt(b.get(k).toString());
    } catch (Exception e) {
      return d;
    }
  }

  private static int intOf(Object v) {
    return v instanceof Boolean ? ((Boolean) v ? 1 : 0) : v == null ? 0 : ((Number) v).intValue();
  }

  private static long longValue(Map<String, Object> b, String k, long d) {
    try {
      return b == null || b.get(k) == null ? d : Long.parseLong(b.get(k).toString());
    } catch (Exception e) {
      return d;
    }
  }

  private static long longOf(Object v) {
    return ((Number) v).longValue();
  }

  private static String text(Map<String, Object> b, String k) {
    return b == null || b.get(k) == null ? "" : b.get(k).toString().trim();
  }

  private static String listText(Object v) {
    if (v == null) return "";
    if (v instanceof Collection) {
      StringBuilder b = new StringBuilder();
      for (Object x : (Collection<?>) v) {
        if (x == null) continue;
        if (b.length() > 0) b.append(',');
        b.append(x);
      }
      return b.toString();
    }
    return v.toString();
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

  private static String merchantCarRequiredError(Map<String, Object> body, int type) {
    if (type == 3 && !legacyRequired(body, "id")) return "请选择编辑车辆";
    if (!legacyRequired(body, "platform_car_id")) return "请选择平台车辆";
    if (!legacyRequired(body, "cash_price")) return "押金不得为空";
    if (!legacyRequired(body, "month_price")) return "月租价格不得为空";
    if (!legacyRequired(body, "day_price")) return "日租价格不得为空";
    if (!legacyRequired(body, "images")) return "车辆照片不得为空";
    if (!legacyRequired(body, "intro")) return "车辆详情图片不得为空";
    if (!legacyRequired(body, "total_stock")) return "总库存不得为空";
    return null;
  }

  /** ThinkPHP Validate::require accepts numeric/string zero, but rejects other empty values. */
  private static boolean legacyRequired(Map<String, Object> body, String key) {
    return body != null && body.containsKey(key) && !legacyEmptyExceptZero(body.get(key));
  }

  private static boolean legacyEmptyExceptZero(Object value) {
    if (value == null) return true;
    if (value instanceof Number) return false;
    if (value instanceof Boolean) return false;
    if (value instanceof CharSequence) return ((CharSequence) value).length() == 0;
    if (value instanceof Collection) return ((Collection<?>) value).isEmpty();
    if (value instanceof Map) return ((Map<?, ?>) value).isEmpty();
    if (value.getClass().isArray()) return java.lang.reflect.Array.getLength(value) == 0;
    return false;
  }

  private static boolean legacyEmpty(Object value) {
    if (value == null) return true;
    if (value instanceof Number) return new BigDecimal(value.toString()).signum() == 0;
    if (value instanceof Boolean) return !((Boolean) value);
    if (value instanceof CharSequence) {
      String text = value.toString();
      return text.isEmpty() || "0".equals(text);
    }
    if (value instanceof Collection) return ((Collection<?>) value).isEmpty();
    if (value instanceof Map) return ((Map<?, ?>) value).isEmpty();
    if (value.getClass().isArray()) return java.lang.reflect.Array.getLength(value) == 0;
    return false;
  }

  private static String amountError(Object value, boolean allowZero) {
    String amount = value == null ? "0" : value.toString().trim();
    if (!amount.matches("^-?\\d+(?:\\.\\d{1,2})?$")) return "金额格式不正确";
    BigDecimal parsed = new BigDecimal(amount);
    if (!allowZero && parsed.signum() == 0) return "金额不能为0";
    return parsed.signum() < 0 ? "金额不能为负数" : null;
  }

  private static long positiveLong(Map<String, Object> body, String key, long defaultValue) {
    try {
      long value =
          body == null || body.get(key) == null
              ? defaultValue
              : Long.parseLong(body.get(key).toString().trim());
      return value > 0 ? value : defaultValue;
    } catch (NumberFormatException exception) {
      return defaultValue;
    }
  }

  private static long offset(long page, long size) {
    try {
      return Math.multiplyExact(page - 1, size);
    } catch (ArithmeticException exception) {
      return Long.MAX_VALUE;
    }
  }

  private static BigDecimal percent(BigDecimal value, int rate) {
    return value
        .multiply(BigDecimal.valueOf(rate))
        .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.DOWN);
  }

  private static long cents(BigDecimal v) {
    return v.multiply(BigDecimal.valueOf(100))
        .setScale(0, java.math.RoundingMode.HALF_UP)
        .longValueExact();
  }

  private static List<String> split(String s) {
    return s.isEmpty() ? Collections.emptyList() : Arrays.asList(s.split(","));
  }

  private static int nvl(Integer v) {
    return v == null ? 0 : v;
  }

  private static String orderState(int state) {
    switch (state) {
      case 1:
        return "待支付";
      case 2:
        return "已支付";
      case 3:
        return "进行中";
      case 4:
        return "还车中";
      case 5:
        return "已完成";
      case 6:
        return "已取消";
      default:
        return "状态错误";
    }
  }

  private static String pickState(int state) {
    switch (state) {
      case 1:
        return "待还车";
      case 2:
        return "审核中";
      case 3:
        return "已确认";
      default:
        return "状态错误";
    }
  }

  private static void formatTimes(List<Map<String, Object>> rows, String key) {
    DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    for (Map<String, Object> r : rows)
      if (r.get(key) != null)
        r.put(
            key,
            Instant.ofEpochSecond(((Number) r.get(key)).longValue())
                .atZone(ZoneId.systemDefault())
                .format(f));
  }

  private static void formatTime(Map<String, Object> row, String key, boolean seconds) {
    Object value = row.get(key);
    if (!(value instanceof Number) || ((Number) value).longValue() == 0) {
      row.put(key, "");
      return;
    }
    row.put(
        key,
        Instant.ofEpochSecond(((Number) value).longValue())
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern(seconds ? "yyyy-MM-dd HH:mm:ss" : "yyyy-MM-dd HH:mm")));
  }

  private static Map<String, Object> page(List<Map<String, Object>> r, int t, long c, long s) {
    Map<String, Object> p = new LinkedHashMap<>();
    p.put("total", t);
    p.put("per_page", s);
    p.put("current_page", c);
    p.put("last_page", t == 0 ? 0 : 1 + (t - 1L) / s);
    p.put("data", r);
    return p;
  }
}
