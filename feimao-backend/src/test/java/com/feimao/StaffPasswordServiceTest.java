package com.feimao;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.feimao.service.StaffPasswordService;
import org.junit.jupiter.api.Test;

class StaffPasswordServiceTest {
  private final StaffPasswordService passwords = new StaffPasswordService();

  @Test
  void newPasswordsUseBcrypt() {
    String encoded = passwords.encode("strong-password");
    assertTrue(encoded.startsWith("$2"));
    assertTrue(passwords.matches("strong-password", encoded));
    assertFalse(passwords.matches("wrong-password", encoded));
  }

  @Test
  void legacyMd5CanLoginForAutomaticUpgrade() {
    String legacy = "482c811da5d5b4bc6d497ffa98491e38";
    assertTrue(passwords.legacy(legacy));
    assertTrue(passwords.matches("password123", legacy));
    assertFalse(passwords.matches("wrong", legacy));
  }
}
