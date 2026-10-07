package com.feimao.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.feimao.entity.BusinessRoleSession;
import com.feimao.mapper.BusinessRoleSessionMapper;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleSessionService {
  private final BusinessRoleSessionMapper sessions;

  public RoleSessionService(BusinessRoleSessionMapper sessions) {
    this.sessions = sessions;
  }

  @Transactional
  public String issue(long id, String type) {
    String token = UUID.randomUUID().toString();
    long now = System.currentTimeMillis() / 1000;
    BusinessRoleSession session = new BusinessRoleSession();
    session.setToken(token);
    session.setSubjectId(id);
    session.setSubjectType(type);
    session.setCreateTime(now);
    // FastAdmin Auth::direct keeps existing sessions and uses a 366-day token lifetime.
    session.setExpireTime(now + 31622400L);
    sessions.insert(session);
    return token;
  }

  public Long subject(String token, String type) {
    if (token == null || token.isEmpty()) return null;
    long now = System.currentTimeMillis() / 1000;
    BusinessRoleSession session =
        sessions.selectOne(
            new LambdaQueryWrapper<BusinessRoleSession>()
                .select(BusinessRoleSession::getSubjectId)
                .eq(BusinessRoleSession::getToken, token)
                .eq(BusinessRoleSession::getSubjectType, type)
                .gt(BusinessRoleSession::getExpireTime, now));
    return session == null ? null : session.getSubjectId();
  }

  public void revoke(String token) {
    if (token != null && !token.isEmpty()) sessions.deleteById(token);
  }
}
