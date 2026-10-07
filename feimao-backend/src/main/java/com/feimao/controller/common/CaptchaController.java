package com.feimao.controller.common;

import com.feimao.service.ImageCaptchaService;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/common/captcha")
public class CaptchaController {
  private static final String DEFAULT_ID = "default";
  private final ImageCaptchaService captchas;

  public CaptchaController(ImageCaptchaService captchas) {
    this.captchas = captchas;
  }

  @GetMapping(value = {"", "/"}, produces = MediaType.IMAGE_PNG_VALUE)
  public ResponseEntity<byte[]> image() {
    return image(DEFAULT_ID);
  }

  @GetMapping(value = "/{id}", produces = MediaType.IMAGE_PNG_VALUE)
  public ResponseEntity<byte[]> image(@PathVariable String id) {
    return ResponseEntity.ok()
        .cacheControl(CacheControl.noStore())
        .header(HttpHeaders.PRAGMA, "no-cache")
        .contentType(MediaType.IMAGE_PNG)
        .body(captchas.create(id));
  }
}
