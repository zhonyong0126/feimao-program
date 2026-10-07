package com.feimao.service;

import com.feimao.entity.Driver;
import java.time.LocalDateTime;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DriverProfileService {
  private final JdbcTemplate jdbc;

  public DriverProfileService(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  @Transactional
  public boolean submitAuthentication(long driverId, Driver value) {
    return jdbc.update(
            "update fm_driver d set d.name=?,d.mobile=?,d.id_card=?,d.driver_license=?,"
                + "d.auth_status=1,d.online_status=0,d.update_time=? "
                + "where d.id=? and d.deleted=0 and d.status=1 and d.online_status<>2 "
                + "and not exists(select 1 from fm_orders o where o.driver_id=d.id "
                + "and o.status in (2,3) and o.deleted=0)",
            value.getName().trim(),
            value.getMobile().trim(),
            value.getIdCard().trim().toUpperCase(),
            value.getDriverLicense().trim(),
            LocalDateTime.now(),
            driverId)
        == 1;
  }

  @Transactional
  public boolean submitVehicle(long driverId, Driver value) {
    return jdbc.update(
            "update fm_driver d set d.plate_number=?,d.vehicle_type=?,d.vehicle_license=?,"
                + "d.auth_status=1,d.online_status=0,d.update_time=? "
                + "where d.id=? and d.deleted=0 and d.status=1 and d.online_status<>2 "
                + "and not exists(select 1 from fm_orders o where o.driver_id=d.id "
                + "and o.status in (2,3) and o.deleted=0)",
            value.getPlateNumber().trim().toUpperCase(),
            value.getVehicleType().trim(),
            value.getVehicleLicense().trim(),
            LocalDateTime.now(),
            driverId)
        == 1;
  }
}
