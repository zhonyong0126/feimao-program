package com.feimao.controller.user;

import com.feimao.common.BusinessResponse;
import com.feimao.entity.BusinessUserToken;
import com.feimao.service.UserTokenService;
import java.util.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/token")
@CrossOrigin
public class TokenController {
  private final UserTokenService tokens;

  public TokenController(UserTokenService tokens) {
    this.tokens = tokens;
  }

  @PostMapping("/check")
  public BusinessResponse<?> check(@RequestHeader(value = "token", required = false) String token) {
    Long id = tokens.userId(token);
    if (id == null) return BusinessResponse.error("Token已过期");
    BusinessUserToken session = tokens.activeToken(token);
    Map<String, Object> row = new LinkedHashMap<>();
    // The legacy token driver returns the plaintext request token even when persistence hashes it.
    row.put("token", token);
    row.put(
        "expires_in",
        Math.max(0, session.getExpiretime() - System.currentTimeMillis() / 1000));
    return BusinessResponse.success("", row);
  }

  @PostMapping("/refresh")
  @Transactional
  public BusinessResponse<?> refresh(@RequestHeader(value = "token", required = false) String token) {
    Long id = tokens.userId(token);
    if (id == null) return BusinessResponse.error("Token已过期");
    String fresh = tokens.refresh(token, id);
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("token", fresh);
    data.put("expires_in", 2592000L);
    return BusinessResponse.success("", data);
  }
}
