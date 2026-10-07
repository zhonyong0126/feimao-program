package com.feimao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feimao.common.BusinessResponse;
import com.feimao.config.LegacyFormRequestFilter;
import com.feimao.controller.admin.SystemAdminController;
import com.feimao.service.AdminSessionService;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class ConfigLegacyContractIntegrationTest {
  @Autowired JdbcTemplate jdbc;
  @Autowired SystemAdminController controller;
  @Autowired AdminSessionService sessions;
  @Autowired ObjectMapper json;

  @Test
  void configEditAcceptsLegacyRowAndBracketFormsAndIgnoresUnknownNames() throws Exception {
    String token = sessions.issue(adminId());
    String siteName = "旧表单配置-" + System.nanoTime();
    Map<String, Object> row = new LinkedHashMap<>();
    row.put("name", siteName);
    row.put("not_a_config", "ignored");
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("row", row);
    BusinessResponse<?> nested = controller.updateConfig(token, body);
    assertEquals(1, nested.getCode());
    assertEquals(1, ((Number) nested.getData()).intValue());
    assertEquals(
        siteName,
        jdbc.queryForObject("select value from ymcx_config where name='name'", String.class));

    Map<String, Object> bracket = new LinkedHashMap<>();
    bracket.put("row[languages][field][]", new String[] {"backend", "frontend", ""});
    bracket.put("row[languages][value][]", new String[] {"zh-cn", "en", "discarded"});
    bracket.put("row[attachmentcategory][]", new String[] {"media", "archive"});
    bracket.put("row[unknown]", "ignored");
    BusinessResponse<?> legacy = controller.updateConfig(token, bracket);
    assertEquals(1, legacy.getCode());
    assertEquals(2, ((Number) legacy.getData()).intValue());
    Map<String, Object> languages =
        json.readValue(
            jdbc.queryForObject(
                "select value from ymcx_config where name='languages'", String.class),
            new TypeReference<LinkedHashMap<String, Object>>() {});
    assertEquals("zh-cn", languages.get("backend"));
    assertEquals("en", languages.get("frontend"));
    assertEquals(
        "media,archive",
        jdbc.queryForObject(
            "select value from ymcx_config where name='attachmentcategory'", String.class));

    Map<String, Object> unknown = new LinkedHashMap<>();
    unknown.put("row[does_not_exist]", "ignored");
    BusinessResponse<?> ignored = controller.updateConfig(token, unknown);
    assertEquals(1, ignored.getCode());
    assertEquals(0, ((Number) ignored.getData()).intValue());

    Map<String, Object> existingName = new LinkedHashMap<>();
    existingName.put("row[name]", "name");
    assertEquals(0, controller.checkConfigNameLegacy(token, existingName).getCode());
    Map<String, Object> availableName = new LinkedHashMap<>();
    availableName.put("a[name]", "legacy_unique_" + System.nanoTime());
    assertEquals(1, controller.checkConfigNameLegacy(token, availableName).getCode());
    Map<?, ?> ruleList = (Map<?, ?>) controller.configRuleListLegacy(token, "required,email");
    assertEquals(2, ((List<?>) ruleList.get("list")).size());
  }

  @Test
  void configSelectPageQueriesTheBusinessTableDefinedBySetting() throws Exception {
    String token = sessions.issue(adminId());
    long categoryId =
        jdbc.queryForObject(
            "select id from ymcx_category where status='normal' order by id limit 1", Long.class);
    long configId =
        jdbc.queryForObject("select id from ymcx_config order by id limit 1", Long.class);
    Map<String, Object> conditions = new LinkedHashMap<>();
    conditions.put("id", categoryId);
    Map<String, Object> setting = new LinkedHashMap<>();
    setting.put("table", "ymcx_category");
    setting.put("field", "name");
    setting.put("primarykey", "id");
    setting.put("conditions", json.writeValueAsString(conditions));
    jdbc.update(
        "update ymcx_config set setting=? where id=?", json.writeValueAsString(setting), configId);

    MockHttpServletRequest request = new MockHttpServletRequest();
    BusinessResponse<?> response =
        controller.configOptions(token, configId, null, null, null, 1, 10, request);
    assertEquals(1, response.getCode());
    Map<?, ?> page = (Map<?, ?>) response.getData();
    assertEquals(1, ((Number) page.get("total")).intValue());
    List<?> list = (List<?>) page.get("list");
    assertEquals(1, list.size());
    Map<?, ?> item = (Map<?, ?>) list.get(0);
    assertEquals(categoryId, ((Number) item.get("id")).longValue());
    assertTrue(item.containsKey("name"));
    assertTrue(item.containsKey("pid"));
    assertFalse(item.containsKey("level"));

    long anotherId =
        jdbc.queryForObject(
            "select id from ymcx_category where id<>? order by id limit 1", Long.class, categoryId);
    BusinessResponse<?> initialized =
        controller.configOptions(
            token,
            configId,
            null,
            null,
            anotherId + "," + categoryId,
            1,
            10,
            request);
    List<?> initializedRows =
        (List<?>) ((Map<?, ?>) initialized.getData()).get("list");
    assertEquals(2, initializedRows.size());
    assertEquals(
        Arrays.asList(anotherId, categoryId),
        Arrays.asList(
            ((Number) ((Map<?, ?>) initializedRows.get(0)).get("id")).longValue(),
            ((Number) ((Map<?, ?>) initializedRows.get(1)).get("id")).longValue()));

    Object legacy =
        controller.configSelectPageLegacy(
            token, configId, null, null, String.valueOf(categoryId), 1, 10, request);
    assertTrue(legacy instanceof Map);
    assertTrue(((Map<?, ?>) legacy).containsKey("list"));
    assertTrue(((Map<?, ?>) legacy).containsKey("total"));

    MockMvc mvc =
        MockMvcBuilders.standaloneSetup(controller)
            .addFilters(new LegacyFormRequestFilter(json))
            .build();
    mvc.perform(
            post("/api/admin/business/system/config/selectpage")
                .header("token", token)
                .queryParam("id", String.valueOf(configId))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("keyValue", String.valueOf(categoryId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.list[0].id").value(categoryId))
        .andExpect(jsonPath("$.total").value(1));
  }

  @Test
  void configPageDecodesLegacyWidgetsAndSerializesTheirEditedValues() throws Exception {
    String token = sessions.issue(adminId());
    String suffix = Long.toString(System.nanoTime(), 36);
    String selectName = "cfg_select_" + suffix;
    String selectsName = "cfg_selects_" + suffix;
    String checkboxName = "cfg_check_" + suffix;
    String radioName = "cfg_radio_" + suffix;
    String arrayName = "cfg_array_" + suffix;
    String dynamicName = "cfg_page_" + suffix;
    String hiddenName = "cfg_hidden_" + suffix;
    String content = "{\"one\":\"选项一\",\"two\":\"选项二\"}";

    insertConfig(selectName, "Attachment category", "select", "", "one", content, "{}", "required");
    insertConfig(selectsName, "多选配置", "selects", "", "one,two", content, "{}", "");
    insertConfig(checkboxName, "复选配置", "checkbox", "", "one,two", content, "{}", "");
    insertConfig(radioName, "单选配置", "radio", "", "two", content, "{}", "");
    insertConfig(
        arrayName,
        "数组配置",
        "array",
        "",
        "{\"first\":\"第一项\",\"second\":\"第二项\"}",
        "",
        "{\"key\":\"字段\",\"value\":\"内容\"}",
        "");
    insertConfig(
        dynamicName,
        "动态关联配置",
        "selectpages",
        selectName + "='one'",
        "1,2",
        "",
        "{\"table\":\"ymcx_category\",\"primarykey\":\"id\",\"field\":\"name\",\"conditions\":\"{\\\"status\\\":\\\"normal\\\"}\"}",
        "");
    insertConfig(hiddenName, "内部余额", "number", "false", "10", "", "{}", "");

    BusinessResponse<?> response = controller.config(token, "system");
    assertEquals(1, response.getCode());
    Map<String, Map<String, Object>> rows = rowsByName((List<?>) response.getData());
    assertFalse(rows.containsKey(hiddenName));
    assertEquals("附件类别", rows.get(selectName).get("title"));
    assertEquals(Collections.singletonList("one"), rows.get(selectName).get("value"));
    assertEquals(Arrays.asList("one", "two"), rows.get(selectsName).get("value"));
    assertEquals(Arrays.asList("one", "two"), rows.get(checkboxName).get("value"));
    assertEquals(Collections.singletonList("two"), rows.get(radioName).get("value"));
    assertEquals("选项一", ((Map<?, ?>) rows.get(selectName).get("content")).get("one"));
    assertEquals("第一项", ((Map<?, ?>) rows.get(arrayName).get("value")).get("first"));
    assertEquals("字段", ((Map<?, ?>) rows.get(arrayName).get("setting")).get("key"));
    assertEquals(selectName + "='one'", rows.get(dynamicName).get("visible"));
    assertEquals("ymcx_category", ((Map<?, ?>) rows.get(dynamicName).get("setting")).get("table"));

    Map<String, Object> updatedArray = new LinkedHashMap<>();
    updatedArray.put("key", "键名也可以叫key");
    updatedArray.put("value", "键名也可以叫value");
    Map<String, Object> update = new LinkedHashMap<>();
    update.put(arrayName, updatedArray);
    update.put(selectsName, Collections.singletonList("two"));
    update.put(checkboxName, Collections.emptyList());
    BusinessResponse<?> saved = controller.updateConfig(token, update);
    assertEquals(1, saved.getCode());
    assertEquals(3, ((Number) saved.getData()).intValue());
    assertEquals(
        updatedArray,
        json.readValue(
            jdbc.queryForObject(
                "select value from ymcx_config where name=?", String.class, arrayName),
            new TypeReference<LinkedHashMap<String, Object>>() {}));
    assertEquals(
        "two",
        jdbc.queryForObject(
            "select value from ymcx_config where name=?", String.class, selectsName));
    assertEquals(
        "",
        jdbc.queryForObject(
            "select value from ymcx_config where name=?", String.class, checkboxName));
  }

  @Test
  void configFrontendContainsTheLegacyArrayDynamicAndVisibilityControls() throws Exception {
    String script =
        new String(
            Files.readAllBytes(Paths.get("src/main/resources/static/admin/js/system.js")),
            StandardCharsets.UTF_8);
    assertTrue(script.contains("function configArrayInput"));
    assertTrue(script.contains("function configDynamicInput"));
    assertTrue(script.contains("function applyConfigVisibility"));
    assertTrue(script.contains("type === 'checkbox'"));
    assertTrue(script.contains("type === 'selectpage' || type === 'selectpages'"));
    assertTrue(script.contains("value = String(value).replace('T', ' ')"));
    assertFalse(script.contains("mail_from]"));
  }

  @Test
  void metadataHelpersAndEmailTestKeepTheLegacyPostContract() throws Exception {
    String token = sessions.issue(adminId());
    MockMvc mvc =
        MockMvcBuilders.standaloneSetup(controller)
            .addFilters(new LegacyFormRequestFilter(json))
            .build();

    mvc.perform(
            post("/api/admin/business/system/config/get_table_list")
                .header("token", token)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(1))
        .andExpect(jsonPath("$.msg").value(""))
        .andExpect(jsonPath("$.data.tableList").isArray());

    mvc.perform(
            post("/api/admin/business/system/config/get_fields_list")
                .header("token", token)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("table", "ymcx_config"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(1))
        .andExpect(jsonPath("$.msg").value(""))
        .andExpect(jsonPath("$.data.fieldList[0].name").value("id"))
        .andExpect(jsonPath("$.data.fieldList[0].type").value("int"));

    mvc.perform(
            post("/api/admin/business/system/config/emailtest")
                .header("token", token)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("row[mail_type]", "0")
                .param("receiver", "receiver@example.com"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(0))
        .andExpect(jsonPath("$.msg").value("邮件功能已关闭"))
        .andExpect(jsonPath("$.data").value(""));
  }

  private long adminId() {
    return jdbc.queryForObject(
        "select id from ymcx_admin where status='normal' order by id limit 1", Long.class);
  }

  private void insertConfig(
      String name,
      String title,
      String type,
      String visible,
      String value,
      String content,
      String setting,
      String rule) {
    jdbc.update(
        "insert into ymcx_config(name,`group`,title,tip,type,visible,value,content,rule,extend,setting) values(?, 'system', ?, '', ?, ?, ?, ?, ?, '', ?)",
        name,
        title,
        type,
        visible,
        value,
        content,
        rule,
        setting);
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Map<String, Object>> rowsByName(List<?> source) {
    Map<String, Map<String, Object>> result = new LinkedHashMap<>();
    for (Object item : source) {
      Map<String, Object> row = (Map<String, Object>) item;
      result.put(String.valueOf(row.get("name")), row);
    }
    return result;
  }
}
