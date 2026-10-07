package com.feimao.controller.common;

import com.feimao.common.BusinessResponse;
import com.feimao.mapper.AjaxMapper;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.Adler32;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Compatibility endpoints used by the original FastAdmin category and upload widgets. */
@RestController
public class AjaxCompatibilityController {
  private static final long ICON_CACHE_SECONDS = 30L * 60 * 60 * 24;
  private static final Pattern HTML_TAG = Pattern.compile("<[^>]*>", Pattern.DOTALL);
  private static final DateTimeFormatter EXPIRES =
      DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
  private final AjaxMapper ajax;

  public AjaxCompatibilityController(AjaxMapper ajax) {
    this.ajax = ajax;
  }

  @GetMapping("/admin/ajax/category")
  public BusinessResponse<List<Map<String, Object>>> category(
      @RequestParam(required = false) String type,
      @RequestParam(required = false) String pid) {
    String filteredType = legacyFilter(type);
    if (!phpTruthy(filteredType)) filteredType = null;
    String filteredPid = legacyFilter(pid);
    if (filteredPid == null || filteredPid.isEmpty()) filteredPid = null;

    List<Map<String, Object>> result = new ArrayList<>();
    for (Map<String, Object> source : ajax.selectCategories(filteredType, filteredPid)) {
      Map<String, Object> item = new LinkedHashMap<>();
      item.put("value", source.get("value"));
      item.put("name", source.get("name"));
      result.add(item);
    }
    return BusinessResponse.success("", result);
  }

  @RequestMapping(
      value = {"/admin/ajax/icon", "/ajax/icon", "/index/ajax/icon"},
      method = {RequestMethod.GET, RequestMethod.POST})
  public ResponseEntity<byte[]> icon(@RequestParam(required = false) String suffix) {
    String filtered = legacyFilter(suffix);
    if (!phpTruthy(filtered)) filtered = "FILE";
    byte[] body = suffixSvg(filtered).getBytes(StandardCharsets.UTF_8);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.parseMediaType("image/svg+xml"));
    headers.set(HttpHeaders.CACHE_CONTROL, "public");
    headers.set(HttpHeaders.PRAGMA, "cache");
    headers.set(
        HttpHeaders.EXPIRES,
        EXPIRES.format(ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(ICON_CACHE_SECONDS)));
    return new ResponseEntity<>(body, headers, HttpStatus.OK);
  }

  private static String suffixSvg(String value) {
    String suffix = firstCodePoints(asciiUpper(value), 4);
    Adler32 checksum = new Adler32();
    checksum.update(suffix.getBytes(StandardCharsets.UTF_8));
    // PHP's unpack('L', hash(..., true)) reads the digest in native little-endian order.
    long total = Integer.toUnsignedLong(Integer.reverseBytes((int) checksum.getValue()));
    int[] rgb = hsvToRgb((total % 360) / 360D, 0.3D, 0.9D);
    String background = "rgb(" + rgb[0] + "," + rgb[1] + "," + rgb[2] + ")";
    return "        <svg version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" x=\"0px\" y=\"0px\" viewBox=\"0 0 512 512\" style=\"enable-background:new 0 0 512 512;\" xml:space=\"preserve\">\n"
        + "            <path style=\"fill:#E2E5E7;\" d=\"M128,0c-17.6,0-32,14.4-32,32v448c0,17.6,14.4,32,32,32h320c17.6,0,32-14.4,32-32V128L352,0H128z\"/>\n"
        + "            <path style=\"fill:#B0B7BD;\" d=\"M384,128h96L352,0v96C352,113.6,366.4,128,384,128z\"/>\n"
        + "            <polygon style=\"fill:#CAD1D8;\" points=\"480,224 384,128 480,128 \"/>\n"
        + "            <path style=\"fill:"
        + background
        + ";\" d=\"M416,416c0,8.8-7.2,16-16,16H48c-8.8,0-16-7.2-16-16V256c0-8.8,7.2-16,16-16h352c8.8,0,16,7.2,16,16 V416z\"/>\n"
        + "            <path style=\"fill:#CAD1D8;\" d=\"M400,432H96v16h304c8.8,0,16-7.2,16-16v-16C416,424.8,408.8,432,400,432z\"/>\n"
        + "            <g><text><tspan x=\"220\" y=\"380\" font-size=\"124\" font-family=\"Verdana, Helvetica, Arial, sans-serif\" fill=\"white\" text-anchor=\"middle\">"
        + suffix
        + "</tspan></text></g>\n"
        + "        </svg>";
  }

  private static int[] hsvToRgb(double h, double s, double v) {
    int sector = (int) Math.floor(h * 6);
    double fraction = h * 6 - sector;
    double p = v * (1 - s);
    double q = v * (1 - fraction * s);
    double t = v * (1 - (1 - fraction) * s);
    double r = 0;
    double g = 0;
    double b = 0;
    switch (sector % 6) {
      case 0:
        r = v;
        g = t;
        b = p;
        break;
      case 1:
        r = q;
        g = v;
        b = p;
        break;
      case 2:
        r = p;
        g = v;
        b = t;
        break;
      case 3:
        r = p;
        g = q;
        b = v;
        break;
      case 4:
        r = t;
        g = p;
        b = v;
        break;
      default:
        r = v;
        g = p;
        b = q;
        break;
    }
    return new int[] {
      (int) Math.floor(r * 255),
      (int) Math.floor(g * 255),
      (int) Math.floor(b * 255)
    };
  }

  private static String legacyFilter(String value) {
    if (value == null) return null;
    String filtered = HTML_TAG.matcher(value.trim()).replaceAll("");
    return filtered
        .replace("&", "&amp;")
        .replace("\"", "&quot;")
        .replace("<", "&lt;")
        .replace(">", "&gt;");
  }

  private static boolean phpTruthy(String value) {
    return value != null && !value.isEmpty() && !"0".equals(value);
  }

  private static String asciiUpper(String value) {
    StringBuilder result = new StringBuilder(value.length());
    for (int offset = 0; offset < value.length(); ) {
      int codePoint = value.codePointAt(offset);
      if (codePoint >= 'a' && codePoint <= 'z') codePoint -= 'a' - 'A';
      result.appendCodePoint(codePoint);
      offset += Character.charCount(value.codePointAt(offset));
    }
    return result.toString();
  }

  private static String firstCodePoints(String value, int count) {
    int length = value.codePointCount(0, value.length());
    return value.substring(0, value.offsetByCodePoints(0, Math.min(count, length)));
  }
}
