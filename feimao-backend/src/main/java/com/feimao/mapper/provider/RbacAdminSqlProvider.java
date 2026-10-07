package com.feimao.mapper.provider;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class RbacAdminSqlProvider {
  private static final Map<String, Definition> DEFINITIONS = new LinkedHashMap<>();

  static {
    define(
        "admin",
        "ymcx_admin",
        "username",
        "nickname",
        "password",
        "salt",
        "avatar",
        "email",
        "mobile",
        "status",
        "merchant_id",
        "loginfailure",
        "createtime",
        "updatetime");
    define("group", "ymcx_auth_group", "pid", "name", "rules", "status", "createtime", "updatetime");
    define(
        "rule",
        "ymcx_auth_rule",
        "type",
        "pid",
        "name",
        "title",
        "icon",
        "url",
        "condition",
        "remark",
        "ismenu",
        "menutype",
        "extend",
        "createtime",
        "updatetime",
        "weigh",
        "status");
  }

  private RbacAdminSqlProvider() {}

  public static String insert(Map<String, Object> parameters) {
    Definition definition = definition(parameters);
    Map<?, ?> values = values(parameters);
    StringBuilder columns = new StringBuilder();
    StringBuilder bindings = new StringBuilder();
    for (String field : definition.fields) {
      if (!values.containsKey(field)) continue;
      if (columns.length() > 0) {
        columns.append(',');
        bindings.append(',');
      }
      columns.append('`').append(field).append('`');
      bindings.append("#{v.").append(field).append('}');
    }
    return "insert into " + definition.table + " (" + columns + ") values (" + bindings + ")";
  }

  public static String update(Map<String, Object> parameters) {
    Definition definition = definition(parameters);
    Map<?, ?> values = values(parameters);
    StringBuilder assignments = new StringBuilder();
    for (String field : definition.fields) {
      if (!values.containsKey(field) || "createtime".equals(field)) continue;
      if (assignments.length() > 0) assignments.append(',');
      assignments.append('`').append(field).append("`=#{v.").append(field).append('}');
    }
    if (assignments.length() == 0) throw new IllegalArgumentException("没有可修改内容");
    return "update " + definition.table + " set " + assignments + " where id=#{id}";
  }

  private static Definition definition(Map<String, Object> parameters) {
    Definition definition = DEFINITIONS.get(String.valueOf(parameters.get("kind")));
    if (definition == null) throw new IllegalArgumentException("RBAC 类型错误");
    return definition;
  }

  private static Map<?, ?> values(Map<String, Object> parameters) {
    Object values = parameters.get("v");
    if (!(values instanceof Map)) throw new IllegalArgumentException("RBAC 字段错误");
    return (Map<?, ?>) values;
  }

  private static void define(String kind, String table, String... fields) {
    DEFINITIONS.put(kind, new Definition(table, Arrays.asList(fields)));
  }

  private static final class Definition {
    private final String table;
    private final List<String> fields;

    private Definition(String table, List<String> fields) {
      this.table = table;
      this.fields = fields;
    }
  }
}
