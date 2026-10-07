package com.feimao.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DriverFinanceService {
  private final JdbcTemplate jdbc;

  public DriverFinanceService(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  @Transactional
  public boolean complete(long driverId, long orderId) {
    List<Long> drivers =
        jdbc.queryForList(
            "select id from fm_driver where id=? and deleted=0 for update", Long.class, driverId);
    if (drivers.isEmpty()) return false;
    int changed =
        jdbc.update(
            "update fm_orders set status=4,complete_time=?,update_time=? "
                + "where id=? and driver_id=? and status=3 and arrive_time is not null and deleted=0",
            LocalDateTime.now(),
            LocalDateTime.now(),
            orderId,
            driverId);
    if (changed == 0) return false;
    BigDecimal amount =
        jdbc.queryForObject(
            "select coalesce(actual_freight,freight,0) from fm_orders where id=?",
            BigDecimal.class,
            orderId);
    if (amount == null) amount = BigDecimal.ZERO;
    if (amount.signum() > 0) {
      jdbc.update(
          "update fm_driver set balance=balance+?,update_time=? where id=? and deleted=0",
          amount,
          LocalDateTime.now(),
          driverId);
      jdbc.update(
          "insert into fm_driver_balance_log(driver_id,amount,type,order_id,remark,create_time) "
              + "values(?,?,1,?,'订单完成收入',?)",
          driverId,
          amount,
          orderId,
          LocalDateTime.now());
    }
    jdbc.update(
        "update fm_driver set online_status=case when status=1 and auth_status=2 then 1 else 0 end,"
            + "update_time=? where id=? and deleted=0",
        LocalDateTime.now(),
        driverId);
    return true;
  }

  public Map<String, Object> income(long driverId, int page, int limit) {
    int safePage = Math.max(1, page);
    int safeLimit = Math.max(1, Math.min(100, limit));
    Integer total =
        jdbc.queryForObject(
            "select count(*) from fm_driver_balance_log where driver_id=?",
            Integer.class,
            driverId);
    List<Map<String, Object>> records =
        jdbc.queryForList(
            "select id,amount,type,order_id,remark,create_time from fm_driver_balance_log "
                + "where driver_id=? order by id desc limit ? offset ?",
            driverId,
            safeLimit,
            (safePage - 1) * safeLimit);
    BigDecimal balance =
        jdbc.queryForObject(
            "select coalesce(balance,0) from fm_driver where id=? and deleted=0",
            BigDecimal.class,
            driverId);
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("records", records);
    result.put("total", total == null ? 0 : total);
    result.put("page", safePage);
    result.put("limit", safeLimit);
    result.put("balance", balance == null ? BigDecimal.ZERO : balance);
    return result;
  }

  public BigDecimal todayIncome(long driverId) {
    BigDecimal amount =
        jdbc.queryForObject(
            "select coalesce(sum(amount),0) from fm_driver_balance_log "
                + "where driver_id=? and type=1 and create_time>=current_date()",
            BigDecimal.class,
            driverId);
    return amount == null ? BigDecimal.ZERO : amount;
  }

  @Transactional
  public boolean withdraw(long driverId, BigDecimal amount) {
    if (amount == null || amount.signum() <= 0) return false;
    int changed =
        jdbc.update(
            "update fm_driver set balance=balance-? "
                + "where id=? and deleted=0 and status=1 and balance>=?",
            amount,
            driverId,
            amount);
    if (changed == 0) return false;
    jdbc.update(
        "insert into fm_withdraw(driver_id,amount,status,create_time,update_time) values(?,?,0,?,?)",
        driverId,
        amount,
        LocalDateTime.now(),
        LocalDateTime.now());
    jdbc.update(
        "insert into fm_driver_balance_log(driver_id,amount,type,remark,create_time) "
            + "values(?,?,2,'提现申请',?)",
        driverId,
        amount.negate(),
        LocalDateTime.now());
    return true;
  }
}
