package com.feimao;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.feimao.service.ImageCaptchaService;
import org.junit.jupiter.api.Test;

class ImageCaptchaServiceTest {
  private final ImageCaptchaService captchas = new ImageCaptchaService();

  @Test
  void captchaIsPngAndRejectsInvalidOrReusedAnswers() {
    byte[] image = captchas.create("admin-login-test");
    assertTrue(image.length > 1000);
    assertTrue(image[0] == (byte) 0x89 && image[1] == 0x50 && image[2] == 0x4e && image[3] == 0x47);
    assertFalse(captchas.verify("admin-login-test", "WRONG"));
    assertFalse(captchas.verify("admin-login-test", "WRONG"));
    assertThrows(IllegalArgumentException.class, () -> captchas.create("../invalid"));
  }
}
