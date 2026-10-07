package com.feimao.controller.admin;

import com.feimao.common.BusinessResponse;
import com.feimao.mapper.AdminLogMapper;
import com.feimao.mapper.ResourceAdminMapper;
import com.feimao.service.AdminSessionService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/business/resources")
public class ResourceAdminController {
  private final ResourceAdminMapper resources;
  private final AdminLogMapper adminLogs;
  private final AdminSessionService sessions;

  public ResourceAdminController(
      ResourceAdminMapper resources,
      AdminLogMapper adminLogs,
      AdminSessionService sessions) {
    this.resources = resources;
    this.adminLogs = adminLogs;
    this.sessions = sessions;
  }

  @GetMapping("/{resource}")
  public BusinessResponse<?> list(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable String resource,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int limit,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String type,
      @RequestParam(required = false) Integer status,
      @RequestParam(required = false) Long brandId,
      @RequestParam(required = false) Long createdFrom,
      @RequestParam(required = false) Long createdTo) {
    if (adminId(token) == null) return unauthorized();
    Resource definition = Resource.from(resource);
    if (definition == null) return BusinessResponse.error("资源类型错误");
    page = Math.max(page, 1);
    limit = Math.min(Math.max(limit, 1), 100);
    String search = keyword == null ? "" : keyword.trim();
    String categoryType = definition == Resource.CATEGORY && type != null ? type.trim() : "";
    Integer statusFilter = status != null && (status == 1 || status == 2) ? status : null;
    Long brandFilter = brandId != null && brandId > 0 ? brandId : null;
    Long from = createdFrom != null && createdFrom > 0 ? createdFrom : null;
    Long to = createdTo != null && createdTo > 0 ? createdTo : null;
    int total =
        resources.count(
            definition.key, search, categoryType, statusFilter, brandFilter, from, to);
    List<Map<String, Object>> rows =
        resources.selectPage(
            definition.key,
            search,
            categoryType,
            statusFilter,
            brandFilter,
            from,
            to,
            limit,
            (page - 1) * limit);
    return BusinessResponse.success(page(rows, total, page, limit));
  }

  public BusinessResponse<?> list(
      String token,
      String resource,
      int page,
      int limit,
      String keyword,
      String type) {
    return list(token, resource, page, limit, keyword, type, null, null, null, null);
  }

  @PostMapping("/{resource}")
  @Transactional
  public BusinessResponse<?> add(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable String resource,
      @RequestBody(required = false) Map<String, Object> body) {
    Long adminId = adminId(token);
    if (adminId == null) return unauthorized();
    Resource definition = Resource.from(resource);
    if (definition == null || !definition.mutable) return BusinessResponse.error("资源类型错误");
    Map<String, Object> values = writableValues(definition, body);
    String validation = definition.validate(values);
    if (validation != null) return BusinessResponse.error(validation);
    validation = validateCategoryParent(definition, 0, values);
    if (validation != null) return BusinessResponse.error(validation);
    long now = now();
    if (definition.columns.contains("createtime")) values.put("createtime", now);
    if (definition.columns.contains("updatetime")) values.put("updatetime", now);
    if (definition.softDelete) values.put("is_delete", 1);
    resources.insertResource(definition.key, values);
    Number id = (Number) values.get("id");
    if (definition == Resource.BANNER)
      resources.updateWeight(definition.key, id.longValue(), id.intValue(), now);
    else if (definition == Resource.CATEGORY && !values.containsKey("weigh"))
      resources.updateWeight(definition.key, id.longValue(), id.intValue(), now);
    log(adminId, "新增" + definition.title, definition.key + ":" + id);
    return BusinessResponse.success(Collections.singletonMap("id", id.longValue()));
  }

  @PutMapping("/{resource}/{id}")
  @Transactional
  public BusinessResponse<?> update(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable String resource,
      @PathVariable long id,
      @RequestBody(required = false) Map<String, Object> body) {
    Long adminId = adminId(token);
    if (adminId == null) return unauthorized();
    Resource definition = Resource.from(resource);
    if (definition == null || !definition.mutable) return BusinessResponse.error("资源类型错误");
    Map<String, Object> values = writableValues(definition, body);
    if (values.isEmpty()) return BusinessResponse.error("没有可修改内容");
    String validation = definition.validateForUpdate(values);
    if (validation != null) return BusinessResponse.error(validation);
    validation = validateCategoryParent(definition, id, values);
    if (validation != null) return BusinessResponse.error(validation);
    if (definition.columns.contains("updatetime")) values.put("updatetime", now());
    if (resources.updateResource(definition.key, id, values) == 0)
      return BusinessResponse.error("记录不存在");
    log(adminId, "修改" + definition.title, definition.key + ":" + id);
    return BusinessResponse.success(1);
  }

  @DeleteMapping("/{resource}/{id}")
  @Transactional
  public BusinessResponse<?> delete(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable String resource,
      @PathVariable long id) {
    Long adminId = adminId(token);
    if (adminId == null) return unauthorized();
    Resource definition = Resource.from(resource);
    if (definition == null || !definition.mutable) return BusinessResponse.error("资源类型错误");
    int changed = resources.deleteResource(definition.key, id, now());
    if (changed == 0) return BusinessResponse.error("记录不存在");
    log(adminId, "删除" + definition.title, definition.key + ":" + id);
    return BusinessResponse.success(1);
  }

  @DeleteMapping("/{resource}")
  @Transactional
  public BusinessResponse<?> deleteMany(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable String resource,
      @RequestBody(required = false) Map<String, Object> body) {
    Long adminId = adminId(token);
    if (adminId == null) return unauthorized();
    Resource definition = Resource.from(resource);
    if (definition != Resource.BRAND && definition != Resource.PLATFORM_CAR)
      return BusinessResponse.error("该资源不支持批量删除");
    List<Long> ids = idList(body == null ? null : body.get("ids"));
    if (ids.isEmpty()) return BusinessResponse.error("请选择要删除的记录");
    long deletedAt = now();
    int changed = 0;
    for (Long id : ids) changed += resources.deleteResource(definition.key, id, deletedAt);
    if (changed == 0) return BusinessResponse.error("记录不存在");
    log(adminId, "批量删除" + definition.title, ids.toString());
    return BusinessResponse.success(changed);
  }

  @GetMapping("/{resource}/recycle")
  public BusinessResponse<?> recycledResources(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable String resource,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int limit) {
    if (adminId(token) == null) return unauthorized();
    Resource definition = Resource.from(resource);
    if (!hasResourceRecycleBin(definition))
      return BusinessResponse.error("该资源不支持回收站");
    page = Math.max(page, 1);
    limit = Math.min(Math.max(limit, 1), 100);
    return BusinessResponse.success(
        page(
            resources.recycledResources(definition.key, limit, (page - 1) * limit),
            resources.recycledResourceCount(definition.key),
            page,
            limit));
  }

  @PostMapping("/{resource}/restore/{id}")
  @Transactional
  public BusinessResponse<?> restoreRecycledResource(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable String resource,
      @PathVariable long id) {
    Long adminId = adminId(token);
    if (adminId == null) return unauthorized();
    Resource definition = Resource.from(resource);
    if (!hasResourceRecycleBin(definition))
      return BusinessResponse.error("该资源不支持回收站");
    int changed =
        resources.restoreRecycledResources(
            definition.key, Collections.singletonList(id), now());
    if (changed == 0) return BusinessResponse.error("回收记录不存在");
    log(adminId, "恢复" + definition.title, definition.key + ":" + id);
    return BusinessResponse.success(changed);
  }

  @PostMapping("/{resource}/restore")
  @Transactional
  public BusinessResponse<?> restoreRecycledResources(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable String resource,
      @RequestBody(required = false) Map<String, Object> body) {
    Long adminId = adminId(token);
    if (adminId == null) return unauthorized();
    Resource definition = Resource.from(resource);
    if (!hasResourceRecycleBin(definition))
      return BusinessResponse.error("该资源不支持回收站");
    boolean all = allRecycleRecords(body);
    List<Long> ids = idList(body == null ? null : body.get("ids"));
    if (!all && ids.isEmpty()) return BusinessResponse.error("请选择要恢复的记录");
    int changed =
        resources.restoreRecycledResources(
            definition.key, all ? Collections.<Long>emptyList() : ids, now());
    if (changed == 0) return BusinessResponse.error("回收站中没有可恢复的记录");
    log(adminId, all ? "恢复全部" + definition.title : "批量恢复" + definition.title,
        all ? "全部" : ids.toString());
    return BusinessResponse.success(changed);
  }

  @DeleteMapping("/{resource}/destroy/{id}")
  @Transactional
  public BusinessResponse<?> destroyRecycledResource(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable String resource,
      @PathVariable long id) {
    Long adminId = adminId(token);
    if (adminId == null) return unauthorized();
    Resource definition = Resource.from(resource);
    if (!hasResourceRecycleBin(definition))
      return BusinessResponse.error("该资源不支持回收站");
    int changed =
        resources.destroyRecycledResources(definition.key, Collections.singletonList(id));
    if (changed == 0) return BusinessResponse.error("回收记录不存在");
    log(adminId, "彻底删除" + definition.title, definition.key + ":" + id);
    return BusinessResponse.success(changed);
  }

  @DeleteMapping("/{resource}/destroy")
  @Transactional
  public BusinessResponse<?> destroyRecycledResources(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable String resource,
      @RequestBody(required = false) Map<String, Object> body) {
    Long adminId = adminId(token);
    if (adminId == null) return unauthorized();
    Resource definition = Resource.from(resource);
    if (!hasResourceRecycleBin(definition))
      return BusinessResponse.error("该资源不支持回收站");
    boolean all = allRecycleRecords(body);
    List<Long> ids = idList(body == null ? null : body.get("ids"));
    if (!all && ids.isEmpty()) return BusinessResponse.error("请选择要彻底删除的记录");
    int changed =
        resources.destroyRecycledResources(
            definition.key, all ? Collections.<Long>emptyList() : ids);
    if (changed == 0) return BusinessResponse.error("回收站中没有可彻底删除的记录");
    log(adminId, all ? "彻底删除全部" + definition.title : "批量彻底删除" + definition.title,
        all ? "全部" : ids.toString());
    return BusinessResponse.success(changed);
  }

  @GetMapping("/maintain/recycle")
  public BusinessResponse<?> recycledMaintains(
      @RequestHeader(value = "token", required = false) String token,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int limit) {
    if (adminId(token) == null) return unauthorized();
    page = Math.max(page, 1);
    limit = Math.min(Math.max(limit, 1), 100);
    return BusinessResponse.success(page(
        resources.recycledMaintains(limit, (page - 1) * limit),
        resources.recycledMaintainCount(), page, limit));
  }

  @PostMapping("/maintain/restore/{id}")
  @Transactional
  public BusinessResponse<?> restoreMaintain(
      @RequestHeader(value = "token", required = false) String token, @PathVariable long id) {
    Long adminId = adminId(token);
    if (adminId == null) return unauthorized();
    if (resources.restoreMaintain(id, now()) != 1) return BusinessResponse.error("回收项目不存在");
    log(adminId, "恢复维修项目", "maintain:" + id);
    return BusinessResponse.success(1);
  }

  @DeleteMapping("/maintain/destroy/{id}")
  @Transactional
  public BusinessResponse<?> destroyMaintain(
      @RequestHeader(value = "token", required = false) String token, @PathVariable long id) {
    Long adminId = adminId(token);
    if (adminId == null) return unauthorized();
    if (resources.destroyMaintain(id) != 1) return BusinessResponse.error("回收项目不存在");
    log(adminId, "彻底删除维修项目", "maintain:" + id);
    return BusinessResponse.success(1);
  }

  @PostMapping("/brand/{id}/sort")
  @Transactional
  public BusinessResponse<?> sortBrand(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable long id,
      @RequestBody(required = false) Map<String, Object> body) {
    Long adminId = adminId(token);
    if (adminId == null) return unauthorized();
    Object raw = body == null ? null : body.get("new_sort");
    if (raw == null && body != null) raw = body.get("weigh");
    Integer weigh = integer(raw);
    if (id <= 0 || weigh == null || weigh < 0) return BusinessResponse.error("排序参数错误");
    int changed = resources.sortBrand(id, weigh, now());
    if (changed == 0) return BusinessResponse.error("品牌不存在");
    log(adminId, "车辆品牌排序", "brand:" + id + ",weigh:" + weigh);
    return BusinessResponse.success(1);
  }

  @PutMapping("/{resource}/status")
  @Transactional
  public BusinessResponse<?> updateStatuses(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable String resource,
      @RequestBody(required = false) Map<String, Object> body) {
    Long adminId = adminId(token);
    if (adminId == null) return unauthorized();
    Resource definition = Resource.from(resource);
    if (definition == null || !definition.mutable || !definition.columns.contains("status"))
      return BusinessResponse.error("该资源不支持批量状态操作");
    List<Long> ids = idList(body == null ? null : body.get("ids"));
    if (ids.isEmpty()) return BusinessResponse.error("请选择要修改的记录");
    Object status = normalizedStatus(definition, body == null ? null : body.get("status"));
    if (status == null) return BusinessResponse.error("状态参数错误");
    int changed = resources.updateStatuses(definition.key, ids, status, now());
    if (changed == 0) return BusinessResponse.error("记录不存在或状态未变化");
    log(adminId, "批量修改" + definition.title + "状态", ids + ":" + status);
    return BusinessResponse.success(changed);
  }

  @PutMapping("/{resource}/order")
  @Transactional
  public BusinessResponse<?> reorder(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable String resource,
      @RequestBody(required = false) Map<String, Object> body) {
    Long adminId = adminId(token);
    if (adminId == null) return unauthorized();
    Resource definition = Resource.from(resource);
    if (definition == null || !definition.mutable || !definition.columns.contains("weigh"))
      return BusinessResponse.error("该资源不支持拖动排序");
    if (!definition.matchesTable(body == null ? null : body.get("table")))
      return BusinessResponse.error("排序数据表错误");
    String primaryKey = text(body == null ? null : body.get("pk"));
    if (!primaryKey.isEmpty() && !"id".equalsIgnoreCase(primaryKey))
      return BusinessResponse.error("排序主键错误");
    // 旧 Ajax 会把缺失或非法 field 强制收敛为 weigh。这里同样固定字段，绝不把请求字段拼入 SQL。
    List<Long> ids = idList(body == null ? null : body.get("ids"));
    if (ids.isEmpty()) return BusinessResponse.error("排序记录不能为空");

    Object rawPid = body == null ? null : body.get("pid");
    boolean pidLimited = hasValue(rawPid);
    if (pidLimited && definition != Resource.CATEGORY)
      return BusinessResponse.error("该资源不支持父级范围排序");
    List<Long> parentIds = pidLimited ? nonNegativeIdList(rawPid) : Collections.<Long>emptyList();
    if (parentIds == null) return BusinessResponse.error("父级范围参数错误");
    if (pidLimited && parentIds.isEmpty()) return BusinessResponse.success(0);

    boolean ascending =
        body != null && "asc".equalsIgnoreCase(String.valueOf(body.get("orderway")));
    List<Map<String, Object>> rows =
        resources.selectOrderWeights(definition.key, ids, parentIds, ascending);
    Map<Long, Integer> weights = new LinkedHashMap<>();
    List<Long> source = new ArrayList<>();
    for (Map<String, Object> row : rows) {
      Long rowId = longValue(row.get("id"));
      Integer weight = integer(row.get("weigh"));
      if (rowId == null || weights.containsKey(rowId)) continue;
      source.add(rowId);
      weights.put(rowId, weight);
    }
    List<Long> desired = new ArrayList<>();
    for (Long id : ids) if (weights.containsKey(id)) desired.add(id);
    if (!pidLimited && desired.size() != ids.size())
      return BusinessResponse.error("排序记录不存在或已删除");
    if (desired.isEmpty()) return BusinessResponse.success(0);

    Long changeId = positiveLong(body == null ? null : body.get("changeid"));
    boolean hasChangeId = hasValue(body == null ? null : body.get("changeid"));
    if (hasChangeId && (changeId == null || !desired.contains(changeId)))
      return BusinessResponse.error("拖动记录不存在或不在父级范围内");
    Map<Long, Integer> assignments =
        hasChangeId
            ? legacyWeightAssignments(desired, source, weights, changeId)
            : orderedWeightAssignments(desired, source, weights);
    for (Map.Entry<Long, Integer> assignment : assignments.entrySet()) {
      if (assignment.getValue() != null)
        resources.updateOrderWeight(definition.key, assignment.getKey(), assignment.getValue());
    }
    log(adminId, "拖动排序" + definition.title, desired.toString());
    return BusinessResponse.success(desired.size());
  }

  private static Map<Long, Integer> orderedWeightAssignments(
      List<Long> desired, List<Long> source, Map<Long, Integer> weights) {
    Map<Long, Integer> result = new LinkedHashMap<>();
    int size = Math.min(desired.size(), source.size());
    for (int index = 0; index < size; index++) {
      Integer weight = weights.get(source.get(index));
      if (weight != null) result.put(desired.get(index), weight);
    }
    return result;
  }

  private static Map<Long, Integer> legacyWeightAssignments(
      List<Long> desired, List<Long> source, Map<Long, Integer> weights, Long changeId) {
    Map<Long, Integer> result = new LinkedHashMap<>();
    int position = desired.indexOf(changeId);
    Long destination =
        position >= 0 && position < source.size()
            ? source.get(position)
            : source.get(source.size() - 1);
    List<Long> changed = new ArrayList<>();
    for (int index = 0; index < desired.size(); index++) {
      if (index >= source.size() || !desired.get(index).equals(source.get(index)))
        changed.add(desired.get(index));
    }
    boolean movedToEarlierPosition = !changed.isEmpty() && changeId.equals(changed.get(0));
    for (int index = 0; index < changed.size(); index++) {
      Long id = changed.get(index);
      Long offset;
      if (id.equals(changeId)) {
        offset = destination;
      } else if (movedToEarlierPosition) {
        offset = index + 1 < changed.size() ? changed.get(index + 1) : changeId;
      } else {
        offset = index > 0 ? changed.get(index - 1) : changeId;
      }
      Integer weight = weights.get(offset);
      if (weight != null) result.put(id, weight);
    }
    return result;
  }

  private static boolean hasResourceRecycleBin(Resource resource) {
    return resource == Resource.BRAND || resource == Resource.PLATFORM_CAR;
  }

  private static boolean allRecycleRecords(Map<String, Object> body) {
    if (body == null || !body.containsKey("ids")) return true;
    Object value = body.get("all");
    return value instanceof Boolean
        ? ((Boolean) value).booleanValue()
        : "1".equals(String.valueOf(value)) || "true".equalsIgnoreCase(String.valueOf(value));
  }

  private Long adminId(String token) {
    return sessions.adminId(token);
  }

  private String validateCategoryParent(Resource definition, long id, Map<String, Object> values) {
    if (definition != Resource.CATEGORY || !values.containsKey("pid")) return null;
    Integer parent = integer(values.get("pid"));
    if (parent == null || parent < 0) return "父分类参数错误";
    if (parent == 0) return null;
    if (parent == id) return "父分类不能是自身或下级分类";
    Map<Long, Long> parents = new HashMap<>();
    for (Map<String, Object> row : resources.categoryParents())
      parents.put(((Number) row.get("id")).longValue(), ((Number) row.get("pid")).longValue());
    long parentId = parent.longValue();
    if (!parents.containsKey(parentId)) return "父分类不存在";
    Long current = parentId;
    Set<Long> visited = new LinkedHashSet<>();
    while (current != null && current > 0 && visited.add(current)) {
      if (current == id) return "父分类不能是自身或下级分类";
      current = parents.get(current);
    }
    return null;
  }

  private void log(long adminId, String title, String content) {
    adminLogs.insertLog(adminId, "/api/admin/business/resources", title, content, now());
  }

  private static Map<String, Object> writableValues(Resource definition, Map<String, Object> body) {
    Map<String, Object> values = new LinkedHashMap<>();
    if (body == null) return values;
    for (String column : definition.columns) {
      if (body.containsKey(column)) values.put(column, body.get(column));
    }
    return values;
  }

  private static BusinessResponse<?> unauthorized() {
    return BusinessResponse.error("管理员未登录");
  }

  private static long now() {
    return System.currentTimeMillis() / 1000;
  }

  private static Integer integer(Object value) {
    try {
      return value == null ? null : Integer.valueOf(String.valueOf(value).trim());
    } catch (NumberFormatException ignored) {
      return null;
    }
  }

  private static Long positiveLong(Object value) {
    try {
      long parsed = value == null ? 0 : Long.parseLong(String.valueOf(value).trim());
      return parsed > 0 ? parsed : null;
    } catch (NumberFormatException ignored) {
      return null;
    }
  }

  private static Long longValue(Object value) {
    return value instanceof Number ? ((Number) value).longValue() : positiveLong(value);
  }

  private static String text(Object value) {
    return value == null ? "" : String.valueOf(value).trim();
  }

  private static boolean hasValue(Object value) {
    if (value == null) return false;
    if (value instanceof Iterable) return ((Iterable<?>) value).iterator().hasNext();
    return !String.valueOf(value).trim().isEmpty();
  }

  private static List<Long> nonNegativeIdList(Object value) {
    List<Long> result = new ArrayList<>();
    Iterable<?> values =
        value instanceof Iterable
            ? (Iterable<?>) value
            : value == null
                ? Collections.emptyList()
                : Arrays.asList(String.valueOf(value).split(","));
    for (Object item : values) {
      try {
        long id = Long.parseLong(String.valueOf(item).trim());
        if (id < 0) return null;
        if (!result.contains(id)) result.add(id);
      } catch (NumberFormatException ignored) {
        return null;
      }
    }
    return result;
  }

  private static Object normalizedStatus(Resource resource, Object value) {
    String status = value == null ? "" : String.valueOf(value).trim();
    if (resource == Resource.COUPON || resource == Resource.CATEGORY)
      return "normal".equals(status) || "hidden".equals(status) ? status : null;
    Integer number = integer(value);
    return number != null && (number == 1 || number == 2) ? number : null;
  }

  private static List<Long> idList(Object value) {
    List<Long> ids = new ArrayList<>();
    Iterable<?> values =
        value instanceof Iterable
            ? (Iterable<?>) value
            : value == null
                ? Collections.emptyList()
                : Arrays.asList(String.valueOf(value).split(","));
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

  private enum Resource {
    BANNER(
        "banner",
        "ymcx_banner",
        "轮播图",
        false,
        true,
        set("image", "weigh", "status", "createtime", "updatetime"),
        list("image"),
        list("image")),
    BRAND(
        "brand",
        "ymcx_brand",
        "车辆品牌",
        true,
        true,
        set("name", "weigh", "status", "createtime", "updatetime"),
        list("name"),
        list("name")),
    PLATFORM_CAR(
        "platform-car",
        "ymcx_platform_car",
        "平台车辆",
        true,
        true,
        set(
            "name",
            "brand_id",
            "image",
            "content",
            "specification",
            "status",
            "weigh",
            "createtime",
            "updatetime"),
        list("name", "specification"),
        list("brand_id")),
    MAINTAIN(
        "maintain",
        "ymcx_maintain",
        "维修项目",
        false,
        true,
        set("name", "image", "introductory", "content", "createtime", "updatetime"),
        list("name", "introductory"),
        list("name", "image")),
    COUPON(
        "coupon",
        "ymcx_coupon",
        "优惠券",
        false,
        true,
        set(
            "score",
            "price",
            "reduceprice",
            "weigh",
            "status",
            "endtime",
            "createtime",
            "updatetime"),
        Collections.<String>emptyList(),
        list("score", "price", "reduceprice")),
    CATEGORY(
        "category",
        "ymcx_category",
        "分类",
        false,
        true,
        set("pid", "type", "name", "nickname", "flag", "image", "keywords", "description", "diyname", "weigh", "status", "createtime", "updatetime"),
        list("name", "nickname", "diyname"),
        list("type", "name")),
    MALFUNCTION(
        "malfunction",
        "ymcx_malfunction",
        "故障上报",
        false,
        false,
        Collections.<String>emptySet(),
        list("number", "content"),
        Collections.<String>emptyList());

    private static final Map<String, Resource> LOOKUP = new HashMap<>();

    static {
      for (Resource resource : values()) LOOKUP.put(resource.key, resource);
    }

    final String key;
    final String table;
    final String title;
    final boolean softDelete;
    final boolean mutable;
    final Set<String> columns;
    final List<String> searchColumns;
    final List<String> requiredColumns;

    Resource(
        String key,
        String table,
        String title,
        boolean softDelete,
        boolean mutable,
        Set<String> columns,
        List<String> searchColumns,
        List<String> requiredColumns) {
      this.key = key;
      this.table = table;
      this.title = title;
      this.softDelete = softDelete;
      this.mutable = mutable;
      this.columns = columns;
      this.searchColumns = searchColumns;
      this.requiredColumns = requiredColumns;
    }

    static Resource from(String key) {
      return LOOKUP.get(key);
    }

    boolean matchesTable(Object requested) {
      String value = text(requested);
      if (value.isEmpty()) return true;
      String legacy = table.startsWith("ymcx_") ? table.substring(5) : table;
      return key.equalsIgnoreCase(value)
          || table.equalsIgnoreCase(value)
          || legacy.equalsIgnoreCase(value);
    }

    String validate(Map<String, Object> values) {
      for (String column : requiredColumns) {
        Object value = values.get(column);
        if (value == null || value.toString().trim().isEmpty()) return column + "不能为空";
      }
      return validateForUpdate(values);
    }

    String validateForUpdate(Map<String, Object> values) {
      if (this == COUPON) {
        for (String column : Arrays.asList("score", "price", "reduceprice")) {
          if (values.containsKey(column) && decimal(values.get(column)) < 0) return "金额和积分不能小于0";
        }
      }
      return null;
    }

    private static double decimal(Object value) {
      try {
        return value == null ? 0 : Double.parseDouble(value.toString());
      } catch (NumberFormatException ignored) {
        return -1;
      }
    }

    private static Set<String> set(String... values) {
      return new LinkedHashSet<>(Arrays.asList(values));
    }

    private static List<String> list(String... values) {
      return Arrays.asList(values);
    }
  }
}
