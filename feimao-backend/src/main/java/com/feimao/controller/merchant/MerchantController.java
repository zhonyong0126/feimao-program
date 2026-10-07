package com.feimao.controller.merchant;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feimao.common.BusinessResponse;
import com.feimao.mapper.MerchantCatalogMapper;
import com.feimao.service.UserTokenService;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/merchant")
@CrossOrigin
public class MerchantController {
  private final MerchantCatalogMapper catalog;
  private final UserTokenService tokens;
  private final ObjectMapper json;

  public MerchantController(MerchantCatalogMapper catalog, UserTokenService tokens, ObjectMapper json) {
    this.catalog = catalog;
    this.tokens = tokens;
    this.json = json;
  }

  @PostMapping("/details")
  public BusinessResponse<?> details(@RequestBody(required = false) Map<String, Object> body) {
    long id = longValue(body, "merchant_id", 0);
    if (id == 0) return BusinessResponse.error("请选择商家");
    Map<String, Object> row = catalog.selectMerchant(id);
    if (row == null || row.isEmpty()) return BusinessResponse.error("商家已关闭，请选择其他商家。", "");
    row.put("address", text(row, "administrative") + text(row, "address"));
    return BusinessResponse.success("操作成功!", row);
  }

  @PostMapping("/brandList")
  public BusinessResponse<?> brandList(@RequestBody(required = false) Map<String, Object> body) {
    long merchantId = longValue(body, "merchant_id", 0), brandId = longValue(body, "brand_id", 0);
    if (merchantId == 0) return BusinessResponse.error("请选择商家");
    return BusinessResponse.success("操作成功!", catalog.selectBrands(merchantId, brandId));
  }

  @PostMapping({"/leaseCarList", "/buyCarList"})
  public BusinessResponse<?> carList(
      @RequestBody(required = false) Map<String, Object> body,
      javax.servlet.http.HttpServletRequest request) {
    long merchantId = longValue(body, "merchant_id", 0), brandId = longValue(body, "brand_id", 0);
    if (merchantId == 0) return BusinessResponse.error("请选择商家");
    long page = positiveLong(body, "page", 1), size = positiveLong(body, "list_rows", 10);
    boolean buy = request.getRequestURI().endsWith("buyCarList");
    List<Map<String, Object>> rows =
        buy
            ? catalog.selectBuyCars(merchantId, brandId, size, (page - 1) * size)
            : catalog.selectLeaseCars(merchantId, brandId, size, (page - 1) * size);
    for (Map<String, Object> row : rows) row.put("index_image", firstImage(text(row, "image")));
    int total =
        buy
            ? catalog.buyCarCount(merchantId, brandId)
            : catalog.leaseCarCount(merchantId, brandId);
    return BusinessResponse.success("操作成功!", page(rows, total, page, size));
  }

  @PostMapping("/carDetails")
  public BusinessResponse<?> carDetails(@RequestBody(required = false) Map<String, Object> body) {
    long id = longValue(body, "merchant_car_id", 0);
    if (id == 0) return BusinessResponse.error("请选择车辆！");
    Map<String, Object> raw = catalog.selectCar(id);
    if (raw == null || raw.isEmpty()) return BusinessResponse.error("车辆不存在");
    if (((Number) raw.get("status")).intValue() != 1) return BusinessResponse.error("车辆已下架");
    raw.put("hour_price", raw.get("day_price"));
    raw.put("images", split(text(raw, "images")));
    raw.put("intro", split(text(raw, "intro")));
    Map<String, Object> car = new LinkedHashMap<>();
    for (String key : Arrays.asList("brand_id", "brand_name", "name", "image", "specification"))
      car.put(key, raw.get(key));
    raw.put("car", car);
    Map<String, Object> merchant = new LinkedHashMap<>();
    merchant.put("name", raw.get("merchant_name"));
    merchant.put("business", raw.get("business"));
    merchant.put("administrative", raw.get("administrative"));
    merchant.put("pick_type", raw.get("pick_type"));
    merchant.put("address", text(raw, "administrative") + text(raw, "address"));
    raw.put("merchant", merchant);
    raw.remove("cash_price");
    raw.remove("installment");
    for (String key : Arrays.asList(
        "brand_id", "name", "image", "specification", "merchant_name", "business",
        "administrative", "address", "pick_type")) raw.remove(key);
    return BusinessResponse.success("操作成功!", raw);
  }

  @PostMapping("/maintainList")
  public BusinessResponse<?> maintainList(@RequestBody(required = false) Map<String, Object> body) {
    long merchantId = longValue(body, "merchant_id", 0);
    if (merchantId == 0) return BusinessResponse.error("请选择商家");
    long page = positiveLong(body, "page", 1), size = positiveLong(body, "list_rows", 10);
    List<Map<String, Object>> rows =
        catalog.selectMaintains(merchantId, size, (page - 1) * size);
    return BusinessResponse.success("操作成功!", page(rows, catalog.maintainCount(merchantId), page, size));
  }

  @PostMapping("/maintainDetails")
  public BusinessResponse<?> maintainDetails(@RequestBody(required = false) Map<String, Object> body) {
    long id = longValue(body, "merchant_maintain_id", 0);
    if (id == 0) return BusinessResponse.error("请选择维修服务");
    Map<String, Object> row = catalog.selectMaintain(id);
    return row == null || row.isEmpty()
        ? BusinessResponse.error("该服务已下架,请选择其他服务", "")
        : BusinessResponse.success("操作成功!", row);
  }

  @PostMapping("/leasePreceptDetails")
  public BusinessResponse<?> leasePrecept(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long uid = tokens.userId(token);
    if (uid == null) return BusinessResponse.error("请先登录");
    long id = longValue(body, "merchant_car_id", 0);
    if (id == 0) return BusinessResponse.error("请选择车辆！");
    Map<String, Object> row = catalog.selectLeasePlan(id);
    if (row == null || row.isEmpty()) return BusinessResponse.error("车辆不存在");
    try {
      row.put(
          "installment", json.readValue(text(row, "installment"), new TypeReference<Object>() {}));
    } catch (Exception ignored) {
      row.put("installment", Collections.emptyList());
    }
    row.putAll(catalog.selectUserAuthentication(uid));
    return BusinessResponse.success("操作成功!", row);
  }

  private static String firstImage(String value) {
    return value.isEmpty() ? "" : value.split(",")[0];
  }

  private static List<String> split(String value) {
    return value.isEmpty() ? Collections.emptyList() : Arrays.asList(value.split(","));
  }

  private static long longValue(Map<String, Object> b, String k, long d) {
    try {
      return b == null || b.get(k) == null ? d : Long.parseLong(b.get(k).toString());
    } catch (Exception e) {
      return d;
    }
  }

  private static long positiveLong(Map<String, Object> b, String k, long d) {
    long value = longValue(b, k, d);
    return value > 0 ? value : d;
  }

  private static String text(Map<String, Object> b, String k) {
    return b == null || b.get(k) == null ? "" : b.get(k).toString();
  }

  private static Map<String, Object> page(List<Map<String, Object>> r, int t, long c, long s) {
    Map<String, Object> p = new LinkedHashMap<>();
    p.put("total", t);
    p.put("per_page", s);
    p.put("current_page", c);
    p.put("last_page", t == 0 ? 0 : (t + s - 1) / s);
    p.put("data", r);
    return p;
  }
}
