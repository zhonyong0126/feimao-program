package com.feimao.service;

import java.nio.charset.StandardCharsets;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

@Service
public class StaffPasswordService {
  private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

  public String encode(String password) {
    return bcrypt.encode(password);
  }

  public boolean matches(String password, String stored) {
    if (password == null || stored == null) return false;
    if (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$"))
      return bcrypt.matches(password, stored);
    return stored.equals(DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8)));
  }

  public boolean legacy(String stored) {
    return stored != null && stored.matches("^[a-fA-F0-9]{32}$");
  }
}
