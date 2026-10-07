package com.feimao.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DriverOrderService {
  private final JdbcTemplate jdbc;

  public DriverOrderService(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  @Transactional
  public boolean grab(long driverId, long orderId) {
    if (!lockAvailableDriver(driverId, true)) return false;
    return jdbc.update(
            "update fm_orders set driver_id=?,status=2,accept_time=?,update_time=? "
                + "where id=? and driver_id is null and status=1 and deleted=0",
            driverId,
            LocalDateTime.now(),
            LocalDateTime.now(),
            orderId)
        == 1;
  }

  @Transactional
  public boolean start(long driverId, long orderId) {
    if (!lockAvailableDriver(driverId, false)) return false;
    int changed =
        jdbc.update(
            "update fm_orders set status=3,start_time=?,update_time=? "
                + "where id=? and driver_id=? and status=2 and deleted=0",
            LocalDateTime.now(),
            LocalDateTime.now(),
            orderId,
            driverId);
    if (changed == 0) return false;
    jdbc.update(
        "update fm_driver set online_status=2,update_time=? where id=? and deleted=0",
        LocalDateTime.now(),
        driverId);
    return true;
  }

  public boolean arrive(long driverId, long orderId) {
    return jdbc.update(
            "update fm_orders set arrive_time=?,update_time=? "
                + "where id=? and driver_id=? and status=3 and arrive_time is null and deleted=0",
            LocalDateTime.now(),
            LocalDateTime.now(),
            orderId,
            driverId)
        == 1;
  }

  @Transactional
  public boolean changeOnlineStatus(long driverId, int onlineStatus) {
    return jdbc.update(
            "update fm_driver d set d.online_status=?,d.update_time=? "
                + "where d.id=? and d.deleted=0 and d.status=1 and d.auth_status=2 "
                + "and d.online_status<>2 and not exists(select 1 from fm_orders o "
                + "where o.driver_id=d.id and o.status in (2,3) and o.deleted=0)",
            onlineStatus,
            LocalDateTime.now(),
            driverId)
        == 1;
  }

  private boolean lockAvailableDriver(long driverId, boolean requireNoActiveOrder) {
    String sql =
        "select d.id from fm_driver d where d.id=? and d.deleted=0 and d.status=1 "
            + "and d.auth_status=2 and d.online_status=1";
    if (requireNoActiveOrder) {
      sql +=
          " and not exists(select 1 from fm_orders o where o.driver_id=d.id "
              + "and o.status in (2,3) and o.deleted=0)";
    }
    List<Long> rows = jdbc.queryForList(sql + " for update", Long.class, driverId);
    return !rows.isEmpty();
  }
}
