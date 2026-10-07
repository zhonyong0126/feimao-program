package com.feimao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.feimao.common.BusinessResponse;
import com.feimao.controller.admin.ResourceAdminController;
import com.feimao.service.AdminSessionService;
import java.util.Arrays;
import java.util.Collections;
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
class ResourceAdminRecycleBinIntegrationTest {
  @Autowired JdbcTemplate jdbc;
  @Autowired ResourceAdminController resources;
  @Autowired AdminSessionService sessions;

  private String token;

  @BeforeEach
  void prepareSession() {
    long adminId =
        jdbc.queryForObject(
            "select id from ymcx_admin where status='normal' order by id limit 1", Long.class);
    token = sessions.issue(adminId);
  }

  @Test
  void brandRecycleBinSupportsSingleBatchAndAllOperations() {
    restoreAll("brand");
    String suffix = Long.toHexString(System.nanoTime());
    long first = insertBrand("回收品牌甲" + suffix);
    long second = insertBrand("回收品牌乙" + suffix);
    long third = insertBrand("回收品牌丙" + suffix);
    deleteMany("brand", first, second, third);

    BusinessResponse<?> recycle = resources.recycledResources(token, "brand", 1, 20);
    assertEquals(1, recycle.getCode());
    assertEquals(3, total(recycle));
    Map<String, Object> row = find(rows(recycle), first);
    assertEquals("回收品牌甲" + suffix, row.get("name"));
    assertNotNull(row.get("deletetime"));

    assertEquals(1, resources.restoreRecycledResource(token, "brand", first).getCode());
    assertActive("ymcx_brand", first);
    assertEquals(
        1,
        resources
            .restoreRecycledResources(token, "brand", ids(second, third))
            .getCode());
    assertActive("ymcx_brand", second);
    assertActive("ymcx_brand", third);

    long fourth = insertBrand("彻删品牌甲" + suffix);
    long fifth = insertBrand("彻删品牌乙" + suffix);
    long sixth = insertBrand("彻删品牌丙" + suffix);
    deleteMany("brand", fourth, fifth, sixth);
    assertEquals(1, resources.destroyRecycledResource(token, "brand", fourth).getCode());
    assertMissing("ymcx_brand", fourth);
    assertEquals(
        1,
        resources.destroyRecycledResources(token, "brand", ids(fifth, sixth)).getCode());
    assertMissing("ymcx_brand", fifth);
    assertMissing("ymcx_brand", sixth);

    long restoreAllId = insertBrand("全部恢复品牌" + suffix);
    deleteMany("brand", restoreAllId);
    assertEquals(1, resources.restoreRecycledResources(token, "brand", all()).getCode());
    assertActive("ymcx_brand", restoreAllId);

    long destroyAllId = insertBrand("全部彻删品牌" + suffix);
    deleteMany("brand", destroyAllId);
    assertEquals(1, resources.destroyRecycledResources(token, "brand", all()).getCode());
    assertMissing("ymcx_brand", destroyAllId);
  }

  @Test
  void platformCarRecycleBinSupportsSingleBatchAndAllOperations() {
    restoreAll("platform-car");
    long brandId = insertBrand("回收车型品牌" + Long.toHexString(System.nanoTime()));
    String suffix = Long.toHexString(System.nanoTime());
    long first = insertPlatformCar("回收车型甲" + suffix, brandId);
    long second = insertPlatformCar("回收车型乙" + suffix, brandId);
    long third = insertPlatformCar("回收车型丙" + suffix, brandId);
    deleteMany("platform-car", first, second, third);

    BusinessResponse<?> recycle = resources.recycledResources(token, "platform-car", 1, 20);
    assertEquals(1, recycle.getCode());
    assertEquals(3, total(recycle));
    assertNotNull(find(rows(recycle), second).get("deletetime"));

    assertEquals(
        1, resources.restoreRecycledResource(token, "platform-car", first).getCode());
    assertEquals(
        1,
        resources
            .restoreRecycledResources(token, "platform-car", ids(second, third))
            .getCode());
    assertActive("ymcx_platform_car", first);
    assertActive("ymcx_platform_car", second);
    assertActive("ymcx_platform_car", third);

    long fourth = insertPlatformCar("彻删车型甲" + suffix, brandId);
    long fifth = insertPlatformCar("彻删车型乙" + suffix, brandId);
    deleteMany("platform-car", fourth, fifth);
    assertEquals(
        1, resources.destroyRecycledResource(token, "platform-car", fourth).getCode());
    assertEquals(
        1,
        resources.destroyRecycledResources(token, "platform-car", ids(fifth)).getCode());
    assertMissing("ymcx_platform_car", fourth);
    assertMissing("ymcx_platform_car", fifth);

    long restoreAllId = insertPlatformCar("全部恢复车型" + suffix, brandId);
    deleteMany("platform-car", restoreAllId);
    assertEquals(
        1, resources.restoreRecycledResources(token, "platform-car", all()).getCode());
    assertActive("ymcx_platform_car", restoreAllId);

    long destroyAllId = insertPlatformCar("全部彻删车型" + suffix, brandId);
    deleteMany("platform-car", destroyAllId);
    assertEquals(
        1, resources.destroyRecycledResources(token, "platform-car", all()).getCode());
    assertMissing("ymcx_platform_car", destroyAllId);
  }

  private void restoreAll(String resource) {
    resources.restoreRecycledResources(token, resource, all());
  }

  private void deleteMany(String resource, long... values) {
    Map<String, Object> body = new LinkedHashMap<>();
    Long[] ids = new Long[values.length];
    for (int index = 0; index < values.length; index++) ids[index] = values[index];
    body.put("ids", Arrays.asList(ids));
    assertEquals(1, resources.deleteMany(token, resource, body).getCode());
  }

  private long insertBrand(String name) {
    jdbc.update(
        "insert into ymcx_brand(name,weigh,status,is_delete,deletetime,createtime,updatetime) values(?,10,1,1,null,1700000001,1700000001)",
        name);
    return jdbc.queryForObject(
        "select id from ymcx_brand where name=? order by id desc limit 1", Long.class, name);
  }

  private long insertPlatformCar(String name, long brandId) {
    jdbc.update(
        "insert into ymcx_platform_car(name,brand_id,image,specification,status,weigh,is_delete,deletetime,createtime,updatetime) values(?,?,'/uploads/test/recycle.jpg','回收测试',1,10,1,null,1700000002,1700000002)",
        name,
        brandId);
    return jdbc.queryForObject(
        "select id from ymcx_platform_car where name=? order by id desc limit 1",
        Long.class,
        name);
  }

  private void assertActive(String table, long id) {
    Map<String, Object> row =
        jdbc.queryForMap("select is_delete,deletetime from " + table + " where id=?", id);
    assertEquals(1, ((Number) row.get("is_delete")).intValue());
    assertNull(row.get("deletetime"));
  }

  private void assertMissing(String table, long id) {
    assertEquals(
        0,
        jdbc.queryForObject(
            "select count(*) from " + table + " where id=?", Integer.class, id));
  }

  private static Map<String, Object> ids(Long... values) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("ids", Arrays.asList(values));
    return body;
  }

  private static Map<String, Object> all() {
    return new LinkedHashMap<>();
  }

  @SuppressWarnings("unchecked")
  private static List<Map<String, Object>> rows(BusinessResponse<?> response) {
    return (List<Map<String, Object>>)
        ((Map<String, Object>) response.getData()).get("data");
  }

  @SuppressWarnings("unchecked")
  private static int total(BusinessResponse<?> response) {
    return ((Number) ((Map<String, Object>) response.getData()).get("total")).intValue();
  }

  private static Map<String, Object> find(List<Map<String, Object>> rows, long id) {
    for (Map<String, Object> row : rows) {
      if (((Number) row.get("id")).longValue() == id) return row;
    }
    assertTrue(false, "回收站列表中缺少记录 " + id);
    return Collections.emptyMap();
  }
}
