package com.feimao.service;

import com.feimao.mapper.AdminPermissionMapper;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class AdminPermissionService {
  private final AdminPermissionMapper permissions;

  public AdminPermissionService(AdminPermissionMapper permissions) {
    this.permissions = permissions;
  }

  public boolean allowed(long adminId, String ruleName) {
    if (adminId == 1) return true;
    List<String> groups = permissions.selectEnabledGroupRules(adminId);
    Set<Long> ruleIds = new HashSet<>();
    for (String value : groups) {
      if (value == null) continue;
      for (String item : value.split(",")) {
        try {
          ruleIds.add(Long.parseLong(item.trim()));
        } catch (NumberFormatException ignored) {
          // Ignore malformed IDs in historical permission data.
        }
      }
    }
    if (ruleIds.isEmpty()) return false;
    return permissions.countAllowedRule(ruleName, ruleIds) > 0;
  }
}
