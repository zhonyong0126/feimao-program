package com.feimao.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

/** Read-only copy of the language dictionaries returned by the original FastAdmin Ajax::lang. */
@Service
public class LegacyLanguageCatalog {
  private static final Pattern VALID_CONTROLLER = Pattern.compile("^[a-zA-Z0-9_./]+$");
  private static final Pattern VALID_LANGUAGE = Pattern.compile("^[a-zA-Z_-]{2,10}$");
  private final Map<String, Object> catalog;

  public LegacyLanguageCatalog(ObjectMapper objectMapper) {
    try (InputStream stream =
        new ClassPathResource("legacy-lang/zh-cn.json").getInputStream()) {
      catalog =
          objectMapper.readValue(stream, new TypeReference<LinkedHashMap<String, Object>>() {});
    } catch (IOException exception) {
      throw new IllegalStateException("无法加载旧版语言包", exception);
    }
  }

  public Map<String, String> get(String module, String controllerName, String language) {
    String lang = language(language);
    // The old project only contains zh-cn language files. ThinkPHP returns an empty object when a
    // different valid language range is requested and no matching files exist.
    if (!"zh-cn".equals(lang)) return Collections.emptyMap();

    String actualModule = "index".equals(module) ? "index" : "admin";
    String controller = controller(controllerName);
    LinkedHashMap<String, String> result = new LinkedHashMap<>();
    prepend(result, controllerMap(actualModule, controller));
    prepend(result, stringMap(catalog.get(actualModule)));
    prepend(result, stringMap(catalog.get("framework")));
    return result;
  }

  private Map<String, String> controllerMap(String module, String controller) {
    Map<String, Object> controllers = objectMap(catalog.get(module + "Controllers"));
    return stringMap(controllers.get(controller));
  }

  private static void prepend(
      LinkedHashMap<String, String> destination, Map<String, String> source) {
    for (Map.Entry<String, String> entry : source.entrySet())
      destination.putIfAbsent(entry.getKey(), entry.getValue());
  }

  private static String controller(String source) {
    String parsed = parseName(source == null ? "" : source);
    if (!VALID_CONTROLLER.matcher(parsed).matches()) return "index";
    return parsed.replace('.', '/');
  }

  private static String parseName(String source) {
    String trimmed = source.trim();
    StringBuilder parsed = new StringBuilder(trimmed.length() + 4);
    for (int i = 0; i < trimmed.length(); i++) {
      char value = trimmed.charAt(i);
      if (value >= 'A' && value <= 'Z') parsed.append('_');
      parsed.append(Character.toLowerCase(value));
    }
    int start = 0;
    int end = parsed.length();
    while (start < end && parsed.charAt(start) == '_') start++;
    while (end > start && parsed.charAt(end - 1) == '_') end--;
    return parsed.substring(start, end);
  }

  private static String language(String source) {
    if (source == null || !VALID_LANGUAGE.matcher(source).matches()) return "zh-cn";
    return source.toLowerCase(java.util.Locale.ROOT);
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> objectMap(Object source) {
    if (!(source instanceof Map)) return Collections.emptyMap();
    return (Map<String, Object>) source;
  }

  private static Map<String, String> stringMap(Object source) {
    Map<String, Object> values = objectMap(source);
    if (values.isEmpty()) return Collections.emptyMap();
    LinkedHashMap<String, String> result = new LinkedHashMap<>();
    for (Map.Entry<String, Object> entry : values.entrySet())
      result.put(entry.getKey(), entry.getValue() == null ? "" : entry.getValue().toString());
    return result;
  }
}
