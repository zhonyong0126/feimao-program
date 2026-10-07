package com.feimao;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.feimao.common.BusinessResponse;
import com.feimao.controller.merchant.MerchantController;
import com.feimao.controller.store.StoreController;
import com.feimao.mapper.MerchantCatalogMapper;
import com.feimao.mapper.StoreWorkspaceMapper;
import com.feimao.service.RoleSessionService;
import com.feimao.service.UserTokenService;
import com.feimao.service.WechatPayService;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class VehicleApiLegacyContractTest {

  @Test
  void storeCarEditUsesLegacyRequiredFieldOrderAndMessages() {
    StoreWorkspaceMapper workspace = mock(StoreWorkspaceMapper.class);
    RoleSessionService sessions = mock(RoleSessionService.class);
    StoreController controller =
        new StoreController(workspace, sessions, mock(WechatPayService.class));
    when(sessions.subject("token", "store")).thenReturn(7L);
    when(workspace.activeMerchant(7L)).thenReturn(7L);

    Map<String, Object> add = validCarBody(2);
    Map<String, String> expected = new LinkedHashMap<>();
    expected.put("platform_car_id", "请选择平台车辆");
    expected.put("cash_price", "押金不得为空");
    expected.put("month_price", "月租价格不得为空");
    expected.put("day_price", "日租价格不得为空");
    expected.put("images", "车辆照片不得为空");
    expected.put("intro", "车辆详情图片不得为空");
    expected.put("total_stock", "总库存不得为空");
    for (Map.Entry<String, String> entry : expected.entrySet()) {
      Map<String, Object> missing = new LinkedHashMap<>(add);
      missing.remove(entry.getKey());
      assertEquals(entry.getValue(), controller.carEdit("token", missing).getMsg(), entry.getKey());
    }

    Map<String, Object> edit = validCarBody(3);
    edit.remove("id");
    assertEquals("请选择编辑车辆", controller.carEdit("token", edit).getMsg());

    assertEquals(1, controller.carEdit("token", add).getCode());
    verify(workspace)
        .insertCar(
            eq(7L),
            eq(3L),
            eq("detail.jpg"),
            eq("car.jpg"),
            eq(BigDecimal.ZERO),
            eq(BigDecimal.ZERO),
            eq(BigDecimal.ZERO),
            eq(BigDecimal.ZERO),
            eq(0),
            anyLong());
  }

  @Test
  void storeCarDetailRequiresIdAndOmitsPlatformNameAndImage() {
    StoreWorkspaceMapper workspace = mock(StoreWorkspaceMapper.class);
    RoleSessionService sessions = mock(RoleSessionService.class);
    StoreController controller =
        new StoreController(workspace, sessions, mock(WechatPayService.class));
    when(sessions.subject("token", "store")).thenReturn(7L);
    when(workspace.activeMerchant(7L)).thenReturn(7L);

    Map<String, Object> request = new LinkedHashMap<>();
    request.put("type", 1);
    assertEquals("请选择车辆", controller.carEdit("token", request).getMsg());

    Map<String, Object> car = new LinkedHashMap<>();
    car.put("id", 9L);
    car.put("name", "不应返回的车型名");
    car.put("image", "not-returned.jpg");
    car.put("images", "a.jpg,b.jpg");
    car.put("intro", "c.jpg");
    when(workspace.car(9L, 7L)).thenReturn(car);
    request.put("id", 9);
    BusinessResponse<?> response = controller.carEdit("token", request);
    assertEquals(1, response.getCode());
    @SuppressWarnings("unchecked")
    Map<String, Object> data = (Map<String, Object>) response.getData();
    assertFalse(data.containsKey("name"));
    assertFalse(data.containsKey("image"));
    assertEquals(Arrays.asList("a.jpg", "b.jpg"), data.get("images"));
  }

  @Test
  void merchantCarDetailMatchesLegacyTopLevelAndNestedMerchantFields() {
    MerchantCatalogMapper catalog = mock(MerchantCatalogMapper.class);
    MerchantController controller =
        new MerchantController(catalog, mock(UserTokenService.class), new ObjectMapper());
    Map<String, Object> row = new LinkedHashMap<>();
    row.put("id", 9L);
    row.put("status", 1);
    row.put("day_price", new BigDecimal("10.00"));
    row.put("cash_price", new BigDecimal("100.00"));
    row.put("installment", "[]");
    row.put("images", "a.jpg,b.jpg");
    row.put("intro", "c.jpg");
    row.put("brand_id", 3L);
    row.put("brand_name", "品牌");
    row.put("name", "车型");
    row.put("image", "car.jpg");
    row.put("specification", "规格");
    row.put("merchant_name", "门店");
    row.put("business", "09:00-18:00");
    row.put("administrative", "广东省深圳市");
    row.put("address", "龙岗区");
    row.put("pick_type", 1);
    when(catalog.selectCar(9L)).thenReturn(row);

    Map<String, Object> request = new LinkedHashMap<>();
    request.put("merchant_car_id", 9);
    BusinessResponse<?> response = controller.carDetails(request);
    assertEquals(1, response.getCode());
    @SuppressWarnings("unchecked")
    Map<String, Object> data = (Map<String, Object>) response.getData();
    assertFalse(data.containsKey("cash_price"));
    assertFalse(data.containsKey("installment"));
    @SuppressWarnings("unchecked")
    Map<String, Object> merchant = (Map<String, Object>) data.get("merchant");
    assertEquals("广东省深圳市", merchant.get("administrative"));
    assertEquals("广东省深圳市龙岗区", merchant.get("address"));
  }

  private static Map<String, Object> validCarBody(int type) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("type", type);
    if (type == 3) body.put("id", 9);
    body.put("platform_car_id", 3);
    body.put("cash_price", "0");
    body.put("month_price", "0");
    body.put("day_price", "0");
    body.put("images", "car.jpg");
    body.put("intro", "detail.jpg");
    body.put("total_stock", 0);
    return body;
  }
}
