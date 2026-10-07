package com.feimao.controller.admin;

import com.feimao.common.BusinessResponse;
import com.feimao.entity.BusinessAddress;
import com.feimao.mapper.AdminLogMapper;
import com.feimao.mapper.BusinessAddressMapper;
import com.feimao.service.AdminSessionService;
import com.feimao.service.BusinessAddressService;
import com.feimao.service.BusinessUserService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.CookieValue;
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

/** Administrative management of user delivery addresses. */
@RestController
public class AddressAdminController {
  private final BusinessAddressMapper addresses;
  private final BusinessAddressService addressService;
  private final BusinessUserService users;
  private final AdminLogMapper logs;
  private final AdminSessionService sessions;

  public AddressAdminController(BusinessAddressMapper addresses,
      BusinessAddressService addressService, BusinessUserService users, AdminLogMapper logs,
      AdminSessionService sessions) {
    this.addresses = addresses;
    this.addressService = addressService;
    this.users = users;
    this.logs = logs;
    this.sessions = sessions;
  }

  @GetMapping("/api/admin/business/addresses")
  public BusinessResponse<?> list(
      @RequestHeader(value = "token", required = false) String token,
      @RequestParam(required = false, name = "user_id") Long userId,
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String administrative,
      @RequestParam(required = false) String address,
      @RequestParam(required = false) String mobile,
      @RequestParam(required = false) Long createdFrom,
      @RequestParam(required = false) Long createdTo,
      @RequestParam(required = false) Long updatedFrom,
      @RequestParam(required = false) Long updatedTo,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int limit) {
    if (admin(token) == null) return unauthorized();
    page = Math.max(page, 1);
    limit = Math.min(Math.max(limit, 1), 100);
    int total = addresses.countAdminRows(userId, clean(name), clean(administrative), clean(address), clean(mobile), createdFrom, createdTo, updatedFrom, updatedTo);
    java.util.List<Map<String, Object>> rows =
        addresses.selectAdminRows(userId, clean(name), clean(administrative), clean(address), clean(mobile), createdFrom, createdTo, updatedFrom, updatedTo, limit, (page - 1) * limit);
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("rows", rows);
    result.put("total", total);
    result.put("page", page);
    result.put("limit", limit);
    return BusinessResponse.success(result);
  }

  public BusinessResponse<?> list(String token,String keyword,Long userId,int page,int limit) {
    return list(token,userId,keyword,null,null,null,null,null,null,null,page,limit);
  }

  @GetMapping("/api/admin/business/addresses/areas")
  public BusinessResponse<?> areas(
      @RequestHeader(value = "token", required = false) String token,
      @RequestParam(required = false) Long parentId,
      @RequestParam(required = false) Integer level,
      @RequestParam(required = false) Long province,
      @RequestParam(required = false) Long city,
      @RequestParam(required = false) String keyword) {
    if (admin(token) == null) return unauthorized();
    long pid = parentId == null ? 0 : Math.max(parentId, 0);
    int areaLevel = level == null ? (pid == 0 ? 1 : 0) : level;
    if (province != null && province > 0) {
      pid = province;
      areaLevel = 2;
    }
    if (city != null && city > 0) {
      pid = city;
      areaLevel = 3;
    }
    if (areaLevel < 0 || areaLevel > 3) return BusinessResponse.error("地区层级错误");
    return BusinessResponse.success(addresses.selectAreas(
        pid, areaLevel, keyword == null ? "" : keyword.trim()));
  }

  /** Exact compatibility endpoint for the original FastAdmin ajax/area action. */
  @RequestMapping(
      value = {"/api/admin/ajax/area", "/admin/ajax/area"},
      method = {RequestMethod.GET, RequestMethod.POST})
  public BusinessResponse<?> legacyAreas(
      @RequestHeader(value = "token", required = false) String token,
      @CookieValue(value = "admin_token", required = false) String cookieToken,
      @RequestParam Map<String, String> parameters) {
    if ((token == null || token.trim().isEmpty()) && cookieToken != null) token = cookieToken;
    if (admin(token) == null) return unauthorized();
    boolean nestedRow = parameters.keySet().stream()
        .anyMatch(key -> key.startsWith("row[") && key.endsWith("]"));
    String province = legacyParameter(parameters,
        nestedRow ? "row[province]" : "province");
    String city = legacyParameter(parameters, nestedRow ? "row[city]" : "city");
    String pid = "0";
    int level = 1;
    if (province != null) {
      pid = province;
      level = 2;
      if (city != null) {
        pid = city;
        level = 3;
      }
    }
    return BusinessResponse.success("", addresses.selectLegacyAreas(pid, level));
  }

  @PostMapping("/api/admin/business/addresses")
  public BusinessResponse<?> add(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long admin = admin(token);
    if (admin == null) return unauthorized();
    long userId = number(body, "user_id", 0);
    long now = now();
    BusinessAddress address = address(body, userId, now);
    addressService.save(address);
    log(admin, "新增用户地址", String.valueOf(address.getId()));
    return BusinessResponse.success(java.util.Collections.singletonMap("id", address.getId()));
  }

  @PutMapping("/api/admin/business/addresses/{id}")
  public BusinessResponse<?> edit(
      @RequestHeader(value = "token", required = false) String token,
      @PathVariable long id,
      @RequestBody(required = false) Map<String, Object> body) {
    Long admin = admin(token);
    if (admin == null) return unauthorized();
    if (addressService.getById(id) == null)
      return BusinessResponse.error("地址不存在");
    long userId = number(body, "user_id", 0);
    BusinessAddress address = address(body, userId, now());
    address.setId(id);
    address.setCreatetime(null);
    addressService.updateById(address);
    log(admin, "编辑用户地址", String.valueOf(id));
    return BusinessResponse.success(1);
  }

  @DeleteMapping("/api/admin/business/addresses/{id}")
  public BusinessResponse<?> delete(
      @RequestHeader(value = "token", required = false) String token, @PathVariable long id) {
    Long admin = admin(token);
    if (admin == null) return unauthorized();
    if (!addressService.removeById(id))
      return BusinessResponse.error("地址不存在");
    log(admin, "删除用户地址", String.valueOf(id));
    return BusinessResponse.success(1);
  }

  @DeleteMapping("/api/admin/business/addresses")
  public BusinessResponse<?> deleteMany(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String,Object> body) {
    Long admin=admin(token);if(admin==null)return unauthorized();
    List<Long> ids=idList(body==null?null:body.get("ids"));
    if(ids.isEmpty())return BusinessResponse.error("请选择要删除的地址");
    int changed=addresses.deleteAdminRows(ids);
    if(changed==0)return BusinessResponse.error("地址不存在");
    log(admin,"批量删除用户地址",ids.toString());return BusinessResponse.success(changed);
  }

  private static List<Long> idList(Object value) { List<Long> ids=new ArrayList<>();if(value instanceof Iterable)for(Object item:(Iterable<?>)value)addId(ids,item);else if(value!=null)for(String item:String.valueOf(value).split(","))addId(ids,item);return ids; }
  private static void addId(List<Long> ids,Object value) { try{long id=Long.parseLong(String.valueOf(value).trim());if(id>0&&!ids.contains(id))ids.add(id);}catch(NumberFormatException ignored){} }
  private static String clean(String value) { return value==null?"":value.trim(); }
  private static String legacyParameter(Map<String, String> parameters, String name) {
    return parameters.containsKey(name) ? clean(parameters.get(name)) : null;
  }

  private Long admin(String token) {
    return sessions.adminId(token);
  }

  private void log(long admin, String title, String content) {
    logs.insertLog(admin, "/api/admin/business/addresses", title, content, now());
  }

  private static BusinessAddress address(Map<String, Object> body, long userId, long now) {
    BusinessAddress address = new BusinessAddress();
    address.setUserId(userId);
    address.setName(text(body, "name"));
    address.setAdministrative(text(body, "administrative"));
    address.setAddress(text(body, "address"));
    address.setMobile(text(body, "mobile"));
    address.setLng(decimal(body, "lng"));
    address.setLat(decimal(body, "lat"));
    address.setCreatetime(now);
    address.setUpdatetime(now);
    return address;
  }

  private static long number(Map<String, Object> body, String key, long fallback) {
    try {
      return Long.parseLong(text(body, key));
    } catch (NumberFormatException ignored) {
      return fallback;
    }
  }

  private static BigDecimal decimal(Map<String, Object> body, String key) {
    try {
      return new BigDecimal(text(body, key));
    } catch (NumberFormatException ignored) {
      return BigDecimal.ZERO;
    }
  }

  private static String text(Map<String, Object> body, String key) {
    return body == null || body.get(key) == null ? "" : String.valueOf(body.get(key)).trim();
  }

  private static long now() {
    return System.currentTimeMillis() / 1000;
  }

  private static BusinessResponse<?> unauthorized() {
    return BusinessResponse.error("管理员未登录");
  }
}
