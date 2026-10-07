package com.feimao.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.feimao.entity.BusinessUser;
import com.feimao.entity.BusinessUserToken;
import com.feimao.mapper.BusinessUserTokenMapper;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserTokenService {
  private static final String USER_TOKEN_PREFIX = "1111111111";
  private final BusinessUserTokenMapper tokens;
  private final BusinessUserService users;

  public UserTokenService(BusinessUserTokenMapper tokens, BusinessUserService users) {
    this.tokens = tokens;
    this.users = users;
  }

  public Long userId(String token) {
    if (token == null || token.trim().isEmpty()) return null;
    long now = System.currentTimeMillis() / 1000;
    BusinessUserToken session =
        tokens.selectOne(
            new LambdaQueryWrapper<BusinessUserToken>()
                .eq(BusinessUserToken::getToken, token)
                .gt(BusinessUserToken::getExpiretime, now));
    if (session == null) return null;
    Long userId = decodeUserId(session.getUserId());
    BusinessUser user = userId == null ? null : users.getById(userId);
    return user != null && "normal".equals(user.getStatus()) ? user.getId() : null;
  }

  public BusinessUserToken activeToken(String token) {
    if (token == null || token.trim().isEmpty()) return null;
    return tokens.selectOne(
        new LambdaQueryWrapper<BusinessUserToken>()
            .eq(BusinessUserToken::getToken, token)
            .gt(BusinessUserToken::getExpiretime, System.currentTimeMillis() / 1000));
  }

  @Transactional
  public String issue(long userId) {
    return create(userId, 31622400L);
  }

  /** The original user login keeps existing sessions and issues a 366-day UUID token. */
  @Transactional
  public String issueLogin(long userId) {
    return create(userId, 31622400L);
  }

  /** The original refresh action revokes only the presented token and creates a 30-day token. */
  @Transactional
  public String refresh(String oldToken, long userId) {
    revoke(oldToken);
    return create(userId, 2592000L);
  }

  private String create(long userId, long lifetime) {
    String token = UUID.randomUUID().toString();
    long now = System.currentTimeMillis() / 1000;
    BusinessUserToken session = new BusinessUserToken();
    session.setToken(token);
    // The legacy Auth library stores the role prefix together with the real user id.
    session.setUserId(Long.parseLong(USER_TOKEN_PREFIX + userId));
    session.setCreatetime(now);
    session.setExpiretime(now + lifetime);
    tokens.insert(session);
    return token;
  }

  public void revoke(String token) {
    if (token != null && !token.isEmpty())
      tokens.delete(
          new LambdaQueryWrapper<BusinessUserToken>().eq(BusinessUserToken::getToken, token));
  }

  public void revokeUser(long userId) {
    tokens.delete(
        new LambdaQueryWrapper<BusinessUserToken>()
            .in(BusinessUserToken::getUserId, userId, Long.parseLong(USER_TOKEN_PREFIX + userId)));
  }

  private static Long decodeUserId(Long storedId) {
    if (storedId == null) return null;
    String value = storedId.toString();
    if (value.startsWith(USER_TOKEN_PREFIX) && value.length() > USER_TOKEN_PREFIX.length()) {
      try {
        return Long.parseLong(value.substring(USER_TOKEN_PREFIX.length()));
      } catch (NumberFormatException ignored) {
        return null;
      }
    }
    // Keep accepting unprefixed rows created by earlier Spring migration builds.
    return storedId;
  }

}
