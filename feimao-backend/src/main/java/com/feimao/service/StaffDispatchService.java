package com.feimao.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StaffDispatchService {
  private final JdbcTemplate jdbc;

  public StaffDispatchService(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  @Transactional
  public boolean dispatch(long staffId, long orderId, long driverId) {
    if (!lockAvailableDriver(driverId)) return false;
    return jdbc.update(
            "update fm_orders set driver_id=?,staff_id=?,status=2,accept_time=?,update_time=? "
                + "where id=? and driver_id is null and status in (0,1) and deleted=0",
            driverId,
            staffId,
            LocalDateTime.now(),
            LocalDateTime.now(),
            orderId)
        == 1;
  }

  @Transactional
  public boolean adminDispatch(long orderId, long driverId) {
    if (!lockAvailableDriver(driverId)) return false;
    return jdbc.update(
            "update fm_orders set driver_id=?,status=2,accept_time=?,update_time=? "
                + "where id=? and driver_id is null and status in (0,1) and deleted=0",
            driverId,
            LocalDateTime.now(),
            LocalDateTime.now(),
            orderId)
        == 1;
  }

  public boolean cancel(long orderId) {
    return jdbc.update(
            "update fm_orders set status=5,update_time=? "
                + "where id=? and driver_id is null and status in (0,1) and deleted=0",
            LocalDateTime.now(),
            orderId)
        == 1;
  }

  private boolean lockAvailableDriver(long driverId) {
    List<Long> rows =
        jdbc.queryForList(
            "select d.id from fm_driver d where d.id=? and d.deleted=0 and d.status=1 "
                + "and d.auth_status=2 and d.online_status=1 "
                + "and not exists(select 1 from fm_orders o where o.driver_id=d.id "
                + "and o.status in (2,3) and o.deleted=0) for update",
            Long.class,
            driverId);
    return !rows.isEmpty();
  }
}
