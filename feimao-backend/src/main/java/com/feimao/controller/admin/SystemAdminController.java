package com.feimao.controller.admin;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feimao.common.BusinessResponse;
import com.feimao.mapper.AdminLogMapper;
import com.feimao.mapper.SystemAdminMapper;
import com.feimao.service.AdminSessionService;
import com.feimao.service.VerificationCodeService;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/business/system")
public class SystemAdminController {
  private static final Pattern EMAIL =
      Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
  private static final DateTimeFormatter ATTACHMENT_TIME =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  private static final List<String> ATTACHMENT_EDIT_FIELDS =
      java.util.Arrays.asList(
          "category",
          "url",
          "imagewidth",
          "imageheight",
          "imagetype",
          "imageframes",
          "filename",
          "filesize",
          "mimetype",
          "extparam",
          "uploadtime",
          "storage");
  private final SystemAdminMapper system;
  private final AdminLogMapper adminLogs;
  private final AdminSessionService sessions;
  private final VerificationCodeService verificationCodes;
  private final JdbcTemplate jdbc;
  private final ObjectMapper json;
  private final Path uploadRoot;
  private final SecureRandom random = new SecureRandom();

  public SystemAdminController(
      SystemAdminMapper system,
      AdminLogMapper adminLogs,
      AdminSessionService sessions,
      VerificationCodeService verificationCodes,
      JdbcTemplate jdbc,
      ObjectMapper json,
      @Value("${business.upload-dir:./uploads}") String uploadDir) {
    this.system = system;
    this.adminLogs = adminLogs;
    this.sessions = sessions;
    this.verificationCodes = verificationCodes;
    this.jdbc = jdbc;
    this.json = json;
    this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
  }

  @GetMapping("/config")
  public BusinessResponse<?> config(
      @RequestHeader(value = "token", required = false) String token,
      @RequestParam(required = false) String group) {
    if (adminId(token) == null) return unauthorized();
    String requestedGroup = hasText(group) ? group.trim() : "";
    List<Map<String, Object>> source = system.configs("");
    List<Map<String, Object>> result = new ArrayList<>();
    for (Map.Entry<String, String> groupEntry : configGroupTitles(source).entrySet()) {
      String currentGroup = groupEntry.getKey();
      if (hasText(requestedGroup) && !currentGroup.equals(requestedGroup)) continue;
      for (Map<String, Object> sourceRow : source) {
        if (!currentGroup.equals(Objects.toString(sourceRow.get("group"), ""))) continue;
        String name = Objects.toString(sourceRow.get("name"), "");
        if ("cdnurl".equals(name)
            || ("basic".equals(currentGroup)
                && java.util.Arrays.asList(
                        "version",
                        "timezone",
                        "forbiddenip",
                        "languages",
                        "fixedpage",
                        "beian")
                    .contains(name))) continue;
        String visible = Objects.toString(sourceRow.get("visible"), "").trim();
        // The legacy template kept conditional rows in the form and evaluated their expression in
        // the browser.  Literal false/0 never becomes visible and remains an internal-only item.
        if ("false".equalsIgnoreCase(visible) || "0".equals(visible)) continue;
        Map<String, Object> row = new LinkedHashMap<>(sourceRow);
        row.put("group_title", groupEntry.getValue());
        row.put("title", configTitle(name, Objects.toString(sourceRow.get("title"), "")));
        normalizeConfigForDisplay(row);
        result.add(row);
      }
    }
    return BusinessResponse.success(result);
  }

  private Map<String, String> configGroupTitles(List<Map<String, Object>> configs) {
    Map<String, String> result = new LinkedHashMap<>();
    for (Map<String, Object> config : configs) {
      if (!"configgroup".equals(Objects.toString(config.get("name"), ""))) continue;
      Object groups = decodeConfigJson(config.get("value"), Collections.emptyMap());
      if (groups instanceof Map) {
        for (Map.Entry<?, ?> entry : ((Map<?, ?>) groups).entrySet()) {
          String name = Objects.toString(entry.getKey(), "").trim();
          if (!hasText(name) || hiddenConfigGroup(name)) continue;
          result.put(name, configGroupTitle(name, Objects.toString(entry.getValue(), name)));
        }
      }
      break;
    }
    if (result.isEmpty()) {
      result.put("basic", "基础配置");
      result.put("system", "小程序配置");
      result.put("agreement", "协议配置");
    }
    return result;
  }

  private static boolean hiddenConfigGroup(String group) {
    return java.util.Arrays.asList("email", "dictionary", "user", "example").contains(group);
  }

  private static String configGroupTitle(String group, String title) {
    if ("Basic".equals(title)) return "基础配置";
    if ("Attachment".equals(title)) return "附件配置";
    if (hasText(title) && !group.equals(title)) return title;
    if ("basic".equals(group)) return "基础配置";
    if ("system".equals(group)) return "小程序配置";
    if ("agreement".equals(group)) return "协议配置";
    if ("attachment".equals(group)) return "附件配置";
    return group;
  }

  private static String configTitle(String name, String title) {
    Map<String, String> translations = new LinkedHashMap<>();
    translations.put("Site name", "站点名称");
    translations.put("Beian", "备案号");
    translations.put("Cdn url", "CDN地址");
    translations.put("Version", "版本号");
    translations.put("Timezone", "时区");
    translations.put("Forbidden ip", "禁止IP");
    translations.put("Languages", "语言");
    translations.put("Fixed page", "后台固定页");
    translations.put("Category type", "分类类型");
    translations.put("Config group", "配置分组");
    translations.put("Attachment category", "附件类别");
    translations.put("Mail type", "邮件发送方式");
    translations.put("Mail smtp host", "SMTP服务器");
    translations.put("Mail smtp port", "SMTP端口");
    translations.put("Mail smtp user", "SMTP用户名");
    translations.put("Mail smtp password", "SMTP密码");
    translations.put("Mail vertify type", "SMTP验证方式");
    translations.put("Mail from", "发件人邮箱");
    if ("name".equals(name)) return "站点名称";
    return translations.getOrDefault(title, title);
  }

  private void normalizeConfigForDisplay(Map<String, Object> row) {
    String type = Objects.toString(row.get("type"), "");
    if (java.util.Arrays.asList("select", "selects", "checkbox", "radio").contains(type)) {
      row.put("value", configSelectionValues(row.get("value")));
    } else if ("array".equals(type)) {
      Object value = decodeConfigJson(row.get("value"), Collections.emptyMap());
      if (value instanceof Map) {
        row.put("value", value);
      } else if (value instanceof List) {
        Map<String, Object> indexed = new LinkedHashMap<>();
        for (int index = 0; index < ((List<?>) value).size(); index++)
          indexed.put(String.valueOf(index), ((List<?>) value).get(index));
        row.put("value", indexed);
      } else {
        row.put("value", Collections.emptyMap());
      }
    }
    row.put("content", configContent(row.get("content")));
    row.put("setting", configSetting(row.get("setting")));
  }

  private static List<String> configSelectionValues(Object raw) {
    List<String> result = new ArrayList<>();
    if (raw instanceof Iterable) {
      for (Object item : (Iterable<?>) raw) result.add(Objects.toString(item, ""));
      return result;
    }
    String value = Objects.toString(raw, "");
    Collections.addAll(result, value.split(",", -1));
    return result;
  }

  private Object configContent(Object raw) {
    if (raw instanceof Map || raw instanceof List) return raw;
    String content = Objects.toString(raw, "");
    if (!hasText(content)) return Collections.emptyMap();
    Object decoded = decodeConfigJson(content, null);
    if (decoded instanceof Map || decoded instanceof List) return decoded;
    Map<String, String> result = new LinkedHashMap<>();
    for (String line : content.split("\\r?\\n")) {
      int separator = line.indexOf('|');
      if (separator > -1)
        result.put(line.substring(0, separator), line.substring(separator + 1));
    }
    return result;
  }

  private Object decodeConfigJson(Object raw, Object fallback) {
    if (raw instanceof Map || raw instanceof List) return raw;
    String content = Objects.toString(raw, "");
    if (!hasText(content)) return fallback;
    try {
      return json.readValue(content, Object.class);
    } catch (IOException | RuntimeException ignored) {
      return fallback;
    }
  }

  @GetMapping("/config/check")
  public BusinessResponse<?> checkConfigName(
      @RequestHeader(value = "token", required = false) String token,
      @RequestParam String name,
      @RequestParam(required = false) Long excludeId) {
    if (adminId(token) == null) return unauthorized();
    String value = name == null ? "" : name.trim();
    if (!value.matches("^[A-Za-z][A-Za-z0-9_]{0,29}$")) return BusinessResponse.error("配置名称格式错误");
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("name", value);
    result.put("available", system.configNameCount(value, excludeId) == 0);
    return BusinessResponse.success(result);
  }

  @PostMapping("/config/check")
  public BusinessResponse<?> checkConfigNameLegacy(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    if (adminId(token) == null) return unauthorized();
    Map<String, Object> row = configUpdateBody(body);
    String name = Objects.toString(row.get("name"), "");
    if (!hasText(name)) return BusinessResponse.error("参数错误");
    if (system.configNameCount(name, null) > 0)
      return BusinessResponse.error("配置名称已存在");
    return BusinessResponse.success(null);
  }

  @GetMapping("/config/rules")
  public BusinessResponse<?> configRules(
      @RequestHeader(value = "token", required = false) String token,
      @RequestParam(required = false) String keyValue) {
    if (adminId(token) == null) return unauthorized();
    return BusinessResponse.success(configRuleItems(keyValue));
  }

  @RequestMapping(value = "/config/rulelist", method = {RequestMethod.GET, RequestMethod.POST})
  public Object configRuleListLegacy(
      @RequestHeader(value = "token", required = false) String token,
      @RequestParam(required = false) String keyValue) {
    if (adminId(token) == null) return unauthorized();
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("list", configRuleItems(keyValue));
    return result;
  }

  private static List<Map<String, Object>> configRuleItems(String keyValue) {
    Map<String, String> rules = new LinkedHashMap<>();
    rules.put("required", "必选");
    rules.put("digits", "数字");
    rules.put("letters", "字母");
    rules.put("date", "日期");
    rules.put("time", "时间");
    rules.put("email", "邮箱");
    rules.put("url", "网址");
    rules.put("qq", "QQ号");
    rules.put("IDcard", "身份证");
    rules.put("tel", "座机电话");
    rules.put("mobile", "手机号");
    rules.put("zipcode", "邮编");
    rules.put("chinese", "中文");
    rules.put("username", "用户名");
    rules.put("password", "密码");
    List<String> selected =
        hasText(keyValue) ? java.util.Arrays.asList(keyValue.split(",")) : Collections.emptyList();
    List<Map<String, Object>> result = new ArrayList<>();
    for (Map.Entry<String, String> entry : rules.entrySet()) {
      if (!selected.isEmpty() && !selected.contains(entry.getKey())) continue;
      Map<String, Object> item = new LinkedHashMap<>();
      item.put("id", entry.getKey());
      item.put("name", entry.getValue());
      result.add(item);
    }
    return result;
  }

  @RequestMapping(
      value = "/config/get_table_list",
      method = {RequestMethod.GET, RequestMethod.POST})
  public BusinessResponse<?> configTableList(
      @RequestHeader(value = "token", required = false) String token) {
    if (adminId(token) == null) return unauthorized();
    Map<String, Object> data = new LinkedHashMap<>();
    data.put(
        "tableList",
        jdbc.queryForList(
            "select table_name as name,table_comment as title "
                + "from information_schema.tables where table_schema=database()"));
    return BusinessResponse.success("", data);
  }

  @RequestMapping(
      value = "/config/get_fields_list",
      method = {RequestMethod.GET, RequestMethod.POST})
  public BusinessResponse<?> configFieldList(
      @RequestHeader(value = "token", required = false) String token,
      @RequestParam(required = false) String table) {
    if (adminId(token) == null) return unauthorized();
    Map<String, Object> data = new LinkedHashMap<>();
    data.put(
        "fieldList",
        jdbc.queryForList(
            "select column_name as name,column_comment as title,data_type as type "
                + "from information_schema.columns "
                + "where table_schema=database() and table_name=? order by ordinal_position",
            table));
    return BusinessResponse.success("", data);
  }

  @GetMapping("/config/options")
  public BusinessResponse<?> configOptions(
      @RequestHeader(value = "token", required = false) String token,
      @RequestParam(required = false) Long id,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String q,
      @RequestParam(required = false) String keyValue,
      @RequestParam(defaultValue = "1") int pageNumber,
      @RequestParam(defaultValue = "10") int pageSize,
      HttpServletRequest request) {
    if (adminId(token) == null) return unauthorized();
    if (id == null || id <= 0) return BusinessResponse.error("参数错误");
    Map<String, Object> config = system.configById(id);
    if (config == null) return BusinessResponse.error("参数错误");
    Map<String, Object> setting = configSetting(config.get("setting"));
    String table = settingValue(setting, "table");
    String field = settingValue(setting, "field");
    if (!hasText(field)) field = settingValue(setting, "value");
    String primaryKey = settingValue(setting, "primarykey");
    if (!hasText(primaryKey)) primaryKey = settingValue(setting, "key");
    if (!safeIdentifier(table) || !safeIdentifier(field) || !safeIdentifier(primaryKey))
      return BusinessResponse.error("动态选择配置错误");
    if (!tableExists(table) || !columnExists(table, field) || !columnExists(table, primaryKey))
      return BusinessResponse.error("动态选择配置错误");

    List<Object> arguments = new ArrayList<>();
    List<String> where = new ArrayList<>();
    List<String> selectedKeys = csvValues(keyValue);
    if (!selectedKeys.isEmpty()) {
      where.add(quoted(primaryKey) + " in (" + placeholders(selectedKeys.size()) + ")");
      arguments.addAll(selectedKeys);
    } else {
      List<String> words = selectPageWords(request, keyword, q);
      List<String> wordConditions = new ArrayList<>();
      String fieldConnector =
          request != null && "OR".equalsIgnoreCase(request.getParameter("andOr"))
              ? " or "
              : " and ";
      for (String word : words) {
        wordConditions.add(
            "(cast("
                + quoted(field)
                + " as char) like ?"
                + fieldConnector
                + "cast("
                + quoted(primaryKey)
                + " as char) like ?)");
        arguments.add("%" + word + "%");
        arguments.add("%" + word + "%");
      }
      if (!wordConditions.isEmpty())
        where.add("(" + String.join(" or ", wordConditions) + ")");
      appendConfigConditions(table, setting.get("conditions"), where, arguments);
    }
    String whereSql = where.isEmpty() ? "" : " where " + String.join(" and ", where);
    Integer total =
        jdbc.queryForObject(
            "select count(*) from " + quoted(table) + whereSql,
            Integer.class,
            arguments.toArray());
    pageNumber = Math.max(pageNumber, 1);
    pageSize = selectedKeys.isEmpty() ? Math.max(pageSize, 1) : 999999;
    List<Object> listArguments = new ArrayList<>(arguments);
    StringBuilder select =
        new StringBuilder("select ")
            .append(quoted(primaryKey))
            .append(',')
            .append(quoted(field));
    String parentField =
        columnExists(table, "pid") ? "pid" : (columnExists(table, "parent_id") ? "parent_id" : "");
    if (hasText(parentField)) select.append(',').append(quoted(parentField));
    select.append(" from ").append(quoted(table)).append(whereSql);
    if (!selectedKeys.isEmpty()) {
      select.append(" order by field(").append(quoted(primaryKey)).append(',');
      select.append(placeholders(selectedKeys.size())).append(')');
      listArguments.addAll(selectedKeys);
    } else {
      List<String> order = selectPageOrder(request, table);
      if (!order.isEmpty()) select.append(" order by ").append(String.join(",", order));
      select.append(" limit ? offset ?");
      listArguments.add(pageSize);
      listArguments.add((pageNumber - 1) * pageSize);
    }
    List<Map<String, Object>> rows = jdbc.queryForList(select.toString(), listArguments.toArray());
    List<Map<String, Object>> list = new ArrayList<>();
    for (Map<String, Object> source : rows) {
      Map<String, Object> item = new LinkedHashMap<>();
      item.put(primaryKey, source.get(primaryKey));
      item.put(field, source.get(field));
      item.put("pid", hasText(parentField) ? source.get(parentField) : 0);
      list.add(item);
    }
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("list", list);
    result.put("total", total == null ? 0 : total);
    return BusinessResponse.success(result);
  }

  @RequestMapping(
      value = "/config/selectpage",
      method = {RequestMethod.GET, RequestMethod.POST})
  public Object configSelectPageLegacy(
      @RequestHeader(value = "token", required = false) String token,
      @RequestParam(required = false) Long id,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String q,
      @RequestParam(required = false) String keyValue,
      @RequestParam(defaultValue = "1") int pageNumber,
      @RequestParam(defaultValue = "10") int pageSize,
      HttpServletRequest request) {
    BusinessResponse<?> response =
        configOptions(token, id, keyword, q, keyValue, pageNumber, pageSize, request);
    return response.getCode() == 1 ? response.getData() : response;
  }

  private List<String> selectPageOrder(HttpServletRequest request, String table) {
    if (request == null) return Collections.emptyList();
    List<String> values = new ArrayList<>();
    request
        .getParameterMap()
        .forEach(
            (name, parameters) -> {
              if (!name.startsWith("orderBy") || parameters == null) return;
              for (String parameter : parameters) {
                if (hasText(parameter)) values.add(parameter.trim());
              }
            });
    List<String> result = new ArrayList<>();
    for (int index = 0; index < values.size(); ) {
      String field = values.get(index++);
      String direction = "asc";
      if (field.matches("(?i)^.+\\s+(asc|desc)$")) {
        int split = field.lastIndexOf(' ');
        direction = field.substring(split + 1).trim();
        field = field.substring(0, split).trim();
      } else if (index < values.size() && values.get(index).matches("(?i)^(asc|desc)$")) {
        direction = values.get(index++);
      }
      if (safeIdentifier(field) && columnExists(table, field))
        result.add(quoted(field) + " " + direction.toLowerCase());
    }
    return result;
  }

  @RequestMapping(value = "/config", method = {RequestMethod.PUT, RequestMethod.POST})
  @Transactional
  public BusinessResponse<?> updateConfig(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long adminId = adminId(token);
    if (adminId == null) return unauthorized();
    Map<String, Object> values = configUpdateBody(body);
    if (values.isEmpty()) return BusinessResponse.error("没有可修改配置");
    int updated = 0;
    List<String> names = new ArrayList<>();
    for (Map<String, Object> config : system.configs("")) {
      String name = Objects.toString(config.get("name"), "");
      if (!values.containsKey(name)) continue;
      system.updateConfig(name, serializeConfigValue(values.get(name)));
      names.add(name);
      updated++;
    }
    log(adminId, "修改系统配置", String.join(",", names));
    return BusinessResponse.success(updated);
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> configSetting(Object raw) {
    if (raw instanceof Map) return new LinkedHashMap<>((Map<String, Object>) raw);
    if (!hasText(Objects.toString(raw, ""))) return Collections.emptyMap();
    try {
      return json.readValue(
          Objects.toString(raw, ""),
          new TypeReference<LinkedHashMap<String, Object>>() {});
    } catch (IOException | RuntimeException ignored) {
      return Collections.emptyMap();
    }
  }

  private static String settingValue(Map<String, Object> setting, String name) {
    return Objects.toString(setting.get(name), "").trim();
  }

  private static boolean safeIdentifier(String value) {
    return hasText(value) && value.matches("^[A-Za-z_][A-Za-z0-9_]*$");
  }

  private static String quoted(String identifier) {
    return "`" + identifier + "`";
  }

  private boolean tableExists(String table) {
    Integer count =
        jdbc.queryForObject(
            "select count(*) from information_schema.tables where table_schema=database() and table_name=?",
            Integer.class,
            table);
    return count != null && count > 0;
  }

  private boolean columnExists(String table, String column) {
    Integer count =
        jdbc.queryForObject(
            "select count(*) from information_schema.columns where table_schema=database() and table_name=? and column_name=?",
            Integer.class,
            table,
            column);
    return count != null && count > 0;
  }

  private static String placeholders(int size) {
    return String.join(",", Collections.nCopies(Math.max(size, 1), "?"));
  }

  private static List<String> csvValues(String value) {
    if (!hasText(value)) return Collections.emptyList();
    List<String> result = new ArrayList<>();
    for (String item : value.split(",")) {
      if (hasText(item) && !result.contains(item.trim())) result.add(item.trim());
    }
    return result;
  }

  private static List<String> selectPageWords(
      HttpServletRequest request, String keyword, String q) {
    Set<String> words = new LinkedHashSet<>();
    addSearchWords(words, keyword);
    addSearchWords(words, q);
    if (request != null) {
      request
          .getParameterMap()
          .forEach(
              (name, values) -> {
                if (!("q_word".equals(name)
                    || "q_word[]".equals(name)
                    || name.startsWith("q_word["))) return;
                if (values != null) for (String value : values) addSearchWords(words, value);
              });
    }
    return new ArrayList<>(words);
  }

  private static void addSearchWords(Set<String> words, String value) {
    if (!hasText(value)) return;
    for (String item : value.trim().split("\\s+")) if (hasText(item)) words.add(item.trim());
  }

  @SuppressWarnings("unchecked")
  private void appendConfigConditions(
      String table, Object raw, List<String> where, List<Object> arguments) {
    Map<String, Object> conditions;
    if (raw instanceof Map) {
      conditions = (Map<String, Object>) raw;
    } else if (hasText(Objects.toString(raw, ""))) {
      try {
        conditions =
            json.readValue(
                Objects.toString(raw, ""),
                new TypeReference<LinkedHashMap<String, Object>>() {});
      } catch (IOException | RuntimeException ignored) {
        return;
      }
    } else {
      return;
    }
    for (Map.Entry<String, Object> entry : conditions.entrySet()) {
      String column = entry.getKey();
      Object value = entry.getValue();
      if (!safeIdentifier(column) || !columnExists(table, column) || legacyEmpty(value)) continue;
      String operator = "=";
      if (value instanceof List && ((List<?>) value).size() == 2) {
        operator = Objects.toString(((List<?>) value).get(0), "=").trim().toLowerCase();
        value = ((List<?>) value).get(1);
      }
      if (!java.util.Arrays.asList("=", "!=", "<>", ">", ">=", "<", "<=", "like", "in", "not in")
          .contains(operator)) continue;
      if ("in".equals(operator) || "not in".equals(operator)) {
        List<Object> values = objectValues(value);
        if (values.isEmpty()) continue;
        where.add(quoted(column) + " " + operator + " (" + placeholders(values.size()) + ")");
        arguments.addAll(values);
      } else {
        where.add(quoted(column) + " " + operator + " ?");
        arguments.add(value);
      }
    }
  }

  private static boolean legacyEmpty(Object value) {
    if (value == null || Boolean.FALSE.equals(value)) return true;
    if (value instanceof Number && ((Number) value).doubleValue() == 0D) return true;
    if (value instanceof String) return !hasText((String) value) || "0".equals(value);
    if (value instanceof Iterable) return !((Iterable<?>) value).iterator().hasNext();
    return false;
  }

  private static List<Object> objectValues(Object value) {
    List<Object> result = new ArrayList<>();
    if (value instanceof Iterable) {
      for (Object item : (Iterable<?>) value) if (!legacyEmpty(item)) result.add(item);
    } else if (value != null && value.getClass().isArray()) {
      int length = java.lang.reflect.Array.getLength(value);
      for (int i = 0; i < length; i++) {
        Object item = java.lang.reflect.Array.get(value, i);
        if (!legacyEmpty(item)) result.add(item);
      }
    } else {
      for (String item : Objects.toString(value, "").split(","))
        if (hasText(item)) result.add(item.trim());
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> configUpdateBody(Map<String, Object> body) {
    if (body == null) return Collections.emptyMap();
    Object row = body.get("row");
    if (row instanceof Map) return new LinkedHashMap<>((Map<String, Object>) row);
    Object alias = body.get("a");
    if (alias instanceof Map) return new LinkedHashMap<>((Map<String, Object>) alias);
    Map<String, Object> nested = new LinkedHashMap<>();
    for (Map.Entry<String, Object> entry : body.entrySet()) {
      String key = entry.getKey();
      if (!(key.startsWith("row[") || key.startsWith("a["))) continue;
      java.util.regex.Matcher matcher = Pattern.compile("\\[([^]]*)]").matcher(key);
      List<String> parts = new ArrayList<>();
      while (matcher.find()) parts.add(matcher.group(1));
      if (parts.isEmpty() || !hasText(parts.get(0))) continue;
      String name = parts.get(0);
      Object value = normalizeFormValue(entry.getValue());
      if (parts.size() == 1 || !hasText(parts.get(1))) {
        nested.put(name, value);
      } else {
        Object current = nested.get(name);
        Map<String, Object> item =
            current instanceof Map
                ? (Map<String, Object>) current
                : new LinkedHashMap<String, Object>();
        item.put(parts.get(1), value);
        nested.put(name, item);
      }
    }
    if (!nested.isEmpty()) return nested;
    Map<String, Object> flat = new LinkedHashMap<>(body);
    flat.remove("row");
    flat.remove("a");
    flat.remove("__token__");
    return flat;
  }

  private static Object normalizeFormValue(Object value) {
    if (value == null || !value.getClass().isArray()) return value;
    List<Object> result = new ArrayList<>();
    int length = java.lang.reflect.Array.getLength(value);
    for (int i = 0; i < length; i++) result.add(java.lang.reflect.Array.get(value, i));
    return result;
  }

  @SuppressWarnings("unchecked")
  private String serializeConfigValue(Object value) {
    if (value == null) return "";
    if (value instanceof Map) {
      Map<String, Object> map = (Map<String, Object>) value;
      boolean parallelKeyValue =
          map.size() == 2
              && map.containsKey("key")
              && map.containsKey("value")
              && (isMultiValue(map.get("key")) || isMultiValue(map.get("value")));
      if (map.containsKey("field") || parallelKeyValue) {
        List<Object> fields = rawObjectValues(map.containsKey("field") ? map.get("field") : map.get("key"));
        List<Object> values = rawObjectValues(map.get("value"));
        Map<String, Object> result = new LinkedHashMap<>();
        for (int index = 0; index < fields.size(); index++) {
          String field = Objects.toString(fields.get(index), "");
          if (hasText(field)) result.put(field, index < values.size() ? values.get(index) : "");
        }
        return jsonValue(result);
      }
      return jsonValue(map);
    }
    if (value instanceof Iterable || value.getClass().isArray()) {
      List<String> items = new ArrayList<>();
      if (value instanceof Iterable) {
        for (Object item : (Iterable<?>) value) items.add(Objects.toString(item, ""));
      } else {
        int length = java.lang.reflect.Array.getLength(value);
        for (int i = 0; i < length; i++)
          items.add(Objects.toString(java.lang.reflect.Array.get(value, i), ""));
      }
      return String.join(",", items);
    }
    return Objects.toString(value, "");
  }

  private static boolean isMultiValue(Object value) {
    return value instanceof Iterable || (value != null && value.getClass().isArray());
  }

  private static List<Object> rawObjectValues(Object value) {
    List<Object> result = new ArrayList<>();
    if (value instanceof Iterable) {
      for (Object item : (Iterable<?>) value) result.add(item);
    } else if (value != null && value.getClass().isArray()) {
      int length = java.lang.reflect.Array.getLength(value);
      for (int i = 0; i < length; i++) result.add(java.lang.reflect.Array.get(value, i));
    } else if (value != null) {
      result.add(value);
    }
    return result;
  }

  private String jsonValue(Object value) {
    try {
      return json.writeValueAsString(value);
    } catch (IOException exception) {
      throw new IllegalArgumentException("配置值格式错误", exception);
    }
  }

  @RequestMapping(
      value = {"/config/emailtest", "/config/email-test"},
      method = {RequestMethod.POST, RequestMethod.PUT})
  public BusinessResponse<?> testEmail(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long adminId = adminId(token);
    if (adminId == null) return unauthorized();
    String receiver = body == null ? "" : value(body, "receiver");
    if (!hasText(receiver)) return BusinessResponse.error("未知参数", "");
    if (!EMAIL.matcher(receiver).matches()) return BusinessResponse.error("请输入正确的邮箱地址", "");
    Map<String, Object> mailConfig = new LinkedHashMap<>();
    for (Map<String, Object> config : system.configs(""))
      mailConfig.put(
          Objects.toString(config.get("name"), ""),
          Objects.toString(config.get("value"), ""));
    mailConfig.putAll(configUpdateBody(body));
    try {
      verificationCodes.sendTestEmail(
          receiver, mailConfig, Objects.toString(mailConfig.get("name"), ""));
      return BusinessResponse.success("", "");
    } catch (IllegalArgumentException | IllegalStateException exception) {
      return BusinessResponse.error(exception.getMessage(), "");
    } catch (RuntimeException exception) {
      return BusinessResponse.error("测试邮件发送失败，请检查 SMTP 配置", "");
    }
  }

  @GetMapping("/attachments")
  public BusinessResponse<?> attachments(
      @RequestHeader(value = "token", required = false) String token,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String category,
      @RequestParam(required = false) Long adminId,
      @RequestParam(required = false) Long userId,
      @RequestParam(required = false) String filename,
      @RequestParam(required = false) Long sizeMin,
      @RequestParam(required = false) Long sizeMax,
      @RequestParam(required = false) String imagewidth,
      @RequestParam(required = false) String imageheight,
      @RequestParam(required = false) String imagetype,
      @RequestParam(required = false) String storage,
      @RequestParam(required = false) String mimetype,
      @RequestParam(required = false) Long createdFrom,
      @RequestParam(required = false) Long createdTo,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int limit) {
    if (adminId(token) == null) return unauthorized();
    page = Math.max(page, 1);
    limit = Math.min(Math.max(limit, 1), 100);
    String search = hasText(keyword) ? keyword.trim() : "";
    String selectedCategory =
        hasText(category) ? ("unclassed".equals(category.trim()) ? "" : category.trim()) : null;
    List<String> mimeTypes = mimeTypes(mimetype);
    String selectedFilename = hasText(filename) ? filename.trim() : "";
    String selectedWidth = hasText(imagewidth) ? imagewidth.trim() : "";
    String selectedHeight = hasText(imageheight) ? imageheight.trim() : "";
    String selectedImageType = hasText(imagetype) ? imagetype.trim() : "";
    String selectedStorage = hasText(storage) ? storage.trim() : "";
    int total =
        system.attachmentCount(
            search,
            selectedCategory,
            adminId,
            userId,
            selectedFilename,
            sizeMin,
            sizeMax,
            selectedWidth,
            selectedHeight,
            selectedImageType,
            selectedStorage,
            mimeTypes,
            createdFrom,
            createdTo);
    List<Map<String, Object>> rows =
        system.attachments(
            search,
            selectedCategory,
            adminId,
            userId,
            selectedFilename,
            sizeMin,
            sizeMax,
            selectedWidth,
            selectedHeight,
            selectedImageType,
            selectedStorage,
            mimeTypes,
            createdFrom,
            createdTo,
            limit,
            (page - 1) * limit);
    for (Map<String, Object> row : rows) {
      String url = String.valueOf(row.getOrDefault("url", ""));
      Object rowCategory = row.get("category");
      if (rowCategory == null || String.valueOf(rowCategory).isEmpty())
        row.put("category", "unclassed");
      row.put("fullurl", url);
      row.put("preview", url);
      row.put("thumb_style", "");
    }
    return BusinessResponse.success(page(rows, total, page, limit));
  }

  /** Kept for direct callers while the HTTP endpoint exposes the complete historical filters. */
  public BusinessResponse<?> attachments(
      String token,
      String keyword,
      String category,
      String mimetype,
      int page,
      int limit) {
    return attachments(
        token,
        keyword,
        category,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        mimetype,
        null,
        null,
        page,
        limit);
  }

  @GetMapping("/attachments/select")
  public BusinessResponse<?> selectAttachments(
      @RequestHeader(value = "token", required = false) String token,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String category,
      @RequestParam(required = false) String mimetype,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int limit) {
    return attachments(token, keyword, category, mimetype, page, limit);
  }

  @GetMapping("/attachments/categories")
  public BusinessResponse<?> attachmentCategories(
      @RequestHeader(value = "token", required = false) String token) {
    if (adminId(token) == null) return unauthorized();
    List<Map<String, Object>> result = new ArrayList<>();
    for (Map.Entry<String, String> entry : attachmentCategoryMap().entrySet()) {
      Map<String, Object> item = new LinkedHashMap<>();
      item.put("category", entry.getKey());
      item.put("label", entry.getValue());
      result.add(item);
    }
    return BusinessResponse.success(result);
  }

  @PutMapping("/attachments/{id}")
  @Transactional
  public BusinessResponse<?> updateAttachment(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable long id,
      @RequestBody(required = false) Map<String, Object> body) {
    Long adminId = adminId(token);
    if (adminId == null) return unauthorized();
    Map<String, Object> existing = system.attachment(id);
    if (existing == null) return BusinessResponse.error("附件记录不存在");
    Map<String, Object> source = attachmentEditBody(body);
    Map<String, Object> values = new LinkedHashMap<>();
    for (String field : ATTACHMENT_EDIT_FIELDS) {
      if (!source.containsKey(field)) continue;
      Object item = attachmentEditValue(field, source.get(field));
      if (!Objects.equals(attachmentEditValue(field, existing.get(field)), item))
        values.put(field, item);
    }
    boolean hasEditableField = false;
    for (String field : ATTACHMENT_EDIT_FIELDS) {
      if (source.containsKey(field)) {
        hasEditableField = true;
        break;
      }
    }
    if (!hasEditableField) return BusinessResponse.error("没有可修改内容");
    if (values.isEmpty()) {
      log(adminId, "编辑附件", String.valueOf(id));
      return BusinessResponse.success(0);
    }
    int changed = system.updateAttachment(id, values, now());
    log(adminId, "编辑附件", String.valueOf(id));
    return BusinessResponse.success(changed);
  }

  @PutMapping("/attachments/classify")
  @Transactional
  public BusinessResponse<?> classifyAttachments(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long adminId = adminId(token);
    if (adminId == null) return unauthorized();
    List<Long> ids = idList(body == null ? null : body.get("ids"));
    if (ids.isEmpty()) return BusinessResponse.error("请选择要归类的附件");
    String category = body == null ? "" : value(body, "category");
    if ("unclassed".equals(category)) category = "";
    if (!category.isEmpty() && !attachmentCategoryMap().containsKey(category))
      return BusinessResponse.error("附件分类不存在");
    int changed = system.classifyAttachments(ids, category);
    log(adminId, "附件归类", ids + ":" + (category.isEmpty() ? "unclassed" : category));
    return BusinessResponse.success(changed);
  }

  @DeleteMapping("/attachments")
  @Transactional
  public BusinessResponse<?> deleteAttachments(
      @RequestHeader(value = "token", required = false) String token,
      @RequestParam(value = "ids", required = false) String requestIds,
      @RequestBody(required = false) Map<String, Object> body) {
    Long adminId = adminId(token);
    if (adminId == null) return unauthorized();
    Object bodyIds = body == null ? null : body.get("ids");
    List<Long> ids = idList(bodyIds == null ? requestIds : bodyIds);
    if (ids.isEmpty()) return BusinessResponse.error("请选择要删除的附件");
    for (Map<String, Object> attachment : system.attachmentsByIds(ids)) {
      if ("local".equals(String.valueOf(attachment.get("storage"))))
        deleteLocalAttachmentQuietly(String.valueOf(attachment.get("url")));
    }
    int deleted = system.deleteAttachments(ids);
    log(adminId, "批量删除附件", ids.toString());
    return BusinessResponse.success(deleted);
  }

  @DeleteMapping("/attachments/{id}")
  @Transactional
  public BusinessResponse<?> deleteAttachment(
      @RequestHeader(value = "token", required = false) String token, @PathVariable long id) {
    Long adminId = adminId(token);
    if (adminId == null) return unauthorized();
    Map<String, Object> attachment = system.attachment(id);
    if (attachment == null || attachment.isEmpty()) {
      log(adminId, "删除附件记录", String.valueOf(id));
      return BusinessResponse.success(0);
    }
    if ("local".equals(String.valueOf(attachment.get("storage"))))
      deleteLocalAttachmentQuietly(String.valueOf(attachment.get("url")));
    int deleted = system.deleteAttachment(id);
    log(adminId, "删除附件记录", String.valueOf(id));
    return BusinessResponse.success(deleted);
  }

  private void deleteLocalAttachmentQuietly(String url) {
    if (url == null || !url.startsWith("/uploads/")) return;
    Path target = uploadRoot.resolve(url.substring("/uploads/".length())).normalize();
    if (!target.startsWith(uploadRoot)) return;
    try {
      if (!Files.exists(target)) return;
      Path realRoot = Files.createDirectories(uploadRoot).toRealPath();
      Path realTarget = target.toRealPath();
      if (realTarget.startsWith(realRoot)) Files.deleteIfExists(realTarget);
    } catch (IOException ignored) {
      // Legacy attachment deletion is best-effort: the database record is still removed.
    }
  }

  @GetMapping("/profile")
  public BusinessResponse<?> profile(
      @RequestHeader(value = "token", required = false) String token,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int limit) {
    Long adminId = adminId(token);
    if (adminId == null) return unauthorized();
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("profile", system.adminProfile(adminId));
    page = Math.max(page, 1);
    limit = Math.min(Math.max(limit, 1), 100);
    int total = system.adminLogCount(adminId);
    List<Map<String, Object>> rows = system.adminLogs(adminId, limit, (page - 1) * limit);
    result.put("logs", page(rows, total, page, limit));
    return BusinessResponse.success(result);
  }

  @RequestMapping(value = "/profile", method = {RequestMethod.PUT, RequestMethod.POST})
  @Transactional
  public BusinessResponse<?> updateProfile(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long adminId = adminId(token);
    if (adminId == null) return unauthorized();
    Map<String, Object> submitted = profileBody(body);
    Map<String, Object> values = new LinkedHashMap<>();
    for (String field : new String[] {"email", "nickname", "password", "avatar"}) {
      Object item = submitted.get(field);
      if (legacyArrayFilterKeeps(item)) values.put(field, profileText(item));
    }

    String email = profileText(values.get("email"));
    if (!EMAIL.matcher(email).matches()) return BusinessResponse.error("请输入正确的Email地址");
    if (system.otherAdminEmailCount(email, adminId) > 0)
      return BusinessResponse.error("邮箱已经存在");

    boolean passwordChanged = values.containsKey("password");
    if (passwordChanged) {
      String password = profileText(values.get("password"));
      if (!password.matches("\\S{6,30}"))
        return BusinessResponse.error("密码长度必须在6-30位之间，不能包含空格");
      String salt = random(10);
      values.put("salt", salt);
      values.put("password", hash(password, salt));
    }
    system.updateAdminProfile(adminId, values, now());
    log(adminId, "修改管理员个人资料", String.valueOf(adminId));
    if (passwordChanged) sessions.revoke(token);
    return BusinessResponse.success(1);
  }

  private Long adminId(String token) {
    return sessions.adminId(token);
  }

  private void log(long adminId, String title, String content) {
    adminLogs.insertLog(adminId, "/api/admin/business/system", title, content, now());
  }

  private String random(int length) {
    final String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    StringBuilder value = new StringBuilder();
    for (int i = 0; i < length; i++)
      value.append(characters.charAt(random.nextInt(characters.length())));
    return value.toString();
  }

  private static String hash(String password, String salt) {
    String first = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));
    return DigestUtils.md5DigestAsHex((first + salt).getBytes(StandardCharsets.UTF_8));
  }

  private static String value(Map<String, Object> body, String field) {
    Object value = body.get(field);
    return value == null ? "" : String.valueOf(value).trim();
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> profileBody(Map<String, Object> body) {
    if (body == null) return Collections.emptyMap();
    for (String key : new String[] {"row", "a", "row/a"}) {
      Object nested = body.get(key);
      if (nested instanceof Map) return (Map<String, Object>) nested;
    }
    Map<String, Object> bracketed = bracketedProfileBody(body, "row[");
    if (!bracketed.isEmpty()) return bracketed;
    bracketed = bracketedProfileBody(body, "a[");
    return bracketed.isEmpty() ? body : bracketed;
  }

  private static Map<String, Object> bracketedProfileBody(
      Map<String, Object> body, String prefix) {
    Map<String, Object> result = new LinkedHashMap<>();
    for (Map.Entry<String, Object> entry : body.entrySet()) {
      String key = entry.getKey();
      if (key != null && key.startsWith(prefix) && key.endsWith("]")) {
        String field = key.substring(prefix.length(), key.length() - 1);
        if (!field.isEmpty()) result.put(field, entry.getValue());
      }
    }
    return result;
  }

  private static boolean legacyArrayFilterKeeps(Object item) {
    if (item == null) return false;
    if (item instanceof Boolean) return (Boolean) item;
    if (item instanceof Number) return ((Number) item).doubleValue() != 0D;
    if (item instanceof CharSequence) {
      String text = item.toString();
      return !text.isEmpty() && !"0".equals(text);
    }
    if (item instanceof java.util.Collection) return !((java.util.Collection<?>) item).isEmpty();
    if (item instanceof Map) return !((Map<?, ?>) item).isEmpty();
    if (item.getClass().isArray()) return java.lang.reflect.Array.getLength(item) > 0;
    return true;
  }

  private static String profileText(Object item) {
    return item == null ? "" : String.valueOf(item);
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> attachmentEditBody(Map<String, Object> body) {
    if (body == null) return Collections.emptyMap();
    Object row = body.get("row");
    return row instanceof Map ? (Map<String, Object>) row : body;
  }

  private static Object attachmentEditValue(String field, Object item) {
    boolean uploadTime = "uploadtime".equals(field);
    boolean numeric =
        uploadTime || "imageframes".equals(field) || "filesize".equals(field);
    if (!numeric) {
      String value = item == null ? "" : String.valueOf(item);
      return "category".equals(field) && "unclassed".equals(value) ? "" : value;
    }
    if (item instanceof Number) return ((Number) item).longValue();
    String value = item == null ? "" : String.valueOf(item).trim();
    if (value.isEmpty()) return 0L;
    try {
      return Long.parseLong(value);
    } catch (NumberFormatException ignored) {
      if (!uploadTime) return 0L;
    }
    try {
      String dateTime = value.replace('T', ' ');
      if (dateTime.length() == 16) dateTime += ":00";
      return LocalDateTime.parse(dateTime, ATTACHMENT_TIME)
          .atZone(ZoneId.of("Asia/Shanghai"))
          .toEpochSecond();
    } catch (RuntimeException ignored) {
      return 0L;
    }
  }

  private Map<String, String> attachmentCategoryMap() {
    Map<String, String> result = new LinkedHashMap<>();
    String configured = system.attachmentCategoryConfig();
    if (hasText(configured)) {
      try {
        Map<String, Object> values =
            json.readValue(configured, new TypeReference<LinkedHashMap<String, Object>>() {});
        for (Map.Entry<String, Object> entry : values.entrySet()) {
          if (hasText(entry.getKey()))
            result.put(
                entry.getKey().trim(),
                entry.getValue() == null ? "" : String.valueOf(entry.getValue()));
        }
      } catch (IOException | RuntimeException ignored) {
        // A malformed historical config must not make the attachment page unavailable.
      }
    }
    result.remove("unclassed");
    result.put("unclassed", "未归类");
    return result;
  }

  private static List<Long> idList(Object value) {
    List<Long> ids = new ArrayList<>();
    Iterable<?> values =
        value instanceof Iterable
            ? (Iterable<?>) value
            : value == null
                ? Collections.emptyList()
                : java.util.Arrays.asList(String.valueOf(value).split(","));
    for (Object item : values) {
      try {
        long id = Long.parseLong(String.valueOf(item).trim());
        if (id > 0 && !ids.contains(id)) ids.add(id);
      } catch (NumberFormatException ignored) {
        // Ignore malformed IDs from historical bulk-operation payloads.
      }
    }
    return ids;
  }

  private static boolean hasText(String value) {
    return value != null && !value.trim().isEmpty();
  }

  private static List<String> mimeTypes(String value) {
    if (!hasText(value)) return Collections.emptyList();
    List<String> result = new ArrayList<>();
    for (String item : value.split(",")) {
      String mimeType = item.trim().replace("/*", "/").replace("*", "%");
      if (!mimeType.isEmpty()) result.add(mimeType);
    }
    return result;
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
