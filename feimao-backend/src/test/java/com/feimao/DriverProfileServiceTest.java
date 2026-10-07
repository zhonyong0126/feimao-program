package com.feimao;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.feimao.entity.Driver;
import com.feimao.service.DriverProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
class DriverProfileServiceTest {
  @Mock JdbcTemplate jdbc;
  private DriverProfileService profiles;

  @BeforeEach
  void setUp() {
    profiles = new DriverProfileService(jdbc);
  }

  @Test
  void authenticationSubmissionRequiresNoActiveOrderAndResetsOnlineState() {
    Driver value = auth();
    when(jdbc.update(any(String.class), eq("张三"), eq("138" + "0013" + "8000"), eq("ID"),
            eq("DL"), any(), eq(9L)))
        .thenReturn(1);
    assertTrue(profiles.submitAuthentication(9L, value));
    verify(jdbc).update(
        org.mockito.ArgumentMatchers.contains("not exists"), eq("张三"), eq("138" + "0013" + "8000"),
        eq("ID"), eq("DL"), any(), eq(9L));
  }

  @Test
  void activeOrderBlocksAuthenticationChanges() {
    when(jdbc.update(any(String.class), any(), any(), any(), any(), any(), eq(9L))).thenReturn(0);
    assertFalse(profiles.submitAuthentication(9L, auth()));
  }

  @Test
  void vehicleChangeReturnsDriverToReview() {
    Driver value = new Driver();
    value.setPlateNumber("粤b12345");
    value.setVehicleType("厢式货车");
    value.setVehicleLicense("VL");
    when(jdbc.update(any(String.class), eq("粤B12345"), eq("厢式货车"), eq("VL"), any(), eq(9L)))
        .thenReturn(1);
    assertTrue(profiles.submitVehicle(9L, value));
    verify(jdbc).update(
        org.mockito.ArgumentMatchers.contains("auth_status=1"), eq("粤B12345"),
        eq("厢式货车"), eq("VL"), any(), eq(9L));
  }

  private Driver auth() {
    Driver value = new Driver();
    value.setName("张三");
    value.setMobile("138" + "0013" + "8000");
    value.setIdCard("id");
    value.setDriverLicense("DL");
    return value;
  }
}
