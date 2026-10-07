package com.feimao;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.feimao.entity.BusinessUser;
import com.feimao.entity.Driver;
import com.feimao.entity.Staff;
import org.junit.jupiter.api.Test;

class SensitiveEntitySerializationTest {
  private final ObjectMapper json = new ObjectMapper();

  @Test
  void driverResponseDoesNotExposePasswordOrOpenid() throws Exception {
    Driver driver = new Driver();
    driver.setName("司机");
    driver.setPassword("password-hash");
    driver.setOpenid("wx-openid");
    String value = json.writeValueAsString(driver);
    assertTrue(value.contains("司机"));
    assertFalse(value.contains("password-hash"));
    assertFalse(value.contains("wx-openid"));
    assertFalse(value.contains("\"password\""));
    assertFalse(value.contains("\"openid\""));
  }

  @Test
  void staffResponseDoesNotExposePassword() throws Exception {
    Staff staff = new Staff();
    staff.setName("员工");
    staff.setPassword("password-hash");
    String value = json.writeValueAsString(staff);
    assertTrue(value.contains("员工"));
    assertFalse(value.contains("password-hash"));
    assertFalse(value.contains("\"password\""));
  }

  @Test
  void migratedUserResponseDoesNotExposePasswordOrOpenid() throws Exception {
    BusinessUser user = new BusinessUser();
    user.setUsername("member");
    user.setPassword("legacy-hash");
    user.setOpenid("legacy-openid");
    String value = json.writeValueAsString(user);
    assertTrue(value.contains("member"));
    assertFalse(value.contains("legacy-hash"));
    assertFalse(value.contains("legacy-openid"));
  }
}
