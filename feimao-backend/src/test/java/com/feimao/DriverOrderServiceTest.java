package com.feimao;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.feimao.service.DriverOrderService;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
class DriverOrderServiceTest {
  @Mock JdbcTemplate jdbc;
  private DriverOrderService orders;

  @BeforeEach
  void setUp() {
    orders = new DriverOrderService(jdbc);
  }

  @Test
  void onlyAvailableApprovedDriverCanGrabOrder() {
    when(jdbc.queryForList(any(String.class), eq(Long.class), eq(9L)))
        .thenReturn(Collections.singletonList(9L));
    when(jdbc.update(any(String.class), eq(9L), any(), any(), eq(21L))).thenReturn(1);
    assertTrue(orders.grab(9L, 21L));
  }

  @Test
  void unavailableDriverCannotAttemptOrderUpdate() {
    when(jdbc.queryForList(any(String.class), eq(Long.class), eq(9L)))
        .thenReturn(Collections.emptyList());
    assertFalse(orders.grab(9L, 21L));
    verify(jdbc, never()).update(any(String.class), any(), any(), any(), any());
  }

  @Test
  void concurrentSecondGrabLosesConditionalUpdate() {
    when(jdbc.queryForList(any(String.class), eq(Long.class), eq(9L)))
        .thenReturn(Collections.singletonList(9L));
    when(jdbc.update(any(String.class), eq(9L), any(), any(), eq(21L))).thenReturn(0);
    assertFalse(orders.grab(9L, 21L));
  }

  @Test
  void startingOrderMarksDriverBusyInSameTransaction() {
    when(jdbc.queryForList(any(String.class), eq(Long.class), eq(9L)))
        .thenReturn(Collections.singletonList(9L));
    when(jdbc.update(any(String.class), any(), any(), eq(21L), eq(9L))).thenReturn(1);
    assertTrue(orders.start(9L, 21L));
    verify(jdbc)
        .update(
            eq("update fm_driver set online_status=2,update_time=? where id=? and deleted=0"),
            any(),
            eq(9L));
  }

  @Test
  void onlineStatusChangeUsesActiveOrderGuard() {
    when(jdbc.update(any(String.class), eq(0), any(), eq(9L))).thenReturn(1);
    assertTrue(orders.changeOnlineStatus(9L, 0));
    verify(jdbc).update(
        org.mockito.ArgumentMatchers.contains("not exists"), eq(0), any(), eq(9L));
  }

  @Test
  void driverWithActiveOrderCannotChangeOnlineStatus() {
    when(jdbc.update(any(String.class), eq(0), any(), eq(9L))).thenReturn(0);
    assertFalse(orders.changeOnlineStatus(9L, 0));
  }
}
