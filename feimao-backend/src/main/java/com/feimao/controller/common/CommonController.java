package com.feimao.controller.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feimao.common.BusinessResponse;
import com.feimao.entity.BusinessAttachment;
import com.feimao.mapper.PublicConfigMapper;
import com.feimao.service.AdminSessionService;
import com.feimao.service.BusinessAttachmentService;
import com.feimao.service.RoleSessionService;
import com.feimao.service.UserTokenService;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/common")
@CrossOrigin
public class CommonController {
  private static final long DEFAULT_DISTRICT_ID = 3L;
  private static final Set<String> ALLOWED =
      new HashSet<>(
          Arrays.asList(
              "jpg", "png", "bmp", "jpeg", "gif", "webp", "zip", "rar", "wav", "mp4",
              "mp3", "webm"));
  private static final Set<String> IMAGE_TYPES =
      new HashSet<>(Arrays.asList("jpg", "jpeg", "png", "bmp", "gif", "webp"));
  private static final Set<String> IMAGE_MIME_TYPES =
      new HashSet<>(
          Arrays.asList(
              "image/gif", "image/jpg", "image/jpeg", "image/bmp", "image/png", "image/webp"));
  private final Path root;
  private final PublicConfigMapper configs;
  private final ObjectMapper objectMapper;
  private final UserTokenService users;
  private final AdminSessionService admins;
  private final RoleSessionService roles;
  private final BusinessAttachmentService attachments;

  public CommonController(
      @Value("${business.upload-dir:./uploads}") String uploadDir,
      PublicConfigMapper configs,
      ObjectMapper objectMapper,
      UserTokenService users,
      AdminSessionService admins,
      RoleSessionService roles,
      BusinessAttachmentService attachments) {
    this.root = Paths.get(uploadDir).toAbsolutePath().normalize();
    this.configs = configs;
    this.objectMapper = objectMapper;
    this.users = users;
    this.admins = admins;
    this.roles = roles;
    this.attachments = attachments;
  }

  @RequestMapping(value = "/init", method = {RequestMethod.GET, RequestMethod.POST})
  public BusinessResponse<?> init(
      @RequestParam(value = "version", required = false) String formVersion,
      @RequestParam(value = "lng", required = false) String formLng,
      @RequestParam(value = "lat", required = false) String formLat,
      @RequestBody(required = false) Map<String, Object> body,
      HttpServletRequest request) {
    String version =
        formVersion != null
            ? formVersion
            : body == null || body.get("version") == null ? "" : body.get("version").toString();
    if (version.isEmpty() || "0".equals(version)) return BusinessResponse.error("未知参数");
    String lng = requestValue(formLng, body, "lng");
    String lat = requestValue(formLat, body, "lat");
    Map<String, Object> city = nearestCity(lng, lat);
    Map<String, Object> versionData = upgrade(version);
    String base =
        request.getScheme()
            + "://"
            + request.getServerName()
            + ((request.getServerPort() == 80 || request.getServerPort() == 443)
                ? ""
                : ":" + request.getServerPort());
    Map<String, Object> upload = new LinkedHashMap<>();
    upload.put("cdnurl", base);
    upload.put("uploadurl", base + "/api/common/upload");
    upload.put("bucket", "local");
    upload.put("maxsize", "10mb");
    upload.put("mimetype", "jpg,png,bmp,jpeg,gif,webp,zip,rar,wav,mp4,mp3,webm");
    upload.put("chunking", false);
    upload.put("chunksize", 2097152);
    upload.put("savekey", "/uploads/{year}{mon}{day}/{filemd5}{.suffix}");
    upload.put("multipart", Collections.emptyList());
    upload.put("multiple", false);
    upload.put("fullmode", false);
    upload.put("thumbstyle", "");
    upload.put("storage", "local");
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("citydata", city);
    data.put("versiondata", versionData);
    data.put("uploaddata", upload);
    data.put("coverdata", null);
    return BusinessResponse.success("", data);
  }

  private String requestValue(String formValue, Map<String, Object> body, String key) {
    if (formValue != null) return formValue.trim();
    Object value = body == null ? null : body.get(key);
    return value == null ? "" : value.toString().trim();
  }

  private Map<String, Object> nearestCity(String lng, String lat) {
    BigDecimal longitude = coordinate(lng, -180, 180);
    BigDecimal latitude = coordinate(lat, -90, 90);
    Map<String, Object> city = null;
    if (longitude != null && latitude != null) {
      city = configs.selectNearestCityWithinRange(longitude, latitude);
    }
    return city != null ? city : configs.selectCityByDistrictId(DEFAULT_DISTRICT_ID);
  }

  private BigDecimal coordinate(String value, int minimum, int maximum) {
    if (value == null || value.isEmpty()) return null;
    try {
      BigDecimal coordinate = new BigDecimal(value);
      if (coordinate.compareTo(BigDecimal.valueOf(minimum)) < 0
          || coordinate.compareTo(BigDecimal.valueOf(maximum)) > 0) return null;
      return coordinate;
    } catch (NumberFormatException exception) {
      return null;
    }
  }

  private Map<String, Object> upgrade(String version) {
    for (Map<String, Object> candidate : configs.selectUpgrades()) {
      String newVersion = Objects.toString(candidate.get("newversion"), "");
      String oldVersion = Objects.toString(candidate.get("oldversion"), "");
      if (newVersion.equals(version) || !matchesVersion(version, oldVersion)) continue;
      Map<String, Object> result = new LinkedHashMap<>();
      result.put("enforce", candidate.get("enforce"));
      result.put("version", version);
      result.put("newversion", newVersion);
      result.put("downloadurl", candidate.get("downloadurl"));
      result.put("packagesize", candidate.get("packagesize"));
      String text = Objects.toString(candidate.get("content"), "");
      String url = Objects.toString(candidate.get("downloadurl"), "");
      result.put(
          "upgradetext",
          text.replace("{version}", version)
              .replace("{newversion}", newVersion)
              .replace("{downloadurl}", url)
              .replace("{url}", url)
              .replace("{packagesize}", Objects.toString(candidate.get("packagesize"), "")));
      return result;
    }
    return null;
  }

  static boolean matchesVersion(String version, String rule) {
    if ("*".equals(rule) || version.equals(rule)) return true;
    String[] actual = version.split("\\.", -1);
    String[] expected = rule.split("\\.", -1);
    if (actual.length != expected.length) return false;
    for (int i = 0; i < actual.length; i++) {
      if (!versionPartMatches(actual[i], expected[i])) return false;
    }
    return true;
  }

  private static boolean versionPartMatches(String actual, String rule) {
    for (String allowed : rule.split(",")) {
      if ("*".equals(allowed) || actual.equals(allowed)) return true;
      int dash = allowed.indexOf('-');
      if (dash < 0) continue;
      try {
        int value = Integer.parseInt(actual);
        if (value >= Integer.parseInt(allowed.substring(0, dash))
            && value <= Integer.parseInt(allowed.substring(dash + 1))) return true;
      } catch (NumberFormatException ignored) {
        // 与旧版本规则相同：无法解析的范围不匹配。
      }
    }
    return false;
  }

  @PostMapping("/upload")
  public BusinessResponse<?> upload(
      @RequestParam(value = "file", required = false) MultipartFile file,
      @RequestHeader(value = "token", required = false) String headerToken,
      @RequestParam(value = "token", required = false) String formToken,
      @RequestParam(value = "category", required = false, defaultValue = "") String category,
      @RequestParam(value = "chunkid", required = false) String chunkId,
      HttpServletRequest request) {
    String token = headerToken == null || headerToken.trim().isEmpty() ? formToken : headerToken;
    // The original Common::upload endpoint is explicitly listed in noNeedLogin.
    if (phpTruthy(chunkId)) return BusinessResponse.error("未开启分片上传功能");
    if (file == null)
      return BusinessResponse.error("未上传文件或超出服务器上传限制");
    String original = Optional.ofNullable(file.getOriginalFilename()).orElse("");
    int dot = original.lastIndexOf('.');
    String ext = dot < 0 ? "" : original.substring(dot + 1).toLowerCase(Locale.ROOT);
    String mimeType = Optional.ofNullable(file.getContentType()).orElse("");
    String checkedMimeType = mimeType.trim().toLowerCase(Locale.ROOT);
    if ("text/html".equals(checkedMimeType)
        || "text/x-php".equals(checkedMimeType)
        || ext.equals("php")
        || ext.equals("html")
        || ext.equals("htm")
        || ext.equals("phar")
        || ext.equals("phtml")
        || ext.startsWith("php")) return BusinessResponse.error("上传文件格式受限制");
    if (file.getSize() > 10L * 1024 * 1024)
      return BusinessResponse.error(fileTooLarge(file.getSize()));
    if (!ALLOWED.contains(ext) || !checkedMimeType.contains("/"))
      return BusinessResponse.error("上传文件格式受限制");
    byte[] content;
    try {
      content = file.getBytes();
    } catch (IOException exception) {
      return BusinessResponse.error("上传失败");
    }
    if (content.length > 10L * 1024 * 1024)
      return BusinessResponse.error(fileTooLarge(content.length));
    boolean imageCandidate = IMAGE_TYPES.contains(ext) || IMAGE_MIME_TYPES.contains(checkedMimeType);
    int[] imageSize = imageCandidate ? imageSize(content, ext) : null;
    if (imageCandidate && imageSize == null)
      return BusinessResponse.error("上传文件不是有效的图片文件");
    String day =
        LocalDate.now(ZoneId.of("Asia/Shanghai")).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    String name;
    try {
      name = digest(content, "MD5") + "." + ext;
    } catch (Exception exception) {
      return BusinessResponse.error("上传失败");
    }
    Path dir = root.resolve(day).normalize();
    Path target = dir.resolve(name).normalize();
    if (!target.startsWith(root)) return BusinessResponse.error("文件路径错误");
    String url = "/uploads/" + day + "/" + name;
    boolean existingAttachment;
    try {
      existingAttachment =
          attachments
                  .lambdaQuery()
                  .eq(BusinessAttachment::getUrl, url)
                  .eq(BusinessAttachment::getStorage, "local")
                  .count()
              > 0;
    } catch (Exception exception) {
      return BusinessResponse.error("上传记录校验失败");
    }
    boolean createdFile = false;
    try {
      Files.createDirectories(dir);
      if (!Files.exists(target)) {
        try {
          Files.write(target, content, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
          createdFile = true;
        } catch (FileAlreadyExistsException ignored) {
          // 并发上传相同内容时复用先写入的 MD5 文件。
        }
      }
    } catch (IOException e) {
      return BusinessResponse.error("上传失败");
    }
    Long userId = users.userId(token);
    Long adminId = admins.adminId(token);
    String width = imageSize == null ? "" : String.valueOf(imageSize[0]);
    String height = imageSize == null ? "" : String.valueOf(imageSize[1]);
    long now = System.currentTimeMillis() / 1000;
    try {
      if (existingAttachment) return uploadSuccess(request, url);
      BusinessAttachment attachment = new BusinessAttachment();
      attachment.setCategory(attachmentCategory(category));
      attachment.setAdminId(adminId == null ? 0L : adminId);
      attachment.setUserId(userId == null ? 0L : userId);
      attachment.setUrl(url);
      attachment.setImagewidth(width);
      attachment.setImageheight(height);
      attachment.setImagetype(ext);
      attachment.setImageframes(0);
      attachment.setFilename(legacyFilename(original));
      attachment.setFilesize((long) content.length);
      attachment.setMimetype(mimeType);
      attachment.setExtparam("");
      attachment.setCreatetime(now);
      attachment.setUpdatetime(now);
      attachment.setUploadtime(now);
      attachment.setStorage("local");
      attachment.setSha1(sha1(target));
      if (!attachments.save(attachment)) throw new IllegalStateException("附件记录保存失败");
    } catch (Exception e) {
      if (createdFile) {
        try {
          Files.deleteIfExists(target);
        } catch (IOException ignored) {
          // 数据库写入失败时只清理本次新建的文件。
        }
      }
      return BusinessResponse.error("上传记录保存失败");
    }
    return uploadSuccess(request, url);
  }

  private BusinessResponse<?> uploadSuccess(HttpServletRequest request, String url) {
    String full =
        request.getScheme()
            + "://"
            + request.getServerName()
            + ((request.getServerPort() == 80 || request.getServerPort() == 443)
                ? ""
                : ":" + request.getServerPort())
            + url;
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("url", url);
    data.put("fullurl", full);
    return BusinessResponse.success("上传成功", data);
  }

  private static boolean phpTruthy(String value) {
    return value != null && !value.isEmpty() && !"0".equals(value);
  }

  private static String fileTooLarge(long bytes) {
    String actual =
        BigDecimal.valueOf(bytes)
            .divide(BigDecimal.valueOf(1024L * 1024L), 2, RoundingMode.HALF_UP)
            .stripTrailingZeros()
            .toPlainString();
    return "当前上传(" + actual + "M)，最大允许上传文件大小:10M";
  }

  private boolean authenticated(String token) {
    if (token == null || token.trim().isEmpty()) return false;
    return users.userId(token) != null
        || admins.adminId(token) != null
        || roles.subject(token, "merchant") != null
        || roles.subject(token, "shop") != null;
  }

  private String attachmentCategory(String requested) {
    String category = requested == null ? "" : requested;
    if (category.isEmpty() || "unclassed".equals(category)) return "";
    try {
      String json = configs.selectAttachmentCategoryConfig();
      Map<String, Object> configured =
          objectMapper.readValue(
              json == null || json.trim().isEmpty() ? "{}" : json,
              new TypeReference<LinkedHashMap<String, Object>>() {});
      return configured.containsKey(category) ? category : "";
    } catch (Exception ignored) {
      return "";
    }
  }

  private String legacyFilename(String original) {
    String filename = original == null ? "" : original.replaceAll("(?s)<[^>]*>", "");
    filename = filename.replace('\\', '/');
    int slash = filename.lastIndexOf('/');
    if (slash >= 0) filename = filename.substring(slash + 1);
    filename =
        filename
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#039;");
    int characters = filename.codePointCount(0, filename.length());
    return characters <= 100
        ? filename
        : filename.substring(0, filename.offsetByCodePoints(0, 100));
  }

  private int[] imageSize(byte[] content, String ext) {
    try {
      BufferedImage image = ImageIO.read(new ByteArrayInputStream(content));
      if (image != null && image.getWidth() > 0 && image.getHeight() > 0)
        return new int[] {image.getWidth(), image.getHeight()};
    } catch (IOException ignored) {
      return null;
    }
    return "webp".equals(ext) ? webpSize(content) : null;
  }

  private int[] webpSize(byte[] value) {
    if (value.length < 30
        || !ascii(value, 0, "RIFF")
        || !ascii(value, 8, "WEBP")
        || littleEndian(value, 4, 4) + 8L > value.length) return null;
    int offset = 12;
    while (offset + 8 <= value.length) {
      long size = littleEndian(value, offset + 4, 4);
      long dataEnd = offset + 8L + size;
      if (size < 0 || dataEnd > value.length) return null;
      int data = offset + 8;
      int width = 0;
      int height = 0;
      if (ascii(value, offset, "VP8X") && size >= 10) {
        width = 1 + (int) littleEndian(value, data + 4, 3);
        height = 1 + (int) littleEndian(value, data + 7, 3);
      } else if (ascii(value, offset, "VP8L") && size >= 5 && (value[data] & 0xff) == 0x2f) {
        long bits = littleEndian(value, data + 1, 4);
        width = 1 + (int) (bits & 0x3fff);
        height = 1 + (int) ((bits >>> 14) & 0x3fff);
      } else if (ascii(value, offset, "VP8 ")
          && size >= 10
          && (value[data + 3] & 0xff) == 0x9d
          && (value[data + 4] & 0xff) == 0x01
          && (value[data + 5] & 0xff) == 0x2a) {
        width = (int) littleEndian(value, data + 6, 2) & 0x3fff;
        height = (int) littleEndian(value, data + 8, 2) & 0x3fff;
      }
      if (width > 0 && height > 0) return new int[] {width, height};
      long next = dataEnd + (size & 1L);
      if (next > Integer.MAX_VALUE || next <= offset) return null;
      offset = (int) next;
    }
    return null;
  }

  private boolean ascii(byte[] value, int offset, String expected) {
    if (offset < 0 || offset + expected.length() > value.length) return false;
    for (int index = 0; index < expected.length(); index++)
      if ((value[offset + index] & 0xff) != expected.charAt(index)) return false;
    return true;
  }

  private long littleEndian(byte[] value, int offset, int length) {
    if (offset < 0 || length < 0 || offset + length > value.length) return -1;
    long result = 0;
    for (int index = 0; index < length; index++)
      result |= (long) (value[offset + index] & 0xff) << (8 * index);
    return result;
  }

  private String digest(byte[] value, String algorithm) throws Exception {
    MessageDigest digest = MessageDigest.getInstance(algorithm);
    digest.update(value);
    return hex(digest.digest());
  }

  private String hex(byte[] value) {
    StringBuilder result = new StringBuilder(value.length * 2);
    for (byte item : value) result.append(String.format("%02x", item & 0xff));
    return result.toString();
  }

  private String sha1(Path file) throws Exception {
    MessageDigest digest = MessageDigest.getInstance("SHA-1");
    try (InputStream input = Files.newInputStream(file)) {
      byte[] buffer = new byte[8192];
      int count;
      while ((count = input.read(buffer)) >= 0) digest.update(buffer, 0, count);
    }
    return hex(digest.digest());
  }
}
