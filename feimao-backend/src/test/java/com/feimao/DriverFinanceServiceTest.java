package com.feimao;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.feimao.service.DriverFinanceService;
import java.math.BigDecimal;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
class DriverFinanceServiceTest {
  @Mock JdbcTemplate jdbc;

  private DriverFinanceService finance;

  @BeforeEach
  void setUp() {
    finance = new DriverFinanceService(jdbc);
  }

  @Test
  void completedOrderCreditsBalanceAndWritesIncomeLog() {
    when(jdbc.queryForList(any(String.class), eq(Long.class), eq(9L)))
        .thenReturn(Collections.singletonList(9L));
    when(jdbc.update(
            eq("update fm_orders set status=4,complete_time=?,update_time=? where id=? and driver_id=? and status=3 and arrive_time is not null and deleted=0"),
            any(), any(), eq(21L), eq(9L)))
        .thenReturn(1);
    when(jdbc.queryForObject(
            "select coalesce(actual_freight,freight,0) from fm_orders where id=?",
            BigDecimal.class,
            21L))
        .thenReturn(new BigDecimal("88.50"));

    assertTrue(finance.complete(9L, 21L));
    verify(jdbc).update(
        eq("update fm_driver set balance=balance+?,update_time=? where id=? and deleted=0"),
        eq(new BigDecimal("88.50")), any(), eq(9L));
    verify(jdbc).update(
        eq("update fm_driver set online_status=case when status=1 and auth_status=2 then 1 else 0 end,update_time=? where id=? and deleted=0"),
        any(), eq(9L));
  }

  @Test
  void repeatedCompletionDoesNotCreditAgain() {
    when(jdbc.queryForList(any(String.class), eq(Long.class), eq(9L)))
        .thenReturn(Collections.singletonList(9L));
    when(jdbc.update(
            eq("update fm_orders set status=4,complete_time=?,update_time=? where id=? and driver_id=? and status=3 and arrive_time is not null and deleted=0"),
            any(), any(), eq(21L), eq(9L)))
        .thenReturn(0);

    assertFalse(finance.complete(9L, 21L));
    verify(jdbc, never()).queryForObject(any(String.class), eq(BigDecimal.class), any(Long.class));
  }

  @Test
  void missingDriverCannotCompleteOrder() {
    when(jdbc.queryForList(any(String.class), eq(Long.class), eq(9L)))
        .thenReturn(Collections.emptyList());
    assertFalse(finance.complete(9L, 21L));
    verify(jdbc, never()).update(any(String.class), any(), any(), any(), any());
  }

  @Test
  void withdrawalAtomicallyDeductsBalanceAndCreatesRecords() {
    BigDecimal amount = new BigDecimal("100.00");
    when(jdbc.update(
            "update fm_driver set balance=balance-? where id=? and deleted=0 and status=1 and balance>=?",
            amount,
            9L,
            amount))
        .thenReturn(1);

    assertTrue(finance.withdraw(9L, amount));
    verify(jdbc).update(
        eq("insert into fm_withdraw(driver_id,amount,status,create_time,update_time) values(?,?,0,?,?)"),
        eq(9L), eq(amount), any(), any());
    verify(jdbc).update(
        eq("insert into fm_driver_balance_log(driver_id,amount,type,remark,create_time) values(?,?,2,'提现申请',?)"),
        eq(9L), eq(new BigDecimal("-100.00")), any());
  }

  @Test
  void insufficientBalanceCreatesNoWithdrawal() {
    BigDecimal amount = new BigDecimal("100.00");
    when(jdbc.update(
            "update fm_driver set balance=balance-? where id=? and deleted=0 and status=1 and balance>=?",
            amount,
            9L,
            amount))
        .thenReturn(0);

    assertFalse(finance.withdraw(9L, amount));
    verify(jdbc, never()).update(
        eq("insert into fm_withdraw(driver_id,amount,status,create_time,update_time) values(?,?,0,?,?)"),
        any(), any(), any(), any());
  }

  @Test
  void todayIncomeUsesOnlyIncomeRowsFromToday() {
    when(jdbc.queryForObject(
            "select coalesce(sum(amount),0) from fm_driver_balance_log where driver_id=? and type=1 and create_time>=current_date()",
            BigDecimal.class,
            9L))
        .thenReturn(new BigDecimal("188.50"));

    assertEquals(new BigDecimal("188.50"), finance.todayIncome(9L));
  }
}
