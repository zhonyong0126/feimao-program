package com.feimao;

import static org.junit.jupiter.api.Assertions.*;

import com.feimao.common.BusinessResponse;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import com.feimao.controller.admin.AddressAdminController;
import com.feimao.controller.admin.BusinessAdminController;
import com.feimao.controller.admin.MerchantServiceAdminController;
import com.feimao.controller.admin.OrderActionAdminController;
import com.feimao.controller.admin.OrderFinanceAdminController;
import com.feimao.controller.admin.PartnerAdminController;
import com.feimao.controller.admin.RbacAdminController;
import com.feimao.controller.admin.ResourceAdminController;
import com.feimao.controller.admin.SystemAdminController;
import com.feimao.controller.admin.UserAccessAdminController;
import com.feimao.controller.admin.UserAdminController;
import com.feimao.controller.common.CommonController;
import com.feimao.service.AdminPermissionService;
import com.feimao.service.AdminSessionService;
import com.feimao.service.WechatTransferService;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class AdminIntegrationTest {
  @Autowired JdbcTemplate jdbc;
  @Autowired BusinessAdminController admins;
  @Autowired AddressAdminController addressAdmin;
  @Autowired ResourceAdminController resources;
  @Autowired MerchantServiceAdminController merchantServices;
  @Autowired OrderFinanceAdminController orderFinance;
  @Autowired OrderActionAdminController orderActions;
  @Autowired PartnerAdminController partnerAdmin;
  @Autowired SystemAdminController systemAdmin;
  @Autowired UserAdminController userAdmin;
  @Autowired UserAccessAdminController userAccess;
  @Autowired RbacAdminController rbac;
  @Autowired CommonController common;
  @Autowired AdminSessionService sessions;
  @Autowired AdminPermissionService permissions;
  @MockBean WechatTransferService transfers;

  @Test
  void adminCanAddLegacyCashRecordWithoutChangingOwnerBalance() {
    long operator = jdbc.queryForObject("select id from ymcx_admin where status='normal' order by id limit 1", Long.class);
    long merchantId = jdbc.queryForObject("select id from ymcx_merchant where is_delete=1 order by id limit 1", Long.class);
    BigDecimal before = jdbc.queryForObject("select money from ymcx_merchant where id=?", BigDecimal.class, merchantId);
    String token = sessions.issue(operator), number = "ADMIN" + System.nanoTime();
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("out_trade_no", number);
    body.put("merchant_id", merchantId);
    body.put("amount", "12.34");
    body.put("status", 1);
    body.put("note", "后台新增测试");
    assertEquals(1, admins.addCash(token, "merchant", body).getCode());
    Map<String, Object> saved = jdbc.queryForMap("select merchant_id,amount,status,note,is_delete from ymcx_merchant_cash where out_trade_no=?", number);
    assertEquals(merchantId, ((Number) saved.get("merchant_id")).longValue());
    assertEquals(0, new BigDecimal("12.34").compareTo((BigDecimal) saved.get("amount")));
    assertEquals(1, ((Number) saved.get("status")).intValue());
    assertEquals("后台新增测试", saved.get("note"));
    assertEquals(1, ((Number) saved.get("is_delete")).intValue());
    assertEquals(0, before.compareTo(jdbc.queryForObject("select money from ymcx_merchant where id=?", BigDecimal.class, merchantId)));
  }

  @Test
  void adminPhysicallyDeletesOrdersRegardlessOfStateLikeLegacyBackend() {
    long operator =
        jdbc.queryForObject(
            "select id from ymcx_admin where status='normal' order by id limit 1", Long.class);
    long userId =
        jdbc.queryForObject(
            "select id from ymcx_user where status='normal' order by id limit 1", Long.class);
    long merchantId =
        jdbc.queryForObject(
            "select id from ymcx_merchant where is_delete=1 order by id limit 1", Long.class);
    String token = sessions.issue(operator);
    long now = System.currentTimeMillis() / 1000;
    String activeNumber = "AD" + System.nanoTime();
    String cancelledNumber = "CD" + System.nanoTime();
    String sql =
        "insert into ymcx_order(user_id,merchant_id,number,type,state,is_delete,createtime,updatetime) "
            + "values(?,?,?,1,?,1,?,?)";
    jdbc.update(sql, userId, merchantId, activeNumber, 2, now, now);
    jdbc.update(sql, userId, merchantId, cancelledNumber, 6, now, now);
    long activeId =
        jdbc.queryForObject(
            "select id from ymcx_order where number=?", Long.class, activeNumber);
    long cancelledId =
        jdbc.queryForObject(
            "select id from ymcx_order where number=?", Long.class, cancelledNumber);

    assertEquals(1, orderFinance.deleteOrder(token, activeId).getCode());
    assertEquals(
        0,
        jdbc.queryForObject(
            "select count(*) from ymcx_order where id=?", Integer.class, activeId));
    assertEquals(1, orderFinance.deleteOrder(token, cancelledId).getCode());
    assertEquals(
        0,
        jdbc.queryForObject(
            "select count(*) from ymcx_order where id=?", Integer.class, cancelledId));
    assertEquals(0, orderFinance.deleteOrder(token, cancelledId).getCode());
    assertEquals(
        "/api/admin/business/orders/" + cancelledId,
        jdbc.queryForObject(
            "select url from ymcx_admin_log where title='删除订单' and content=? order by id desc limit 1",
            String.class,
            String.valueOf(cancelledId)));
  }

  @Test
  void uploadedFileIsRegisteredAsAttachment() throws Exception {
    long admin =
        jdbc.queryForObject(
            "select id from ymcx_admin where status='normal' order by id limit 1", Long.class);
    String token = sessions.issue(admin);
    byte[] basePng =
        Base64.getDecoder()
            .decode(
                "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=");
    byte[] suffix = ("upload-" + System.nanoTime()).getBytes(StandardCharsets.UTF_8);
    byte[] png = Arrays.copyOf(basePng, basePng.length + suffix.length);
    System.arraycopy(suffix, 0, png, basePng.length, suffix.length);
    MockMultipartFile file = new MockMultipartFile("file", "pixel.png", "image/png", png);
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setScheme("http");
    request.setServerName("localhost");
    request.setServerPort(8080);

    Map<String, Object> data =
        (Map<String, Object>) common.upload(file, token, null, "admin-test", null, request).getData();
    String url = String.valueOf(data.get("url"));
    try {
      Map<String, Object> saved =
          jdbc.queryForMap(
              "select category,admin_id,user_id,imagewidth,imageheight,imageframes,filename,filesize,mimetype,imagetype,extparam,storage,sha1,createtime,updatetime,uploadtime from ymcx_attachment where url=?",
              url);
      assertEquals("", saved.get("category"));
      assertEquals(admin, ((Number) saved.get("admin_id")).longValue());
      assertEquals(0, ((Number) saved.get("user_id")).longValue());
      assertEquals("1", saved.get("imagewidth"));
      assertEquals("1", saved.get("imageheight"));
      assertEquals(0, ((Number) saved.get("imageframes")).intValue());
      assertEquals("pixel.png", saved.get("filename"));
      assertEquals((long) png.length, ((Number) saved.get("filesize")).longValue());
      assertEquals("image/png", saved.get("mimetype"));
      assertEquals("png", saved.get("imagetype"));
      assertEquals("", saved.get("extparam"));
      assertEquals("local", saved.get("storage"));
      assertEquals(40, String.valueOf(saved.get("sha1")).length());
      assertTrue(((Number) saved.get("createtime")).longValue() > 0);
      assertTrue(((Number) saved.get("updatetime")).longValue() > 0);
      assertTrue(((Number) saved.get("uploadtime")).longValue() > 0);
      String expectedMd5 = org.springframework.util.DigestUtils.md5DigestAsHex(png);
      assertTrue(url.matches("/uploads/\\d{8}/[0-9a-f]{32}\\.png"));
      assertTrue(url.endsWith("/" + expectedMd5 + ".png"));
    } finally {
      Files.deleteIfExists(
          Paths.get("uploads", url.substring("/uploads/".length())).toAbsolutePath());
    }
  }

  @Test
  void attachmentUploadMatchesLegacyMediaArchiveWhitelistAndClassification() throws Exception {
    jdbc.update(
        "update ymcx_config set value=? where name='attachmentcategory'",
        "{\"media\":\"音视频\",\"archive\":\"压缩包\"}");
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setScheme("http");
    request.setServerName("localhost");
    request.setServerPort(8080);
    Map<String, String> legal = new LinkedHashMap<>();
    legal.put("wav", "audio/wav");
    legal.put("mp3", "audio/mpeg");
    legal.put("mp4", "video/mp4");
    legal.put("webm", "video/webm");
    legal.put("zip", "application/zip");
    legal.put("rar", "application/vnd.rar");
    List<Path> uploaded = new ArrayList<>();
    try {
      for (Map.Entry<String, String> item : legal.entrySet()) {
        String ext = item.getKey();
        byte[] content =
            ("legacy-upload-" + ext + "-" + System.nanoTime())
                .getBytes(StandardCharsets.UTF_8);
        String filename = "mp3".equals(ext) ? "<b>song</b>.mp3" : "sample." + ext;
        MockMultipartFile file =
            new MockMultipartFile("file", filename, item.getValue(), content);
        BusinessResponse<?> response = common.upload(file, null, null, "media", null, request);
        assertEquals(1, response.getCode(), ext);
        Map<String, Object> responseData = (Map<String, Object>) response.getData();
        String url = String.valueOf(responseData.get("url"));
        String md5 = org.springframework.util.DigestUtils.md5DigestAsHex(content);
        assertTrue(url.matches("/uploads/\\d{8}/[0-9a-f]{32}\\." + ext), ext);
        assertTrue(url.endsWith("/" + md5 + "." + ext), ext);
        uploaded.add(
            Paths.get("uploads", url.substring("/uploads/".length())).toAbsolutePath());
        Map<String, Object> saved =
            jdbc.queryForMap(
                "select category,admin_id,user_id,url,imagewidth,imageheight,imagetype,imageframes,filename,filesize,mimetype,extparam,storage,sha1 from ymcx_attachment where url=? order by id desc limit 1",
                url);
        assertEquals("media", saved.get("category"), ext);
        assertEquals(0, ((Number) saved.get("admin_id")).longValue(), ext);
        assertEquals(0, ((Number) saved.get("user_id")).longValue(), ext);
        assertEquals(url, saved.get("url"), ext);
        assertEquals("", saved.get("imagewidth"), ext);
        assertEquals("", saved.get("imageheight"), ext);
        assertEquals(ext, saved.get("imagetype"), ext);
        assertEquals(0, ((Number) saved.get("imageframes")).intValue(), ext);
        assertEquals("mp3".equals(ext) ? "song.mp3" : "sample." + ext, saved.get("filename"), ext);
        assertEquals((long) content.length, ((Number) saved.get("filesize")).longValue(), ext);
        assertEquals(item.getValue(), saved.get("mimetype"), ext);
        assertEquals("", saved.get("extparam"), ext);
        assertEquals("local", saved.get("storage"), ext);
        assertEquals(40, String.valueOf(saved.get("sha1")).length(), ext);
      }

      byte[] duplicateContent =
          ("duplicate-" + System.nanoTime()).getBytes(StandardCharsets.UTF_8);
      MockMultipartFile duplicateFile =
          new MockMultipartFile("file", "duplicate.zip", "application/zip", duplicateContent);
      BusinessResponse<?> firstDuplicate =
          common.upload(duplicateFile, null, null, "archive", null, request);
      BusinessResponse<?> secondDuplicate =
          common.upload(duplicateFile, null, null, "archive", null, request);
      assertEquals(1, firstDuplicate.getCode());
      assertEquals(1, secondDuplicate.getCode());
      String duplicateUrl =
          String.valueOf(((Map<?, ?>) firstDuplicate.getData()).get("url"));
      assertEquals(duplicateUrl, ((Map<?, ?>) secondDuplicate.getData()).get("url"));
      Path duplicatePath =
          Paths.get("uploads", duplicateUrl.substring("/uploads/".length())).toAbsolutePath();
      uploaded.add(duplicatePath);
      assertEquals(
          1,
          jdbc.queryForObject(
              "select count(*) from ymcx_attachment where url=? and storage='local'",
              Integer.class,
              duplicateUrl));
      assertTrue(Files.exists(duplicatePath));

      byte[] unclassedContent =
          ("unclassed-" + System.nanoTime()).getBytes(StandardCharsets.UTF_8);
      BusinessResponse<?> unclassed =
          common.upload(
              new MockMultipartFile(
                  "file", "unclassed.mp3", "audio/mpeg", unclassedContent),
              null,
              null,
              "unclassed",
              null,
              request);
      assertEquals(1, unclassed.getCode());
      String unclassedUrl =
          String.valueOf(((Map<?, ?>) unclassed.getData()).get("url"));
      uploaded.add(
          Paths.get("uploads", unclassedUrl.substring("/uploads/".length())).toAbsolutePath());
      assertEquals(
          "",
          jdbc.queryForObject(
              "select category from ymcx_attachment where url=? order by id desc limit 1",
              String.class,
              unclassedUrl));

      byte[] invalidCategoryContent =
          ("invalid-category-" + System.nanoTime()).getBytes(StandardCharsets.UTF_8);
      BusinessResponse<?> invalidCategory =
          common.upload(
              new MockMultipartFile(
                  "file", "invalid.zip", "application/zip", invalidCategoryContent),
              null,
              null,
              " media ",
              null,
              request);
      assertEquals(1, invalidCategory.getCode());
      String invalidCategoryUrl =
          String.valueOf(((Map<?, ?>) invalidCategory.getData()).get("url"));
      uploaded.add(
          Paths.get("uploads", invalidCategoryUrl.substring("/uploads/".length())).toAbsolutePath());
      assertEquals(
          "",
          jdbc.queryForObject(
              "select category from ymcx_attachment where url=? order by id desc limit 1",
              String.class,
              invalidCategoryUrl));

      int before = jdbc.queryForObject("select count(*) from ymcx_attachment", Integer.class);
      assertEquals(
          0,
          common
              .upload(
                  new MockMultipartFile(
                      "file", "document.pdf", "application/pdf", "%PDF-1.7".getBytes(StandardCharsets.UTF_8)),
                  null,
                  null,
                  "media",
                  null,
                  request)
              .getCode());
      assertEquals(
          0,
          common
              .upload(
                  new MockMultipartFile(
                      "file", "page.zip", "text/html", "<html></html>".getBytes(StandardCharsets.UTF_8)),
                  null,
                  null,
                  "archive",
                  null,
                  request)
              .getCode());
      assertEquals(
          0,
          common
              .upload(
                  new MockMultipartFile(
                      "file", "fake.jpg", "image/jpeg", "not-an-image".getBytes(StandardCharsets.UTF_8)),
                  null,
                  null,
                  "media",
                  null,
                  request)
              .getCode());
      assertEquals(
          before,
          jdbc.queryForObject("select count(*) from ymcx_attachment", Integer.class));
    } finally {
      for (Path path : uploaded) Files.deleteIfExists(path);
    }
  }

  @Test
  void merchantApprovalPreservesCommissionAndDeliveryFeeSettings() {
    long admin =
        jdbc.queryForObject(
            "select id from ymcx_admin where status='normal' order by id limit 1", Long.class);
    long merchant =
        jdbc.queryForObject(
            "select id from ymcx_merchant where is_delete=1 order by id limit 1", Long.class);
    jdbc.update("update ymcx_merchant set examine_status=1 where id=?", merchant);
    String token = sessions.issue(admin);
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("id", merchant);
    body.put("type", 2);
    body.put("username", "approval-test-" + System.nanoTime());
    body.put("password", "Test123456");
    body.put("commission_normal_rate", 20);
    body.put("commission_merchant_rate", 15);
    body.put("commission_shop_rate", 10);
    for (int index = 1; index <= 4; index++) body.put("service_distance" + index, index * 3);
    for (int index = 1; index <= 5; index++) body.put("service_money" + index, index * 10);

    assertEquals(1, admins.examineApplication(token, "merchant", body).getCode());
    Map<String, Object> approved =
        jdbc.queryForMap(
            "select"
                + " examine_status,commission_normal_rate,commission_merchant_rate,commission_shop_rate,service_distance4,service_money5"
                + " from ymcx_merchant where id=?",
            merchant);
    assertEquals(2, ((Number) approved.get("examine_status")).intValue());
    assertEquals(20, ((Number) approved.get("commission_normal_rate")).intValue());
    assertEquals(15, ((Number) approved.get("commission_merchant_rate")).intValue());
    assertEquals(10, ((Number) approved.get("commission_shop_rate")).intValue());
    assertEquals(12, ((Number) approved.get("service_distance4")).intValue());
    assertEquals(
        0,
        new BigDecimal("50").compareTo(new BigDecimal(approved.get("service_money5").toString())));
  }

  @Test
  void adminSoftDeletesMerchantCashLikeLegacyBackend() {
    long operator =
        jdbc.queryForObject(
            "select id from ymcx_admin where status='normal' order by id limit 1", Long.class);
    long merchant =
        jdbc.queryForObject(
            "select id from ymcx_merchant where is_delete=1 order by id limit 1", Long.class);
    long now = System.currentTimeMillis() / 1000;
    String number = "ADMIN-CASH-DELETE-" + System.nanoTime();
    jdbc.update(
        "insert into ymcx_merchant_cash(out_trade_no,merchant_id,amount,status,createtime,updatetime,is_delete) values(?,?,1,1,?,?,1)",
        number,
        merchant,
        now,
        now);
    long id =
        jdbc.queryForObject(
            "select id from ymcx_merchant_cash where out_trade_no=?", Long.class, number);

    String token = sessions.issue(operator);
    assertEquals(1, admins.deleteCash(token, "merchant", id).getCode());
    assertEquals(
        2,
        jdbc.queryForObject(
            "select is_delete from ymcx_merchant_cash where id=?", Integer.class, id));
    assertNotNull(
        jdbc.queryForObject(
            "select deletetime from ymcx_merchant_cash where id=?", Long.class, id));
    assertEquals(0, admins.deleteCash(token, "merchant", id).getCode());
  }

  @Test
  void rejectedMerchantCashIsReturnedExactlyOnce() {
    long admin =
        jdbc.queryForObject(
            "select id from ymcx_admin where status='normal' order by id limit 1", Long.class);
    String token = sessions.issue(admin);
    Map<String, Object> m =
        jdbc.queryForMap(
            "select id,money from ymcx_merchant where is_delete=1 order by id limit 1");
    long merchant = ((Number) m.get("id")).longValue(), now = System.currentTimeMillis() / 1000;
    BigDecimal before = new BigDecimal(m.get("money").toString()), amount = new BigDecimal("3.25");
    jdbc.update(
        "insert into"
            + " ymcx_merchant_cash(out_trade_no,merchant_id,amount,status,createtime,updatetime,is_delete)"
            + " values(?,?,?,1,?,?,1)",
        "ADMIN-ROLLBACK-TEST",
        merchant,
        amount,
        now,
        now);
    long cash =
        jdbc.queryForObject(
            "select id from ymcx_merchant_cash where out_trade_no='ADMIN-ROLLBACK-TEST'",
            Long.class);
    assertEquals(1, admins.cashDetail(token, "merchant", cash).getCode());
    assertEquals(1, admins.cashList(token, "merchant", 1, 10, 1, "ADMIN-ROLLBACK-TEST").getCode());
    Map<String, Object> b = new LinkedHashMap<>();
    b.put("id", cash);
    b.put("note", "测试拒绝");
    assertEquals(1, admins.rejectCash(token, "merchant", b).getCode());
    BigDecimal after =
        jdbc.queryForObject(
            "select money from ymcx_merchant where id=?", BigDecimal.class, merchant);
    assertEquals(0, before.add(amount).compareTo(after));
    assertEquals(0, admins.rejectCash(token, "merchant", b).getCode(), "重复审核必须拒绝");
  }

  @Test
  void unconfiguredWechatTransferNeverMarksCashAsPaid() {
    when(transfers.transfer(anyString(), anyString(), anyLong(), anyString()))
        .thenThrow(new IllegalStateException("微信提现未配置"));
    long admin =
        jdbc.queryForObject(
            "select id from ymcx_admin where status='normal' order by id limit 1", Long.class);
    long merchant =
        jdbc.queryForObject(
            "select m.id from ymcx_merchant m join ymcx_user u on u.id=m.user_id where"
                + " m.is_delete=1 order by m.id limit 1",
            Long.class);
    long now = System.currentTimeMillis() / 1000;
    String number = "TRANSFER-CONFIG-" + System.nanoTime();
    jdbc.update(
        "insert into"
            + " ymcx_merchant_cash(out_trade_no,merchant_id,amount,status,createtime,updatetime,is_delete)"
            + " values(?,?,1.00,1,?,?,1)",
        number,
        merchant,
        now,
        now);
    long cashId =
        jdbc.queryForObject(
            "select id from ymcx_merchant_cash where out_trade_no=?", Long.class, number);
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("id", cashId);
    assertEquals(0, admins.approveCash(sessions.issue(admin), "merchant", body).getCode());
    assertEquals(
        1,
        jdbc.queryForObject(
            "select status from ymcx_merchant_cash where id=?", Integer.class, cashId));
    doReturn("MOCK-WECHAT-BATCH")
        .when(transfers)
        .transfer(anyString(), anyString(), anyLong(), anyString());
    assertEquals(1, admins.approveCash(sessions.issue(admin), "merchant", body).getCode());
    Map<String, Object> paid =
        jdbc.queryForMap(
            "select status,pay_number,pay_money from ymcx_merchant_cash where id=?", cashId);
    assertEquals(2, ((Number) paid.get("status")).intValue());
    assertEquals("MOCK-WECHAT-BATCH", paid.get("pay_number"));
    assertEquals(
        0, new BigDecimal("1.00").compareTo(new BigDecimal(paid.get("pay_money").toString())));
    assertEquals(
        1,
        jdbc.queryForObject(
            "select count(*) from ymcx_platform_log where order_id=? and user_type=2 and type=3",
            Integer.class,
            cashId));
  }

  @Test
  void adminCanManageBusinessResources() {
    long admin =
        jdbc.queryForObject(
            "select id from ymcx_admin where status='normal' order by id limit 1", Long.class);
    String token = sessions.issue(admin);
    String name = "资源迁移回滚测试-" + System.nanoTime();
    assertEquals(1, merchantServices.maintainOptions(token, null, 1, 10).getCode());

    Map<String, Object> create = new LinkedHashMap<>();
    create.put("name", name);
    create.put("weigh", 99);
    create.put("status", 1);
    assertEquals(1, resources.add(token, "brand", create).getCode());

    long id =
        jdbc.queryForObject(
            "select id from ymcx_brand where name=? and is_delete=1", Long.class, name);
    Map<String, Object> update = new LinkedHashMap<>();
    update.put("name", name + "-已修改");
    assertEquals(1, resources.update(token, "brand", id, update).getCode());
    assertEquals(
        name + "-已修改",
        jdbc.queryForObject("select name from ymcx_brand where id=?", String.class, id));

    Map<String, Object> sort = new LinkedHashMap<>();
    sort.put("new_sort", 7);
    assertEquals(1, resources.sortBrand(token, id, sort).getCode());
    assertEquals(
        7, jdbc.queryForObject("select weigh from ymcx_brand where id=?", Integer.class, id));
    sort.put("new_sort", -1);
    assertEquals(0, resources.sortBrand(token, id, sort).getCode());

    Map<String, Object> second = new LinkedHashMap<>();
    second.put("name", name + "-排序2");
    second.put("weigh", 98);
    second.put("status", 1);
    assertEquals(1, resources.add(token, "brand", second).getCode());
    long secondId =
        jdbc.queryForObject(
            "select id from ymcx_brand where name=? and is_delete=1", Long.class, name + "-排序2");
    Map<String, Object> reorder = new LinkedHashMap<>();
    reorder.put("ids", Arrays.asList(secondId, id));
    reorder.put("orderway", "asc");
    assertEquals(1, resources.reorder(token, "brand", reorder).getCode());
    assertEquals(
        7, jdbc.queryForObject("select weigh from ymcx_brand where id=?", Integer.class, secondId));
    assertEquals(
        98, jdbc.queryForObject("select weigh from ymcx_brand where id=?", Integer.class, id));
    reorder.put("ids", Arrays.asList(id, Long.MAX_VALUE));
    assertEquals(0, resources.reorder(token, "brand", reorder).getCode());
    assertEquals(
        98, jdbc.queryForObject("select weigh from ymcx_brand where id=?", Integer.class, id));

    Map<String, Object> bulkStatus = new LinkedHashMap<>();
    bulkStatus.put("ids", Arrays.asList(id, id, "invalid"));
    bulkStatus.put("status", 2);
    assertEquals(1, resources.updateStatuses(token, "brand", bulkStatus).getCode());
    assertEquals(
        2, jdbc.queryForObject("select status from ymcx_brand where id=?", Integer.class, id));
    bulkStatus.put("status", 3);
    assertEquals(0, resources.updateStatuses(token, "brand", bulkStatus).getCode());
    bulkStatus.put("status", 1);
    assertEquals(1, resources.updateStatuses(token, "brand", bulkStatus).getCode());

    assertEquals(1, resources.delete(token, "brand", id).getCode());
    assertEquals(1, resources.delete(token, "brand", secondId).getCode());
    assertEquals(
        2, jdbc.queryForObject("select is_delete from ymcx_brand where id=?", Integer.class, id));

    Map<String, Object> category = new LinkedHashMap<>();
    category.put("pid", 0);
    category.put("type", "page");
    category.put("name", "分类迁移回滚测试-" + System.nanoTime());
    category.put("nickname", "category-test");
    category.put("weigh", 10);
    category.put("status", "normal");
    assertEquals(1, resources.add(token, "category", category).getCode());
    long categoryId =
        jdbc.queryForObject(
            "select id from ymcx_category where name=?", Long.class, category.get("name"));
    Map<String, Object> child = new LinkedHashMap<>(category);
    child.put("pid", categoryId);
    child.put("name", category.get("name") + "-子分类");
    child.put("nickname", "category-child");
    child.remove("weigh");
    assertEquals(1, resources.add(token, "category", child).getCode());
    long childId =
        jdbc.queryForObject(
            "select id from ymcx_category where name=?", Long.class, child.get("name"));
    assertEquals(
        childId,
        jdbc.queryForObject("select weigh from ymcx_category where id=?", Long.class, childId));
    Map<String, Object> cycle = new LinkedHashMap<>();
    cycle.put("pid", childId);
    assertEquals(0, resources.update(token, "category", categoryId, cycle).getCode());
    Map<String, Object> categoryStatus = new LinkedHashMap<>();
    categoryStatus.put("ids", Collections.singletonList(childId));
    categoryStatus.put("status", "hidden");
    assertEquals(1, resources.updateStatuses(token, "category", categoryStatus).getCode());
    assertEquals(
        "hidden",
        jdbc.queryForObject(
            "select status from ymcx_category where id=?", String.class, childId));
    assertEquals(1, resources.list(token, "category", 1, 20, "category-test", "page").getCode());
    assertEquals(1, resources.delete(token, "category", childId).getCode());
    assertEquals(1, resources.delete(token, "category", categoryId).getCode());
  }

  @Test
  void merchantCarInventoryRulesArePreserved() {
    long admin =
        jdbc.queryForObject(
            "select id from ymcx_admin where status='normal' order by id limit 1", Long.class);
    long merchant =
        jdbc.queryForObject(
            "select id from ymcx_merchant where is_delete=1 order by id limit 1", Long.class);
    long brand =
        jdbc.queryForObject(
            "select id from ymcx_brand where is_delete=1 order by id limit 1", Long.class);
    String token = sessions.issue(admin);

    Map<String, Object> platformCar = new LinkedHashMap<>();
    platformCar.put("name", "库存规则回滚测试-" + System.nanoTime());
    platformCar.put("brand_id", brand);
    platformCar.put("image", "/uploads/test.png");
    assertEquals(1, resources.add(token, "platform-car", platformCar).getCode());
    long platformCarId =
        jdbc.queryForObject(
            "select id from ymcx_platform_car where name=?", Long.class, platformCar.get("name"));

    Map<String, Object> car = new LinkedHashMap<>();
    car.put("merchant_id", merchant);
    car.put("platform_car_id", platformCarId);
    car.put("name", "测试车辆");
    car.put("total_stock", 5);
    assertEquals(1, merchantServices.addCar(token, car).getCode());
    long carId =
        jdbc.queryForObject(
            "select id from ymcx_merchant_car where merchant_id=? and platform_car_id=?",
            Long.class,
            merchant,
            platformCarId);
    jdbc.update("update ymcx_merchant_car set stock=3 where id=?", carId);

    Map<String, Object> invalid = new LinkedHashMap<>();
    invalid.put("total_stock", 1);
    assertEquals(0, merchantServices.updateCar(token, carId, invalid).getCode());
    Map<String, Object> valid = new LinkedHashMap<>();
    valid.put("total_stock", 7);
    assertEquals(1, merchantServices.updateCar(token, carId, valid).getCode());
    Map<String, Object> inventory =
        jdbc.queryForMap("select total_stock,stock from ymcx_merchant_car where id=?", carId);
    assertEquals(7, ((Number) inventory.get("total_stock")).intValue());
    assertEquals(5, ((Number) inventory.get("stock")).intValue());
  }

  @Test
  void adminOrderGuestLifecycleMatchesLegacyPageAndEndpointRules() {
    long operator =
        jdbc.queryForObject(
            "select id from ymcx_admin where status='normal' order by id limit 1", Long.class);
    Map<String, Object> order =
        jdbc.queryForMap(
            "select id,user_id from ymcx_order where type=1 and is_delete=1 order by id limit 1");
    String token = sessions.issue(operator);
    Map<String, Object> guest = new LinkedHashMap<>();
    guest.put("order_id", order.get("id"));
    guest.put("name", "同行人回滚测试");
    guest.put("mobile", "138" + "0013" + "8000");
    guest.put("idcard", "110101" + "19900101" + "0011");
    guest.put("insure_money", "0.01");
    assertEquals(1, orderFinance.addOrderGuest(token, guest).getCode());
    long guestId =
        jdbc.queryForObject(
            "select id from ymcx_order_guest where name='同行人回滚测试' order by id desc limit 1",
            Long.class);
    assertEquals(
        ((Number) order.get("user_id")).longValue(),
        jdbc.queryForObject(
            "select user_id from ymcx_order_guest where id=?", Long.class, guestId));
    guest.put("name", "已编辑同行人");
    assertEquals(1, orderFinance.editOrderGuest(token, guestId, guest).getCode());
    jdbc.update("update ymcx_order_guest set is_insure=2 where id=?", guestId);
    assertEquals(1, orderFinance.editOrderGuest(token, guestId, guest).getCode());
    assertEquals(1, orderFinance.deleteOrderGuest(token, guestId).getCode());
    assertEquals(0, orderFinance.deleteOrderGuest(token, guestId).getCode());
  }

  @Test
  void adminInstallmentLifecycleProtectsPaidBills() {
    long admin =
        jdbc.queryForObject(
            "select id from ymcx_admin where status='normal' order by id limit 1", Long.class);
    Map<String, Object> user = jdbc.queryForMap("select id from ymcx_user order by id limit 1");
    Map<String, Object> merchant =
        jdbc.queryForMap("select id from ymcx_merchant where is_delete=1 order by id limit 1");
    String token = sessions.issue(admin);
    String number = "INSTALL" + System.nanoTime();
    long now = System.currentTimeMillis() / 1000;
    jdbc.update(
        "insert into ymcx_order(user_id,merchant_id,number,type,lease_type,state,pick_state,"
            + "cash_state,source_type,source_id,lease_money,cash_price,pay_price,last_all_money,"
            + "is_delete,createtime,updatetime) values(?,?,?,1,4,2,1,1,1,0,0,0,0,0,1,?,?)",
        user.get("id"),
        merchant.get("id"),
        number,
        now,
        now);
    Map<String, Object> bill = new LinkedHashMap<>();
    bill.put("user_id", user.get("id"));
    bill.put("number", number);
    bill.put("num", 1);
    bill.put("price", "199.00");
    BusinessResponse<?> added = orderFinance.addInstallment(token, bill);
    assertEquals(1, added.getCode());
    @SuppressWarnings("unchecked")
    long billId = ((Number) ((Map<String, Object>) added.getData()).get("id")).longValue();
    assertEquals(
        ((Number) user.get("id")).longValue(),
        jdbc.queryForObject(
            "select user_id from ymcx_order_child where id=?", Long.class, billId));
    assertEquals(1, orderFinance.addInstallment(token, bill).getCode());

    bill.put("num", 2);
    bill.put("price", "299.00");
    assertEquals(1, orderFinance.editInstallment(token, billId, bill).getCode());
    jdbc.update("update ymcx_order_child set state=2 where id=?", billId);
    assertEquals(1, orderFinance.editInstallment(token, billId, bill).getCode());
    assertEquals(1, orderFinance.deleteInstallment(token, billId).getCode());
    assertEquals(
        "/api/admin/business/installments",
        jdbc.queryForObject(
            "select url from ymcx_admin_log where title='新增订单分期' and content=? order by id desc limit 1",
            String.class,
            String.valueOf(billId)));
    assertEquals(
        "/api/admin/business/installments/" + billId,
        jdbc.queryForObject(
            "select url from ymcx_admin_log where title='删除订单分期' and content=? order by id desc limit 1",
            String.class,
            String.valueOf(billId)));
  }

  @Test
  void adminOrderAndFinanceQueriesUseImportedSchema() {
    long admin =
        jdbc.queryForObject(
            "select id from ymcx_admin where status='normal' order by id limit 1", Long.class);
    String token = sessions.issue(admin);
    assertEquals(1, orderFinance.orders(token, null, null, null, null, 1, 5).getCode());
    assertEquals(1, orderFinance.orderGuests(token, null, null, 1, 5).getCode());
    assertEquals(1, orderFinance.installments(token, null, null, 1, 5).getCode());
    for (String kind : Arrays.asList("merchant", "shop", "system", "platform")) {
      assertEquals(1, orderFinance.finance(token, kind, null, null, 1, 5).getCode());
    }
    assertEquals(1, orderFinance.userCoupons(token, null, null, 1, 5).getCode());
    assertEquals(1, orderFinance.signLogs(token, null, 1, 5).getCode());
    assertEquals(1, orderFinance.operationLogs(token, null, 1, 5).getCode());
  }

  @Test
  void adminUserCouponLifecycleMatchesGenericLegacyCrud() {
    long admin =
        jdbc.queryForObject(
            "select id from ymcx_admin where status='normal' order by id limit 1", Long.class);
    long userId = jdbc.queryForObject("select id from ymcx_user order by id limit 1", Long.class);
    String token = sessions.issue(admin);
    long endtime = System.currentTimeMillis() / 1000 + 86400;
    Map<String, Object> coupon = new LinkedHashMap<>();
    coupon.put("user_id", userId);
    coupon.put("price", "100.00");
    coupon.put("reduceprice", "10.00");
    coupon.put("state", 2);
    coupon.put("endtime", endtime);
    coupon.put("usetime", endtime - 3600);

    assertEquals(1, orderFinance.addUserCoupon(token, coupon).getCode());
    long couponId =
        jdbc.queryForObject(
            "select id from ymcx_user_coupon where user_id=? and endtime=? order by id desc limit"
                + " 1",
            Long.class,
            userId,
            endtime);
    assertEquals(1, orderFinance.userCoupons(token, userId, 2, 1, 10).getCode());

    coupon.put("reduceprice", "20.00");
    assertEquals(1, orderFinance.editUserCoupon(token, couponId, coupon).getCode());
    assertEquals(
        0,
        new BigDecimal("20.00")
            .compareTo(
                jdbc.queryForObject(
                    "select reduceprice from ymcx_user_coupon where id=?",
                    BigDecimal.class,
                    couponId)));

    coupon.put("state", 1);
    coupon.put("usetime", "");
    assertEquals(1, orderFinance.editUserCoupon(token, couponId, coupon).getCode());
    assertNull(jdbc.queryForObject("select usetime from ymcx_user_coupon where id=?", Long.class, couponId));
    assertEquals(1, orderFinance.deleteUserCoupon(token, couponId).getCode());
    assertNotNull(
        jdbc.queryForObject(
            "select deletetime from ymcx_user_coupon where id=?", Long.class, couponId));

    Map<String, Object> ids = new LinkedHashMap<>();
    ids.put("ids", Arrays.asList(couponId));
    assertEquals(1, orderFinance.restoreUserCoupons(token, ids).getCode());
    assertNull(
        jdbc.queryForObject(
            "select deletetime from ymcx_user_coupon where id=?", Long.class, couponId));
    assertEquals(1, orderFinance.deleteUserCoupons(token, ids).getCode());
    assertEquals(1, orderFinance.destroyUserCoupons(token, ids).getCode());
    assertEquals(
        0,
        jdbc.queryForObject(
            "select count(*) from ymcx_user_coupon where id=?", Integer.class, couponId));
  }

  @Test
  void adminSignLogSupportsLegacyCrudAndDateRange() {
    long admin = jdbc.queryForObject("select id from ymcx_admin where status='normal' order by id limit 1", Long.class);
    long userId = jdbc.queryForObject("select id from ymcx_user order by id limit 1", Long.class);
    String token = sessions.issue(admin);
    Map<String,Object> body = new LinkedHashMap<>();
    body.put("user_id", userId);
    body.put("score", 7);
    assertEquals(1, orderFinance.addSignLog(token, body).getCode());
    long id = jdbc.queryForObject("select id from ymcx_sign_log where user_id=? and score=7 order by id desc limit 1", Long.class, userId);
    assertEquals(1, orderFinance.signLogs(token, userId, "2000-01-01", "2099-12-31", 1, 10).getCode());
    body.put("score", 9);
    assertEquals(1, orderFinance.editSignLog(token, id, body).getCode());
    assertEquals(9, jdbc.queryForObject("select score from ymcx_sign_log where id=?", Integer.class, id));
    assertEquals(1, orderFinance.deleteSignLog(token, id).getCode());
    assertEquals(0, jdbc.queryForObject("select count(*) from ymcx_sign_log where id=?", Integer.class, id));
  }

  @Test
  void adminOperationLogDetailAndBulkDeleteAreAudited() {
    assertEquals(
        1,
        jdbc.queryForObject("select count(*) from ymcx_admin where id=1", Integer.class),
        "旧系统超级管理员必须存在");
    String token = sessions.issue(1L);
    long now = System.currentTimeMillis() / 1000;
    String marker = "LOG-MIGRATION-" + System.nanoTime();
    jdbc.update(
        "insert into ymcx_admin_log(admin_id,username,url,title,content,ip,useragent,createtime)"
            + " values(1,'admin','/test','迁移测试',?,'127.0.0.1','test',?)",
        marker + "-1",
        now);
    jdbc.update(
        "insert into ymcx_admin_log(admin_id,username,url,title,content,ip,useragent,createtime)"
            + " values(1,'admin','/test','迁移测试',?,'127.0.0.1','test',?)",
        marker + "-2",
        now);
    List<Long> ids =
        jdbc.queryForList(
            "select id from ymcx_admin_log where content like ? order by id",
            Long.class,
            marker + "%");
    assertEquals(2, ids.size());
    assertEquals(1, orderFinance.operationLogDetail(token, ids.get(0)).getCode());

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("ids", Arrays.asList(ids.get(0), ids.get(1), ids.get(1), "invalid"));
    assertEquals(1, orderFinance.deleteOperationLogs(token, body).getCode());
    assertEquals(
        0,
        jdbc.queryForObject(
            "select count(*) from ymcx_admin_log where id in (?,?)",
            Integer.class,
            ids.get(0),
            ids.get(1)));
    assertEquals(
        1,
        jdbc.queryForObject(
            "select count(*) from ymcx_admin_log where title='删除管理员操作日志' and content=?",
            Integer.class,
            ids.toString()));
  }

  @Test
  void adminOrderActionsReturnInventoryExactlyOnce() {
    long admin =
        jdbc.queryForObject(
            "select id from ymcx_admin where status='normal' order by id limit 1", Long.class);
    Map<String, Object> car =
        jdbc.queryForMap(
            "select mc.id,mc.merchant_id,mc.total_stock from ymcx_merchant_car mc join"
                + " ymcx_merchant m on m.id=mc.merchant_id and m.is_delete=1 where mc.is_delete=1"
                + " and mc.total_stock>0 order by mc.id limit 1");
    long carId = ((Number) car.get("id")).longValue();
    int available = ((Number) car.get("total_stock")).intValue() - 1;
    jdbc.update("update ymcx_merchant_car set stock=? where id=?", available, carId);
    String token = sessions.issue(admin);
    long now = System.currentTimeMillis() / 1000;
    String number = "A" + System.currentTimeMillis();
    jdbc.update(
        "insert into"
            + " ymcx_order(merchant_id,merchant_car_id,number,type,state,pick_state,cash_state,source_type,source_id,lease_money,cash_price,pay_price,last_all_money,is_delete,createtime,updatetime)"
            + " values(?,?,?,1,4,2,1,1,0,0,0,0,0,1,?,?)",
        car.get("merchant_id"),
        carId,
        number,
        now,
        now);
    long orderId =
        jdbc.queryForObject("select id from ymcx_order where number=?", Long.class, number);
    Map<String, Object> confirm = new LinkedHashMap<>();
    confirm.put("refund_cash_price", 0);
    assertEquals(1, orderActions.confirmReturn(token, orderId, confirm).getCode());
    assertEquals(
        5, jdbc.queryForObject("select state from ymcx_order where id=?", Integer.class, orderId));
    assertEquals(
        available + 1,
        jdbc.queryForObject(
            "select stock from ymcx_merchant_car where id=?", Integer.class, carId));
    assertEquals(0, orderActions.confirmReturn(token, orderId, confirm).getCode());

    jdbc.update("update ymcx_merchant_car set stock=? where id=?", available, carId);
    String cancelNumber = "C" + System.currentTimeMillis();
    jdbc.update(
        "insert into"
            + " ymcx_order(merchant_id,merchant_car_id,number,type,state,pick_state,cash_state,source_type,source_id,lease_money,cash_price,pay_price,last_all_money,is_delete,createtime,updatetime)"
            + " values(?,?,?,1,1,1,1,1,0,0,0,0,0,1,?,?)",
        car.get("merchant_id"),
        carId,
        cancelNumber,
        now,
        now);
    long cancelId =
        jdbc.queryForObject("select id from ymcx_order where number=?", Long.class, cancelNumber);
    Map<String, Object> cancel = new LinkedHashMap<>();
    cancel.put("cancel_refund_price", 0);
    assertEquals(1, orderActions.cancel(token, cancelId, cancel).getCode());
    assertEquals(
        6, jdbc.queryForObject("select state from ymcx_order where id=?", Integer.class, cancelId));
    assertEquals(
        available + 1,
        jdbc.queryForObject(
            "select stock from ymcx_merchant_car where id=?", Integer.class, carId));
    assertEquals(0, orderActions.cancel(token, cancelId, cancel).getCode());

    jdbc.update("update ymcx_merchant_car set stock=? where id=?", available, carId);
    String buyNumber = "B" + System.nanoTime();
    jdbc.update(
        "insert into"
            + " ymcx_order(merchant_id,merchant_car_id,number,type,state,pick_state,cash_state,source_type,source_id,lease_money,cash_price,pay_price,last_all_money,is_delete,createtime,updatetime)"
            + " values(?,?,?,2,1,1,1,1,0,0,0,0,0,1,?,?)",
        car.get("merchant_id"),
        carId,
        buyNumber,
        now,
        now);
    long buyId =
        jdbc.queryForObject("select id from ymcx_order where number=?", Long.class, buyNumber);
    assertEquals(1, orderActions.cancel(token, buyId, cancel).getCode());
    assertEquals(
        available,
        jdbc.queryForObject(
            "select stock from ymcx_merchant_car where id=?", Integer.class, carId));
    assertEquals(
        "/api/admin/business/orders/" + orderId + "/confirm-return",
        jdbc.queryForObject(
            "select url from ymcx_admin_log where title='管理员确认还车' and content=? order by id desc limit 1",
            String.class,
            String.valueOf(orderId)));
    assertEquals(
        "/api/admin/business/orders/" + buyId + "/cancel",
        jdbc.queryForObject(
            "select url from ymcx_admin_log where title='管理员取消订单' and content=? order by id desc limit 1",
            String.class,
            String.valueOf(buyId)));
  }

  @Test
  void adminCanManageConfigurationAttachmentsAndProfile() throws Exception {
    long admin =
        jdbc.queryForObject(
            "select id from ymcx_admin where status='normal' order by id limit 1", Long.class);
    String token = sessions.issue(admin);
    Map<String, Object> config =
        jdbc.queryForMap("select name,value from ymcx_config order by id limit 1");
    Map<String, Object> update = new LinkedHashMap<>();
    update.put(String.valueOf(config.get("name")), "ROLLBACK-" + System.nanoTime());
    assertEquals(1, systemAdmin.updateConfig(token, update).getCode());
    assertEquals(
        update.get(config.get("name")),
        jdbc.queryForObject(
            "select value from ymcx_config where name=?", String.class, config.get("name")));
    BusinessResponse<?> visibleConfigs = systemAdmin.config(token, null);
    assertEquals(1, visibleConfigs.getCode());
    List<?> visibleConfigRows = (List<?>) visibleConfigs.getData();
    assertEquals(11, visibleConfigRows.size());
    Set<String> visibleGroups = new LinkedHashSet<>();
    Set<String> visibleNames = new LinkedHashSet<>();
    for (Object item : visibleConfigRows) {
      Map<?, ?> row = (Map<?, ?>) item;
      visibleGroups.add(String.valueOf(row.get("group")));
      visibleNames.add(String.valueOf(row.get("name")));
    }
    assertEquals(
        new LinkedHashSet<>(Arrays.asList("basic", "system", "agreement")), visibleGroups);
    assertTrue(visibleNames.contains("name"));
    assertTrue(visibleNames.contains("avatar"));
    assertFalse(visibleNames.contains("mail_smtp_pass"));
    assertFalse(visibleNames.contains("cdnurl"));
    assertFalse(visibleNames.contains("version"));
    assertFalse(visibleNames.contains("system_money"));
    assertTrue(((List<?>) systemAdmin.config(token, "email").getData()).isEmpty());
    assertEquals(
        false,
        ((Map<?, ?>)
                systemAdmin
                    .checkConfigName(token, String.valueOf(config.get("name")), null)
                    .getData())
            .get("available"));
    assertEquals(
        true,
        ((Map<?, ?>)
                systemAdmin
                    .checkConfigName(token, "migration_" + System.nanoTime(), null)
                    .getData())
            .get("available"));
    assertEquals(1, systemAdmin.configRules(token, "required,email").getCode());
    assertEquals(2, ((List<?>) systemAdmin.configRules(token, "required,email").getData()).size());
    assertEquals(
        0,
        systemAdmin
            .configOptions(token, null, "系统", null, null, 1, 10, new MockHttpServletRequest())
            .getCode());
    assertEquals(1, systemAdmin.attachments(token, null, null, null, 1, 5).getCode());
    Map<String, Object> attachmentFilterSource =
        jdbc.queryForMap(
            "select id,category,admin_id,user_id,url,imagewidth,imageheight,imagetype,filename,"
                + "filesize,mimetype,storage,createtime from ymcx_attachment order by id limit 1");
    String filterCategory = String.valueOf(attachmentFilterSource.get("category"));
    BusinessResponse<?> filteredAttachments =
        systemAdmin.attachments(
            token,
            String.valueOf(attachmentFilterSource.get("id")),
            filterCategory.isEmpty() ? "unclassed" : filterCategory,
            ((Number) attachmentFilterSource.get("admin_id")).longValue(),
            ((Number) attachmentFilterSource.get("user_id")).longValue(),
            String.valueOf(attachmentFilterSource.get("filename")),
            ((Number) attachmentFilterSource.get("filesize")).longValue(),
            ((Number) attachmentFilterSource.get("filesize")).longValue(),
            String.valueOf(attachmentFilterSource.get("imagewidth")),
            String.valueOf(attachmentFilterSource.get("imageheight")),
            String.valueOf(attachmentFilterSource.get("imagetype")),
            String.valueOf(attachmentFilterSource.get("storage")),
            "image/*,video/*",
            ((Number) attachmentFilterSource.get("createtime")).longValue(),
            ((Number) attachmentFilterSource.get("createtime")).longValue(),
            1,
            5);
    assertEquals(1, filteredAttachments.getCode());
    Map<?, ?> filteredPage = (Map<?, ?>) filteredAttachments.getData();
    assertEquals(1, ((Number) filteredPage.get("total")).intValue());
    Map<?, ?> filteredAttachment = (Map<?, ?>) ((List<?>) filteredPage.get("data")).get(0);
    assertEquals(attachmentFilterSource.get("url"), filteredAttachment.get("fullurl"));
    assertEquals(attachmentFilterSource.get("url"), filteredAttachment.get("preview"));
    assertEquals(
        filterCategory.isEmpty() ? "unclassed" : filterCategory,
        filteredAttachment.get("category"));
    List<Long> attachmentIds =
        jdbc.queryForList("select id from ymcx_attachment order by id limit 2", Long.class);
    assertFalse(attachmentIds.isEmpty());
    Map<String, Object> classify = new LinkedHashMap<>();
    classify.put("ids", attachmentIds);
    classify.put("category", "category1");
    assertEquals(1, systemAdmin.classifyAttachments(token, classify).getCode());
    assertEquals(
        attachmentIds.size(),
        jdbc.queryForObject(
            "select count(*) from ymcx_attachment where category='category1' and id in ("
                + String.join(",", Collections.nCopies(attachmentIds.size(), "?"))
                + ")",
            Integer.class,
            attachmentIds.toArray()));
    assertEquals(1, systemAdmin.attachmentCategories(token).getCode());
    assertEquals(
        1, systemAdmin.selectAttachments(token, null, "category1", "image/*", 1, 5).getCode());
    classify.put("category", "unclassed");
    assertEquals(1, systemAdmin.classifyAttachments(token, classify).getCode());

    String testDirectory = "migration-test-" + System.nanoTime();
    Path localFile = Paths.get("uploads", testDirectory, "delete.txt").toAbsolutePath();
    Files.createDirectories(localFile.getParent());
    Files.write(localFile, "migration".getBytes(StandardCharsets.UTF_8));
    String localUrl = "/uploads/" + testDirectory + "/" + localFile.getFileName();
    jdbc.update(
        "insert into"
            + " ymcx_attachment(admin_id,user_id,url,filename,filesize,mimetype,storage,sha1,createtime,updatetime,uploadtime)"
            + " values(1,0,?,?,9,'text/plain','local','',?,?,?)",
        localUrl,
        localFile.getFileName().toString(),
        System.currentTimeMillis() / 1000,
        System.currentTimeMillis() / 1000,
        System.currentTimeMillis() / 1000);
    long localAttachmentId =
        jdbc.queryForObject(
            "select id from ymcx_attachment where url=? order by id desc limit 1",
            Long.class,
            localUrl);
    assertEquals(1, systemAdmin.deleteAttachment(token, localAttachmentId).getCode());
    assertFalse(Files.exists(localFile));
    Files.deleteIfExists(localFile.getParent());

    jdbc.update(
        "insert into"
            + " ymcx_attachment(admin_id,user_id,url,filename,filesize,mimetype,storage,sha1,createtime,updatetime,uploadtime)"
            + " values(1,0,'/uploads/../outside.txt','outside.txt',1,'text/plain','local','',?,?,?)",
        System.currentTimeMillis() / 1000,
        System.currentTimeMillis() / 1000,
        System.currentTimeMillis() / 1000);
    long unsafeAttachmentId =
        jdbc.queryForObject(
            "select id from ymcx_attachment where url='/uploads/../outside.txt' order by id desc"
                + " limit 1",
            Long.class);
    assertEquals(1, systemAdmin.deleteAttachment(token, unsafeAttachmentId).getCode());
    assertEquals(
        0,
        jdbc.queryForObject(
            "select count(*) from ymcx_attachment where id=?", Integer.class, unsafeAttachmentId));
    assertEquals(1, systemAdmin.deleteAttachment(token, Long.MAX_VALUE).getCode());
    assertEquals(1, systemAdmin.profile(token, 1, 5).getCode());

    Map<String, Object> profile = new LinkedHashMap<>();
    String originalMobile =
        jdbc.queryForObject("select mobile from ymcx_admin where id=?", String.class, admin);
    profile.put("nickname", "管理员回滚测试");
    profile.put("email", "rollback-test@example.com");
    profile.put("mobile", "199" + String.format("%08d", System.nanoTime() % 100000000));
    assertEquals(1, systemAdmin.updateProfile(token, profile).getCode());
    assertEquals(
        "管理员回滚测试",
        jdbc.queryForObject("select nickname from ymcx_admin where id=?", String.class, admin));
    assertEquals(
        originalMobile,
        jdbc.queryForObject("select mobile from ymcx_admin where id=?", String.class, admin));
    Map<?, ?> profileResult = (Map<?, ?>) systemAdmin.profile(token, 1, 5).getData();
    Map<?, ?> logPage = (Map<?, ?>) profileResult.get("logs");
    List<?> logRows = (List<?>) logPage.get("data");
    assertFalse(logRows.isEmpty());
    Map<?, ?> profileLog = (Map<?, ?>) logRows.get(0);
    assertTrue(profileLog.containsKey("id"));
    assertTrue(profileLog.containsKey("title"));
    assertTrue(profileLog.containsKey("url"));
    assertTrue(profileLog.containsKey("ip"));
    assertTrue(profileLog.containsKey("createtime"));
    assertFalse(profileLog.containsKey("content"));
  }

  @Test
  void attachmentEditClassificationAndBulkDeleteFollowLegacyRules() throws Exception {
    long admin =
        jdbc.queryForObject(
            "select id from ymcx_admin where status='normal' order by id limit 1", Long.class);
    String token = sessions.issue(admin);
    assertEquals(
        1,
        jdbc.update(
            "update ymcx_config set value=? where name='attachmentcategory'",
            "{\"legacy\":\"旧分类\",\"second\":\"第二分类\"}"));

    BusinessResponse<?> categories = systemAdmin.attachmentCategories(token);
    assertEquals(1, categories.getCode());
    List<?> categoryItems = (List<?>) categories.getData();
    assertEquals(3, categoryItems.size());
    assertEquals("legacy", ((Map<?, ?>) categoryItems.get(0)).get("category"));
    assertEquals("旧分类", ((Map<?, ?>) categoryItems.get(0)).get("label"));
    assertEquals("unclassed", ((Map<?, ?>) categoryItems.get(2)).get("category"));
    assertEquals("未归类", ((Map<?, ?>) categoryItems.get(2)).get("label"));

    String directoryName = "legacy-attachment-" + System.nanoTime();
    Path directory = Paths.get("uploads", directoryName).toAbsolutePath();
    Path existingFile = directory.resolve("existing.png");
    Files.createDirectories(directory);
    Files.write(existingFile, "attachment".getBytes(StandardCharsets.UTF_8));
    long now = System.currentTimeMillis() / 1000;
    String existingUrl = "/uploads/" + directoryName + "/existing.png";
    String missingUrl = "/uploads/" + directoryName + "/missing.png";
    try {
      jdbc.update(
          "insert into ymcx_attachment(category,admin_id,user_id,url,imagewidth,imageheight,"
              + "imagetype,imageframes,filename,filesize,mimetype,extparam,createtime,updatetime,"
              + "uploadtime,storage,sha1) values('',?,0,?,'1','1','png',1,'before.png',10,"
              + "'image/png','',?,1,?,'local','')",
          admin,
          existingUrl,
          now,
          now);
      jdbc.update(
          "insert into ymcx_attachment(category,admin_id,user_id,url,filename,filesize,mimetype,"
              + "createtime,updatetime,uploadtime,storage,sha1) values('',?,0,?,'missing.png',1,"
              + "'image/png',?,?,?,'local','')",
          admin,
          missingUrl,
          now,
          now,
          now);
      long existingId =
          jdbc.queryForObject(
              "select id from ymcx_attachment where url=? order by id desc limit 1",
              Long.class,
              existingUrl);
      long missingId =
          jdbc.queryForObject(
              "select id from ymcx_attachment where url=? order by id desc limit 1",
              Long.class,
              missingUrl);

      Map<String, Object> edit = new LinkedHashMap<>();
      edit.put("category", "legacy");
      edit.put("url", existingUrl);
      edit.put("imagewidth", "320");
      edit.put("imageheight", "240");
      edit.put("imagetype", "png");
      edit.put("imageframes", 2);
      edit.put("filename", "edited.png");
      edit.put("filesize", 10);
      edit.put("mimetype", "image/png");
      edit.put("extparam", " legacy-edit ");
      edit.put("uploadtime", now - 100);
      edit.put("storage", "local");
      edit.put("admin_id", admin + 1000);
      assertEquals(1, systemAdmin.updateAttachment(token, existingId, edit).getCode());
      Map<String, Object> edited =
          jdbc.queryForMap("select * from ymcx_attachment where id=?", existingId);
      assertEquals("legacy", edited.get("category"));
      assertEquals("320", String.valueOf(edited.get("imagewidth")));
      assertEquals("240", String.valueOf(edited.get("imageheight")));
      assertEquals("png", edited.get("imagetype"));
      assertEquals(2, ((Number) edited.get("imageframes")).intValue());
      assertEquals("edited.png", edited.get("filename"));
      assertEquals(10, ((Number) edited.get("filesize")).intValue());
      assertEquals("image/png", edited.get("mimetype"));
      assertEquals(" legacy-edit ", edited.get("extparam"));
      assertEquals(now - 100, ((Number) edited.get("uploadtime")).longValue());
      assertEquals("local", edited.get("storage"));
      assertEquals(admin, ((Number) edited.get("admin_id")).longValue());

      long editedUpdateTime = ((Number) edited.get("updatetime")).longValue();
      BusinessResponse<?> unchangedEdit = systemAdmin.updateAttachment(token, existingId, edit);
      assertEquals(1, unchangedEdit.getCode());
      assertEquals(0, ((Number) unchangedEdit.getData()).intValue());
      assertEquals(
          editedUpdateTime,
          jdbc.queryForObject(
                  "select updatetime from ymcx_attachment where id=?", Long.class, existingId)
              .longValue());

      Map<String, Object> emptyNumbers = new LinkedHashMap<>();
      emptyNumbers.put("imageframes", "");
      emptyNumbers.put("filesize", "");
      emptyNumbers.put("uploadtime", "");
      assertEquals(1, systemAdmin.updateAttachment(token, existingId, emptyNumbers).getCode());
      Map<String, Object> clearedNumbers =
          jdbc.queryForMap(
              "select imageframes,filesize,uploadtime,updatetime from ymcx_attachment where id=?",
              existingId);
      assertEquals(0, ((Number) clearedNumbers.get("imageframes")).intValue());
      assertEquals(0, ((Number) clearedNumbers.get("filesize")).intValue());
      assertEquals(0, ((Number) clearedNumbers.get("uploadtime")).longValue());
      long updateTime = ((Number) clearedNumbers.get("updatetime")).longValue();
      BusinessResponse<?> unchangedNumbers =
          systemAdmin.updateAttachment(token, existingId, emptyNumbers);
      assertEquals(1, unchangedNumbers.getCode());
      assertEquals(0, ((Number) unchangedNumbers.getData()).intValue());
      assertEquals(
          updateTime,
          jdbc.queryForObject(
                  "select updatetime from ymcx_attachment where id=?", Long.class, existingId)
              .longValue());

      Map<String, Object> classify = new LinkedHashMap<>();
      classify.put("ids", Arrays.asList(existingId, missingId));
      classify.put("category", "second");
      assertEquals(1, systemAdmin.classifyAttachments(token, classify).getCode());
      assertEquals(
          2,
          jdbc.queryForObject(
              "select count(*) from ymcx_attachment where id in (?,?) and category='second'",
              Integer.class,
              existingId,
              missingId));
      assertEquals(
          updateTime,
          jdbc.queryForObject(
                  "select updatetime from ymcx_attachment where id=?", Long.class, existingId)
              .longValue());

      classify.put("category", "not-configured");
      assertEquals(0, systemAdmin.classifyAttachments(token, classify).getCode());
      classify.put("ids", Collections.singletonList(Long.MAX_VALUE));
      classify.put("category", "legacy");
      assertEquals(1, systemAdmin.classifyAttachments(token, classify).getCode());
      classify.put("ids", Arrays.asList(existingId, missingId));
      classify.put("category", "unclassed");
      assertEquals(1, systemAdmin.classifyAttachments(token, classify).getCode());
      assertEquals(
          2,
          jdbc.queryForObject(
              "select count(*) from ymcx_attachment where id in (?,?) and category=''",
              Integer.class,
              existingId,
              missingId));

      Map<String, Object> delete = new LinkedHashMap<>();
      delete.put("ids", Arrays.asList(existingId, missingId, Long.MAX_VALUE));
      assertEquals(1, systemAdmin.deleteAttachments(token, null, delete).getCode());
      assertFalse(Files.exists(existingFile));
      assertEquals(
          0,
          jdbc.queryForObject(
              "select count(*) from ymcx_attachment where id in (?,?)",
              Integer.class,
              existingId,
              missingId));
      assertEquals(1, systemAdmin.deleteAttachments(token, null, delete).getCode());
    } finally {
      Files.deleteIfExists(existingFile);
      Files.deleteIfExists(directory);
    }
  }

  @Test
  void adminPartnerLifecycleBindsAndReleasesUser() {
    long operator =
        jdbc.queryForObject(
            "select id from ymcx_admin where status='normal' order by id limit 1", Long.class);
    long userId =
        jdbc.queryForObject(
            "select id from ymcx_user where shop_id=0 order by id limit 1", Long.class);
    String token = sessions.issue(operator), suffix = String.valueOf(System.nanoTime());
    Map<String, Object> shop = new LinkedHashMap<>();
    shop.put("user_id", userId);
    shop.put("username", "shop" + suffix);
    shop.put("password", "test123456");
    shop.put("name", "酒店迁移测试" + suffix);
    shop.put("nickname", "酒店联系人");
    shop.put("mobile", "138" + "0013" + "8000");
    shop.put("address", "回滚测试地址");
    shop.put("status", 2);
    assertEquals(1, partnerAdmin.add(token, "shop", shop).getCode());
    assertEquals(
        1, partnerAdmin.list(token, "shop", 1, 10, String.valueOf(shop.get("name")), 2).getCode());
    long shopId =
        jdbc.queryForObject(
            "select id from ymcx_shop where username=?", Long.class, shop.get("username"));
    assertEquals(1, partnerAdmin.detail(token, "shop", shopId).getCode());
    assertEquals(
        2,
        jdbc.queryForObject("select status from ymcx_shop where id=?", Integer.class, shopId));
    assertEquals(
        shopId,
        jdbc.queryForObject("select shop_id from ymcx_user where id=?", Long.class, userId));
    Map<String, Object> edit = new LinkedHashMap<>();
    edit.put("nickname", "已编辑酒店联系人");
    assertEquals(1, partnerAdmin.edit(token, "shop", shopId, edit).getCode());
    assertEquals(1, partnerAdmin.delete(token, "shop", shopId).getCode());
    assertEquals(
        2,
        jdbc.queryForObject("select is_delete from ymcx_shop where id=?", Integer.class, shopId));
    assertEquals(
        0, jdbc.queryForObject("select shop_id from ymcx_user where id=?", Integer.class, userId));

    long merchantId =
        jdbc.queryForObject(
            "select m.id from ymcx_merchant m where m.is_delete=1 and exists(select 1 from"
                + " ymcx_merchant_car c where c.merchant_id=m.id and c.is_delete=1) limit 1",
            Long.class);
    assertEquals(0, partnerAdmin.delete(token, "merchant", merchantId).getCode());
  }

  @Test
  void dashboardIncludesBusinessUserTrendAndAttachmentMetrics() {
    long operator =
        jdbc.queryForObject(
            "select id from ymcx_admin where status='normal' order by id limit 1", Long.class);
    Map<String, Object> dashboard =
        (Map<String, Object>) admins.dashboard(sessions.issue(operator)).getData();
    assertEquals(7, ((List<?>) dashboard.get("signup_labels")).size());
    assertEquals(7, ((List<?>) dashboard.get("signup_counts")).size());
    assertEquals(
        jdbc.queryForObject("select count(*) from ymcx_user", Integer.class),
        dashboard.get("totaluser"));
    assertEquals(
        jdbc.queryForObject("select count(*) from ymcx_attachment", Integer.class),
        dashboard.get("attachmentnums"));
    assertTrue(((Number) dashboard.get("attachmentsize")).longValue() >= 0);
    assertTrue(dashboard.containsKey("todayusersignup"));
    assertTrue(dashboard.containsKey("sevendau"));
    assertEquals(
        jdbc.queryForObject("select count(*) from ymcx_category", Integer.class),
        dashboard.get("totalcategory"));
    assertTrue(((Number) dashboard.get("dbtablenums")).intValue() >= 43);
    assertTrue(((Number) dashboard.get("dbsize")).longValue() > 0);
  }

  @Test
  void adminAddressLifecycleUsesImportedUserAddressTable() {
    long operator =
        jdbc.queryForObject(
            "select id from ymcx_admin where status='normal' order by id limit 1", Long.class);
    long userId = jdbc.queryForObject("select id from ymcx_user order by id limit 1", Long.class);
    String token = sessions.issue(operator), suffix = String.valueOf(System.nanoTime());
    assertEquals(1, addressAdmin.areas(token, null, null, null, null, null).getCode());
    Map<String, Object> address = new LinkedHashMap<>();
    address.put("user_id", userId);
    address.put("name", "地址测试" + suffix);
    address.put("mobile", "138" + "0013" + "8000");
    address.put("administrative", "广东省-深圳市-南山区");
    address.put("address", "迁移回滚测试地址");
    address.put("lng", "113.123456");
    address.put("lat", "22.123456");
    assertEquals(1, addressAdmin.add(token, address).getCode());
    long addressId =
        jdbc.queryForObject(
            "select id from ymcx_address where name=?", Long.class, address.get("name"));
    assertEquals(1, addressAdmin.list(token, suffix, userId, 1, 10).getCode());
    address.put("address", "已编辑的回滚测试地址");
    assertEquals(1, addressAdmin.edit(token, addressId, address).getCode());
    assertEquals(
        "已编辑的回滚测试地址",
        jdbc.queryForObject(
            "select address from ymcx_address where id=?", String.class, addressId));
    assertEquals(1, addressAdmin.delete(token, addressId).getCode());
    assertEquals(
        0,
        jdbc.queryForObject(
            "select count(*) from ymcx_address where id=?", Integer.class, addressId));
  }

  @Test
  void memberGroupAndRuleTreeLifecycleIsConsistent() {
    long operator =
        jdbc.queryForObject(
            "select id from ymcx_admin where status='normal' order by id limit 1", Long.class);
    String token = sessions.issue(operator), suffix = String.valueOf(System.nanoTime());
    Map<String, Object> parent = new LinkedHashMap<>();
    parent.put("name", "api/migrated-" + suffix);
    parent.put("title", "会员权限迁移测试");
    parent.put("ismenu", 1);
    assertEquals(1, userAccess.addRule(token, parent).getCode());
    long parentId =
        jdbc.queryForObject(
            "select id from ymcx_user_rule where name=?", Long.class, parent.get("name"));

    Map<String, Object> child = new LinkedHashMap<>();
    child.put("pid", parentId);
    child.put("name", "api/migrated-" + suffix + "/view");
    child.put("title", "查看测试权限");
    assertEquals(1, userAccess.addRule(token, child).getCode());
    long childId =
        jdbc.queryForObject(
            "select id from ymcx_user_rule where name=?", Long.class, child.get("name"));

    Map<String, Object> group = new LinkedHashMap<>();
    group.put("name", "会员权限组-" + suffix);
    group.put("rule_ids", Arrays.asList(parentId, childId));
    assertEquals(1, userAccess.addGroup(token, group).getCode());
    long groupId =
        jdbc.queryForObject(
            "select id from ymcx_user_group where name=?", Long.class, group.get("name"));
    assertEquals(1, userAccess.groups(token).getCode());
    assertEquals(1, userAccess.rules(token).getCode());

    parent.put("pid", childId);
    assertEquals(0, userAccess.editRule(token, parentId, parent).getCode());
    assertEquals(1, userAccess.deleteRule(token, parentId).getCode());
    assertEquals(
        "",
        jdbc.queryForObject("select rules from ymcx_user_group where id=?", String.class, groupId));
    assertEquals(1, userAccess.deleteGroup(token, groupId).getCode());
  }

  @Test
  void adminMemberLifecycleUsesBusinessUserTableAndCleansTokens() {
    long operator =
        jdbc.queryForObject(
            "select id from ymcx_admin where status='normal' order by id limit 1", Long.class);
    String token = sessions.issue(operator), suffix = String.valueOf(System.nanoTime());
    Map<String, Object> member = new LinkedHashMap<>();
    member.put("username", "member" + suffix);
    member.put("nickname", "会员迁移测试");
    member.put("mobile", "19" + suffix.substring(suffix.length() - 9));
    member.put("email", "member-" + suffix + "@example.com");
    member.put("password", "test123456");
    member.put("money", "10.00");
    member.put("score", 20);
    assertEquals(1, userAdmin.add(token, member).getCode());
    long userId =
        jdbc.queryForObject(
            "select id from ymcx_user where username=?", Long.class, member.get("username"));
    assertEquals(
        1,
        userAdmin
            .list(token, userId, "会员迁移", member.get("mobile").toString(), null, null, null, 1, 10)
            .getCode());

    Map<String, Object> edit = new LinkedHashMap<>();
    edit.put("nickname", "已编辑会员");
    edit.put("status", "hidden");
    edit.put("money", "15.50");
    edit.put("score", 27);
    edit.put("memo", "后台活动补积分");
    edit.put("birthday", "1995-06-18");
    edit.put("truename", "迁移测试会员");
    edit.put("jointime", "2024-01-02T03:04:00");
    assertEquals(1, userAdmin.edit(token, userId, edit).getCode());
    assertEquals(
        "hidden",
        jdbc.queryForObject("select status from ymcx_user where id=?", String.class, userId));
    assertEquals(
        "已编辑会员",
        jdbc.queryForObject("select nickname from ymcx_user where id=?", String.class, userId));
    assertEquals(
        LocalDateTime.of(2024, 1, 2, 3, 4)
            .atZone(ZoneId.systemDefault())
            .toEpochSecond(),
        jdbc.queryForObject("select jointime from ymcx_user where id=?", Long.class, userId));
    assertEquals(
        new BigDecimal("10.00"),
        jdbc.queryForObject("select money from ymcx_user where id=?", BigDecimal.class, userId));
    assertEquals(
        20, jdbc.queryForObject("select score from ymcx_user where id=?", Integer.class, userId));
    assertEquals(
        0,
        jdbc.queryForObject(
            "select count(*) from ymcx_user_money_log where user_id=?", Integer.class, userId));
    assertEquals(
        0,
        jdbc.queryForObject(
            "select count(*) from ymcx_user_score_log where user_id=?", Integer.class, userId));
    Map<String, Object> invalidMoney = new LinkedHashMap<>();
    invalidMoney.put("money", "-0.01");
    assertEquals(0, userAdmin.edit(token, userId, invalidMoney).getCode());
    jdbc.update(
        "insert into ymcx_user_token(token,user_id,createtime,expiretime) values(?,?,?,?)",
        "MEMBER-DELETE-" + suffix,
        userId,
        System.currentTimeMillis() / 1000,
        System.currentTimeMillis() / 1000 + 3600);
    assertEquals(1, userAdmin.delete(token, userId).getCode());
    assertEquals(
        0,
        jdbc.queryForObject(
            "select count(*) from ymcx_user_token where user_id=?", Integer.class, userId));
  }

  @Test
  void adminMemberBatchToolbarUpdatesStatusAndDeletesSelectedMembers() {
    long operator =
        jdbc.queryForObject(
            "select id from ymcx_admin where status='normal' order by id limit 1", Long.class);
    String token = sessions.issue(operator), suffix = String.valueOf(System.nanoTime());
    List<Long> ids = new ArrayList<>();
    for (int index = 0; index < 2; index++) {
      Map<String, Object> member = new LinkedHashMap<>();
      member.put("username", "batch-member-" + index + "-" + suffix);
      member.put("nickname", "批量会员" + index);
      member.put("mobile", "18" + index + suffix.substring(suffix.length() - 8));
      assertEquals(1, userAdmin.add(token, member).getCode());
      ids.add(
          jdbc.queryForObject(
              "select id from ymcx_user where username=?",
              Long.class,
              member.get("username")));
    }

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("ids", ids);
    body.put("status", "hidden");
    assertEquals(1, userAdmin.updateStatuses(token, body).getCode());
    assertEquals(
        2,
        jdbc.queryForObject(
            "select count(*) from ymcx_user where id in (?,?) and status='hidden'",
            Integer.class,
            ids.get(0),
            ids.get(1)));

    long now = System.currentTimeMillis() / 1000;
    for (Long id : ids) {
      jdbc.update(
          "insert into ymcx_user_token(token,user_id,createtime,expiretime) values(?,?,?,?)",
          "MEMBER-BATCH-DELETE-" + id + "-" + suffix,
          id,
          now,
          now + 3600);
    }
    assertEquals(1, userAdmin.deleteMany(token, body).getCode());
    assertEquals(
        0,
        jdbc.queryForObject(
            "select count(*) from ymcx_user where id in (?,?)",
            Integer.class,
            ids.get(0),
            ids.get(1)));
    assertEquals(
        0,
        jdbc.queryForObject(
            "select count(*) from ymcx_user_token where user_id in (?,?)",
            Integer.class,
            ids.get(0),
            ids.get(1)));
  }

  @Test
  void rbacRuleGroupAndAdminLifecycleIsConsistent() {
    long operator =
        jdbc.queryForObject(
            "select id from ymcx_admin where status='normal' order by id limit 1", Long.class);
    String token = sessions.issue(operator);
    String suffix = String.valueOf(System.nanoTime());

    Map<String, Object> rule = new LinkedHashMap<>();
    rule.put("name", "/test/" + suffix);
    rule.put("title", "权限迁移回滚测试");
    rule.put("type", "file");
    rule.put("pid", 1);
    assertEquals(1, rbac.addRule(token, rule).getCode());
    long ruleId =
        jdbc.queryForObject(
            "select id from ymcx_auth_rule where name=?", Long.class, rule.get("name"));

    Map<String, Object> group = new LinkedHashMap<>();
    group.put("name", "权限组回滚测试-" + suffix);
    group.put("pid", 1);
    group.put("rule_ids", Collections.singletonList(ruleId));
    assertEquals(1, rbac.addGroup(token, group).getCode());
    long groupId =
        jdbc.queryForObject(
            "select id from ymcx_auth_group where name=?", Long.class, group.get("name"));

    Map<String, Object> admin = new LinkedHashMap<>();
    admin.put("username", "rb" + suffix.substring(Math.max(0, suffix.length() - 12)));
    admin.put("password", "test123456");
    admin.put("nickname", "RBAC测试管理员");
    admin.put("group_ids", Collections.singletonList(groupId));
    assertEquals(1, rbac.addAdmin(token, admin).getCode());
    long adminId =
        jdbc.queryForObject(
            "select id from ymcx_admin where username=?", Long.class, admin.get("username"));
    assertEquals(
        1,
        jdbc.queryForObject(
            "select count(*) from ymcx_auth_group_access where uid=? and group_id=?",
            Integer.class,
            adminId,
            groupId));
    assertTrue(permissions.allowed(adminId, String.valueOf(rule.get("name"))));
    assertFalse(permissions.allowed(adminId, "order/index"));
    assertTrue(permissions.allowed(1, "any/super-admin-rule"));
    assertEquals(1, rbac.admins(token, "rollback", 1, 5).getCode());
    assertEquals(1, rbac.groups(token).getCode());
    assertEquals(1, rbac.rules(token).getCode());

    assertEquals(1, rbac.deleteAdmin(token, adminId).getCode());
    assertEquals(1, rbac.deleteGroup(token, groupId).getCode());
    assertEquals(1, rbac.deleteRule(token, ruleId).getCode());
  }

  @Test
  void adminAuthenticationReviewUpdatesOnlyPendingRecordsAndWritesLogs() {
    long operator =
        jdbc.queryForObject(
            "select id from ymcx_admin where status='normal' order by id limit 1", Long.class);
    long identityUser =
        jdbc.queryForObject(
            "select id from ymcx_user where status='normal' order by id limit 1", Long.class);
    long staffUser =
        jdbc.queryForObject(
            "select id from ymcx_user where status='normal' and id<>? order by id limit 1",
            Long.class,
            identityUser);
    String token = sessions.issue(operator);
    jdbc.update(
        "update ymcx_user set authentication=2,authentication_name='后台审核测试' where id=?",
        identityUser);
    jdbc.update(
        "update ymcx_user set staff_authentication=2,staff_name='员工审核测试',staff_work='/uploads/test.png' where id=?",
        staffUser);

    assertEquals(1, admins.authList(token, "identity", 1, 10).getCode());
    assertEquals(1, admins.authList(token, "staff", 1, 10).getCode());
    assertEquals(1, admins.authList(token, "identity", 0, 0).getCode());
    assertEquals(0, admins.authList(token, "invalid", 1, 10).getCode());

    Map<String, Object> approve = new LinkedHashMap<>();
    approve.put("id", identityUser);
    approve.put("approved", 1);
    assertEquals(1, admins.examineAuth(token, "identity", approve).getCode());
    assertEquals(3, jdbc.queryForObject("select authentication from ymcx_user where id=?", Integer.class, identityUser));
    assertEquals(0, admins.examineAuth(token, "identity", approve).getCode());

    Map<String, Object> reject = new LinkedHashMap<>();
    reject.put("id", staffUser);
    reject.put("approved", 0);
    assertEquals(1, admins.examineAuth(token, "staff", reject).getCode());
    assertEquals(4, jdbc.queryForObject("select staff_authentication from ymcx_user where id=?", Integer.class, staffUser));
    assertEquals(0, admins.examineAuth(token, "staff", reject).getCode());

    assertEquals(
        2,
        jdbc.queryForObject(
            "select count(*) from ymcx_admin_log where admin_id=? and title in ('审核用户实名认证','审核用户员工认证') and content in (?,?)",
            Integer.class,
            operator,
            String.valueOf(identityUser),
            String.valueOf(staffUser)));
  }
}
