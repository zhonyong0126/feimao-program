package com.feimao;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.feimao.service.DriverAdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
class DriverAdminServiceTest {
  @Mock JdbcTemplate jdbc;
  private DriverAdminService service;

  @BeforeEach
  void setUp() {
    service = new DriverAdminService(jdbc);
  }

  @Test
  void disablingDriverUsesActiveOrderGuard() {
    when(jdbc.update(any(String.class), any(), eq(8L))).thenReturn(1);
    assertTrue(service.changeStatus(8L, 0));
    verify(jdbc).update(
        org.mockito.ArgumentMatchers.contains("not exists"), any(), eq(8L));
  }

  @Test
  void transportingDriverCannotBeDisabled() {
    when(jdbc.update(any(String.class), any(), eq(8L))).thenReturn(0);
    assertFalse(service.changeStatus(8L, 0));
  }

  @Test
  void rejectingDriverUsesActiveOrderGuard() {
    when(jdbc.update(any(String.class), any(), eq(8L))).thenReturn(1);
    assertTrue(service.review(8L, 3));
    verify(jdbc).update(
        org.mockito.ArgumentMatchers.contains("o.status in (2,3)"), any(), eq(8L));
  }

  @Test
  void enablingAndApprovingExistingDriverRemainAvailable() {
    when(jdbc.update(any(String.class), any(), eq(8L))).thenReturn(1, 1);
    assertTrue(service.changeStatus(8L, 1));
    assertTrue(service.review(8L, 2));
  }
}
