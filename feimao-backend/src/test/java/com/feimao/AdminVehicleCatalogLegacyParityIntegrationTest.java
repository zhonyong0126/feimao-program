package com.feimao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.feimao.common.BusinessResponse;
import com.feimao.controller.admin.MerchantServiceAdminController;
import com.feimao.controller.admin.ResourceAdminController;
import com.feimao.service.AdminSessionService;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class AdminVehicleCatalogLegacyParityIntegrationTest {
  @Autowired JdbcTemplate jdbc;
  @Autowired ResourceAdminController resources;
  @Autowired MerchantServiceAdminController merchantServices;
  @Autowired AdminSessionService sessions;

  private String token;
  private long merchantId;

  @BeforeEach
  void prepareSession() {
    long adminId =
        jdbc.queryForObject(
            "select id from ymcx_admin where status='normal' order by id limit 1", Long.class);
    token = sessions.issue(adminId);
    merchantId =
        jdbc.queryForObject(
            "select id from ymcx_merchant where is_delete=1 and deletetime is null order by id limit 1",
            Long.class);
  }

  @Test
  void platformListIncludesLegacyBrandAndExcludesSoftDeletedRows() {
    String suffix = Long.toHexString(System.nanoTime());
    long brandId = insertBrand("目录品牌" + suffix, null);
    long visible = insertPlatformCar("目录车辆" + suffix, brandId, null);
    insertPlatformCar("已删除车辆" + suffix, brandId, 1700000100L);

    BusinessResponse<?> response =
        resources.list(
            token,
            "platform-car",
            1,
            20,
            "目录品牌" + suffix,
            null,
            1,
            brandId,
            1699999999L,
            1800000000L);

    assertEquals(1, response.getCode());
    List<Map<String, Object>> rows = rows(response);
    assertEquals(1, rows.size());
    assertEquals(visible, number(rows.get(0).get("id")));
    assertEquals("目录品牌" + suffix, rows.get(0).get("brand_name"));

    Map<String, Object> status = new LinkedHashMap<>();
    status.put("ids", Arrays.asList(visible));
    status.put("status", 2);
    assertEquals(1, resources.updateStatuses(token, "platform-car", status).getCode());
    assertEquals(
        2,
        jdbc.queryForObject(
            "select status from ymcx_platform_car where id=?", Integer.class, visible));

    Map<String, Object> deletion = new LinkedHashMap<>();
    deletion.put("ids", Arrays.asList(visible));
    assertEquals(1, resources.deleteMany(token, "platform-car", deletion).getCode());
    Map<String, Object> deleted =
        jdbc.queryForMap(
            "select is_delete,deletetime from ymcx_platform_car where id=?", visible);
    assertEquals(1, number(deleted.get("is_delete")));
    assertNotNull(deleted.get("deletetime"));
  }

  @Test
  void merchantCarListKeepsLegacyColumnsStatusAndBulkSoftDelete() {
    String suffix = Long.toHexString(System.nanoTime());
    long brandId = insertBrand("门店车品牌" + suffix, null);
    long platformCarId = insertPlatformCar("门店车型" + suffix, brandId, null);
    long first = insertMerchantCar(platformCarId, "在售甲" + suffix, null, 1);
    long second = insertMerchantCar(platformCarId, "在售乙" + suffix, null, 1);
    insertMerchantCar(platformCarId, "历史删除" + suffix, 1700000200L, 1);

    BusinessResponse<?> response =
        merchantServices.cars(
            token,
            merchantId,
            brandId,
            1,
            1699999999L,
            1800000000L,
            "门店车型" + suffix,
            1,
            20);
    assertEquals(1, response.getCode());
    List<Map<String, Object>> rows = rows(response);
    assertEquals(2, rows.size());
    assertEquals("门店车品牌" + suffix, rows.get(0).get("brand_name"));
    assertEquals("门店车型" + suffix, rows.get(0).get("platform_car_name"));
    assertNotNull(rows.get(0).get("image"));

    Map<String, Object> status = new LinkedHashMap<>();
    status.put("ids", Arrays.asList(first, second));
    status.put("status", 2);
    assertEquals(1, merchantServices.updateCarStatuses(token, status).getCode());
    assertEquals(
        2,
        jdbc.queryForObject(
            "select count(*) from ymcx_merchant_car where id in (?,?) and status=2",
            Integer.class,
            first,
            second));

    Map<String, Object> deletion = new LinkedHashMap<>();
    deletion.put("ids", Arrays.asList(first, second));
    assertEquals(1, merchantServices.deleteCars(token, deletion).getCode());
    assertEquals(
        2,
        jdbc.queryForObject(
            "select count(*) from ymcx_merchant_car where id in (?,?) and is_delete=1 and deletetime is not null",
            Integer.class,
            first,
            second));
  }

  private long insertBrand(String name, Long deleteTime) {
    jdbc.update(
        "insert into ymcx_brand(name,weigh,status,is_delete,deletetime,createtime,updatetime) values(?,999,1,1,?,1700000001,1700000001)",
        name,
        deleteTime);
    return jdbc.queryForObject(
        "select id from ymcx_brand where name=? order by id desc limit 1", Long.class, name);
  }

  private long insertPlatformCar(String name, long brandId, Long deleteTime) {
    jdbc.update(
        "insert into ymcx_platform_car(name,brand_id,image,specification,status,weigh,is_delete,deletetime,createtime,updatetime) values(?,?,'/uploads/test/car.jpg','测试型号',1,99,1,?,1700000002,1700000002)",
        name,
        brandId,
        deleteTime);
    return jdbc.queryForObject(
        "select id from ymcx_platform_car where name=? order by id desc limit 1",
        Long.class,
        name);
  }

  private long insertMerchantCar(long platformCarId, String name, Long deleteTime, int status) {
    jdbc.update(
        "insert into ymcx_merchant_car(platform_car_id,merchant_id,name,images,intro,cash_price,month_price,day_price,hour_price,stock,total_stock,status,is_delete,deletetime,createtime,updatetime) values(?,?,?,'/uploads/test/list.jpg','/uploads/test/detail.jpg',10,20,3,1,5,5,?,1,?,1700000003,1700000003)",
        platformCarId,
        merchantId,
        name,
        status,
        deleteTime);
    return jdbc.queryForObject(
        "select id from ymcx_merchant_car where name=? order by id desc limit 1",
        Long.class,
        name);
  }

  @SuppressWarnings("unchecked")
  private static List<Map<String, Object>> rows(BusinessResponse<?> response) {
    Map<String, Object> page = (Map<String, Object>) response.getData();
    return (List<Map<String, Object>>) page.get("data");
  }

  private static long number(Object value) {
    return ((Number) value).longValue();
  }
}
