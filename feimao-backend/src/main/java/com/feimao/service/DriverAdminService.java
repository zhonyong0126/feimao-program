package com.feimao.service;

import java.time.LocalDateTime;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DriverAdminService {
  private final JdbcTemplate jdbc;

  public DriverAdminService(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public boolean changeStatus(long driverId, int status) {
    if (status == 1) {
      return jdbc.update(
              "update fm_driver set status=1,update_time=? where id=? and deleted=0",
              LocalDateTime.now(),
              driverId)
          == 1;
    }
    return jdbc.update(
            "update fm_driver d set d.status=0,d.online_status=0,d.update_time=? "
                + "where d.id=? and d.deleted=0 and d.online_status<>2 "
                + "and not exists(select 1 from fm_orders o where o.driver_id=d.id "
                + "and o.status in (2,3) and o.deleted=0)",
            LocalDateTime.now(),
            driverId)
        == 1;
  }

  public boolean review(long driverId, int authStatus) {
    if (authStatus == 2) {
      return jdbc.update(
              "update fm_driver set auth_status=2,update_time=? where id=? and deleted=0",
              LocalDateTime.now(),
              driverId)
          == 1;
    }
    return jdbc.update(
            "update fm_driver d set d.auth_status=3,d.online_status=0,d.update_time=? "
                + "where d.id=? and d.deleted=0 and d.online_status<>2 "
                + "and not exists(select 1 from fm_orders o where o.driver_id=d.id "
                + "and o.status in (2,3) and o.deleted=0)",
            LocalDateTime.now(),
            driverId)
        == 1;
  }
}
