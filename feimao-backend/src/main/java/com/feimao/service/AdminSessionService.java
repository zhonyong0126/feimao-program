package com.feimao.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.feimao.entity.AdminSession;
import com.feimao.mapper.AdminSessionMapper;
import java.security.SecureRandom;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminSessionService {
  private static final char[] CHARS =
      "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
  private final AdminSessionMapper sessions;
  private final SecureRandom random = new SecureRandom();

  public AdminSessionService(AdminSessionMapper sessions) {
    this.sessions = sessions;
  }

  @Transactional
  public String issue(long adminId) {
    String token = random(48);
    long now = System.currentTimeMillis() / 1000;
    sessions.delete(
        new LambdaQueryWrapper<AdminSession>().eq(AdminSession::getAdminId, adminId));
    AdminSession session = new AdminSession();
    session.setToken(token);
    session.setAdminId(adminId);
    session.setCreateTime(now);
    session.setExpireTime(now + 8 * 3600);
    sessions.insert(session);
    return token;
  }

  public Long adminId(String token) {
    if (token == null || token.isEmpty()) return null;
    long now = System.currentTimeMillis() / 1000;
    return sessions.selectActiveAdminId(token, now);
  }

  public void revoke(String token) {
    if (token != null && !token.isEmpty()) sessions.deleteById(token);
  }

  private String random(int n) {
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < n; i++) b.append(CHARS[random.nextInt(CHARS.length)]);
    return b.toString();
  }
}
