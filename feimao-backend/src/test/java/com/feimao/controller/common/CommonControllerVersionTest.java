package com.feimao.controller.common;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CommonControllerVersionTest {
  @Test
  void matchesFastAdminVersionRules() {
    assertTrue(CommonController.matchesVersion("1.2.3", "*"));
    assertTrue(CommonController.matchesVersion("1.2.3", "1.2.3"));
    assertTrue(CommonController.matchesVersion("1.2.3", "1.1-3.*"));
    assertTrue(CommonController.matchesVersion("1.2.3", "1.1,2,4.3"));
    assertFalse(CommonController.matchesVersion("1.2.3", "1.4-6.*"));
    assertFalse(CommonController.matchesVersion("1.2.3", "1.2"));
  }
}
