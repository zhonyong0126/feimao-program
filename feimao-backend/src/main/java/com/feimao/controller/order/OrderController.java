package com.feimao.controller.order;

import com.feimao.common.BusinessResponse;
import com.feimao.mapper.BusinessOrderMapper;
import com.feimao.service.SmsNotificationService;
import com.feimao.service.UserTokenService;
import com.feimao.service.WechatPayService;
import com.feimao.service.InsuranceService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RestController
@RequestMapping("/api/order")
@CrossOrigin
public class OrderController {
  private final BusinessOrderMapper orders;
  private final UserTokenService tokens;
  private final WechatPayService pay;
  private final InsuranceService insurance;
  private final SmsNotificationService notifications;
  private final SecureRandom random = new SecureRandom();

  public OrderController(
      BusinessOrderMapper orders, UserTokenService tokens, WechatPayService pay,
      InsuranceService insurance, SmsNotificationService notifications) {
    this.orders = orders;
    this.tokens = tokens;
    this.pay = pay;
    this.insurance = insurance;
    this.notifications = notifications;
  }

  @PostMapping("/insureTest")
  public BusinessResponse<?> insureTest() {
    return insuranceProbe("insure");
  }

  @PostMapping("/insureListTest")
  public BusinessResponse<?> insureListTest() {
    return insuranceProbe("query");
  }

  @PostMapping("/insureCancel")
  public BusinessResponse<?> insureCancel() {
    return insuranceProbe("cancel");
  }

  private BusinessResponse<?> insuranceProbe(String action) {
    try {
      return BusinessResponse.success("操作成功!", insurance.legacyProbe(action));
    } catch (IllegalStateException exception) {
      return BusinessResponse.error(exception.getMessage());
    }
  }

  @PostMapping("/orderPage")
  public BusinessResponse<?> orderPage(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long uid = tokens.userId(token);
    if (uid == null) return unauthorized();
    long carId = longValue(body, "merchant_car_id", 0);
    if (carId == 0) return BusinessResponse.error("参数错误");
    Map<String, Object> car = orders.rentableCar(carId);
    if (car == null || car.isEmpty()) return BusinessResponse.error("门店车辆不存在");
    car.remove("stock");
    car.put("brand", textValue(car.get("brand")));
    Map<String, Object> merchant = orders.activeMerchant(((Number) car.get("merchant_id")).longValue());
    if (merchant == null || merchant.isEmpty()) return BusinessResponse.error("门店不存在");
    LocalDateTime start = parseTime(text(body, "starttime")),
        end = parseTime(text(body, "endtime"));
    if (start == null || end == null || !end.isAfter(start))
      return BusinessResponse.error("租赁时间错误");
    int leaseType = intValue(body, "lease_type", 1),
        people = intValue(body, "people", 1),
        pickType = intValue(body, "pick_type", 1),
        pickSingle = intValue(body, "pick_single", 1);
    if (people <= 0) return BusinessResponse.error("乘车人数错误");
    long units;
    BigDecimal unitPrice;
    if (leaseType == 1) {
      if (!start.toLocalDate().equals(end.toLocalDate())) return BusinessResponse.error("时租时间不能跨天");
      units = leaseUnits(start, end, 3600);
      unitPrice = money(car.get("hour_price"));
    } else if (leaseType == 2) {
      units = leaseUnits(start, end, 24 * 3600);
      if (units > 30) return BusinessResponse.error("日租时间最长时间不能超过30天");
      unitPrice = money(car.get("day_price"));
    } else {
      units = 1;
      unitPrice = money(car.get("month_price"));
    }
    Map<String, Object> address = Collections.emptyMap();
    BigDecimal pickPrice = BigDecimal.ZERO;
    if (pickType != 1 && longValue(body, "address_id", 0) > 0) {
      long addressId = longValue(body, "address_id", 0);
      address = orders.userAddress(addressId, uid);
      if (address == null || address.isEmpty()) return BusinessResponse.error("地址不存在");
      double distance =
          haversine(
              number(merchant.get("lat")),
              number(merchant.get("lng")),
              number(address.get("lat")),
              number(address.get("lng")));
      distance = Math.round(distance * 100D) / 100D;
      pickPrice = servicePrice(merchant, distance);
    }
    BigDecimal pickMoney = pickSingle == 1 ? pickPrice : pickPrice.multiply(BigDecimal.valueOf(2));
    long insureUnits = leaseType == 1 ? 1 : leaseType == 2 ? units : leaseType == 3 ? 30 : 0;
    BigDecimal insurePrice = configMoney("insure_price");
    BigDecimal insureMoney = insurePrice.multiply(BigDecimal.valueOf(people * insureUnits));
    BigDecimal leaseMoney = unitPrice.multiply(BigDecimal.valueOf(units));
    BigDecimal cash = money(car.get("cash_price"));
    BigDecimal total = leaseMoney.add(pickMoney).add(insureMoney).add(cash);
    Map<String, Object> agreements = new LinkedHashMap<>();
    for (Map<String, Object> row : orders.leaseConfigs())
      agreements.put(row.get("name").toString(), row.get("value"));
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("lease_price", unitPrice);
    result.put("num", units);
    result.put("people", people);
    result.put("lease_money", leaseMoney);
    result.put("pick_single", pickSingle);
    result.put("pick_price", pickPrice);
    result.put("pick_money", pickMoney);
    result.put("insure_price", insurePrice);
    result.put("insure_num", insureUnits);
    result.put("insure_money", insureMoney);
    result.put("cash_money", cash);
    result.put("all_money", total);
    result.put("merchant_car", car);
    result.put("address", address.isEmpty() ? Collections.emptyList() : address);
    result.put("config", agreements);
    result.put("service_money", serviceMoney(merchant));
    Map<String, Object> params = body == null ? new LinkedHashMap<>() : new LinkedHashMap<>(body);
    params.remove("token");
    result.put("params", params);
    return BusinessResponse.success("操作成功!", result);
  }

  @PostMapping("/leaseCarPay")
  @Transactional
  public BusinessResponse<?> leaseCarPay(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long uid = tokens.userId(token);
    if (uid == null) return unauthorized();
    int sourceType = intValue(body, "source_type", 0);
    long sourceId = longValue(body, "source_id", 0);
    if (sourceType < 1 || sourceType > 3 || (sourceType > 1 && sourceId == 0))
      return BusinessResponse.error("下单来源参数错误");
    long carId = longValue(body, "merchant_car_id", 0);
    if (carId == 0) return BusinessResponse.error("请选择门店车辆");
    if (text(body, "pick_type").isEmpty()) return BusinessResponse.error("取车方式不得为空");
    if (text(body, "lease_type").isEmpty()) return BusinessResponse.error("租赁方式不得为空");
    if (text(body, "starttime").isEmpty() || text(body, "endtime").isEmpty())
      return BusinessResponse.error("租赁时间不得为空");
    String name = text(body, "name"), mobile = text(body, "mobile"), idcard = text(body, "idcard");
    if (name.isEmpty()) return BusinessResponse.error("姓名不得为空");
    if (mobile.isEmpty()) return BusinessResponse.error("手机号不得为空");
    if (!mobile.matches("^1[3-9]\\d{9}$")) return BusinessResponse.error("联系电话格式错误");
    if (idcard.isEmpty()) return BusinessResponse.error("身份证号不得为空");
    int leaseType = intValue(body, "lease_type", 1);
    int pickType = intValue(body, "pick_type", 1), pickSingle = intValue(body, "pick_single", 1);
    long addressId = longValue(body, "address_id", 0);
    if (pickType != 1 && addressId == 0) return BusinessResponse.error("请选择取车地址!");
    Map<String, Object> car = orders.lockRentableCar(carId);
    if (car == null || car.isEmpty()) return BusinessResponse.error("门店车辆不存在");
    if (((Number) car.get("stock")).intValue() <= 0) return BusinessResponse.error("车辆剩余库存不足");
    Map<String, Object> merchant = orders.activeMerchant(((Number) car.get("merchant_id")).longValue());
    if (merchant == null || merchant.isEmpty()) return BusinessResponse.error("门店不存在");
    LocalDateTime start = parseTime(text(body, "starttime")),
        end = parseTime(text(body, "endtime"));
    if (start == null || end == null || !end.isAfter(start)) return BusinessResponse.error("租赁时间错误");
    long units;
    BigDecimal unitPrice;
    if (leaseType == 1) {
      units = leaseUnits(start, end, 3600);
      unitPrice = money(car.get("hour_price"));
    } else if (leaseType == 2) {
      units = leaseUnits(start, end, 24 * 3600);
      unitPrice = money(car.get("day_price"));
    } else {
      units = 1;
      unitPrice = money(car.get("month_price"));
    }
    Map<String, Object> address = Collections.emptyMap();
    BigDecimal pickPrice = BigDecimal.ZERO;
    if (pickType != 1) {
      address = orders.userAddress(addressId, uid);
      if (address == null || address.isEmpty()) return BusinessResponse.error("地址不存在");
      double distance =
          haversine(
              number(merchant.get("lat")),
              number(merchant.get("lng")),
              number(address.get("lat")),
              number(address.get("lng")));
      distance = Math.round(distance * 100D) / 100D;
      pickPrice = servicePrice(merchant, distance);
    }
    BigDecimal pickMoney = pickSingle == 1 ? pickPrice : pickPrice.multiply(BigDecimal.valueOf(2));
    List<Map<String, Object>> guests = guestList(body.get("guest"));
    long insureUnits = leaseType == 1 ? 1 : leaseType == 2 ? units : leaseType == 3 ? 30 : 0;
    BigDecimal perPersonInsure =
        configMoney("insure_price").multiply(BigDecimal.valueOf(insureUnits));
    BigDecimal insureMoney = perPersonInsure.multiply(BigDecimal.valueOf(guests.size() + 1));
    BigDecimal leaseMoney = unitPrice.multiply(BigDecimal.valueOf(units)),
        cash = money(car.get("cash_price")),
        total = leaseMoney.add(pickMoney).add(insureMoney).add(cash);
    String number = orderNumber();
    String orderName =
        car.get("name") + "-" + units + (leaseType == 1 ? "小时" : leaseType == 2 ? "天" : "月");
    long startEpoch = start.atZone(ZoneId.systemDefault()).toEpochSecond(),
        endEpoch = end.atZone(ZoneId.systemDefault()).toEpochSecond();
    String
        pickAddress =
            address.isEmpty()
                ? null
                : text(address, "administrative").replace("-", "") + text(address, "address"),
        pickPoint = address.isEmpty() ? "" : address.get("lat") + "," + address.get("lng");
    Map<String, Object> values = new LinkedHashMap<>();
    values.put("user_id", uid);
    values.put("merchant_id", car.get("merchant_id"));
    values.put("number", number);
    values.put("order_name", orderName);
    values.put("service_name", car.get("name"));
    values.put("service_image", textValue(car.get("image")));
    values.put("merchant_car_id", carId);
    values.put("pick_type", pickType);
    values.put("address_id", addressId);
    values.put("address_name", address.isEmpty() ? "" : address.get("name"));
    values.put("address_phone", address.isEmpty() ? "" : address.get("mobile"));
    values.put("pick_address", pickAddress);
    values.put("pick_point", pickPoint);
    values.put("price", total);
    values.put("num", units);
    values.put("name", name);
    values.put("mobile", mobile);
    values.put("idcard", idcard);
    values.put("lease_type", leaseType);
    values.put("lease_duration", String.valueOf(units));
    values.put("lease_price", unitPrice);
    String takeBackText = text(body, "take_back_time");
    values.put("take_back_time", takeBackText.isEmpty() ? null : epoch(takeBackText));
    values.put("cash_price", cash);
    values.put("lease_money", leaseMoney);
    values.put("pick_single", pickSingle);
    values.put("pick_price", pickMoney);
    values.put("insure_money", insureMoney);
    values.put("cash_state", cash.compareTo(BigDecimal.ZERO) > 0 ? 2 : 1);
    values.put("starttime", startEpoch);
    values.put("endtime", endEpoch);
    values.put("source_type", sourceType);
    values.put("source_id", sourceId);
    values.put("createtime", now());
    values.put("updatetime", now());
    orders.insertLeaseOrder(values);
    long orderId = ((Number) values.get("id")).longValue();
    insertGuest(uid, orderId, name, mobile, idcard, perPersonInsure);
    for (Map<String, Object> guest : guests)
      insertGuest(
          uid,
          orderId,
          text(guest, "name"),
          text(guest, "mobile"),
          text(guest, "idcard"),
          perPersonInsure);
    orders.decrementStock(carId, now());
    Map<String, Object> order = orders.paymentOrder(number);
    return requestPayment(order, total, notifyPath(1), "租车:", true);
  }

  @PostMapping("/installmentDetail")
  public BusinessResponse<?> installmentDetail(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long uid = tokens.userId(token);
    if (uid == null) return unauthorized();
    String number = text(body, "order_number");
    if (number.isEmpty()) return BusinessResponse.error("请选择支付订单!");
    List<Map<String, Object>> details = orders.installmentDetails(number);
    return BusinessResponse.success("操作成功!", details);
  }

  @PostMapping("/installment")
  @Transactional
  public BusinessResponse<?> installment(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long uid = tokens.userId(token);
    if (uid == null) return unauthorized();
    String number = text(body, "order_number");
    long childId = longValue(body, "should_id", 0);
    if (number.isEmpty() || childId == 0) return BusinessResponse.error("请选择支付订单!");
    Map<String, Object> child = orders.lockInstallment(childId);
    if (child == null || child.isEmpty()) return BusinessResponse.error("订单支付失败!");
    String childNumber = orderNumber();
    if (orders.assignInstallmentNumber(childId, childNumber, now()) != 1)
      return BusinessResponse.error("订单支付失败!");
    Map<String, Object> payment = new LinkedHashMap<>();
    payment.put("number", childNumber);
    payment.put("order_name", "租车第:" + child.get("num") + "期");
    payment.put("openid", orders.userOpenid(uid));
    payment.put("type", 4);
    payment.put("state", 1);
    return requestPayment(
        payment, money(child.get("price")), "/api/notify/installmentNotify", "分期");
  }

  @PostMapping("/buyCarPay")
  @Transactional
  public BusinessResponse<?> buyCarPay(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    return createSimpleOrder(token, body, 2);
  }

  @PostMapping("/maintainPay")
  @Transactional
  public BusinessResponse<?> maintainPay(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    return createSimpleOrder(token, body, 3);
  }

  @PostMapping({"/againLeaseCarPay", "/againBuyCarPay", "/againMaintainPay"})
  public BusinessResponse<?> repay(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body,
      javax.servlet.http.HttpServletRequest request) {
    Long uid = tokens.userId(token);
    if (uid == null) return unauthorized();
    String number = text(body, "order_number");
    if (number.isEmpty()) return BusinessResponse.error("请选择订单");
    Map<String, Object> order = orders.userPaymentOrder(number, uid);
    if (order == null || order.isEmpty()) return BusinessResponse.error("订单不存在");
    if (((Number) order.get("state")).intValue() != 1) return BusinessResponse.error("订单不是待支付状态");
    int expected =
        request.getRequestURI().contains("Lease")
            ? 1
            : request.getRequestURI().contains("Buy") ? 2 : 3;
    if (((Number) order.get("type")).intValue() != expected) return BusinessResponse.error("订单类型不匹配");
    BigDecimal amount =
        money(order.get("price"))
            .subtract(money(order.get("discounts_price")));
    return requestPayment(
        order, amount, notifyPath(expected), expected == 1 ? "租车:" : expected == 2 ? "购买:" : "维修:");
  }

  private BusinessResponse<?> createSimpleOrder(String token, Map<String, Object> body, int type) {
    Long uid = tokens.userId(token);
    if (uid == null) return unauthorized();
    int num = intValue(body, "num", 0);
    long serviceId = longValue(body, type == 2 ? "merchant_car_id" : "merchant_maintain_id", 0);
    if (serviceId == 0 || num <= 0)
      return BusinessResponse.error(type == 2 ? "请选择车辆！" : "请选择维修服务");
    String name = text(body, "name"),
        mobile = text(body, "mobile"),
        takeBackText = text(body, "take_back_time"),
        serviceImage = text(body, "service_image");
    if (name.isEmpty() || mobile.isEmpty() || takeBackText.isEmpty() || serviceImage.isEmpty())
      return BusinessResponse.error("请填完善订单信息！");
    int pickType = intValue(body, "pick_type", 1);
    if (type == 2 && !hasPositive(body, "pick_type"))
      return BusinessResponse.error("请填完善订单信息！");
    if (type == 2 && pickType == 3 && text(body, "pick_address").isEmpty())
      return BusinessResponse.error("请选择取车地址!");
    Map<String, Object> service = orders.simpleService(type, serviceId);
    if (service == null || service.isEmpty())
      return BusinessResponse.error(
          type == 2 ? "该车辆下架,请选择其他车辆" : "该服务已下架,请选择其他服务");
    BigDecimal price = money(service.get("price")).multiply(BigDecimal.valueOf(num));
    BigDecimal discount = BigDecimal.ZERO;
    long couponId = longValue(body, "coupon_id", 0);
    if (legacyTruthy(body == null ? null : body.get("coupon_id"))) {
      Map<String, Object> coupon = orders.usableCoupon(couponId, uid);
      if (coupon == null || coupon.isEmpty()) return BusinessResponse.error("优惠券无法使用!");
      if (price.compareTo(money(coupon.get("price"))) < 0)
        return BusinessResponse.error("优惠券无法使用!");
      discount = money(coupon.get("reduceprice"));
    }
    String number = orderNumber();
    long takeBack = epoch(takeBackText);
    if (takeBack <= 0) return BusinessResponse.error("服务时间格式错误");
    String orderName = service.get("name") + "*" + num;
    Map<String, Object> values = new LinkedHashMap<>();
    values.put("user_id", uid);
    values.put("merchant_id", service.get("merchant_id"));
    values.put("number", number);
    values.put("type", type);
    values.put("order_name", orderName);
    values.put("service_name", service.get("name"));
    values.put("service_image", serviceImage);
    values.put("merchant_car_id", type == 2 ? serviceId : null);
    values.put("merchant_service_id", type == 3 ? serviceId : null);
    values.put("pick_type", type == 2 ? pickType : null);
    values.put(
        "pick_address",
        type == 2 && body != null && body.containsKey("pick_address")
            ? text(body, "pick_address")
            : null);
    values.put("price", price);
    values.put("num", num);
    values.put("name", name);
    values.put("mobile", mobile);
    values.put("take_back_time", takeBack);
    values.put("coupon_id", couponId);
    values.put("discounts_price", discount);
    values.put("createtime", now());
    values.put("updatetime", now());
    orders.insertSimpleOrder(values);
    Map<String, Object> order = orders.paymentOrder(number);
    BigDecimal payable = price.subtract(discount);
    return requestPayment(order, payable, notifyPath(type), "购买:");
  }

  private BusinessResponse<?> requestPayment(
      Map<String, Object> order, BigDecimal amount, String notifyPath, String prefix) {
    return requestPayment(order, amount, notifyPath, prefix, false);
  }

  private BusinessResponse<?> requestPayment(
      Map<String, Object> order,
      BigDecimal amount,
      String notifyPath,
      String prefix,
      boolean rollbackOnFailure) {
    try {
      Map<String, String> config =
          pay.unifiedOrder(
              text(order, "openid"),
              order.get("number").toString(),
              prefix + order.get("order_name"),
              amount
                  .multiply(BigDecimal.valueOf(100))
                  .setScale(0, RoundingMode.HALF_UP)
                  .longValue(),
              notifyPath);
      return BusinessResponse.success("成功!", config);
    } catch (Exception e) {
      if (rollbackOnFailure && TransactionSynchronizationManager.isActualTransactionActive())
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
      return BusinessResponse.error("支付失败重新支付!");
    }
  }

  private static String notifyPath(int type) {
    return type == 1
        ? "/api/notify/leaseCarNotify"
        : type == 2 ? "/api/notify/buyCarNotify" : "/api/notify/maintainNotify";
  }

  @PostMapping("/list")
  public BusinessResponse<?> list(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long uid = tokens.userId(token);
    if (uid == null) return unauthorized();
    long page = positiveLong(body, "page", 1), size = positiveLong(body, "list_rows", 10);
    int type = nonNegativeInt(body, "type", 1), state = nonNegativeInt(body, "state", 0);
    List<Map<String, Object>> rows = orders.userOrders(uid, type, state, size, offset(page, size));
    return BusinessResponse.success("操作成功!", page(rows, orders.userOrdersCount(uid, type, state), page, size));
  }

  @PostMapping("/detail")
  public BusinessResponse<?> detail(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long uid = tokens.userId(token);
    if (uid == null) return unauthorized();
    String number = text(body, "order_number");
    if (number.isEmpty()) return BusinessResponse.error("请选择支付订单!");
    Map<String, Object> result = orders.userOrder(number, uid);
    if (result == null || result.isEmpty()) return BusinessResponse.error("订单不存在");
    result.put(
        "merchant_address",
        text(result, "merchant_administrative") + text(result, "merchant_address"));
    result.put("createtime", formatEpoch(result.get("createtime"), true));
    result.put("starttime", formatEpoch(result.get("starttime"), false));
    result.put("endtime", formatEpoch(result.get("endtime"), false));
    result.put("cancel_time", formatEpoch(result.get("cancel_time"), false));
    result.put("take_back_time", formatEpoch(result.get("take_back_time"), false));
    List<Map<String, Object>> guests = orders.userOrderGuests(((Number) result.get("id")).longValue());
    if (!guests.isEmpty()) guests.remove(0);
    result.put("guest", guests);
    return BusinessResponse.success("操作成功!", result);
  }

  @PostMapping("/cancel")
  @Transactional
  public BusinessResponse<?> cancel(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long uid = tokens.userId(token);
    if (uid == null) return unauthorized();
    String number = text(body, "order_number");
    if (number.isEmpty()) return BusinessResponse.error("请选择支付订单!");
    Map<String, Object> order = orders.lockUserOrderForCancel(number, uid);
    if (order == null || order.isEmpty()) return BusinessResponse.error("订单不存在!");
    if (((Number) order.get("state")).intValue() != 1) return BusinessResponse.error("订单已支付无法取消!");
    long currentTime = now();
    if (orders.cancelUserOrder(((Number) order.get("id")).longValue(), currentTime) != 1)
      return BusinessResponse.error("订单取消失败!");
    if (order.get("merchant_car_id") != null)
      orders.restoreMerchantCarStock(
          ((Number) order.get("merchant_car_id")).longValue(), currentTime);
    return BusinessResponse.success("操作成功!", 1);
  }

  @PostMapping("/backCar")
  @Transactional
  public BusinessResponse<?> backCar(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long uid = tokens.userId(token);
    if (uid == null) return unauthorized();
    String number = text(body, "order_number");
    if (number.isEmpty()) return BusinessResponse.error("请选择支付订单!");
    Map<String, Object> order = orders.lockUserOrderForReturn(number, uid);
    if (order == null || order.isEmpty()) return BusinessResponse.error("订单不存在");
    if (((Number) order.get("state")).intValue() != 3) return BusinessResponse.error("订单不在进行中无法还车!");
    int pick = ((Number) order.get("pick_state")).intValue();
    if (pick == 2 || pick == 3) return BusinessResponse.error("车辆已提交还车，请等待管理员审核!");
    long orderId = ((Number) order.get("id")).longValue();
    if (orders.returnUserCar(
        orderId,
        text(body, "back_image"),
        text(body, "back_content"),
        now()) != 1) return BusinessResponse.error("还车失败!");
    try {
      notifications.notifyMerchantReturnRequested(orderId);
    } catch (RuntimeException ignored) {
      // SMS is a best-effort side effect and must not roll back a successful return request.
    }
    return BusinessResponse.success("还车成功!", 1);
  }

  private static Map<String, Object> page(
      List<Map<String, Object>> rows, int total, long current, long size) {
    Map<String, Object> p = new LinkedHashMap<>();
    p.put("total", total);
    p.put("per_page", size);
    p.put("current_page", current);
    p.put("last_page", total == 0 ? 0 : 1 + (total - 1L) / size);
    p.put("data", rows);
    return p;
  }

  private static int intValue(Map<String, Object> b, String k, int d) {
    try {
      int v = b == null || b.get(k) == null ? d : Integer.parseInt(b.get(k).toString());
      return v > 0 ? v : d;
    } catch (Exception e) {
      return d;
    }
  }

  private static int nonNegativeInt(Map<String, Object> body, String key, int defaultValue) {
    long value = longValue(body, key, defaultValue);
    return value < 0 || value > Integer.MAX_VALUE ? defaultValue : (int) value;
  }

  private static long longValue(Map<String, Object> b, String k, long d) {
    try {
      return b == null || b.get(k) == null ? d : Long.parseLong(b.get(k).toString());
    } catch (Exception e) {
      return d;
    }
  }

  private static long positiveLong(Map<String, Object> body, String key, long defaultValue) {
    long value = longValue(body, key, defaultValue);
    return value > 0 ? value : defaultValue;
  }

  private static long offset(long page, long size) {
    try {
      return Math.multiplyExact(page - 1, size);
    } catch (ArithmeticException exception) {
      return Long.MAX_VALUE;
    }
  }

  private static boolean hasPositive(Map<String, Object> body, String key) {
    return longValue(body, key, 0) > 0;
  }

  private static boolean legacyTruthy(Object value) {
    if (value == null) return false;
    if (value instanceof Boolean) return (Boolean) value;
    if (value instanceof Number)
      try {
        return new BigDecimal(value.toString()).compareTo(BigDecimal.ZERO) != 0;
      } catch (NumberFormatException ignored) {
        return true;
      }
    if (value instanceof Collection) return !((Collection<?>) value).isEmpty();
    if (value instanceof Map) return !((Map<?, ?>) value).isEmpty();
    String text = value.toString();
    return !text.isEmpty() && !"0".equals(text);
  }

  private static String text(Map<String, Object> b, String k) {
    return b == null || b.get(k) == null ? "" : b.get(k).toString().trim();
  }

  private static long now() {
    return System.currentTimeMillis() / 1000;
  }

  private String orderNumber() {
    for (int tries = 0; tries < 10; tries++) {
      StringBuilder n = new StringBuilder();
      for (int i = 0; i < 6; i++) n.append(random.nextInt(10));
      n.append(System.currentTimeMillis() / 1000);
      for (int i = 0; i < 6; i++) n.append(random.nextInt(10));
      if (orders.orderNumberCount(n.toString()) == 0) return n.toString();
    }
    throw new IllegalStateException("订单号生成失败");
  }

  @SuppressWarnings("unchecked")
  private static List<Map<String, Object>> guestList(Object value) {
    if (!(value instanceof List)) return Collections.emptyList();
    List<Map<String, Object>> out = new java.util.ArrayList<>();
    for (Object item : (List<?>) value)
      if (item instanceof Map) out.add((Map<String, Object>) item);
    return out;
  }

  private void insertGuest(
      long uid, long orderId, String name, String mobile, String idcard, BigDecimal insure) {
    orders.insertOrderGuest(uid, orderId, name, mobile, idcard, insure, now());
  }

  private static long epoch(String value) {
    if (value.matches("^\\d+$")) {
      try {
        return Long.parseLong(value);
      } catch (NumberFormatException ignored) {
      }
    }
    LocalDateTime time = parseTime(value);
    return time == null ? 0 : time.atZone(ZoneId.systemDefault()).toEpochSecond();
  }

  private BigDecimal configMoney(String name) {
    return money(orders.configValue(name));
  }

  private static BigDecimal money(Object value) {
    try {
      return value == null
          ? BigDecimal.ZERO
          : new BigDecimal(value.toString()).setScale(2, RoundingMode.HALF_UP);
    } catch (Exception e) {
      return BigDecimal.ZERO;
    }
  }

  private static double number(Object value) {
    try {
      return value == null ? 0 : Double.parseDouble(value.toString());
    } catch (Exception e) {
      return 0;
    }
  }

  private static LocalDateTime parseTime(String value) {
    if (value.isEmpty()) return null;
    for (DateTimeFormatter f :
        new DateTimeFormatter[] {
          DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
          DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
          DateTimeFormatter.ISO_LOCAL_DATE_TIME
        })
      try {
        return LocalDateTime.parse(value, f);
      } catch (DateTimeParseException ignored) {
      }
    try {
      return LocalDate.parse(value).atStartOfDay();
    } catch (Exception ignored) {
      return null;
    }
  }

  private static long leaseUnits(LocalDateTime start, LocalDateTime end, long secondsPerUnit) {
    long seconds = Duration.between(start, end).getSeconds();
    return Math.max(1, (seconds + secondsPerUnit - 1) / secondsPerUnit);
  }

  private static String formatEpoch(Object value) {
    return formatEpoch(value, true);
  }

  private static String formatEpoch(Object value, boolean seconds) {
    if (!(value instanceof Number) || ((Number) value).longValue() <= 0) return "";
    return Instant.ofEpochSecond(((Number) value).longValue())
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern(seconds ? "yyyy-MM-dd HH:mm:ss" : "yyyy-MM-dd HH:mm"));
  }

  private static String textValue(Object value) {
    return value == null ? "" : value.toString().trim();
  }

  private static BusinessResponse<?> unauthorized() {
    if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes) {
      HttpServletResponse response =
          ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
      if (response != null) response.setStatus(HttpStatus.UNAUTHORIZED.value());
    }
    return new BusinessResponse<>(401, "请登录后操作", now(), null);
  }

  private static double haversine(double lat1, double lon1, double lat2, double lon2) {
    double p1 = Math.toRadians(lat1),
        p2 = Math.toRadians(lat2),
        dp = Math.toRadians(lat2 - lat1),
        dl = Math.toRadians(lon2 - lon1);
    double a =
        Math.sin(dp / 2) * Math.sin(dp / 2)
            + Math.cos(p1) * Math.cos(p2) * Math.sin(dl / 2) * Math.sin(dl / 2);
    return 6371 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  }

  private static BigDecimal servicePrice(Map<String, Object> m, double km) {
    for (int i = 1; i <= 4; i++) {
      double boundary = number(m.get("service_distance" + i));
      if (km < boundary) return money(m.get("service_money" + i));
    }
    return money(m.get("service_money5"));
  }

  private static String serviceMoney(Map<String, Object> m) {
    return m.get("service_distance1") + "公里内费用￥" + money(m.get("service_money1")).stripTrailingZeros().toPlainString()
        + "，" + m.get("service_distance1") + "-" + m.get("service_distance2") + "公里内费用￥" + money(m.get("service_money2")).stripTrailingZeros().toPlainString()
        + "，" + m.get("service_distance2") + "-" + m.get("service_distance3") + "公里内费用￥" + money(m.get("service_money3")).stripTrailingZeros().toPlainString()
        + "，" + m.get("service_distance3") + "-" + m.get("service_distance4") + "公里内费用￥" + money(m.get("service_money4")).stripTrailingZeros().toPlainString()
        + "，" + m.get("service_distance4") + "公里外费用￥" + money(m.get("service_money5")).stripTrailingZeros().toPlainString();
  }
}
