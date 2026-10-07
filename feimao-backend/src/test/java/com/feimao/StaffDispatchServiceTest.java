package com.feimao;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.feimao.service.StaffDispatchService;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
class StaffDispatchServiceTest {
  @Mock JdbcTemplate jdbc;
  private StaffDispatchService dispatch;

  @BeforeEach
  void setUp() {
    dispatch = new StaffDispatchService(jdbc);
  }

  @Test
  void availableDriverCanBeAssignedAtomically() {
    when(jdbc.queryForList(any(String.class), eq(Long.class), eq(8L)))
        .thenReturn(Collections.singletonList(8L));
    when(jdbc.update(any(String.class), eq(8L), eq(3L), any(), any(), eq(21L))).thenReturn(1);
    assertTrue(dispatch.dispatch(3L, 21L, 8L));
  }

  @Test
  void offlineOrUnapprovedDriverCannotBeAssigned() {
    when(jdbc.queryForList(any(String.class), eq(Long.class), eq(8L)))
        .thenReturn(Collections.emptyList());
    assertFalse(dispatch.dispatch(3L, 21L, 8L));
    verify(jdbc, never()).update(any(String.class), any(), any(), any(), any(), any());
  }

  @Test
  void concurrentOrderClaimCannotBeOverwritten() {
    when(jdbc.queryForList(any(String.class), eq(Long.class), eq(8L)))
        .thenReturn(Collections.singletonList(8L));
    when(jdbc.update(any(String.class), eq(8L), eq(3L), any(), any(), eq(21L))).thenReturn(0);
    assertFalse(dispatch.dispatch(3L, 21L, 8L));
  }

  @Test
  void onlyUnassignedPendingOrderCanBeCancelled() {
    when(jdbc.update(any(String.class), any(), eq(21L))).thenReturn(1);
    assertTrue(dispatch.cancel(21L));
  }

  @Test
  void adminDispatchUsesSameDriverAndOrderConcurrencyRules() {
    when(jdbc.queryForList(any(String.class), eq(Long.class), eq(8L)))
        .thenReturn(Collections.singletonList(8L));
    when(jdbc.update(any(String.class), eq(8L), any(), any(), eq(21L))).thenReturn(1);
    assertTrue(dispatch.adminDispatch(21L, 8L));
  }
}
