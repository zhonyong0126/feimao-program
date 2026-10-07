package com.feimao;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.feimao.common.BusinessResponse;
import com.feimao.controller.order.OrderController;
import com.feimao.controller.store.StoreController;
import com.feimao.mapper.BusinessOrderMapper;
import com.feimao.mapper.StoreWorkspaceMapper;
import com.feimao.service.InsuranceService;
import com.feimao.service.RoleSessionService;
import com.feimao.service.SmsNotificationService;
import com.feimao.service.UserTokenService;
import com.feimao.service.WechatPayService;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.apache.ibatis.annotations.Select;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class OrderStoreLegacyContractTest {

  @Test
  void simpleOrderCouponKeepsLegacyQueryMessageAndUncappedDiscount() throws Exception {
    Select select =
        BusinessOrderMapper.class
            .getMethod("usableCoupon", long.class, long.class)
            .getAnnotation(Select.class);
    String couponSql = String.join(" ", select.value()).toLowerCase();
    assertFalse(couponSql.contains("endtime"));
    assertFalse(couponSql.contains("not exists"));
    assertTrue(couponSql.contains("state=1"));
    assertTrue(couponSql.contains("user_id=#{userid}"));

    BusinessOrderMapper orders = mock(BusinessOrderMapper.class);
    UserTokenService tokens = mock(UserTokenService.class);
    WechatPayService pay = mock(WechatPayService.class);
    when(tokens.userId("token")).thenReturn(11L);
    Map<String, Object> service = new LinkedHashMap<>();
    service.put("id", 3L);
    service.put("price", new BigDecimal("10.00"));
    service.put("merchant_id", 5L);
    service.put("name", "车辆");
    service.put("image", "car.jpg");
    when(orders.simpleService(2, 3L)).thenReturn(service);
    Map<String, Object> coupon = new LinkedHashMap<>();
    coupon.put("id", 9L);
    coupon.put("price", new BigDecimal("5.00"));
    coupon.put("reduceprice", new BigDecimal("15.00"));
    when(orders.usableCoupon(9L, 11L)).thenReturn(coupon);
    when(orders.paymentOrder(anyString()))
        .thenAnswer(
            invocation -> {
              Map<String, Object> payment = new LinkedHashMap<>();
              payment.put("number", invocation.getArgument(0));
              payment.put("order_name", "车辆*1");
              payment.put("openid", "openid");
              return payment;
            });
    when(pay.unifiedOrder(anyString(), anyString(), anyString(), anyLong(), anyString()))
        .thenReturn(Collections.singletonMap("appId", "test"));
    OrderController controller =
        new OrderController(
            orders,
            tokens,
            pay,
            mock(InsuranceService.class),
            mock(SmsNotificationService.class));
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("merchant_car_id", 3);
    body.put("num", 1);
    body.put("name", "张三");
    body.put("mobile", "138" + "0000" + "0000");
    body.put("take_back_time", 1_800_000_000L);
    body.put("pick_type", 1);
    body.put("service_image", "car.jpg");
    body.put("coupon_id", 9);

    assertEquals(1, controller.buyCarPay("token", body).getCode());

    ArgumentCaptor<Map<String, Object>> values = ArgumentCaptor.forClass(Map.class);
    verify(orders).insertSimpleOrder(values.capture());
    assertEquals(new BigDecimal("15.00"), values.getValue().get("discounts_price"));
    ArgumentCaptor<Long> cents = ArgumentCaptor.forClass(Long.class);
    verify(pay)
        .unifiedOrder(anyString(), anyString(), eq("购买:车辆*1"), cents.capture(), eq("/api/notify/buyCarNotify"));
    assertEquals(-500L, cents.getValue().longValue());

    when(orders.usableCoupon(9L, 11L)).thenReturn(null);
    assertEquals("优惠券无法使用!", controller.buyCarPay("token", body).getMsg());
    coupon.put("price", new BigDecimal("11.00"));
    when(orders.usableCoupon(9L, 11L)).thenReturn(coupon);
    assertEquals("优惠券无法使用!", controller.buyCarPay("token", body).getMsg());
  }

  @Test
  void orderPageKeepsLegacyShapeAndDurationDefaults() {
    BusinessOrderMapper orders = mock(BusinessOrderMapper.class);
    UserTokenService tokens = mock(UserTokenService.class);
    when(tokens.userId("token")).thenReturn(11L);
    Map<String, Object> car = new LinkedHashMap<>();
    car.put("id", 3L);
    car.put("merchant_id", 5L);
    car.put("platform_car_id", 8L);
    car.put("status", 1);
    car.put("cash_price", new BigDecimal("50.00"));
    car.put("month_price", new BigDecimal("300.00"));
    car.put("day_price", new BigDecimal("20.00"));
    car.put("hour_price", new BigDecimal("3.00"));
    car.put("platform_car_name", "平台车辆");
    car.put("name", "平台车辆");
    car.put("image", "car.jpg");
    car.put("brand_id", 9L);
    car.put("specification", "双座");
    car.put("pick_type", 3);
    car.put("brand", null);
    car.put("stock", 99);
    when(orders.rentableCar(3L)).thenReturn(car);
    Map<String, Object> merchant = new LinkedHashMap<>();
    merchant.put("id", 5L);
    merchant.put("lat", 0);
    merchant.put("lng", 0);
    for (int i = 1; i <= 4; i++) {
      merchant.put("service_distance" + i, i);
      merchant.put("service_money" + i, i);
    }
    merchant.put("service_money5", 5);
    when(orders.activeMerchant(5L)).thenReturn(merchant);
    when(orders.leaseConfigs()).thenReturn(Collections.emptyList());
    when(orders.configValue("insure_price")).thenReturn("1.00");
    OrderController controller =
        new OrderController(
            orders,
            tokens,
            mock(WechatPayService.class),
            mock(InsuranceService.class),
            mock(SmsNotificationService.class));
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("token", "body-token");
    body.put("merchant_car_id", 3);
    body.put("lease_type", 2);
    body.put("starttime", "2026-08-23 10:00");
    body.put("endtime", "2026-08-24 10:00");

    BusinessResponse<?> response = controller.orderPage("token", body);

    assertEquals(1, response.getCode());
    @SuppressWarnings("unchecked")
    Map<String, Object> result = (Map<String, Object>) response.getData();
    assertEquals(17, result.size());
    assertEquals(1L, ((Number) result.get("num")).longValue());
    assertEquals(1, ((Number) result.get("people")).intValue());
    assertEquals(1, ((Number) result.get("pick_single")).intValue());
    assertTrue(result.get("address") instanceof List);
    assertTrue(((List<?>) result.get("address")).isEmpty());
    @SuppressWarnings("unchecked")
    Map<String, Object> responseCar = (Map<String, Object>) result.get("merchant_car");
    assertEquals("平台车辆", responseCar.get("platform_car_name"));
    assertEquals("双座", responseCar.get("specification"));
    assertEquals(3, ((Number) responseCar.get("pick_type")).intValue());
    assertEquals("", responseCar.get("brand"));
    assertFalse(responseCar.containsKey("stock"));
    @SuppressWarnings("unchecked")
    Map<String, Object> params = (Map<String, Object>) result.get("params");
    assertFalse(params.containsKey("token"));
  }

  @Test
  void paymentFailureUsesLegacyFixedMessageAndNullData() {
    BusinessOrderMapper orders = mock(BusinessOrderMapper.class);
    UserTokenService tokens = mock(UserTokenService.class);
    WechatPayService pay = mock(WechatPayService.class);
    when(tokens.userId("token")).thenReturn(11L);
    Map<String, Object> order = new LinkedHashMap<>();
    order.put("number", "N1");
    order.put("type", 1);
    order.put("state", 1);
    order.put("order_name", "车辆-1小时");
    order.put("openid", "openid");
    order.put("price", new BigDecimal("12.00"));
    order.put("discounts_price", BigDecimal.ZERO);
    when(orders.userPaymentOrder("N1", 11L)).thenReturn(order);
    when(pay.unifiedOrder(anyString(), anyString(), anyString(), anyLong(), anyString()))
        .thenThrow(new IllegalStateException("不得泄露的支付配置错误"));
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRequestURI()).thenReturn("/api/order/againLeaseCarPay");
    OrderController controller =
        new OrderController(
            orders,
            tokens,
            pay,
            mock(InsuranceService.class),
            mock(SmsNotificationService.class));

    BusinessResponse<?> response =
        controller.repay("token", Collections.singletonMap("order_number", "N1"), request);

    assertEquals(0, response.getCode());
    assertEquals("支付失败重新支付!", response.getMsg());
    assertNull(response.getData());
  }

  @Test
  void leaseDeliveryWithoutAddressUsesLegacyErrorBeforeDatabaseLookup() {
    BusinessOrderMapper orders = mock(BusinessOrderMapper.class);
    UserTokenService tokens = mock(UserTokenService.class);
    when(tokens.userId("token")).thenReturn(11L);
    OrderController controller =
        new OrderController(
            orders,
            tokens,
            mock(WechatPayService.class),
            mock(InsuranceService.class),
            mock(SmsNotificationService.class));
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("source_type", 1);
    body.put("merchant_car_id", 3);
    body.put("pick_type", 3);
    body.put("lease_type", 1);
    body.put("starttime", "2026-08-23 09:00");
    body.put("endtime", "2026-08-23 10:00");
    body.put("name", "张三");
    body.put("mobile", "138" + "0000" + "0000");
    body.put("idcard", "legacy-id");

    BusinessResponse<?> response = controller.leaseCarPay("token", body);

    assertEquals(0, response.getCode());
    assertEquals("请选择取车地址!", response.getMsg());
    verify(orders, never()).lockRentableCar(anyLong());
  }

  @Test
  void protectedStoreActionReturnsLegacyHttpAndJson401() throws Exception {
    StoreWorkspaceMapper workspace = mock(StoreWorkspaceMapper.class);
    RoleSessionService sessions = mock(RoleSessionService.class);
    StoreController controller =
        new StoreController(workspace, sessions, mock(WechatPayService.class));
    MockMvc mvc = MockMvcBuilders.standaloneSetup(controller).build();

    mvc.perform(
            post("/api/store/index")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value(401))
        .andExpect(jsonPath("$.msg").value("请登录后操作"));
  }

  @Test
  void storeSessionRechecksMerchantStatusAndAcceptsLegacyLargePageSize() {
    StoreWorkspaceMapper workspace = mock(StoreWorkspaceMapper.class);
    RoleSessionService sessions = mock(RoleSessionService.class);
    StoreController controller =
        new StoreController(workspace, sessions, mock(WechatPayService.class));
    when(sessions.subject("token", "store")).thenReturn(7L);
    when(workspace.activeMerchant(7L)).thenReturn(null);
    assertEquals(401, controller.index("token").getCode());

    when(workspace.activeMerchant(7L)).thenReturn(7L);
    when(workspace.cashRows(7L, 9_999_999_999L, 0L)).thenReturn(Collections.emptyList());
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("page", 1);
    body.put("list_rows", 9_999_999_999L);
    BusinessResponse<?> response = controller.cashList("token", body);
    assertEquals(1, response.getCode());
    @SuppressWarnings("unchecked")
    Map<String, Object> page = (Map<String, Object>) response.getData();
    assertEquals(9_999_999_999L, ((Number) page.get("per_page")).longValue());
    assertEquals(0L, ((Number) page.get("last_page")).longValue());
    verify(workspace).cashRows(7L, 9_999_999_999L, 0L);
  }

  @Test
  void storeRefundUsesLegacyTwoDecimalAmountValidation() {
    StoreWorkspaceMapper workspace = mock(StoreWorkspaceMapper.class);
    RoleSessionService sessions = mock(RoleSessionService.class);
    when(sessions.subject("token", "store")).thenReturn(7L);
    when(workspace.activeMerchant(7L)).thenReturn(7L);
    StoreController controller =
        new StoreController(workspace, sessions, mock(WechatPayService.class));
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("order_number", "N1");
    body.put("return_type", 1);
    body.put("refund_cash_price", "1.234");
    assertEquals("金额格式不正确", controller.backCar("token", body).getMsg());
    body.put("refund_cash_price", "-1.00");
    assertEquals("金额不能为负数", controller.backCar("token", body).getMsg());
  }

  @Test
  void orderRequiresLegacySourceAndAcceptsUnixServiceTimeAndCallerImage() {
    BusinessOrderMapper orders = mock(BusinessOrderMapper.class);
    UserTokenService tokens = mock(UserTokenService.class);
    WechatPayService pay = mock(WechatPayService.class);
    SmsNotificationService notifications = mock(SmsNotificationService.class);
    when(tokens.userId("token")).thenReturn(11L);
    OrderController controller =
        new OrderController(orders, tokens, pay, mock(InsuranceService.class), notifications);

    Map<String, Object> lease = new LinkedHashMap<>();
    lease.put("merchant_car_id", 1);
    lease.put("name", "张三");
    lease.put("mobile", "138" + "0000" + "0000");
    lease.put("idcard", "110101" + "19900101" + "123X");
    assertEquals("下单来源参数错误", controller.leaseCarPay("token", lease).getMsg());

    Map<String, Object> service = new LinkedHashMap<>();
    service.put("id", 3L);
    service.put("price", new BigDecimal("20.00"));
    service.put("merchant_id", 5L);
    service.put("name", "车辆");
    service.put("image", "database-image.jpg");
    when(orders.simpleService(2, 3L)).thenReturn(service);
    doAnswer(
            invocation -> {
              invocation.<Map<String, Object>>getArgument(0).put("id", 99L);
              return 1;
            })
        .when(orders)
        .insertSimpleOrder(anyMap());
    when(orders.paymentOrder(anyString()))
        .thenAnswer(
            invocation -> {
              Map<String, Object> payment = new LinkedHashMap<>();
              payment.put("number", invocation.getArgument(0));
              payment.put("order_name", "车辆*1");
              payment.put("openid", "openid");
              return payment;
            });
    when(pay.unifiedOrder(anyString(), anyString(), anyString(), anyLong(), anyString()))
        .thenReturn(Collections.singletonMap("appId", "test"));

    Map<String, Object> buy = new LinkedHashMap<>();
    buy.put("merchant_car_id", 3);
    buy.put("num", 1);
    buy.put("name", "张三");
    buy.put("mobile", "x");
    buy.put("take_back_time", 1_800_000_000L);
    buy.put("pick_type", 1);
    buy.put("service_image", "caller-image.jpg");
    assertEquals(1, controller.buyCarPay("token", buy).getCode());
    ArgumentCaptor<Map<String, Object>> values = ArgumentCaptor.forClass(Map.class);
    verify(orders).insertSimpleOrder(values.capture());
    assertEquals(1_800_000_000L, values.getValue().get("take_back_time"));
    assertEquals("caller-image.jpg", values.getValue().get("service_image"));
  }

  @Test
  void leaseUsesOnlyLegacyValidatorRulesAndAllowsLegacyPassengerValues() {
    BusinessOrderMapper orders = mock(BusinessOrderMapper.class);
    UserTokenService tokens = mock(UserTokenService.class);
    WechatPayService pay = mock(WechatPayService.class);
    when(tokens.userId("token")).thenReturn(11L);
    Map<String, Object> car = new LinkedHashMap<>();
    car.put("id", 3L);
    car.put("merchant_id", 5L);
    car.put("stock", 2);
    car.put("cash_price", BigDecimal.ZERO);
    car.put("month_price", new BigDecimal("10.00"));
    car.put("hour_price", new BigDecimal("2.00"));
    car.put("name", "车辆");
    car.put("image", "car.jpg");
    when(orders.lockRentableCar(3L)).thenReturn(car);
    Map<String, Object> merchant = new LinkedHashMap<>();
    merchant.put("id", 5L);
    when(orders.activeMerchant(5L)).thenReturn(merchant);
    when(orders.leaseConfigs()).thenReturn(Collections.emptyList());
    when(orders.configValue("insure_price")).thenReturn("1.00");
    doAnswer(
            invocation -> {
              invocation.<Map<String, Object>>getArgument(0).put("id", 99L);
              return 1;
            })
        .when(orders)
        .insertLeaseOrder(anyMap());
    when(orders.paymentOrder(anyString()))
        .thenAnswer(
            invocation -> {
              Map<String, Object> payment = new LinkedHashMap<>();
              payment.put("number", invocation.getArgument(0));
              payment.put("order_name", "车辆-1月");
              payment.put("openid", "openid");
              return payment;
            });
    when(pay.unifiedOrder(anyString(), anyString(), anyString(), anyLong(), anyString()))
        .thenReturn(Collections.singletonMap("appId", "test"));
    OrderController controller =
        new OrderController(
            orders,
            tokens,
            pay,
            mock(InsuranceService.class),
            mock(SmsNotificationService.class));

    Map<String, Object> guest = new LinkedHashMap<>();
    guest.put("name", "");
    guest.put("mobile", "legacy");
    guest.put("idcard", "legacy-id");
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("source_type", 1);
    body.put("merchant_car_id", 3);
    body.put("pick_type", 1);
    body.put("pick_single", 9);
    body.put("lease_type", 4);
    body.put("starttime", "2026-08-23 09:00");
    body.put("endtime", "2026-08-23 12:00");
    body.put("name", "张三");
    body.put("mobile", "138" + "0000" + "0000");
    body.put("idcard", "legacy-id");
    body.put("guest", Collections.singletonList(guest));

    assertEquals(1, controller.leaseCarPay("token", body).getCode());
    verify(orders, times(2))
        .insertOrderGuest(anyLong(), eq(99L), anyString(), anyString(), anyString(), any(), anyLong());

    body.put("lease_type", 1);
    body.put("starttime", "2026-08-23 23:00");
    body.put("endtime", "2026-08-24 01:00");
    body.remove("pick_single");
    assertEquals(1, controller.leaseCarPay("token", body).getCode());
    ArgumentCaptor<Map<String, Object>> leaseValues = ArgumentCaptor.forClass(Map.class);
    verify(orders, times(2)).insertLeaseOrder(leaseValues.capture());
    assertEquals(0, new BigDecimal("0.00").compareTo((BigDecimal) leaseValues.getAllValues().get(0).get("insure_money")));
    Map<String, Object> secondLease = leaseValues.getAllValues().get(1);
    assertEquals(2L, ((Number) secondLease.get("num")).longValue());
    assertEquals(1, ((Number) secondLease.get("pick_single")).intValue());
  }

  @Test
  void returnRequestSendsLegacyMerchantSmsWithoutBreakingMainTransaction() {
    BusinessOrderMapper orders = mock(BusinessOrderMapper.class);
    UserTokenService tokens = mock(UserTokenService.class);
    SmsNotificationService notifications = mock(SmsNotificationService.class);
    when(tokens.userId("token")).thenReturn(11L);
    Map<String, Object> order = new LinkedHashMap<>();
    order.put("id", 77L);
    order.put("state", 3);
    order.put("pick_state", 1);
    when(orders.lockUserOrderForReturn("N1", 11L)).thenReturn(order);
    when(orders.returnUserCar(eq(77L), anyString(), anyString(), anyLong())).thenReturn(1);
    doThrow(new IllegalStateException("SMS unavailable"))
        .when(notifications)
        .notifyMerchantReturnRequested(77L);
    OrderController controller =
        new OrderController(
            orders, tokens, mock(WechatPayService.class), mock(InsuranceService.class), notifications);

    BusinessResponse<?> response =
        controller.backCar("token", Collections.singletonMap("order_number", "N1"));

    assertEquals(1, response.getCode());
    assertEquals("还车成功!", response.getMsg());
    verify(notifications).notifyMerchantReturnRequested(77L);
  }
}
