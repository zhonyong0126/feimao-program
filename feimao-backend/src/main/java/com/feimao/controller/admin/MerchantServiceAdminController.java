package com.feimao.controller.admin;

import com.feimao.common.BusinessResponse;
import com.feimao.mapper.MerchantServiceAdminMapper;
import com.feimao.mapper.AdminLogMapper;
import com.feimao.service.AdminSessionService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
@RequestMapping("/api/admin/business/merchant-services")
public class MerchantServiceAdminController {
  private final MerchantServiceAdminMapper serviceAdmin;
  private final AdminLogMapper logs;
  private final AdminSessionService sessions;
  public MerchantServiceAdminController(MerchantServiceAdminMapper serviceAdmin, AdminLogMapper logs,
      AdminSessionService sessions) {
    this.serviceAdmin = serviceAdmin;
    this.logs = logs;
    this.sessions = sessions;
  }

  @GetMapping("/cars")
  public BusinessResponse<?> cars(
      @RequestHeader(value = "token", required = false) String token,
      @RequestParam(required = false) Long merchantId,
      @RequestParam(required = false) Long brandId,
      @RequestParam(required = false) Integer status,
      @RequestParam(required = false) Long createdFrom,
      @RequestParam(required = false) Long createdTo,
      @RequestParam(required = false) String keyword,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int limit) {
    if (adminId(token) == null) return unauthorized();
    page = Math.max(page, 1);
    limit = Math.min(Math.max(limit, 1), 100);
    String search = keyword == null ? "" : keyword.trim();
    Long merchantFilter = merchantId != null && merchantId > 0 ? merchantId : null;
    Long brandFilter = brandId != null && brandId > 0 ? brandId : null;
    Integer statusFilter = status != null && (status == 1 || status == 2) ? status : null;
    Long from = createdFrom != null && createdFrom > 0 ? createdFrom : null;
    Long to = createdTo != null && createdTo > 0 ? createdTo : null;
    List<Map<String, Object>> rows =
        serviceAdmin.selectCars(
            merchantFilter,
            brandFilter,
            statusFilter,
            from,
            to,
            search,
            limit,
            (page - 1) * limit);
    return BusinessResponse.success(
        page(
            rows,
            serviceAdmin.carCount(
                merchantFilter, brandFilter, statusFilter, from, to, search),
            page,
            limit));
  }

  @GetMapping("/platform-cars")
  public BusinessResponse<?> platformCars(
      @RequestHeader(value = "token", required = false) String token,
      @RequestParam(required = false) Long brandId) {
    if (adminId(token) == null) return unauthorized();
    return BusinessResponse.success(serviceAdmin.selectPlatformCars(brandId));
  }

  @PostMapping("/cars")
  @Transactional
  public BusinessResponse<?> addCar(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long adminId = adminId(token);
    if (adminId == null) return unauthorized();
    long merchantId = longValue(body, "merchant_id");
    long platformCarId = longValue(body, "platform_car_id");
    int totalStock = intValue(body, "total_stock", -1);
    if (merchantId <= 0 || platformCarId <= 0 || totalStock < 0)
      return BusinessResponse.error("门店、平台车辆和总库存不能为空");
    if (!activeMerchant(merchantId) || !activePlatformCar(platformCarId))
      return BusinessResponse.error("门店或平台车辆不存在");
    if (serviceAdmin.duplicateCar(merchantId, platformCarId, 0) > 0)
      return BusinessResponse.error("该门店已添加此车辆");
    Map<String, Object> values = carValues(body);
    values.put("merchant_id", merchantId);
    values.put("platform_car_id", platformCarId);
    values.put("total_stock", totalStock);
    values.put("stock", totalStock);
    values.put("admin_id", adminId);
    values.put("is_delete", 1);
    values.put("createtime", now());
    values.put("updatetime", now());
    serviceAdmin.insertCar(merchantId, platformCarId, totalStock, adminId, now(), values);
    Long id = serviceAdmin.lastInsertId();
    log(adminId, "新增门店车辆", String.valueOf(id));
    return BusinessResponse.success(Collections.singletonMap("id", id));
  }

  @PutMapping("/cars/{id}")
  @Transactional
  public BusinessResponse<?> updateCar(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable long id,
      @RequestBody(required = false) Map<String, Object> body) {
    Long adminId = adminId(token);
    if (adminId == null) return unauthorized();
    Map<String, Object> old = serviceAdmin.lockCar(id);
    if (old == null || old.isEmpty()) return BusinessResponse.error("门店车辆不存在");
    long merchantId = longValue(body, "merchant_id", number(old.get("merchant_id")));
    long platformCarId = longValue(body, "platform_car_id", number(old.get("platform_car_id")));
    int oldTotal = number(old.get("total_stock"));
    int oldStock = number(old.get("stock"));
    int totalStock = intValue(body, "total_stock", oldTotal);
    int rented = oldTotal - oldStock;
    if (totalStock < rented) return BusinessResponse.error("总库存不能少于已租出车辆数");
    if (!activeMerchant(merchantId) || !activePlatformCar(platformCarId))
      return BusinessResponse.error("门店或平台车辆不存在");
    if (serviceAdmin.duplicateCar(merchantId, platformCarId, id) > 0)
      return BusinessResponse.error("该门店已添加此车辆");
    Map<String, Object> values = carValues(body);
    values.put("merchant_id", merchantId);
    values.put("platform_car_id", platformCarId);
    values.put("total_stock", totalStock);
    values.put("stock", totalStock - rented);
    values.put("updatetime", now());
    serviceAdmin.updateCar(id, merchantId, platformCarId, totalStock, totalStock - rented, now(), values);
    log(adminId, "修改门店车辆", String.valueOf(id));
    return BusinessResponse.success(1);
  }

  @DeleteMapping("/cars/{id}")
  @Transactional
  public BusinessResponse<?> deleteCar(
      @RequestHeader(value = "token", required = false) String token, @PathVariable long id) {
    Long adminId = adminId(token);
    if (adminId == null) return unauthorized();
    int changed =
        serviceAdmin.deleteCar(id, now());
    if (changed == 0) return BusinessResponse.error("门店车辆不存在");
    log(adminId, "删除门店车辆", String.valueOf(id));
    return BusinessResponse.success(1);
  }

  @DeleteMapping("/cars")
  @Transactional
  public BusinessResponse<?> deleteCars(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long adminId = adminId(token);
    if (adminId == null) return unauthorized();
    List<Long> ids = idList(body == null ? null : body.get("ids"));
    if (ids.isEmpty()) return BusinessResponse.error("请选择要删除的门店车辆");
    long deletedAt = now();
    int changed = 0;
    for (Long id : ids) changed += serviceAdmin.deleteCar(id, deletedAt);
    if (changed == 0) return BusinessResponse.error("门店车辆不存在");
    log(adminId, "批量删除门店车辆", ids.toString());
    return BusinessResponse.success(changed);
  }

  @GetMapping("/cars/recycle")
  public BusinessResponse<?> recycledCars(
      @RequestHeader(value = "token", required = false) String token,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int limit) {
    if (adminId(token) == null) return unauthorized();
    page = Math.max(page, 1);
    limit = Math.min(Math.max(limit, 1), 100);
    return BusinessResponse.success(
        page(
            serviceAdmin.selectRecycledCars(limit, (page - 1) * limit),
            serviceAdmin.recycledCarCount(),
            page,
            limit));
  }

  @PostMapping("/cars/restore/{id}")
  @Transactional
  public BusinessResponse<?> restoreRecycledCar(
      @RequestHeader(value = "token", required = false) String token, @PathVariable long id) {
    Long adminId = adminId(token);
    if (adminId == null) return unauthorized();
    int changed =
        serviceAdmin.restoreRecycledCars(Collections.singletonList(id), now());
    if (changed == 0) return BusinessResponse.error("回收记录不存在");
    log(adminId, "恢复门店车辆", String.valueOf(id));
    return BusinessResponse.success(changed);
  }

  @PostMapping("/cars/restore")
  @Transactional
  public BusinessResponse<?> restoreRecycledCars(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long adminId = adminId(token);
    if (adminId == null) return unauthorized();
    boolean all = allRecycleRecords(body);
    List<Long> ids = idList(body == null ? null : body.get("ids"));
    if (!all && ids.isEmpty()) return BusinessResponse.error("请选择要恢复的记录");
    int changed =
        serviceAdmin.restoreRecycledCars(
            all ? Collections.<Long>emptyList() : ids, now());
    if (changed == 0) return BusinessResponse.error("回收站中没有可恢复的记录");
    log(adminId, all ? "恢复全部门店车辆" : "批量恢复门店车辆", all ? "全部" : ids.toString());
    return BusinessResponse.success(changed);
  }

  @DeleteMapping("/cars/destroy/{id}")
  @Transactional
  public BusinessResponse<?> destroyRecycledCar(
      @RequestHeader(value = "token", required = false) String token, @PathVariable long id) {
    Long adminId = adminId(token);
    if (adminId == null) return unauthorized();
    int changed = serviceAdmin.destroyRecycledCars(Collections.singletonList(id));
    if (changed == 0) return BusinessResponse.error("回收记录不存在");
    log(adminId, "彻底删除门店车辆", String.valueOf(id));
    return BusinessResponse.success(changed);
  }

  @DeleteMapping("/cars/destroy")
  @Transactional
  public BusinessResponse<?> destroyRecycledCars(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long adminId = adminId(token);
    if (adminId == null) return unauthorized();
    boolean all = allRecycleRecords(body);
    List<Long> ids = idList(body == null ? null : body.get("ids"));
    if (!all && ids.isEmpty()) return BusinessResponse.error("请选择要彻底删除的记录");
    int changed =
        serviceAdmin.destroyRecycledCars(all ? Collections.<Long>emptyList() : ids);
    if (changed == 0) return BusinessResponse.error("回收站中没有可彻底删除的记录");
    log(
        adminId,
        all ? "彻底删除全部门店车辆" : "批量彻底删除门店车辆",
        all ? "全部" : ids.toString());
    return BusinessResponse.success(changed);
  }

  @PutMapping("/cars/status")
  @Transactional
  public BusinessResponse<?> updateCarStatuses(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long adminId = adminId(token);
    if (adminId == null) return unauthorized();
    List<Long> ids = idList(body == null ? null : body.get("ids"));
    int status = intValue(body, "status", 0);
    if (ids.isEmpty() || (status != 1 && status != 2))
      return BusinessResponse.error("状态参数错误");
    int changed = serviceAdmin.updateCarStatuses(ids, status, now());
    if (changed == 0) return BusinessResponse.error("门店车辆不存在或状态未变化");
    log(adminId, "修改门店车辆状态", ids + ":" + status);
    return BusinessResponse.success(changed);
  }

  @GetMapping("/maintains")
  public BusinessResponse<?> maintains(
      @RequestHeader(value = "token", required = false) String token,
      @RequestParam(required = false) Long merchantId,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int limit) {
    if (adminId(token) == null) return unauthorized();
    page = Math.max(page, 1);
    limit = Math.min(Math.max(limit, 1), 100);
    List<Map<String, Object>> rows = serviceAdmin.selectMaintains(merchantId, limit, (page - 1) * limit);
    return BusinessResponse.success(page(rows, serviceAdmin.maintainCount(merchantId), page, limit));
  }

  @GetMapping("/maintain-options")
  public BusinessResponse<?> maintainOptions(
      @RequestHeader(value = "token", required = false) String token,
      @RequestParam(required = false) String keyword,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int limit) {
    if (adminId(token) == null) return unauthorized();
    page = Math.max(page, 1);
    limit = Math.min(Math.max(limit, 1), 100);
    String search = keyword == null ? "" : keyword.trim();
    List<Map<String, Object>> rows = serviceAdmin.selectMaintainOptions(search, limit, (page - 1) * limit);
    return BusinessResponse.success(page(rows, serviceAdmin.maintainOptionCount(search), page, limit));
  }

  @PostMapping("/maintains")
  @Transactional
  public BusinessResponse<?> addMaintain(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long adminId = adminId(token);
    if (adminId == null) return unauthorized();
    long merchantId = longValue(body, "merchant_id");
    long maintainId = longValue(body, "maintain_id");
    BigDecimal price = decimal(body, "price");
    if (merchantId <= 0 || maintainId <= 0 || price == null || price.signum() < 0)
      return BusinessResponse.error("门店、维修项目和价格参数错误");
    Long ownerAdminId = serviceAdmin.merchantAdminId(merchantId);
    if (ownerAdminId == null || serviceAdmin.maintainExists(maintainId) == 0)
      return BusinessResponse.error("门店或维修项目不存在");
    if (serviceAdmin.duplicateMaintain(merchantId, maintainId) > 0)
      return BusinessResponse.error("该门店已添加此维修服务");
    serviceAdmin.insertMaintain(merchantId, maintainId, ownerAdminId, price, now());
    Long id = serviceAdmin.lastInsertId();
    log(adminId, "新增门店维修服务", String.valueOf(id));
    return BusinessResponse.success(Collections.singletonMap("id", id));
  }

  @PutMapping("/maintains/{id}")
  @Transactional
  public BusinessResponse<?> updateMaintain(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable long id,
      @RequestBody(required = false) Map<String, Object> body) {
    Long adminId = adminId(token);
    if (adminId == null) return unauthorized();
    BigDecimal price = decimal(body, "price");
    if (price == null || price.signum() < 0) return BusinessResponse.error("价格参数错误");
    int changed =
        serviceAdmin.updateMaintain(id, price, now());
    if (changed == 0) return BusinessResponse.error("门店维修服务不存在");
    log(adminId, "修改门店维修服务", String.valueOf(id));
    return BusinessResponse.success(1);
  }

  @DeleteMapping("/maintains/{id}")
  @Transactional
  public BusinessResponse<?> deleteMaintain(
      @RequestHeader(value = "token", required = false) String token, @PathVariable long id) {
    Long adminId = adminId(token);
    if (adminId == null) return unauthorized();
    if (serviceAdmin.deleteMaintain(id, now()) == 0)
      return BusinessResponse.error("门店维修服务不存在");
    log(adminId, "删除门店维修服务", String.valueOf(id));
    return BusinessResponse.success(1);
  }

  @GetMapping("/maintains/recycle")
  public BusinessResponse<?> recycledMaintains(
      @RequestHeader(value = "token", required = false) String token,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int limit) {
    if (adminId(token) == null) return unauthorized();
    page = Math.max(page, 1);
    limit = Math.min(Math.max(limit, 1), 100);
    return BusinessResponse.success(
        page(
            serviceAdmin.selectRecycledMaintains(limit, (page - 1) * limit),
            serviceAdmin.recycledMaintainCount(),
            page,
            limit));
  }

  @PostMapping("/maintains/restore/{id}")
  @Transactional
  public BusinessResponse<?> restoreRecycledMaintain(
      @RequestHeader(value = "token", required = false) String token, @PathVariable long id) {
    Long adminId = adminId(token);
    if (adminId == null) return unauthorized();
    int changed =
        serviceAdmin.restoreRecycledMaintains(Collections.singletonList(id), now());
    if (changed == 0) return BusinessResponse.error("回收记录不存在");
    log(adminId, "恢复门店维修服务", String.valueOf(id));
    return BusinessResponse.success(changed);
  }

  @PostMapping("/maintains/restore")
  @Transactional
  public BusinessResponse<?> restoreRecycledMaintains(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long adminId = adminId(token);
    if (adminId == null) return unauthorized();
    boolean all = allRecycleRecords(body);
    List<Long> ids = idList(body == null ? null : body.get("ids"));
    if (!all && ids.isEmpty()) return BusinessResponse.error("请选择要恢复的记录");
    int changed =
        serviceAdmin.restoreRecycledMaintains(
            all ? Collections.<Long>emptyList() : ids, now());
    if (changed == 0) return BusinessResponse.error("回收站中没有可恢复的记录");
    log(
        adminId,
        all ? "恢复全部门店维修服务" : "批量恢复门店维修服务",
        all ? "全部" : ids.toString());
    return BusinessResponse.success(changed);
  }

  @DeleteMapping("/maintains/destroy/{id}")
  @Transactional
  public BusinessResponse<?> destroyRecycledMaintain(
      @RequestHeader(value = "token", required = false) String token, @PathVariable long id) {
    Long adminId = adminId(token);
    if (adminId == null) return unauthorized();
    int changed = serviceAdmin.destroyRecycledMaintains(Collections.singletonList(id));
    if (changed == 0) return BusinessResponse.error("回收记录不存在");
    log(adminId, "彻底删除门店维修服务", String.valueOf(id));
    return BusinessResponse.success(changed);
  }

  @DeleteMapping("/maintains/destroy")
  @Transactional
  public BusinessResponse<?> destroyRecycledMaintains(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long adminId = adminId(token);
    if (adminId == null) return unauthorized();
    boolean all = allRecycleRecords(body);
    List<Long> ids = idList(body == null ? null : body.get("ids"));
    if (!all && ids.isEmpty()) return BusinessResponse.error("请选择要彻底删除的记录");
    int changed =
        serviceAdmin.destroyRecycledMaintains(all ? Collections.<Long>emptyList() : ids);
    if (changed == 0) return BusinessResponse.error("回收站中没有可彻底删除的记录");
    log(
        adminId,
        all ? "彻底删除全部门店维修服务" : "批量彻底删除门店维修服务",
        all ? "全部" : ids.toString());
    return BusinessResponse.success(changed);
  }

  private Map<String, Object> carValues(Map<String, Object> body) {
    Map<String, Object> values = new LinkedHashMap<>();
    if (body == null) return values;
    for (String field :
        new String[] {
          "name",
          "intro",
          "images",
          "price",
          "cash_price",
          "month_price",
          "day_price",
          "hour_price",
          "installment",
          "status",
          "merchant_longitude",
          "merchant_latitude"
        }) {
      if (body.containsKey(field)) values.put(field, body.get(field));
    }
    return values;
  }

  private boolean activeMerchant(long id) {
    return serviceAdmin.activeMerchant(id) > 0;
  }

  private boolean activePlatformCar(long id) {
    return serviceAdmin.activePlatformCar(id) > 0;
  }

  private Long adminId(String token) {
    return sessions.adminId(token);
  }

  private void log(long adminId, String title, String content) {
    logs.insertLog(adminId, "/api/admin/business/merchant-services", title, content, now());
  }

  private static long longValue(Map<String, Object> body, String field) {
    return longValue(body, field, 0);
  }

  private static long longValue(Map<String, Object> body, String field, long fallback) {
    if (body == null || !body.containsKey(field)) return fallback;
    try {
      return Long.parseLong(String.valueOf(body.get(field)));
    } catch (NumberFormatException ignored) {
      return fallback;
    }
  }

  private static int intValue(Map<String, Object> body, String field, int fallback) {
    return (int) longValue(body, field, fallback);
  }

  private static BigDecimal decimal(Map<String, Object> body, String field) {
    if (body == null || body.get(field) == null) return null;
    try {
      return new BigDecimal(String.valueOf(body.get(field)));
    } catch (NumberFormatException ignored) {
      return null;
    }
  }

  private static int number(Object value) {
    return value == null ? 0 : ((Number) value).intValue();
  }

  private static List<Long> idList(Object value) {
    List<Long> ids = new ArrayList<>();
    Iterable<?> values =
        value instanceof Iterable
            ? (Iterable<?>) value
            : value == null
                ? Collections.emptyList()
                : Arrays.asList(String.valueOf(value).split(","));
    for (Object item : values) {
      try {
        long id = Long.parseLong(String.valueOf(item).trim());
        if (id > 0 && !ids.contains(id)) ids.add(id);
      } catch (NumberFormatException ignored) {
        // Match the historical bulk endpoint by ignoring malformed members.
      }
    }
    return ids;
  }

  private static boolean allRecycleRecords(Map<String, Object> body) {
    if (body == null || !body.containsKey("ids")) return true;
    Object value = body.get("all");
    return value instanceof Boolean
        ? ((Boolean) value).booleanValue()
        : "1".equals(String.valueOf(value)) || "true".equalsIgnoreCase(String.valueOf(value));
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
}
