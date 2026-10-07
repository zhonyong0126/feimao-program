package com.feimao.controller.common;

import com.feimao.common.BusinessResponse;
import com.feimao.mapper.PublicCatalogMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.dao.DataAccessException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Compatibility endpoints for the original business-chuxing user application. */
@RestController
@RequestMapping("/api/index")
@CrossOrigin
public class IndexController {
  private final PublicCatalogMapper catalog;

  public IndexController(PublicCatalogMapper catalog) {
    this.catalog = catalog;
  }

  @PostMapping("/config")
  public BusinessResponse<List<Map<String, Object>>> config(
      @RequestBody(required = false) Map<String, Object> body) {
    String name = text(body, "name");
    return BusinessResponse.success("操作成功!", catalog.selectConfig(name));
  }

  @PostMapping("/bannerList")
  public BusinessResponse<List<Map<String, Object>>> bannerList() {
    return BusinessResponse.success("操作成功!",
        catalog.selectBanners());
  }

  @PostMapping("/brandList")
  public BusinessResponse<List<Map<String, Object>>> brandList() {
    return BusinessResponse.success("操作成功!",
        catalog.selectBrands());
  }

  @PostMapping("/merchantList")
  public BusinessResponse<Map<String, Object>> merchantList(
      @RequestBody(required = false) Map<String, Object> body) {
    double lng = number(body, "lng", 0);
    double lat = number(body, "lat", 0);
    if (lng == 0 || lat == 0) return BusinessResponse.error("参数错误");
    long page = positiveLong(body, "page", 1);
    long size = positiveLong(body, "list_rows", 10);
    List<Map<String, Object>> rows =
        catalog.selectNearbyMerchants(lat, lng, size, offset(page, size));
    formatMerchants(rows, true);
    return BusinessResponse.success("操作成功!", page(rows, catalog.merchantCount(), page, size));
  }

  @PostMapping("/mapMerchantList")
  public BusinessResponse<Map<String, Object>> mapMerchantList(
      @RequestBody(required = false) Map<String, Object> body) {
    double lng = number(body, "lng", 0);
    double lat = number(body, "lat", 0);
    if (lng == 0 || lat == 0) return BusinessResponse.error("参数错误");
    long selected = (long) number(body, "merchant_id", 1);
    List<Map<String, Object>> rows =
        catalog.selectMapMerchants(lat, lng);
    formatMerchants(rows, false);
    Map<String, Object> active = null;
    for (Map<String, Object> row : rows) {
      if ((selected > 0 && selected == ((Number) row.get("merchant_id")).longValue())
          || (selected == 0 && active == null)) {
        row.put("is_active", 1);
        active = row;
      }
    }
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("list", rows);
    data.put("data", active == null ? Collections.emptyList() : active);
    return BusinessResponse.success("操作成功!", data);
  }

  @PostMapping("/leaseCarList")
  public BusinessResponse<Map<String, Object>> leaseCarList(
      @RequestBody(required = false) Map<String, Object> body) {
    long page = positiveLong(body, "page", 1);
    long size = positiveLong(body, "list_rows", 10);
    long brandId = longValue(body, "brand_id", 0);
    int sort = nonNegativeInt(body, "sort", 0);
    List<Map<String, Object>> rows =
        catalog.selectLeaseCars(brandId, sort, size, offset(page, size));
    return BusinessResponse.success("操作成功!", page(rows, catalog.leaseCarCount(brandId), page, size));
  }

  @PostMapping("/bugCarList")
  public BusinessResponse<Map<String, Object>> buyCarList(
      @RequestBody(required = false) Map<String, Object> body) {
    long page = positiveLong(body, "page", 1);
    long size = positiveLong(body, "list_rows", 10);
    long brandId = longValue(body, "brand_id", 0);
    int sort = nonNegativeInt(body, "sort", 0);
    List<Map<String, Object>> rows =
        catalog.selectBuyCars(brandId, sort, size, offset(page, size));
    return BusinessResponse.success("操作成功!", page(rows, catalog.buyCarCount(brandId), page, size));
  }

  @PostMapping("/leaseMerchantCarList")
  public BusinessResponse<?> leaseMerchantCarList(
      @RequestBody(required = false) Map<String, Object> body) {
    long carId = (long) number(body, "platform_car_id", 0);
    if (carId <= 0) return BusinessResponse.error("请选择车辆！");
    List<Map<String, Object>> rows =
        catalog.selectLeaseMerchants(carId);
    nestLeaseMerchants(rows);
    return BusinessResponse.success("操作成功!", rows);
  }

  @PostMapping("/bugMerchantCarList")
  public BusinessResponse<?> buyMerchantCarList(
      @RequestBody(required = false) Map<String, Object> body) {
    long carId = (long) number(body, "platform_car_id", 0);
    if (carId <= 0) return BusinessResponse.error("请选择车辆！");
    List<Map<String, Object>> rows =
        catalog.selectBuyMerchants(carId);
    nestBuyMerchants(rows);
    return BusinessResponse.success("操作成功!", rows);
  }

  @PostMapping("/maintainList")
  public BusinessResponse<Map<String, Object>> maintainList(
      @RequestBody(required = false) Map<String, Object> body) {
    long page = positiveLong(body, "page", 1);
    long size = positiveLong(body, "list_rows", 10);
    try {
      List<Map<String, Object>> rows =
          catalog.selectMaintains(size, offset(page, size));
      return BusinessResponse.success("操作成功!", page(rows, catalog.maintainCount(), page, size));
    } catch (DataAccessException ex) {
      return BusinessResponse.error("维修服务数据读取失败");
    }
  }

  @PostMapping("/maintainMerchantCarList")
  public BusinessResponse<?> maintainMerchantList(
      @RequestBody(required = false) Map<String, Object> body) {
    long maintainId = (long) number(body, "maintain_id", 0);
    if (maintainId <= 0) return BusinessResponse.error("请选择维修服务");
    List<Map<String, Object>> rows =
        catalog.selectMaintainMerchants(maintainId);
    nestMaintainMerchants(rows);
    return BusinessResponse.success("操作成功!", rows);
  }

  private static void formatMerchants(List<Map<String, Object>> rows, boolean roundedMeters) {
    for (Map<String, Object> row : rows) {
      row.put("address", text(row, "administrative") + text(row, "address"));
      double meters = ((Number) row.get("distance")).doubleValue();
      if (meters >= 1000) {
        row.put("distance", decimal(meters / 1000, 1) + "km");
      } else {
        // merchantList in the legacy source called rand(distance, 0), which is invalid when
        // distance is positive.  Rounding preserves its intended metre display without making
        // this public endpoint fail; mapMerchantList returned the calculated distance directly.
        row.put("distance", (roundedMeters ? Long.toString(Math.round(meters)) : decimal(meters, 8)) + "m");
      }
    }
  }

  private static void nestLeaseMerchants(List<Map<String, Object>> rows) {
    for (Map<String, Object> row : rows) {
      Map<String, Object> merchant = new LinkedHashMap<>();
      merchant.put("id", row.remove("mid"));
      merchant.put("pick_type", row.remove("pick_type"));
      merchant.put("image", value(row.remove("merchant_image")));
      Object avatar = row.remove("avatar");
      merchant.put("avatar", avatar);
      Object administrative = row.remove("administrative");
      merchant.put("administrative", administrative);
      merchant.put("address", value(administrative) + value(row.remove("address")));
      merchant.put("name", row.remove("merchant_name"));
      merchant.put("index_image", avatar);
      row.put("merchant", merchant);
    }
  }

  private static void nestBuyMerchants(List<Map<String, Object>> rows) {
    for (Map<String, Object> row : rows) {
      Map<String, Object> merchant = baseMerchant(row);
      merchant.put("index_image", firstImage(value(merchant.get("image"))));
      row.put("merchant", merchant);
    }
  }

  private static void nestMaintainMerchants(List<Map<String, Object>> rows) {
    for (Map<String, Object> row : rows) row.put("merchant", baseMerchant(row));
  }

  private static Map<String, Object> baseMerchant(Map<String, Object> row) {
    Map<String, Object> merchant = new LinkedHashMap<>();
    merchant.put("id", row.remove("mid"));
    merchant.put("pick_type", row.remove("pick_type"));
    merchant.put("image", value(row.remove("merchant_image")));
    merchant.put("address", value(row.remove("address")));
    merchant.put("name", row.remove("merchant_name"));
    return merchant;
  }

  private static String firstImage(String images) {
    return images.isEmpty() ? "" : images.split(",", -1)[0];
  }

  private static String decimal(double value, int scale) {
    return BigDecimal.valueOf(value)
        .setScale(scale, RoundingMode.HALF_UP)
        .stripTrailingZeros()
        .toPlainString();
  }

  private static String value(Object value) {
    return value == null ? "" : value.toString();
  }

  private static Map<String, Object> page(
      List<Map<String, Object>> rows, int total, long current, long size) {
    Map<String, Object> page = new LinkedHashMap<>();
    page.put("total", total);
    page.put("per_page", size);
    page.put("current_page", current);
    page.put("last_page", total == 0 ? 0 : (total + size - 1) / size);
    page.put("data", rows);
    return page;
  }

  private static long positiveLong(Map<String, Object> body, String key, long fallback) {
    long value = longValue(body, key, fallback);
    return value > 0 ? value : fallback;
  }

  private static int nonNegativeInt(Map<String, Object> body, String key, int fallback) {
    long value = longValue(body, key, fallback);
    return value >= 0 && value <= Integer.MAX_VALUE ? (int) value : fallback;
  }

  private static long longValue(Map<String, Object> body, String key, long fallback) {
    if (body == null || body.get(key) == null || body.get(key).toString().isEmpty()) return fallback;
    try {
      return Long.parseLong(body.get(key).toString());
    } catch (NumberFormatException ignored) {
      return fallback;
    }
  }

  private static long offset(long page, long size) {
    try {
      return Math.multiplyExact(page - 1, size);
    } catch (ArithmeticException ignored) {
      return Long.MAX_VALUE;
    }
  }

  private static double number(Map<String, Object> body, String key, double fallback) {
    if (body == null || body.get(key) == null || body.get(key).toString().isEmpty())
      return fallback;
    try {
      return Double.parseDouble(body.get(key).toString());
    } catch (NumberFormatException ignored) {
      return fallback;
    }
  }

  private static String text(Map<String, Object> body, String key) {
    if (body == null || body.get(key) == null) return "";
    return body.get(key).toString();
  }
}
