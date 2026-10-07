package com.feimao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.feimao.common.Result;
import com.feimao.controller.driver.DriverController;
import com.feimao.entity.Driver;
import com.feimao.entity.Order;
import com.feimao.service.DriverService;
import com.feimao.service.DriverOrderService;
import com.feimao.service.OrderService;
import com.feimao.service.WechatLoginService;
import com.feimao.utils.JwtUtils;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class DriverOnlineStatusTest {
  @Mock DriverService drivers;
  @Mock OrderService orders;
  @Mock DriverOrderService driverOrders;
  @Mock JwtUtils jwt;
  @Mock WechatLoginService wechat;

  private DriverController controller;

  @BeforeEach
  void setUp() {
    controller = new DriverController();
    ReflectionTestUtils.setField(controller, "driverService", drivers);
    ReflectionTestUtils.setField(controller, "orderService", orders);
    ReflectionTestUtils.setField(controller, "driverOrderService", driverOrders);
    ReflectionTestUtils.setField(controller, "jwtUtils", jwt);
    ReflectionTestUtils.setField(controller, "wechatLoginService", wechat);
  }

  @Test
  void authenticatedAndApprovedDriverCanGoOnline() {
    Driver current = driver(0);
    when(jwt.getUserId("valid-token")).thenReturn(9L);
    when(drivers.getById(9L)).thenReturn(current);
    when(driverOrders.changeOnlineStatus(9L, 1)).thenReturn(true);

    Result<Boolean> result =
        controller.updateOnlineStatus(
            "Bearer valid-token", Collections.singletonMap("onlineStatus", 1));

    assertEquals(200, result.getCode());
    assertTrue(result.getData());
    verify(driverOrders).changeOnlineStatus(9L, 1);
  }

  @Test
  void busyDriverCannotChangeOnlineStatus() {
    when(jwt.getUserId("valid-token")).thenReturn(9L);
    when(drivers.getById(9L)).thenReturn(driver(2));

    Result<Boolean> result =
        controller.updateOnlineStatus(
            "Bearer valid-token", Collections.singletonMap("onlineStatus", 0));

    assertEquals(500, result.getCode());
    assertEquals("运输中不能切换在线状态", result.getMsg());
    assertFalse(Boolean.TRUE.equals(result.getData()));
  }

  @Test
  void invalidOnlineStatusIsRejectedBeforeDatabaseWrite() {
    when(jwt.getUserId("valid-token")).thenReturn(9L);

    Result<Boolean> result =
        controller.updateOnlineStatus(
            "Bearer valid-token", Collections.singletonMap("onlineStatus", 2));

    assertEquals(500, result.getCode());
    verifyNoInteractions(drivers);
  }

  @Test
  void driverCanConfirmArrivalOnlyForOwnTransportingOrder() {
    Order order = order(9L, 3);
    when(jwt.getUserId("valid-token")).thenReturn(9L);
    when(orders.getById(21L)).thenReturn(order);
    when(driverOrders.arrive(9L, 21L)).thenReturn(true);

    Result<Boolean> result = controller.arrive("Bearer valid-token", 21L);

    assertEquals(200, result.getCode());
    assertTrue(result.getData());
    verify(driverOrders).arrive(9L, 21L);
  }

  @Test
  void driverCannotStartAnotherDriversOrder() {
    when(jwt.getUserId("valid-token")).thenReturn(9L);
    when(orders.getById(21L)).thenReturn(order(8L, 2));

    Result<Boolean> result = controller.startTransport("Bearer valid-token", 21L);

    assertEquals(500, result.getCode());
    assertEquals("订单不存在或不属于当前司机", result.getMsg());
  }

  @Test
  void driverCannotReadAnotherDriversOrderDetail() {
    when(jwt.getUserId("valid-token")).thenReturn(9L);
    when(orders.getById(21L)).thenReturn(order(8L, 2));

    Result<Order> result = controller.orderDetail("Bearer valid-token", 21L);

    assertEquals(500, result.getCode());
    assertEquals("订单不存在或不属于当前司机", result.getMsg());
  }

  @Test
  void driverWechatLoginUsesRealCodeExchangeAndStableOpenid() {
    Driver current = driver(0);
    when(wechat.exchangeCode("wx-code")).thenReturn("stable-openid");
    when(drivers.getByOpenid("stable-openid")).thenReturn(current);
    when(jwt.generateToken(9L, "driver")).thenReturn("driver-token");

    Result<java.util.Map<String, Object>> result =
        controller.wxLogin(Collections.singletonMap("code", "wx-code"));

    assertEquals(200, result.getCode());
    assertEquals("driver-token", result.getData().get("token"));
    verify(wechat).exchangeCode("wx-code");
  }

  private static Driver driver(int onlineStatus) {
    Driver driver = new Driver();
    driver.setId(9L);
    driver.setDeleted(0);
    driver.setStatus(1);
    driver.setAuthStatus(2);
    driver.setOnlineStatus(onlineStatus);
    return driver;
  }

  private static Order order(long driverId, int status) {
    Order order = new Order();
    order.setId(21L);
    order.setDriverId(driverId);
    order.setStatus(status);
    order.setDeleted(0);
    return order;
  }
}
