package com.feimao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.feimao.service.DriverWithdrawAdminService;
import com.feimao.service.WechatTransferService;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
class DriverWithdrawAdminServiceTest {
  @Mock JdbcTemplate jdbc;
  @Mock WechatTransferService transfers;
  private DriverWithdrawAdminService service;

  @BeforeEach
  void setUp() {
    service = new DriverWithdrawAdminService(jdbc, transfers);
  }

  @Test
  void approvalLocksRequestAndStoresWechatBatch() {
    Map<String, Object> row = new HashMap<>();
    row.put("driver_id", 8L);
    row.put("amount", new BigDecimal("88.50"));
    row.put("openid", "openid-8");
    when(jdbc.queryForList(any(String.class), eq(21L)))
        .thenReturn(Collections.singletonList(row));
    when(transfers.transfer("openid-8", "FMDW21", 8850L, "司机提现"))
        .thenReturn("batch-21");
    when(jdbc.update(any(String.class), eq("batch-21"), any(), any(), any(), eq(21L)))
        .thenReturn(1);

    assertEquals("batch-21", service.approve(21L, "通过"));
  }

  @Test
  void driverWithoutWechatCannotBeApproved() {
    Map<String, Object> row = new HashMap<>();
    row.put("amount", BigDecimal.TEN);
    when(jdbc.queryForList(any(String.class), eq(21L)))
        .thenReturn(Collections.singletonList(row));
    assertThrows(IllegalStateException.class, () -> service.approve(21L, null));
  }

  @Test
  void rejectionRefundsBalanceAndWritesRefundLog() {
    Map<String, Object> row = new HashMap<>();
    row.put("driver_id", 8L);
    row.put("amount", new BigDecimal("20.00"));
    when(jdbc.queryForList(any(String.class), eq(21L)))
        .thenReturn(Collections.singletonList(row));
    when(jdbc.update(any(String.class), eq("资料不符"), any(), any(), eq(21L))).thenReturn(1);
    when(jdbc.update(any(String.class), eq(new BigDecimal("20.00")), any(), eq(8L)))
        .thenReturn(1);

    assertTrue(service.reject(21L, "资料不符"));
    verify(jdbc).update(
        eq("insert into fm_driver_balance_log(driver_id,amount,type,remark,create_time) values(?,?,3,'提现拒绝退款',?)"),
        eq(8L), eq(new BigDecimal("20.00")), any());
  }

  @Test
  void reviewedRequestCannotBeRejectedAgain() {
    when(jdbc.queryForList(any(String.class), eq(21L))).thenReturn(Collections.emptyList());
    assertFalse(service.reject(21L, "重复请求"));
  }
}
