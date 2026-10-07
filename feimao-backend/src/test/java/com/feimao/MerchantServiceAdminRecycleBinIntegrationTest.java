package com.feimao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.feimao.common.BusinessResponse;
import com.feimao.controller.admin.MerchantServiceAdminController;
import com.feimao.mapper.MerchantServiceAdminMapper;
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
class MerchantServiceAdminRecycleBinIntegrationTest {
  @Autowired JdbcTemplate jdbc;
  @Autowired MerchantServiceAdminController services;
  @Autowired MerchantServiceAdminMapper mapper;
  @Autowired AdminSessionService sessions;

  private String token;
  private long adminId;

  @BeforeEach
  void prepareSession() {
    adminId =
        jdbc.queryForObject(
            "select id from ymcx_admin where status='normal' order by id limit 1", Long.class);
    token = sessions.issue(adminId);
  }

  @Test
  void merchantCarRecycleBinSupportsListSingleBatchAndAllOperations() {
    services.restoreRecycledCars(token, all());
    String suffix = Long.toHexString(System.nanoTime());
    long first = insertCar("回收门店车辆甲" + suffix);
    long second = insertCar("回收门店车辆乙" + suffix);
    long third = insertCar("回收门店车辆丙" + suffix);
    softDeleteCars(first, second, third);

    BusinessResponse<?> recycle = services.recycledCars(token, 1, 100);
    assertEquals(1, recycle.getCode());
    assertNotNull(find(rows(recycle), first).get("deletetime"));
    assertSoftDeletedCar(first);

    assertEquals(1, services.restoreRecycledCar(token, first).getCode());
    assertEquals(1, services.restoreRecycledCars(token, ids(second, third)).getCode());
    assertActiveCar(first);
    assertActiveCar(second);
    assertActiveCar(third);

    long fourth = insertCar("彻删门店车辆甲" + suffix);
    long fifth = insertCar("彻删门店车辆乙" + suffix);
    long sixth = insertCar("彻删门店车辆丙" + suffix);
    softDeleteCars(fourth, fifth, sixth);
    assertEquals(1, services.destroyRecycledCar(token, fourth).getCode());
    assertEquals(1, services.destroyRecycledCars(token, ids(fifth, sixth)).getCode());
    assertMissing("ymcx_merchant_car", fourth);
    assertMissing("ymcx_merchant_car", fifth);
    assertMissing("ymcx_merchant_car", sixth);

    long restoreAllId = insertCar("全部恢复门店车辆" + suffix);
    softDeleteCars(restoreAllId);
    assertEquals(1, services.restoreRecycledCars(token, all()).getCode());
    assertActiveCar(restoreAllId);

    long destroyAllId = insertCar("全部彻删门店车辆" + suffix);
    softDeleteCars(destroyAllId);
    assertEquals(1, services.destroyRecycledCars(token, all()).getCode());
    assertMissing("ymcx_merchant_car", destroyAllId);
  }

  @Test
  void merchantMaintainUsesSoftDeleteAndSupportsCompleteRecycleContract() {
    services.restoreRecycledMaintains(token, all());
    String suffix = Long.toHexString(System.nanoTime());
    long first = insertMaintain("10.01");
    long second = insertMaintain("10.02");
    long third = insertMaintain("10.03");
    softDeleteMaintains(first, second, third);

    assertNotNull(
        jdbc.queryForObject(
            "select deletetime from ymcx_merchant_maintain where id=?", Long.class, first));
    assertEquals(0, mapper.duplicateMaintain(900000001L, 900000002L));
    assertEquals(0, services.updateMaintain(token, first, price("99.00")).getCode());
    BusinessResponse<?> recycle = services.recycledMaintains(token, 1, 100);
    assertEquals(1, recycle.getCode());
    assertNotNull(find(rows(recycle), first).get("deletetime"));

    assertEquals(1, services.restoreRecycledMaintain(token, first).getCode());
    assertEquals(1, services.restoreRecycledMaintains(token, ids(second, third)).getCode());
    assertActiveMaintain(first);
    assertActiveMaintain(second);
    assertActiveMaintain(third);

    long fourth = insertMaintain("20.01");
    long fifth = insertMaintain("20.02");
    long sixth = insertMaintain("20.03");
    softDeleteMaintains(fourth, fifth, sixth);
    assertEquals(1, services.destroyRecycledMaintain(token, fourth).getCode());
    assertEquals(1, services.destroyRecycledMaintains(token, ids(fifth, sixth)).getCode());
    assertMissing("ymcx_merchant_maintain", fourth);
    assertMissing("ymcx_merchant_maintain", fifth);
    assertMissing("ymcx_merchant_maintain", sixth);

    long restoreAllId = insertMaintain("30.01");
    softDeleteMaintains(restoreAllId);
    assertEquals(1, services.restoreRecycledMaintains(token, all()).getCode());
    assertActiveMaintain(restoreAllId);

    long destroyAllId = insertMaintain("30.02");
    softDeleteMaintains(destroyAllId);
    assertEquals(1, services.destroyRecycledMaintains(token, all()).getCode());
    assertMissing("ymcx_merchant_maintain", destroyAllId);
  }

  private long insertCar(String name) {
    jdbc.update(
        "insert into ymcx_merchant_car(platform_car_id,merchant_id,name,stock,total_stock,status,is_delete,deletetime,createtime,updatetime,admin_id) values(1,1,?,1,1,1,1,null,1700000001,1700000001,?)",
        name,
        adminId);
    return jdbc.queryForObject(
        "select id from ymcx_merchant_car where name=? order by id desc limit 1", Long.class, name);
  }

  private long insertMaintain(String price) {
    jdbc.update(
        "insert into ymcx_merchant_maintain(merchant_id,admin_id,maintain_id,price,deletetime,createtime,updatetime) values(900000001,?,900000002,?,null,1700000002,1700000002)",
        adminId,
        price);
    return jdbc.queryForObject(
        "select id from ymcx_merchant_maintain where merchant_id=900000001 and price=? order by id desc limit 1",
        Long.class,
        price);
  }

  private void softDeleteCars(long... values) {
    for (long id : values) assertEquals(1, services.deleteCar(token, id).getCode());
  }

  private void softDeleteMaintains(long... values) {
    for (long id : values) assertEquals(1, services.deleteMaintain(token, id).getCode());
  }

  private void assertSoftDeletedCar(long id) {
    Map<String, Object> row =
        jdbc.queryForMap("select is_delete,deletetime from ymcx_merchant_car where id=?", id);
    assertEquals(1, ((Number) row.get("is_delete")).intValue());
    assertNotNull(row.get("deletetime"));
  }

  private void assertActiveCar(long id) {
    Map<String, Object> row =
        jdbc.queryForMap("select is_delete,deletetime from ymcx_merchant_car where id=?", id);
    assertEquals(1, ((Number) row.get("is_delete")).intValue());
    assertNull(row.get("deletetime"));
  }

  private void assertActiveMaintain(long id) {
    assertNull(
        jdbc.queryForObject(
            "select deletetime from ymcx_merchant_maintain where id=?", Long.class, id));
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

  private static Map<String, Object> price(String value) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("price", value);
    return body;
  }

  @SuppressWarnings("unchecked")
  private static List<Map<String, Object>> rows(BusinessResponse<?> response) {
    return (List<Map<String, Object>>)
        ((Map<String, Object>) response.getData()).get("data");
  }

  private static Map<String, Object> find(List<Map<String, Object>> rows, long id) {
    for (Map<String, Object> row : rows) {
      if (((Number) row.get("id")).longValue() == id) return row;
    }
    assertTrue(false, "回收站列表中缺少记录 " + id);
    return Collections.emptyMap();
  }
}
