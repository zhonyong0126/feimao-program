package com.feimao.service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Spring equivalents of the cache operations performed by the original Ajax::wipecache action. */
@Service
public class LegacyAjaxMaintenanceService {
  private static final Pattern VERSION = Pattern.compile("(.*)\\.([0-9]+)$");
  private final JdbcTemplate jdbc;
  private final ObjectProvider<CacheManager> cacheManagers;
  private final boolean appDebug;

  public LegacyAjaxMaintenanceService(
      JdbcTemplate jdbc,
      ObjectProvider<CacheManager> cacheManagers,
      @Value("${legacy.app-debug:true}") boolean appDebug) {
    this.jdbc = jdbc;
    this.cacheManagers = cacheManagers;
    this.appDebug = appDebug;
  }

  @Transactional
  public void wipe(String requestedType) {
    String type = requestedType == null ? "" : requestedType;
    switch (type) {
      case "all":
        clearContentCaches();
        refreshBrowserVersion();
        break;
      case "content":
        clearContentCaches();
        break;
      case "template":
      case "addons":
        // FreeMarker templates and Spring components are not compiled into writable runtime cache
        // folders, so the old TEMP_PATH/Service::refresh operations have no mutable equivalent.
        break;
      case "browser":
        refreshBrowserVersion();
        break;
      default:
        // The old switch also succeeds without doing anything for an absent or unknown type.
        break;
    }
  }

  private void clearContentCaches() {
    CacheManager manager = cacheManagers.getIfAvailable();
    if (manager == null) return;
    for (String name : manager.getCacheNames()) {
      Cache cache = manager.getCache(name);
      if (cache != null) cache.clear();
    }
  }

  private void refreshBrowserVersion() {
    // The source deployment has app.debug=true. This branch deliberately mirrors FastAdmin's
    // production-only version bump and can be enabled with legacy.app-debug=false.
    if (appDebug) return;
    List<String> values =
        jdbc.query(
            "select value from ymcx_config where name='version' limit 1",
            (result, row) -> result.getString(1));
    if (values.isEmpty()) return;
    String oldVersion = values.get(0);
    if (oldVersion == null) return;
    Matcher match = VERSION.matcher(oldVersion);
    if (!match.matches()) return;
    long last;
    try {
      last = Long.parseLong(match.group(2));
    } catch (NumberFormatException ignored) {
      return;
    }
    String newVersion = match.group(1) + "." + (last + 1);
    if (!newVersion.equals(oldVersion))
      jdbc.update("update ymcx_config set value=? where name='version'", newVersion);
  }
}
