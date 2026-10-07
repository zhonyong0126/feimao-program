package com.feimao;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
class ResourceAdminWeighOrderIntegrationTest {
  @Autowired JdbcTemplate jdbc;
  @Autowired ResourceAdminController resources;
  @Autowired AdminSessionService sessions;

  private String token;
  private long first;
  private long second;
  private long third;
  private long otherParent;

  @BeforeEach
  void prepareCategories() {
    long adminId =
        jdbc.queryForObject(
            "select id from ymcx_admin where status='normal' order by id limit 1", Long.class);
    token = sessions.issue(adminId);
    String suffix = Long.toHexString(System.nanoTime());
    first = insertCategory(0, "排序甲" + suffix, "sort-a-" + suffix, 910, 1710000001L);
    second = insertCategory(0, "排序乙" + suffix, "sort-b-" + suffix, 340, 1710000002L);
    third = insertCategory(0, "排序丙" + suffix, "sort-c-" + suffix, 120, 1710000003L);
    otherParent =
        insertCategory(987654321L, "其它父级" + suffix, "sort-x-" + suffix, 555, 1710000004L);
  }

  @Test
  void legacyPayloadRotatesExistingWeightsInsidePidWithoutTouchingUpdateTime() {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("ids", third + "," + otherParent + "," + first + "," + second);
    body.put("changeid", third);
    body.put("field", "status");
    body.put("table", "category");
    body.put("pk", "id");
    body.put("orderway", "desc");
    body.put("pid", "0");

    assertEquals(1, resources.reorder(token, "category", body).getCode());
    assertEquals(910, weight(third));
    assertEquals(340, weight(first));
    assertEquals(120, weight(second));
    assertEquals(555, weight(otherParent));
    assertEquals(1710000001L, updateTime(first));
    assertEquals(1710000002L, updateTime(second));
    assertEquals(1710000003L, updateTime(third));
    assertEquals(1710000004L, updateTime(otherParent));
    assertEquals(
        Arrays.asList(120, 340, 910),
        jdbc.queryForList(
            "select weigh from ymcx_category where id in (?,?,?) order by weigh",
            Integer.class,
            first,
            second,
            third));

    body.put("ids", first + "," + otherParent + "," + second + "," + third);
    body.put("changeid", third);
    assertEquals(1, resources.reorder(token, "category", body).getCode());
    assertEquals(910, weight(first));
    assertEquals(340, weight(second));
    assertEquals(120, weight(third));
    assertEquals(555, weight(otherParent));
    assertEquals(1710000001L, updateTime(first));
    assertEquals(1710000002L, updateTime(second));
    assertEquals(1710000003L, updateTime(third));
  }

  @Test
  void currentPayloadAlsoReusesWeightsAndRejectsTablesOutsideWhitelist() {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("ids", Arrays.asList(second, first, third));
    body.put("orderway", "asc");

    assertEquals(1, resources.reorder(token, "category", body).getCode());
    assertEquals(120, weight(second));
    assertEquals(340, weight(first));
    assertEquals(910, weight(third));

    Map<String, Object> unsafe = new LinkedHashMap<>();
    unsafe.put("ids", Arrays.asList(first, second, third));
    unsafe.put("table", "admin");
    unsafe.put("pk", "id");
    assertEquals(0, resources.reorder(token, "category", unsafe).getCode());
    assertEquals(340, weight(first));
    assertEquals(120, weight(second));
    assertEquals(910, weight(third));
  }

  private long insertCategory(long pid, String name, String diyName, int weigh, long updateTime) {
    jdbc.update(
        "insert into ymcx_category(pid,type,name,nickname,flag,image,keywords,description,"
            + "diyname,createtime,updatetime,weigh,status) values(?,'test',?,'','','','','',?,"
            + "1700000000,?,?, 'normal')",
        pid,
        name,
        diyName,
        updateTime,
        weigh);
    return jdbc.queryForObject(
        "select id from ymcx_category where diyname=? order by id desc limit 1",
        Long.class,
        diyName);
  }

  private int weight(long id) {
    return jdbc.queryForObject(
        "select weigh from ymcx_category where id=?", Integer.class, id);
  }

  private long updateTime(long id) {
    return jdbc.queryForObject(
        "select updatetime from ymcx_category where id=?", Long.class, id);
  }
}
