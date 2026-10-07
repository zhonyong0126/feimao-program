package com.feimao.mapper.provider;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ResourceAdminSqlProvider {
  private static final Map<String, Definition> DEFINITIONS = new LinkedHashMap<>();

  static {
    define("banner", "ymcx_banner", "image", "weigh", "status", "createtime", "updatetime");
    define(
        "brand",
        "ymcx_brand",
        "name",
        "weigh",
        "status",
        "createtime",
        "updatetime",
        "is_delete");
    define(
        "platform-car",
        "ymcx_platform_car",
        "name",
        "brand_id",
        "image",
        "content",
        "specification",
        "status",
        "weigh",
        "createtime",
        "updatetime",
        "is_delete");
    define(
        "maintain",
        "ymcx_maintain",
        "name",
        "image",
        "introductory",
        "content",
        "createtime",
        "updatetime");
    define(
        "coupon",
        "ymcx_coupon",
        "score",
        "price",
        "reduceprice",
        "weigh",
        "status",
        "endtime",
        "createtime",
        "updatetime");
    define(
        "category",
        "ymcx_category",
        "pid",
        "type",
        "name",
        "nickname",
        "flag",
        "image",
        "keywords",
        "description",
        "diyname",
        "weigh",
        "status",
        "createtime",
        "updatetime");
  }

  private ResourceAdminSqlProvider() {}

  public static String insert(Map<String, Object> parameters) {
    Definition definition = definition(parameters);
    Map<?, ?> values = values(parameters);
    StringBuilder columns = new StringBuilder();
    StringBuilder bindings = new StringBuilder();
    for (String column : definition.columns) {
      if (!values.containsKey(column)) continue;
      if (columns.length() > 0) {
        columns.append(',');
        bindings.append(',');
      }
      columns.append(column);
      bindings.append("#{v.").append(column).append('}');
    }
    if (columns.length() == 0) throw new IllegalArgumentException("资源新增字段不能为空");
    return "insert into "
        + definition.table
        + " ("
        + columns
        + ") values ("
        + bindings
        + ")";
  }

  public static String update(Map<String, Object> parameters) {
    Definition definition = definition(parameters);
    Map<?, ?> values = values(parameters);
    StringBuilder assignments = new StringBuilder();
    for (String column : definition.columns) {
      if (!values.containsKey(column) || "createtime".equals(column) || "is_delete".equals(column))
        continue;
      if (assignments.length() > 0) assignments.append(',');
      assignments.append(column).append("=#{v.").append(column).append('}');
    }
    if (assignments.length() == 0) throw new IllegalArgumentException("资源修改字段不能为空");
    return "update "
        + definition.table
        + " set "
        + assignments
        + " where id=#{id}"
        + (definition.softDelete ? " and is_delete=1 and deletetime is null" : "");
  }

  private static Definition definition(Map<String, Object> parameters) {
    Definition definition = DEFINITIONS.get(String.valueOf(parameters.get("resource")));
    if (definition == null) throw new IllegalArgumentException("资源类型错误");
    return definition;
  }

  private static Map<?, ?> values(Map<String, Object> parameters) {
    Object values = parameters.get("v");
    if (!(values instanceof Map)) throw new IllegalArgumentException("资源字段错误");
    return (Map<?, ?>) values;
  }

  private static void define(String key, String table, String... columns) {
    List<String> fields = Arrays.asList(columns);
    DEFINITIONS.put(key, new Definition(table, fields, fields.contains("is_delete")));
  }

  private static final class Definition {
    private final String table;
    private final List<String> columns;
    private final boolean softDelete;

    private Definition(String table, List<String> columns, boolean softDelete) {
      this.table = table;
      this.columns = columns;
      this.softDelete = softDelete;
    }
  }
}
