package com.feimao.controller.admin;

import com.feimao.common.BusinessResponse;
import com.feimao.mapper.AdminLogMapper;
import com.feimao.mapper.OrderFinanceAdminMapper;
import com.feimao.service.AdminSessionService;
import com.feimao.service.InsuranceService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.transaction.annotation.Transactional;
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
@RequestMapping("/api/admin/business")
public class OrderFinanceAdminController {
  private final OrderFinanceAdminMapper financeAdmin;
  private final AdminLogMapper adminLogs;
  private final AdminSessionService sessions;
  private final InsuranceService insurance;

  public OrderFinanceAdminController(
      OrderFinanceAdminMapper financeAdmin,
      AdminLogMapper adminLogs,
      AdminSessionService sessions,
      InsuranceService insurance) {
    this.financeAdmin = financeAdmin;
    this.adminLogs = adminLogs;
    this.sessions = sessions;
    this.insurance = insurance;
  }

  @GetMapping("/orders")
  public BusinessResponse<?> orders(
      @RequestHeader(value = "token", required = false) String token,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String orderNumber,
      @RequestParam(required = false) String userName,
      @RequestParam(required = false) String merchantName,
      @RequestParam(required = false) String orderName,
      @RequestParam(required = false) String customerName,
      @RequestParam(required = false) String mobile,
      @RequestParam(required = false) String leaseDuration,
      @RequestParam(required = false) Integer type,
      @RequestParam(required = false) Integer state,
      @RequestParam(required = false) Long merchantId,
      @RequestParam(required = false) Integer leaseType,
      @RequestParam(required = false) Integer sourceType,
      @RequestParam(required = false) BigDecimal priceMin,
      @RequestParam(required = false) BigDecimal priceMax,
      @RequestParam(required = false) BigDecimal discountMin,
      @RequestParam(required = false) BigDecimal discountMax,
      @RequestParam(required = false) Long createdFrom,
      @RequestParam(required = false) Long createdTo,
      @RequestParam(required = false) Long startFrom,
      @RequestParam(required = false) Long startTo,
      @RequestParam(required = false) Long endFrom,
      @RequestParam(required = false) Long endTo,
      @RequestParam(required = false) Long takeBackFrom,
      @RequestParam(required = false) Long takeBackTo,
      @RequestParam(required = false) Long backFrom,
      @RequestParam(required = false) Long backTo,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int limit) {
    if (adminId(token) == null) return unauthorized();
    page = positivePage(page);
    limit = pageSize(limit);
    String search = hasText(keyword) ? keyword.trim() : "";
    Map<String, Long> ranges = new LinkedHashMap<>();
    ranges.put("startFrom", startFrom); ranges.put("startTo", startTo); ranges.put("endFrom", endFrom); ranges.put("endTo", endTo);
    ranges.put("takeBackFrom", takeBackFrom); ranges.put("takeBackTo", takeBackTo); ranges.put("backFrom", backFrom); ranges.put("backTo", backTo);
    int total = financeAdmin.orderCount(search, text(orderNumber), text(userName), text(merchantName), text(orderName), text(customerName), text(mobile), text(leaseDuration), type, state, merchantId, leaseType, sourceType, priceMin, priceMax, discountMin, discountMax, createdFrom, createdTo, ranges);
    List<Map<String, Object>> rows =
        financeAdmin.orders(search, text(orderNumber), text(userName), text(merchantName), text(orderName), text(customerName), text(mobile), text(leaseDuration), type, state, merchantId, leaseType, sourceType, priceMin, priceMax, discountMin, discountMax, createdFrom, createdTo, ranges, limit, (page - 1) * limit);
    Map<String, Object> result = page(rows, total, page, limit);
    result.put("summary", financeAdmin.orderSummary(search, text(orderNumber), text(userName), text(merchantName), text(orderName), text(customerName), text(mobile), text(leaseDuration), type, state, merchantId, leaseType, sourceType, priceMin, priceMax, discountMin, discountMax, createdFrom, createdTo, ranges));
    return BusinessResponse.success(result);
  }

  /** 保留原有内部调用签名；新增筛选条件仅扩展 HTTP 入参，不破坏既有调用方。 */
  public BusinessResponse<?> orders(
      String token, String keyword, Integer type, Integer state, Long merchantId, int page, int limit) {
    return orders(token, keyword, null, null, null, null, null, null, null, type, state, merchantId, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, page, limit);
  }

  @GetMapping("/orders/{id}")
  public BusinessResponse<?> orderDetail(
      @RequestHeader(value = "token", required = false) String token, @PathVariable long id) {
    if (adminId(token) == null) return unauthorized();
    Map<String, Object> row = financeAdmin.order(id);
    if (row == null || row.isEmpty()) return BusinessResponse.error("订单不存在");
    Map<String, Object> result = new LinkedHashMap<>();
    String[] fields = {"number","state","cash_state","pick_state","lease_type","user_id","merchant_id","type","order_name","service_name","service_image","starttime","endtime","pick_type","address_name","address_phone","pick_address","price","cash_price","lease_money","pick_single","pick_price","insure_money","discounts_price","name","mobile","idcard","lease_duration","back_image","back_content","return_type","refund_cash_price","refund_number","createtime","paytime","transaction_id","cancel_time","cancel_refund_price","cancel_refund_number","source_type"};
    for (String field : fields) result.put(field, row.get(field));
    result.put("user_id", row.get("user_nickname"));
    result.put("merchant_id", row.get("merchant_name"));
    result.put("state", label(row.get("state"), "待支付", "已支付", "进行中", "还车中", "已完成", "已取消"));
    result.put("cash_state", label(row.get("cash_state"), "无押金", "待退还", "已原路退还"));
    result.put("pick_state", label(row.get("pick_state"), "待还车", "审核中", "已确认"));
    result.put("lease_type", label(row.get("lease_type"), "时租", "日租", "月租"));
    result.put("type", label(row.get("type"), "租车", "购车"));
    result.put("source_type", label(row.get("source_type"), "普通下单", "扫门店码下单", "扫酒店码下单"));
    int pickType = number(row.get("pick_type"));
    result.put("pick_type", pickType == 1 ? "到店取车" : pickType == 3 ? "送车上门" : row.get("pick_type"));
    if (pickType == 1) result.remove("pick_single");
    else result.put("pick_single", label(row.get("pick_single"), "单程", "双程"));
    result.put("return_type", label(row.get("return_type"), "无损", "有损"));
    if (number(row.get("state")) != 6) {
      result.remove("cancel_time"); result.remove("cancel_refund_price"); result.remove("cancel_refund_number");
    }
    String backImages = row.get("back_image") == null ? "" : String.valueOf(row.get("back_image")).trim();
    result.put("back_image", backImages.isEmpty() ? java.util.Collections.emptyList() : java.util.Arrays.asList(backImages.split(",")));
    return BusinessResponse.success(result);
  }

  @DeleteMapping("/orders/{id}")
  @Transactional
  public BusinessResponse<?> deleteOrder(
      @RequestHeader(value = "token", required = false) String token, @PathVariable long id) {
    Long admin = adminId(token);
    if (admin == null) return unauthorized();
    if (financeAdmin.deleteOrder(id, now()) != 1) return BusinessResponse.error("订单不存在");
    log(admin, "/api/admin/business/orders/" + id, "删除订单", String.valueOf(id));
    return BusinessResponse.success(1);
  }

  @DeleteMapping("/orders")
  @Transactional
  public BusinessResponse<?> deleteOrders(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long admin = adminId(token);
    if (admin == null) return unauthorized();
    List<Long> ids = idList(body == null ? null : body.get("ids"));
    if (ids.isEmpty()) return BusinessResponse.error("请选择要删除的订单");
    int changed = financeAdmin.deleteOrders(ids, now());
    if (changed == 0) return BusinessResponse.error("订单不存在");
    log(admin, "/api/admin/business/orders", "批量删除订单", ids.toString());
    return BusinessResponse.success(changed);
  }

  @GetMapping("/order-guests")
  public BusinessResponse<?> orderGuests(
      @RequestHeader(value = "token", required = false) String token,
      @RequestParam(required = false) Long orderId,
      @RequestParam(required = false) String userName,
      @RequestParam(required = false) String orderNumber,
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String mobile,
      @RequestParam(required = false) String idcard,
      @RequestParam(required = false) String bizOrderSn,
      @RequestParam(required = false) Integer insureState,
      @RequestParam(required = false) Long createdFrom,
      @RequestParam(required = false) Long createdTo,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int limit) {
    if (adminId(token) == null) return unauthorized();
    page = positivePage(page);
    limit = pageSize(limit);
    int total = financeAdmin.guestCount(orderId, text(userName), text(orderNumber), text(name), text(mobile), text(idcard), text(bizOrderSn), insureState, createdFrom, createdTo);
    BigDecimal insureMoney = financeAdmin.guestInsuranceSum(orderId, text(userName), text(orderNumber), text(name), text(mobile), text(idcard), text(bizOrderSn), insureState, createdFrom, createdTo);
    List<Map<String, Object>> rows =
        financeAdmin.guests(orderId, text(userName), text(orderNumber), text(name), text(mobile), text(idcard), text(bizOrderSn), insureState, createdFrom, createdTo, limit, (page - 1) * limit);
    Map<String, Object> result = page(rows, total, page, limit);
    result.put("insure_money", insureMoney);
    return BusinessResponse.success(result);
  }

  public BusinessResponse<?> orderGuests(
      String token, Long orderId, Integer insureState, int page, int limit) {
    return orderGuests(token, orderId, null, null, null, null, null, null, insureState, null, null, page, limit);
  }

  @GetMapping("/order-guests/{id}/policy")
  public BusinessResponse<?> orderGuestPolicy(
      @RequestHeader(value = "token", required = false) String token, @PathVariable long id) {
    if (adminId(token) == null) return unauthorized();
    try {
      return BusinessResponse.success(insurance.queryGuestPolicy(id));
    } catch (IllegalArgumentException | IllegalStateException exception) {
      return BusinessResponse.error(exception.getMessage());
    }
  }

  @PostMapping("/order-guests")
  @Transactional
  public BusinessResponse<?> addOrderGuest(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long admin = adminId(token);
    if (admin == null) return unauthorized();
    long orderId = longValue(body, "order_id", 0);
    Map<String, Object> order = financeAdmin.orderForGuest(orderId);
    if (order == null || order.isEmpty()) return BusinessResponse.error("订单不存在");
    if (((Number) order.get("type")).intValue() != 1)
      return BusinessResponse.error("只有租车订单可以添加同行人");
    String error = guestValidation(body);
    if (error != null) return BusinessResponse.error(error);
    long now = now();
    Map<String, Object> values = new LinkedHashMap<>();
    values.put("user_id", order.get("user_id"));
    values.put("order_id", orderId);
    values.put("name", value(body, "name"));
    values.put("mobile", value(body, "mobile"));
    values.put("idcard", value(body, "idcard"));
    values.put("createtime", now);
    values.put("updatetime", now);
    values.put("insure_money", money(body == null ? null : body.get("insure_money")));
    financeAdmin.insertGuest(values);
    long id = ((Number) values.get("id")).longValue();
    log(admin, "/api/admin/business/order-guests", "新增订单同行人", String.valueOf(id));
    return BusinessResponse.success(java.util.Collections.singletonMap("id", id));
  }

  @PutMapping("/order-guests/{id}")
  @Transactional
  public BusinessResponse<?> editOrderGuest(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable long id,
      @RequestBody(required = false) Map<String, Object> body) {
    Long admin = adminId(token);
    if (admin == null) return unauthorized();
    String error = guestValidation(body);
    if (error != null) return BusinessResponse.error(error);
    int changed =
        financeAdmin.updateGuest(
            id,
            value(body, "name"),
            value(body, "mobile"),
            value(body, "idcard"),
            money(body == null ? null : body.get("insure_money")),
            now());
    if (changed == 0) return BusinessResponse.error("同行人不存在");
    log(admin, "/api/admin/business/order-guests/" + id, "编辑订单同行人", String.valueOf(id));
    return BusinessResponse.success(1);
  }

  @DeleteMapping("/order-guests/{id}")
  @Transactional
  public BusinessResponse<?> deleteOrderGuest(
      @RequestHeader(value = "token", required = false) String token, @PathVariable long id) {
    Long admin = adminId(token);
    if (admin == null) return unauthorized();
    long now = now();
    int changed = financeAdmin.deleteGuest(id, now);
    if (changed == 0) return BusinessResponse.error("同行人不存在");
    log(admin, "/api/admin/business/order-guests/" + id, "删除订单同行人", String.valueOf(id));
    return BusinessResponse.success(1);
  }

  @GetMapping("/order-guests/recycle")
  public BusinessResponse<?> recycledOrderGuests(
      @RequestHeader(value = "token", required = false) String token,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int limit) {
    if (adminId(token) == null) return unauthorized();
    page = positivePage(page);
    limit = pageSize(limit);
    return BusinessResponse.success(
        page(
            financeAdmin.recycledGuests(limit, (page - 1) * limit),
            financeAdmin.recycledGuestCount(),
            page,
            limit));
  }

  @PostMapping("/order-guests/restore/{id}")
  @Transactional
  public BusinessResponse<?> restoreOrderGuest(
      @RequestHeader(value = "token", required = false) String token, @PathVariable long id) {
    Long admin = adminId(token);
    if (admin == null) return unauthorized();
    if (financeAdmin.restoreGuest(id, now()) != 1)
      return BusinessResponse.error("回收同行人不存在");
    log(
        admin,
        "/api/admin/business/order-guests/restore/" + id,
        "恢复订单同行人",
        String.valueOf(id));
    return BusinessResponse.success(1);
  }

  @DeleteMapping("/order-guests/destroy/{id}")
  @Transactional
  public BusinessResponse<?> destroyOrderGuest(
      @RequestHeader(value = "token", required = false) String token, @PathVariable long id) {
    Long admin = adminId(token);
    if (admin == null) return unauthorized();
    if (financeAdmin.destroyGuest(id) != 1)
      return BusinessResponse.error("回收同行人不存在");
    log(
        admin,
        "/api/admin/business/order-guests/destroy/" + id,
        "彻底删除订单同行人",
        String.valueOf(id));
    return BusinessResponse.success(1);
  }

  @PostMapping("/order-guests/restore")
  @Transactional
  public BusinessResponse<?> restoreOrderGuests(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long admin = adminId(token);
    if (admin == null) return unauthorized();
    List<Long> ids = idList(body == null ? null : body.get("ids"));
    int changed = financeAdmin.restoreGuests(ids, now());
    if (changed == 0) return BusinessResponse.error("回收站中没有可恢复的同行人");
    log(
        admin,
        "/api/admin/business/order-guests/restore",
        "批量恢复订单同行人",
        ids.isEmpty() ? "全部" : ids.toString());
    return BusinessResponse.success(changed);
  }

  @DeleteMapping("/order-guests/destroy")
  @Transactional
  public BusinessResponse<?> destroyOrderGuests(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long admin = adminId(token);
    if (admin == null) return unauthorized();
    List<Long> ids = idList(body == null ? null : body.get("ids"));
    int changed = financeAdmin.destroyGuests(ids);
    if (changed == 0) return BusinessResponse.error("回收站中没有可彻底删除的同行人");
    log(
        admin,
        "/api/admin/business/order-guests/destroy",
        "批量彻底删除订单同行人",
        ids.isEmpty() ? "全部" : ids.toString());
    return BusinessResponse.success(changed);
  }

  private static String guestValidation(Map<String, Object> body) {
    if (value(body, "name").isEmpty()
        || value(body, "mobile").isEmpty()
        || value(body, "idcard").isEmpty()) return "姓名、手机号和身份证号不能为空";
    return null;
  }

  @GetMapping("/installments")
  public BusinessResponse<?> installments(
      @RequestHeader(value = "token", required = false) String token,
      @RequestParam(required = false) Long userId,
      @RequestParam(required = false) String number,
      @RequestParam(required = false) String childNumber,
      @RequestParam(required = false) Integer num,
      @RequestParam(required = false) BigDecimal priceMin,
      @RequestParam(required = false) BigDecimal priceMax,
      @RequestParam(required = false) BigDecimal payPriceMin,
      @RequestParam(required = false) BigDecimal payPriceMax,
      @RequestParam(required = false) String transactionId,
      @RequestParam(required = false) Long payFrom,
      @RequestParam(required = false) Long payTo,
      @RequestParam(required = false) Long createdFrom,
      @RequestParam(required = false) Long createdTo,
      @RequestParam(required = false) Long updatedFrom,
      @RequestParam(required = false) Long updatedTo,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int limit) {
    if (adminId(token) == null) return unauthorized();
    page = positivePage(page);
    limit = pageSize(limit);
    Map<String, Long> times = new LinkedHashMap<>();
    times.put("payFrom", payFrom); times.put("payTo", payTo);
    times.put("createdFrom", createdFrom); times.put("createdTo", createdTo);
    times.put("updatedFrom", updatedFrom); times.put("updatedTo", updatedTo);
    int total = financeAdmin.installmentCount(userId, text(number), text(childNumber), num, priceMin, priceMax, payPriceMin, payPriceMax, text(transactionId), times);
    List<Map<String, Object>> rows = financeAdmin.installments(userId, text(number), text(childNumber), num, priceMin, priceMax, payPriceMin, payPriceMax, text(transactionId), times, limit, (page - 1) * limit);
    return BusinessResponse.success(page(rows, total, page, limit));
  }

  public BusinessResponse<?> installments(
      String token, String number, Integer state, int page, int limit) {
    return installments(token, null, number, null, null, null, null, null, null, null, null, null, null, null, null, null, page, limit);
  }

  @PostMapping("/installments")
  @Transactional
  public BusinessResponse<?> addInstallment(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long admin = adminId(token);
    if (admin == null) return unauthorized();
    if (body == null || body.isEmpty()) return BusinessResponse.error("请求参数不能为空");
    Map<String, Object> values = installmentValues(body, now());
    financeAdmin.insertInstallment(values);
    long id = ((Number) values.get("id")).longValue();
    log(admin, "/api/admin/business/installments", "新增订单分期", String.valueOf(id));
    return BusinessResponse.success(java.util.Collections.singletonMap("id", id));
  }

  @PutMapping("/installments/{id}")
  @Transactional
  public BusinessResponse<?> editInstallment(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable long id,
      @RequestBody(required = false) Map<String, Object> body) {
    Long admin = adminId(token);
    if (admin == null) return unauthorized();
    if (body == null || body.isEmpty()) return BusinessResponse.error("请求参数不能为空");
    if (financeAdmin.installment(id) == null) return BusinessResponse.error("分期账单不存在");
    if (financeAdmin.updateInstallment(id, installmentValues(body, now())) != 1)
      return BusinessResponse.error("分期账单不存在");
    log(admin, "/api/admin/business/installments/" + id, "编辑订单分期", String.valueOf(id));
    return BusinessResponse.success(1);
  }

  @DeleteMapping("/installments/{id}")
  @Transactional
  public BusinessResponse<?> deleteInstallment(
      @RequestHeader(value = "token", required = false) String token, @PathVariable long id) {
    Long admin = adminId(token);
    if (admin == null) return unauthorized();
    if (financeAdmin.deleteInstallment(id) != 1) return BusinessResponse.error("分期账单不存在");
    log(admin, "/api/admin/business/installments/" + id, "删除订单分期", String.valueOf(id));
    return BusinessResponse.success(1);
  }

  private static Map<String, Object> installmentValues(Map<String, Object> body, long current) {
    Map<String, Object> values = new LinkedHashMap<>();
    values.put("user_id", longValue(body, "user_id", 0));
    values.put("number", value(body, "number"));
    values.put("child_number", value(body, "child_number"));
    values.put("num", longValue(body, "num", 0));
    values.put("price", money(body.get("price")));
    values.put("pay_price", money(body.get("pay_price")));
    values.put("transaction_id", value(body, "transaction_id"));
    values.put("paytime", longValue(body, "paytime", 0));
    values.put("createtime", current);
    values.put("updatetime", current);
    return values;
  }

  @PostMapping("/user-coupons")
  @Transactional
  public BusinessResponse<?> addUserCoupon(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long admin = adminId(token);
    if (admin == null) return unauthorized();
    long now = now();
    Map<String, Object> values = couponValues(body, now);
    financeAdmin.insertUserCoupon(values);
    long id = ((Number) values.get("id")).longValue();
    log(admin, "/api/admin/business/user-coupons", "发放用户优惠券", String.valueOf(id));
    return BusinessResponse.success(java.util.Collections.singletonMap("id", id));
  }

  @PutMapping("/user-coupons/{id}")
  @Transactional
  public BusinessResponse<?> editUserCoupon(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable long id,
      @RequestBody(required = false) Map<String, Object> body) {
    Long admin = adminId(token);
    if (admin == null) return unauthorized();
    int changed = financeAdmin.updateUserCoupon(id, couponValues(body, now()));
    if (changed == 0) return BusinessResponse.error("优惠券不存在");
    log(admin, "/api/admin/business/user-coupons/" + id, "编辑用户优惠券", String.valueOf(id));
    return BusinessResponse.success(1);
  }

  @DeleteMapping("/user-coupons/{id}")
  @Transactional
  public BusinessResponse<?> deleteUserCoupon(
      @RequestHeader(value = "token", required = false) String token, @PathVariable long id) {
    Long admin = adminId(token);
    if (admin == null) return unauthorized();
    long now = now();
    int changed = financeAdmin.deleteUserCoupon(id, now);
    if (changed == 0) return BusinessResponse.error("优惠券不存在");
    log(admin, "/api/admin/business/user-coupons/" + id, "删除用户优惠券", String.valueOf(id));
    return BusinessResponse.success(1);
  }

  @DeleteMapping("/user-coupons")
  @Transactional
  public BusinessResponse<?> deleteUserCoupons(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long admin = adminId(token);
    if (admin == null) return unauthorized();
    List<Long> ids = idList(body == null ? null : body.get("ids"));
    if (ids.isEmpty()) return BusinessResponse.error("请选择要删除的优惠券");
    int changed = financeAdmin.deleteUserCoupons(ids, now());
    if (changed == 0) return BusinessResponse.error("优惠券不存在");
    log(admin, "/api/admin/business/user-coupons", "批量删除用户优惠券", ids.toString());
    return BusinessResponse.success(changed);
  }

  @GetMapping("/user-coupons/recycle")
  public BusinessResponse<?> recycledUserCoupons(
      @RequestHeader(value = "token", required = false) String token,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int limit) {
    if (adminId(token) == null) return unauthorized();
    page = positivePage(page);
    limit = pageSize(limit);
    return BusinessResponse.success(page(
        financeAdmin.recycledUserCoupons(limit, (page - 1) * limit),
        financeAdmin.recycledUserCouponCount(), page, limit));
  }

  @PostMapping("/user-coupons/restore/{id}")
  @Transactional
  public BusinessResponse<?> restoreUserCoupon(
      @RequestHeader(value = "token", required = false) String token, @PathVariable long id) {
    Long admin = adminId(token);
    if (admin == null) return unauthorized();
    if (financeAdmin.restoreUserCoupon(id, now()) != 1) return BusinessResponse.error("回收优惠券不存在");
    log(admin, "/api/admin/business/user-coupons/restore/" + id, "恢复用户优惠券", String.valueOf(id));
    return BusinessResponse.success(1);
  }

  @DeleteMapping("/user-coupons/destroy/{id}")
  @Transactional
  public BusinessResponse<?> destroyUserCoupon(
      @RequestHeader(value = "token", required = false) String token, @PathVariable long id) {
    Long admin = adminId(token);
    if (admin == null) return unauthorized();
    if (financeAdmin.destroyUserCoupon(id) != 1) return BusinessResponse.error("回收优惠券不存在");
    log(admin, "/api/admin/business/user-coupons/destroy/" + id, "彻底删除用户优惠券", String.valueOf(id));
    return BusinessResponse.success(1);
  }

  @PostMapping("/user-coupons/restore")
  @Transactional
  public BusinessResponse<?> restoreUserCoupons(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long admin = adminId(token);
    if (admin == null) return unauthorized();
    List<Long> ids = idList(body == null ? null : body.get("ids"));
    int changed = ids.isEmpty() ? financeAdmin.restoreAllUserCoupons(now()) : financeAdmin.restoreUserCoupons(ids, now());
    if (changed == 0) return BusinessResponse.error("回收站中没有可恢复的优惠券");
    log(admin, "/api/admin/business/user-coupons/restore", "批量恢复用户优惠券", ids.isEmpty() ? "全部" : ids.toString());
    return BusinessResponse.success(changed);
  }

  @DeleteMapping("/user-coupons/destroy")
  @Transactional
  public BusinessResponse<?> destroyUserCoupons(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long admin = adminId(token);
    if (admin == null) return unauthorized();
    List<Long> ids = idList(body == null ? null : body.get("ids"));
    int changed = ids.isEmpty() ? financeAdmin.destroyAllUserCoupons() : financeAdmin.destroyUserCoupons(ids);
    if (changed == 0) return BusinessResponse.error("回收站中没有可彻底删除的优惠券");
    log(admin, "/api/admin/business/user-coupons/destroy", "批量彻底删除用户优惠券", ids.isEmpty() ? "全部" : ids.toString());
    return BusinessResponse.success(changed);
  }

  private static Map<String, Object> couponValues(Map<String, Object> body, long current) {
    Map<String, Object> values = new LinkedHashMap<>();
    values.put("user_id", longValue(body, "user_id", 0));
    values.put("price", money(body == null ? null : body.get("price")));
    values.put("reduceprice", money(body == null ? null : body.get("reduceprice")));
    values.put("state", longValue(body, "state", 1));
    values.put("endtime", nullableLong(body, "endtime"));
    values.put("usetime", nullableLong(body, "usetime"));
    values.put("createtime", current);
    values.put("updatetime", current);
    return values;
  }

  @GetMapping("/finance/{kind}")
  public BusinessResponse<?> finance(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable String kind,
      @RequestParam(required = false) Long ownerId,
      @RequestParam(required = false) Integer type,
      @RequestParam(required = false) String nickname,
      @RequestParam(required = false) String outTradeNo,
      @RequestParam(required = false) String orderNumber,
      @RequestParam(required = false) BigDecimal amountMin,
      @RequestParam(required = false) BigDecimal amountMax,
      @RequestParam(required = false) Long createdFrom,
      @RequestParam(required = false) Long createdTo,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int limit) {
    if (adminId(token) == null) return unauthorized();
    if (!financeKind(kind)) return BusinessResponse.error("流水类型错误");
    page = positivePage(page);
    limit = pageSize(limit);
    int total;
    BigDecimal amount;
    List<Map<String, Object>> rows;
    Map<String, Object> platformSummary = null;
    if ("platform".equals(kind)) {
      total =
          financeAdmin.platformFinanceCount(
              ownerId,
              type,
              text(nickname),
              text(outTradeNo),
              amountMin,
              amountMax,
              createdFrom,
              createdTo);
      rows =
          financeAdmin.platformFinanceRows(
              ownerId,
              type,
              text(nickname),
              text(outTradeNo),
              amountMin,
              amountMax,
              createdFrom,
              createdTo,
              limit,
              (page - 1) * limit);
      platformSummary =
          financeAdmin.platformFinanceSummary(
              ownerId,
              type,
              text(nickname),
              text(outTradeNo),
              amountMin,
              amountMax,
              createdFrom,
              createdTo);
      amount = decimalValue(platformSummary, "money");
    } else if ("system".equals(kind)) {
      total =
          financeAdmin.systemFinanceCount(
              type, text(orderNumber), amountMin, amountMax, createdFrom, createdTo);
      rows =
          financeAdmin.systemFinanceRows(
              type,
              text(orderNumber),
              amountMin,
              amountMax,
              createdFrom,
              createdTo,
              limit,
              (page - 1) * limit);
      amount =
          financeAdmin.systemFinanceAmount(
              type, text(orderNumber), amountMin, amountMax, createdFrom, createdTo);
    } else {
      total = financeAdmin.financeCount(kind, ownerId, type);
      amount = financeAdmin.financeAmount(kind, ownerId, type);
      rows = financeAdmin.financeRows(kind, ownerId, type, limit, (page - 1) * limit);
    }
    decorateFinanceRows(kind, rows);
    Map<String, Object> result = page(rows, total, page, limit);
    result.put("amount", amount == null ? BigDecimal.ZERO : amount);
    if ("platform".equals(kind)) {
      Map<String, Object> extend = new LinkedHashMap<>();
      for (String field : new String[] {"money", "in_money", "out_money", "cash_money"}) {
        BigDecimal value = decimalValue(platformSummary, field);
        result.put(field, value);
        extend.put(field, value);
      }
      result.put("extend", extend);
    }
    if ("system".equals(kind)) {
      String configured = financeAdmin.systemMoney();
      result.put("system_money", configured == null ? "0" : configured);
      Map<String, Object> extend = new LinkedHashMap<>();
      extend.put("money", amount == null ? BigDecimal.ZERO : amount);
      extend.put("system_money", configured == null ? "0" : configured);
      result.put("extend", extend);
    }
    return BusinessResponse.success(result);
  }

  public BusinessResponse<?> finance(
      String token, String kind, Long ownerId, Integer type, int page, int limit) {
    return finance(
        token,
        kind,
        ownerId,
        type,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        page,
        limit);
  }

  @PostMapping("/finance/{kind}")
  @Transactional
  public BusinessResponse<?> addFinance(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable String kind,
      @RequestBody(required = false) Map<String, Object> body) {
    Long admin = adminId(token);
    if (admin == null) return unauthorized();
    if (!"merchant".equals(kind) && !"shop".equals(kind))
      return BusinessResponse.error("该流水不允许新增");
    Map<String, Object> values = financeValues(kind, body);
    String error = financeValidation(kind, values);
    if (error != null) return BusinessResponse.error(error);
    financeAdmin.insertFinanceRow(kind, values);
    long id = ((Number) values.get("id")).longValue();
    log(admin, "/api/admin/business/finance/" + kind, "新增资金流水", String.valueOf(id));
    return BusinessResponse.success(financeAdmin.financeRow(kind, id));
  }

  @PutMapping("/finance/{kind}/{id}")
  @Transactional
  public BusinessResponse<?> updateFinance(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable String kind,
      @PathVariable long id,
      @RequestBody(required = false) Map<String, Object> body) {
    Long admin = adminId(token);
    if (admin == null) return unauthorized();
    if (!"merchant".equals(kind) && !"shop".equals(kind))
      return BusinessResponse.error("该流水不允许修改");
    Map<String, Object> values = financeValues(kind, body);
    values.put("id", id);
    String error = financeValidation(kind, values);
    if (error != null) return BusinessResponse.error(error);
    if (financeAdmin.updateFinanceRow(kind, values) != 1) return BusinessResponse.error("流水不存在");
    log(admin, "/api/admin/business/finance/" + kind + "/" + id, "修改资金流水", String.valueOf(id));
    return BusinessResponse.success(financeAdmin.financeRow(kind, id));
  }

  @DeleteMapping("/finance/{kind}/{id}")
  @Transactional
  public BusinessResponse<?> deleteFinance(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable String kind,
      @PathVariable long id) {
    Long admin = adminId(token);
    if (admin == null) return unauthorized();
    if (!financeKind(kind)) return BusinessResponse.error("流水类型错误");
    if (financeAdmin.deleteFinanceRow(kind, id, now()) != 1) return BusinessResponse.error("流水不存在");
    log(admin, "/api/admin/business/finance/" + kind + "/" + id, "删除资金流水", String.valueOf(id));
    return BusinessResponse.success(1);
  }

  @DeleteMapping("/finance/{kind}")
  @Transactional
  public BusinessResponse<?> deleteFinances(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable String kind,
      @RequestBody(required = false) Map<String, Object> body) {
    Long admin = adminId(token);
    if (admin == null) return unauthorized();
    if (!financeKind(kind)) return BusinessResponse.error("流水类型错误");
    List<Long> ids = idList(body == null ? null : body.get("ids"));
    if (ids.isEmpty()) return BusinessResponse.error("请选择要删除的流水");
    int changed = financeAdmin.deleteFinanceRows(kind, ids, now());
    if (changed == 0) return BusinessResponse.error("流水不存在");
    log(admin, "/api/admin/business/finance/" + kind, "批量删除资金流水", ids.toString());
    return BusinessResponse.success(changed);
  }

  @GetMapping("/finance/{kind}/recycle")
  public BusinessResponse<?> recycledFinance(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable String kind,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int limit) {
    if (adminId(token) == null) return unauthorized();
    if (!financeKind(kind)) return BusinessResponse.error("流水类型错误");
    page = positivePage(page);
    limit = pageSize(limit);
    List<Map<String, Object>> rows =
        financeAdmin.recycledFinanceRows(kind, limit, (page - 1) * limit);
    decorateFinanceRows(kind, rows);
    return BusinessResponse.success(
        page(rows, financeAdmin.recycledFinanceCount(kind), page, limit));
  }

  @PostMapping("/finance/{kind}/restore/{id}")
  @Transactional
  public BusinessResponse<?> restoreFinance(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable String kind,
      @PathVariable long id) {
    Long admin = adminId(token);
    if (admin == null) return unauthorized();
    if (!financeKind(kind)) return BusinessResponse.error("流水类型错误");
    if (financeAdmin.restoreFinanceRow(kind, id, now()) != 1) return BusinessResponse.error("回收记录不存在");
    log(admin, "/api/admin/business/finance/" + kind + "/restore/" + id, "恢复资金流水", String.valueOf(id));
    return BusinessResponse.success(1);
  }

  @PostMapping("/finance/{kind}/restore")
  @Transactional
  public BusinessResponse<?> restoreFinances(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable String kind,
      @RequestBody(required = false) Map<String, Object> body) {
    Long admin = adminId(token);
    if (admin == null) return unauthorized();
    if (!financeKind(kind)) return BusinessResponse.error("流水类型错误");
    List<Long> ids = idList(body == null ? null : body.get("ids"));
    if (ids.isEmpty()) return BusinessResponse.error("请选择要恢复的流水");
    int changed = financeAdmin.restoreFinanceRows(kind, ids, now());
    if (changed == 0) return BusinessResponse.error("回收记录不存在");
    log(
        admin,
        "/api/admin/business/finance/" + kind + "/restore",
        "批量恢复资金流水",
        ids.toString());
    return BusinessResponse.success(changed);
  }

  @DeleteMapping("/finance/{kind}/destroy/{id}")
  @Transactional
  public BusinessResponse<?> destroyFinance(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable String kind,
      @PathVariable long id) {
    Long admin = adminId(token);
    if (admin == null) return unauthorized();
    if (!financeKind(kind)) return BusinessResponse.error("流水类型错误");
    if (financeAdmin.destroyFinanceRow(kind, id) != 1) return BusinessResponse.error("回收记录不存在");
    log(admin, "/api/admin/business/finance/" + kind + "/destroy/" + id, "彻底删除资金流水", String.valueOf(id));
    return BusinessResponse.success(1);
  }

  @DeleteMapping("/finance/{kind}/destroy")
  @Transactional
  public BusinessResponse<?> destroyFinances(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable String kind,
      @RequestBody(required = false) Map<String, Object> body) {
    Long admin = adminId(token);
    if (admin == null) return unauthorized();
    if (!financeKind(kind)) return BusinessResponse.error("流水类型错误");
    List<Long> ids = idList(body == null ? null : body.get("ids"));
    if (ids.isEmpty()) return BusinessResponse.error("请选择要彻底删除的流水");
    int changed = financeAdmin.destroyFinanceRows(kind, ids);
    if (changed == 0) return BusinessResponse.error("回收记录不存在");
    log(
        admin,
        "/api/admin/business/finance/" + kind + "/destroy",
        "批量彻底删除资金流水",
        ids.toString());
    return BusinessResponse.success(changed);
  }

  private static void decorateFinanceRows(String kind, List<Map<String, Object>> rows) {
    for (Map<String, Object> row : rows) {
      int type = number(row.get("type"));
      if ("platform".equals(kind)) {
        row.put("type_text", platformType(type));
        row.put("user_type_text", platformUserType(number(row.get("user_type"))));
      } else if ("system".equals(kind)) {
        row.put("type_text", systemMoneyType(type));
      }
    }
  }

  private static String platformType(int type) {
    switch (type) {
      case 1:
        return "租车订单支付";
      case 2:
        return "租车押金退款";
      case 3:
        return "商家余额提现";
      case 4:
        return "酒店余额提现";
      case 5:
        return "取消订单退款";
      default:
        return "未知类型";
    }
  }

  private static String platformUserType(int type) {
    switch (type) {
      case 1:
        return "用户";
      case 2:
        return "门店";
      case 3:
        return "酒店";
      default:
        return "未知主体";
    }
  }

  private static String systemMoneyType(int type) {
    switch (type) {
      case 1:
        return "正常下单平台获取";
      case 2:
        return "扫门店码平台获取";
      case 3:
        return "扫酒店码平台获取";
      default:
        return "未知类型";
    }
  }

  private static BigDecimal decimalValue(Map<String, Object> values, String field) {
    if (values == null || values.get(field) == null) return BigDecimal.ZERO;
    try {
      return new BigDecimal(String.valueOf(values.get(field)));
    } catch (NumberFormatException ignored) {
      return BigDecimal.ZERO;
    }
  }

  private static boolean financeKind(String kind) {
    return "merchant".equals(kind)
        || "shop".equals(kind)
        || "system".equals(kind)
        || "platform".equals(kind);
  }

  private Map<String, Object> financeValues(String kind, Map<String, Object> body) {
    Map<String, Object> values = new LinkedHashMap<>();
    values.put("owner_id", longValue(body, "owner_id", 0));
    values.put("money", money(body == null ? null : body.get("money")));
    values.put("before", money(body == null ? null : body.get("before")));
    values.put("after", money(body == null ? null : body.get("after")));
    values.put("type", longValue(body, "type", 1));
    values.put("money_type", longValue(body, "money_type", 1));
    values.put("memo", body == null || body.get("memo") == null ? "" : String.valueOf(body.get("memo")).trim());
    values.put("is_delete", longValue(body, "is_delete", 1));
    values.put("order_id", longValue(body, "order_id", 0));
    values.put("now", now());
    return values;
  }

  private String financeValidation(String kind, Map<String, Object> values) {
    long owner = ((Number) values.get("owner_id")).longValue();
    long type = ((Number) values.get("type")).longValue();
    long moneyType = ((Number) values.get("money_type")).longValue();
    long isDelete = ((Number) values.get("is_delete")).longValue();
    if (owner <= 0 || financeAdmin.financeOwnerExists(kind, owner) == 0) return "所属方不存在";
    if (type < 0 || moneyType < 0) return "流水类型错误";
    if (isDelete != 1 && isDelete != 2) return "删除状态错误";
    return null;
  }

  @GetMapping("/user-coupons")
  public BusinessResponse<?> userCoupons(
      @RequestHeader(value = "token", required = false) String token,
      @RequestParam(required = false) Long userId,
      @RequestParam(required = false) Integer state,
      @RequestParam(required = false) BigDecimal priceMin,
      @RequestParam(required = false) BigDecimal priceMax,
      @RequestParam(required = false) BigDecimal reduceMin,
      @RequestParam(required = false) BigDecimal reduceMax,
      @RequestParam(required = false) String userName,
      @RequestParam(required = false) Long endFrom,
      @RequestParam(required = false) Long endTo,
      @RequestParam(required = false) Long createdFrom,
      @RequestParam(required = false) Long createdTo,
      @RequestParam(required = false) Long updatedFrom,
      @RequestParam(required = false) Long updatedTo,
      @RequestParam(required = false) Long usedFrom,
      @RequestParam(required = false) Long usedTo,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int limit) {
    if (adminId(token) == null) return unauthorized();
    page = positivePage(page);
    limit = pageSize(limit);
    Map<String, Long> times = new LinkedHashMap<>();
    times.put("endFrom", endFrom); times.put("endTo", endTo);
    times.put("createdFrom", createdFrom); times.put("createdTo", createdTo);
    times.put("updatedFrom", updatedFrom); times.put("updatedTo", updatedTo);
    times.put("usedFrom", usedFrom); times.put("usedTo", usedTo);
    int total = financeAdmin.userCouponCount(userId, state, priceMin, priceMax, reduceMin, reduceMax, text(userName), times);
    return BusinessResponse.success(
        page(
            financeAdmin.userCoupons(userId, state, priceMin, priceMax, reduceMin, reduceMax, text(userName), times, limit, (page - 1) * limit),
            total,
            page,
            limit));
  }

  public BusinessResponse<?> userCoupons(
      String token, Long userId, Integer state, int page, int limit) {
    return userCoupons(token, userId, state, null, null, null, null, null, null, null, null, null, null, null, null, null, page, limit);
  }

  @GetMapping("/sign-logs")
  public BusinessResponse<?> signLogs(
      @RequestHeader(value = "token", required = false) String token,
      @RequestParam(required = false) Long userId,
      @RequestParam(required = false) String createdFrom,
      @RequestParam(required = false) String createdTo,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int limit) {
    if (adminId(token) == null) return unauthorized();
    page = positivePage(page);
    limit = pageSize(limit);
    String from = text(createdFrom), to = text(createdTo);
    if (hasText(from) && from.length() == 10) from += " 00:00:00";
    if (hasText(to) && to.length() == 10) to += " 23:59:59";
    int total = financeAdmin.signLogCount(userId, from, to);
    return BusinessResponse.success(
        page(financeAdmin.signLogs(userId, from, to, limit, (page - 1) * limit), total, page, limit));
  }

  public BusinessResponse<?> signLogs(String token, Long userId, int page, int limit) {
    return signLogs(token, userId, null, null, page, limit);
  }

  @PostMapping("/sign-logs")
  @Transactional
  public BusinessResponse<?> addSignLog(@RequestHeader(value="token",required=false) String token,@RequestBody(required=false) Map<String,Object> body) {
    Long admin=adminId(token);if(admin==null)return unauthorized();
    Map<String,Object> generated=new LinkedHashMap<>();
    financeAdmin.insertSignLog(longValue(body,"user_id",0),number(body==null?null:body.get("score")),generated);
    Object id=generated.get("id");log(admin,"/api/admin/business/sign-logs","新增签到记录",String.valueOf(id));
    return BusinessResponse.success(java.util.Collections.singletonMap("id",id));
  }

  @PutMapping("/sign-logs/{id}")
  @Transactional
  public BusinessResponse<?> editSignLog(@RequestHeader(value="token",required=false) String token,@PathVariable long id,@RequestBody(required=false) Map<String,Object> body) {
    Long admin=adminId(token);if(admin==null)return unauthorized();
    if(financeAdmin.updateSignLog(id,longValue(body,"user_id",0),number(body==null?null:body.get("score")))==0)return BusinessResponse.error("签到记录不存在");
    log(admin,"/api/admin/business/sign-logs/"+id,"编辑签到记录",String.valueOf(id));return BusinessResponse.success(1);
  }

  @DeleteMapping("/sign-logs/{id}")
  @Transactional
  public BusinessResponse<?> deleteSignLog(@RequestHeader(value="token",required=false) String token,@PathVariable long id) {
    Long admin=adminId(token);if(admin==null)return unauthorized();
    if(financeAdmin.deleteSignLog(id)==0)return BusinessResponse.error("签到记录不存在");
    log(admin,"/api/admin/business/sign-logs/"+id,"删除签到记录",String.valueOf(id));return BusinessResponse.success(1);
  }

  @GetMapping("/operation-logs")
  public BusinessResponse<?> operationLogs(
      @RequestHeader(value = "token", required = false) String token,
      @RequestParam(required = false) Long adminId,
      @RequestParam(required = false) String username,
      @RequestParam(required = false) String title,
      @RequestParam(required = false) String url,
      @RequestParam(required = false) String ip,
      @RequestParam(required = false) Long createdFrom,
      @RequestParam(required = false) Long createdTo,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int limit) {
    Long current = adminId(token);
    if (current == null) return unauthorized();
    page = positivePage(page);
    limit = pageSize(limit);
    List<Long> allowed = visibleAdminIds(current);
    int total = financeAdmin.operationLogCount(allowed, adminId, text(username), text(title), text(url), text(ip), createdFrom, createdTo);
    return BusinessResponse.success(
        page(financeAdmin.operationLogs(allowed, adminId, text(username), text(title), text(url), text(ip), createdFrom, createdTo, limit, (page - 1) * limit), total, page, limit));
  }

  public BusinessResponse<?> operationLogs(String token, String keyword, int page, int limit) {
    return operationLogs(token, null, keyword, keyword, null, null, null, null, page, limit);
  }

  @GetMapping("/operation-logs/{id}")
  public BusinessResponse<?> operationLogDetail(
      @RequestHeader(value = "token", required = false) String token, @PathVariable long id) {
    Long admin = adminId(token);
    if (admin == null) return unauthorized();
    Map<String, Object> row = financeAdmin.operationLog(id, visibleAdminIds(admin));
    return row == null || row.isEmpty()
        ? BusinessResponse.error("操作日志不存在")
        : BusinessResponse.success(row);
  }

  @DeleteMapping("/operation-logs")
  @Transactional
  public BusinessResponse<?> deleteOperationLogs(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long admin = adminId(token);
    if (admin == null) return unauthorized();
    List<Long> ids = idList(body == null ? null : body.get("ids"));
    if (ids.isEmpty()) return BusinessResponse.error("请选择要删除的操作日志");
    int changed = financeAdmin.deleteOperationLogs(ids, visibleAdminIds(admin));
    if (changed == 0) return BusinessResponse.error("操作日志不存在或无权删除");
    log(admin, "/api/admin/business/operation-logs", "删除管理员操作日志", ids.toString());
    return BusinessResponse.success(changed);
  }

  private void log(long adminId, String path, String title, String content) {
    adminLogs.insertLog(adminId, path, title, content, now());
  }

  private List<Long> visibleAdminIds(long adminId) {
    if (adminId == 1) return null;
    List<Long> roots = financeAdmin.adminGroupIds(adminId);
    List<Map<String,Object>> groups = financeAdmin.adminGroupTree();
    List<Long> all = new ArrayList<>(roots);
    boolean changed = true;
    while (changed) {
      changed = false;
      for (Map<String,Object> group : groups) {
        long id = ((Number)group.get("id")).longValue();
        long pid = group.get("pid") == null ? 0 : ((Number)group.get("pid")).longValue();
        if (all.contains(pid) && !all.contains(id)) { all.add(id); changed = true; }
      }
    }
    List<Long> admins = all.isEmpty() ? new ArrayList<Long>() : financeAdmin.adminsInGroups(all);
    if (!admins.contains(adminId)) admins.add(adminId);
    return admins;
  }

  private static String value(Map<String, Object> body, String key) {
    return body == null || body.get(key) == null ? "" : String.valueOf(body.get(key)).trim();
  }

  private static long longValue(Map<String, Object> body, String key, long fallback) {
    try {
      return Long.parseLong(value(body, key));
    } catch (NumberFormatException ignored) {
      return fallback;
    }
  }

  private static Long nullableLong(Map<String, Object> body, String key) {
    String value = value(body, key);
    if (value.isEmpty()) return null;
    try {
      return Long.valueOf(value);
    } catch (NumberFormatException ignored) {
      return null;
    }
  }

  private static List<Long> idList(Object value) {
    List<Long> result = new ArrayList<>();
    if (value instanceof Iterable) {
      for (Object item : (Iterable<?>) value) addId(result, item);
    } else if (value != null) {
      for (String item : String.valueOf(value).split(",")) addId(result, item);
    }
    return result;
  }

  private static void addId(List<Long> ids, Object value) {
    try {
      long id = Long.parseLong(String.valueOf(value).trim());
      if (id > 0 && !ids.contains(id)) ids.add(id);
    } catch (NumberFormatException ignored) {
      // Ignore malformed IDs from historical bulk-operation payloads.
    }
  }

  private static BigDecimal money(Object value) {
    try {
      return value == null ? BigDecimal.ZERO : new BigDecimal(String.valueOf(value));
    } catch (NumberFormatException ignored) {
      return BigDecimal.ZERO;
    }
  }

  private static long now() {
    return System.currentTimeMillis() / 1000;
  }

  private Long adminId(String token) {
    return sessions.adminId(token);
  }

  private static boolean hasText(String value) {
    return value != null && !value.trim().isEmpty();
  }

  private static String text(String value) {
    return hasText(value) ? value.trim() : "";
  }

  private static int number(Object value) {
    try { return value == null ? 0 : Integer.parseInt(String.valueOf(value)); }
    catch (NumberFormatException ignored) { return 0; }
  }

  private static Object label(Object value, String... labels) {
    int index = number(value) - 1;
    return index >= 0 && index < labels.length ? labels[index] : value;
  }

  private static int positivePage(int page) {
    return Math.max(page, 1);
  }

  private static int pageSize(int limit) {
    return Math.min(Math.max(limit, 1), 100);
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

}
