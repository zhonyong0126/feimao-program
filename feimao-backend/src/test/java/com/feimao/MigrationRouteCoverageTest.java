package com.feimao;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class MigrationRouteCoverageTest {
  private static final Pattern USER_API = Pattern.compile("['\"](/api/[A-Za-z0-9_./-]+)");
  private static final Pattern ROLE_API =
      Pattern.compile("url\\s*:\\s*[`'\"](/(?:driver|staff)/[^`'\"]*)");
  private static final Pattern ADMIN_LINK = Pattern.compile("href=\"(/admin/[^\"?]*)");
  private static final Pattern ADMIN_API =
      Pattern.compile("['\"](/api/admin/[A-Za-z0-9_./?=&-]+)");
  private static final Pattern PAGE_PATH = Pattern.compile("\"path\"\\s*:\\s*\"([^\"]+)\"");

  @Autowired RequestMappingHandlerMapping mappings;

  @Test
  void frontendRequestsAndAdminLinksHaveSpringRoutes() throws IOException {
    Path project = projectRoot();
    Set<String> routes = new LinkedHashSet<>();
    mappings.getHandlerMethods().keySet().forEach(info -> routes.addAll(info.getPatternValues()));

    Set<String> userRequests = scan(project.resolve("feimao-user"), USER_API, ".vue", ".js");
    Set<String> roleRequests = new LinkedHashSet<>();
    roleRequests.addAll(
        scan(project.resolve("feimao-driver/api"), ROLE_API, ".js"));
    roleRequests.addAll(
        scan(project.resolve("feimao-staff/api"), ROLE_API, ".js"));
    Set<String> adminLinks =
        scan(
            project.resolve("feimao-backend/src/main/resources/templates/admin"),
            ADMIN_LINK,
            ".ftl");
    Set<String> adminRequests = new LinkedHashSet<>();
    adminRequests.addAll(
        scan(
            project.resolve("feimao-backend/src/main/resources/templates/admin"),
            ADMIN_API,
            ".ftl"));
    adminRequests.addAll(
        scan(
            project.resolve("feimao-backend/src/main/resources/static/admin/js"),
            ADMIN_API,
            ".js"));
    assertTrue(!userRequests.isEmpty(), "未扫描到小程序API请求");
    assertTrue(!roleRequests.isEmpty(), "未扫描到司机/员工API请求");
    assertTrue(!adminLinks.isEmpty(), "未扫描到后台页面链接");
    assertTrue(!adminRequests.isEmpty(), "未扫描到后台API请求");
    for (String path : userRequests)
      assertTrue(matches(routes, path), "小程序请求没有对应Spring路由: " + path);
    for (String path : roleRequests) {
      String requestPath = "/api" + path.replaceAll("\\$\\{[^}]+}", "1");
      assertTrue(matches(routes, requestPath), "司机/员工请求没有对应Spring路由: " + requestPath);
    }
    for (String path : adminLinks)
      assertTrue(matches(routes, path), "后台链接没有对应Spring路由: " + path);
    for (String path : adminRequests)
      assertTrue(matchesFrontendApi(routes, path), "后台请求没有对应Spring路由: " + path);
  }

  @Test
  void everyRegisteredDriverAndStaffPageExists() throws IOException {
    Path project = projectRoot();
    assertRegisteredPagesExist(project.resolve("feimao-driver"));
    assertRegisteredPagesExist(project.resolve("feimao-staff"));
  }

  @Test
  void adminDeliveryTemplatesDoNotUseRemovedEntityFields() throws IOException {
    Path templates =
        projectRoot().resolve("feimao-backend/src/main/resources/templates/admin");
    StringBuilder source = new StringBuilder();
    try (Stream<Path> files = Files.walk(templates)) {
      files
          .filter(Files::isRegularFile)
          .filter(path -> path.toString().endsWith(".ftl"))
          .filter(path -> !path.toString().endsWith("driver/withdrawals.ftl"))
          .forEach(
              path -> {
                try {
                  source.append(new String(Files.readAllBytes(path), StandardCharsets.UTF_8));
                } catch (IOException exception) {
                  throw new IllegalStateException("读取后台模板失败: " + path, exception);
                }
              });
    }
    for (String removedField :
        new String[] {
          ".carNumber", ".carType", ".carLoad", ".carLength", ".weight", ".amount",
          ".startContact", ".endContact"
        }) {
      assertFalse(source.indexOf(removedField) >= 0, "后台模板仍引用已移除字段: " + removedField);
    }
  }

  @Test
  void driverAndStaffClientsHandleHttpUnauthorizedResponses() throws IOException {
    Path project = projectRoot();
    for (String app : new String[] {"feimao-driver", "feimao-staff"}) {
      String source =
          new String(
              Files.readAllBytes(project.resolve(app).resolve("api/index.js")),
              StandardCharsets.UTF_8);
      assertTrue(source.contains("res.statusCode === 401"), app + " 未处理HTTP 401");
      assertTrue(source.contains("removeStorageSync('userInfo')"), app + " 未清理失效账号资料");
      assertTrue(source.contains("reLaunch({ url: '/pages/my/index' })"), app + " 未返回登录页");
    }
  }

  @Test
  void anxinMenuKeepsOriginalVisibleNamesAndSeparatesFinanceEntries() throws IOException {
    String menu =
        new String(
            Files.readAllBytes(
                projectRoot().resolve("feimao-backend/src/main/resources/templates/admin/layout.ftl")),
            StandardCharsets.UTF_8);
    for (String name :
        new String[] {
          "控制台", "门店管理", "品牌管理", "平台车辆管理", "门店车辆管理", "订单列表",
          "酒店管理", "财务管理", "门店提现订单", "酒店提现订单", "平台流水管理",
          "平台收益管理", "订单乘坐人管理", "常规管理", "系统配置", "附件管理",
          "个人资料", "权限管理", "管理员管理", "管理员日志", "角色组", "菜单规则",
          "用户管理", "会员管理", "轮播图管理"
        }) {
      assertTrue(menu.contains(">" + name + "<"), "安鑫后台缺少旧菜单名称: " + name);
    }
    for (String hidden :
        new String[] {
          "商户审核", "门店审核", "维修项目", "门店维修项目", "订单分期", "用户优惠券",
          "门店资金流水", "酒店资金流水", "会员组", "会员权限", "用户地址", "实名认证",
          "员工认证", "签到记录", "优惠券", "分类管理", "故障上报"
        }) {
      assertFalse(menu.contains(">" + hidden + "<"), "原权限表隐藏的功能不应出现在菜单: " + hidden);
    }
    assertFalse(menu.contains("平台流水与收益"), "旧菜单被错误合并为平台流水与收益");
  }

  @Test
  void adminApiHelperLoadsBeforeNestedPageScripts() throws IOException {
    String layout =
        new String(
            Files.readAllBytes(
                projectRoot().resolve("feimao-backend/src/main/resources/templates/admin/layout.ftl")),
            StandardCharsets.UTF_8);
    int helper = layout.indexOf("function api(url, method, data)");
    int nested = layout.indexOf("<#nested>");
    assertTrue(helper >= 0, "后台缺少公共API请求方法");
    assertTrue(helper < nested, "公共API请求方法必须先于页面业务脚本加载");
  }

  @Test
  void resourceListUsesFirstLegacyImageInsteadOfCommaJoinedUrl() throws IOException {
    String resourceScript =
        new String(
            Files.readAllBytes(
                projectRoot().resolve("feimao-backend/src/main/resources/static/admin/js/resource.js")),
            StandardCharsets.UTF_8);
    assertTrue(
        resourceScript.contains("String(value).split(',')"),
        "平台车辆等旧多图字段必须拆分后再作为缩略图地址");
  }

  @Test
  void profilePageKeepsLegacyReadonlyMobileAndLogColumns() throws IOException {
    Path resources = projectRoot().resolve("feimao-backend/src/main/resources");
    String template =
        new String(
            Files.readAllBytes(resources.resolve("templates/admin/profile/index.ftl")),
            StandardCharsets.UTF_8);
    assertTrue(
        Pattern.compile("name=\\\"mobile\\\"[^>]*disabled").matcher(template).find(),
        "个人资料手机号必须保持旧后台只读语义");
    for (String heading : new String[] {"编号", "标题", "请求地址", "网络地址", "创建时间"})
      assertTrue(template.contains("<th>" + heading + "</th>"), "个人日志缺少旧字段: " + heading);
    assertFalse(template.contains("<th>内容</th>"), "个人日志不应展示旧页面没有的请求内容列");
  }

  @Test
  void systemConfigPageUsesLegacyGroupsAndTypedControls() throws IOException {
    Path resources = projectRoot().resolve("feimao-backend/src/main/resources");
    String template =
        new String(
            Files.readAllBytes(resources.resolve("templates/admin/system/list.ftl")),
            StandardCharsets.UTF_8);
    String script =
        new String(
            Files.readAllBytes(resources.resolve("static/admin/js/system.js")),
            StandardCharsets.UTF_8);
    assertTrue(template.contains("id=\"configGroupTabs\""), "系统配置缺少旧版分组标签");
    assertTrue(template.contains("id=\"resetConfig\""), "系统配置缺少旧版重置操作");
    assertTrue(template.contains("id=\"saveConfig\""), "系统配置缺少旧版整组保存操作");
    for (String type : new String[] {"editor", "number", "image", "select", "radio", "checkbox"})
      assertTrue(script.contains("type === '" + type + "'"), "系统配置缺少类型控件: " + type);
    assertFalse(script.contains("testEmailReceiver"), "旧页面已隐藏邮件组，不应在配置页暴露测试入口");
  }

  @Test
  void hiddenLegacyFunctionsAreNotReintroducedThroughPageTabs() throws IOException {
    Path templates = projectRoot().resolve("feimao-backend/src/main/resources/templates/admin");
    String system =
        new String(
            Files.readAllBytes(templates.resolve("system/list.ftl")), StandardCharsets.UTF_8);
    for (String hidden : new String[] {"地址", "实名认证", "员工认证", "签到"})
      assertFalse(system.contains(">" + hidden + "<"), "系统页错误暴露旧菜单隐藏功能: " + hidden);
    String access =
        new String(
            Files.readAllBytes(templates.resolve("access/list.ftl")), StandardCharsets.UTF_8);
    for (String hidden : new String[] {"会员组", "会员权限"})
      assertFalse(access.contains(">" + hidden + "<"), "权限页错误暴露旧菜单隐藏功能: " + hidden);
    String fund =
        new String(
            Files.readAllBytes(templates.resolve("fund/list.ftl")), StandardCharsets.UTF_8);
    for (String hidden : new String[] {"商户流水", "门店流水"})
      assertFalse(fund.contains(">" + hidden + "<"), "资金页错误暴露旧菜单隐藏功能: " + hidden);
  }

  @Test
  void everyGenericAdminDetailUsesChineseFieldLabels() throws IOException {
    Path admin = projectRoot().resolve("feimao-backend/src/main/resources");
    for (String script :
        new String[] {"order-data.js", "fund.js", "system.js", "access.js", "partner.js"}) {
      String source =
          new String(
              Files.readAllBytes(admin.resolve("static/admin/js").resolve(script)),
              StandardCharsets.UTF_8);
      assertTrue(source.contains("adminDetailLabel("), script + " 的详情未使用中文字段名");
    }
    String layout =
        new String(
            Files.readAllBytes(admin.resolve("templates/admin/layout.ftl")),
            StandardCharsets.UTF_8);
    int start = layout.indexOf("var adminDetailLabels = {");
    int end = layout.indexOf("function adminDetailLabel", start);
    assertTrue(start >= 0 && end > start, "后台缺少详情中文字段字典");
    Matcher labels = Pattern.compile("[A-Za-z0-9_]+:'([^']*)'").matcher(layout.substring(start, end));
    while (labels.find())
      assertFalse(labels.group(1).matches(".*[A-Za-z_].*"), "详情标题仍含英文: " + labels.group(1));
  }

  @Test
  void adminFormsDoNotExposeEnglishIdentifierAbbreviations() throws IOException {
    Path scripts = projectRoot().resolve("feimao-backend/src/main/resources/static/admin/js");
    StringBuilder source = new StringBuilder();
    for (String script :
        new String[] {
          "order-data.js", "fund.js", "system.js", "access.js", "resource.js", "partner.js",
          "attachment-select.js"
        })
      source.append(new String(Files.readAllBytes(scripts.resolve(script)), StandardCharsets.UTF_8));
    source.append(
        new String(
            Files.readAllBytes(
                projectRoot()
                    .resolve(
                        "feimao-backend/src/main/resources/templates/admin/system/attachment-select.ftl")),
            StandardCharsets.UTF_8));
    for (String label :
        new String[] {
          "用户ID", "订单ID", "门店ID", "商户ID", "品牌ID", "父ID", "所属方ID",
          "MIME 类型", "Mime类型", "管理员ID", "会员ID", "YYYY-MM-DDTHH:mm"
        })
      assertFalse(source.indexOf(label) >= 0, "后台表单仍展示英文缩写: " + label);
  }

  @Test
  void merchantAdminKeepsLegacyVehicleEntryAndMapPicker() throws IOException {
    Path resources = projectRoot().resolve("feimao-backend/src/main/resources");
    String partner =
        new String(
            Files.readAllBytes(resources.resolve("static/admin/js/partner.js")),
            StandardCharsets.UTF_8);
    String merchantCars =
        new String(
            Files.readAllBytes(resources.resolve("static/admin/js/merchant-service.js")),
            StandardCharsets.UTF_8);
    String template =
        new String(
            Files.readAllBytes(resources.resolve("templates/admin/partner/list.ftl")),
            StandardCharsets.UTF_8);
    assertTrue(partner.contains(">门店车辆</a>"), "商户列表缺少旧版门店车辆入口");
    assertTrue(partner.contains("id=\"merchantMap\""), "商户新增/编辑缺少旧版地图选点");
    assertTrue(template.contains("map.qq.com/api/gljs"), "商户地图脚本未加载");
    assertTrue(merchantCars.contains("initialMerchantId"), "门店车辆页未接收商户筛选参数");
  }

  @Test
  void legacyOrderControllerKeepsAllPublishedApiMethods() {
    Set<String> routes = new LinkedHashSet<>();
    mappings.getHandlerMethods().keySet().forEach(info -> routes.addAll(info.getPatternValues()));
    for (String method :
        new String[] {
          "insureTest", "insureListTest", "insureCancel", "list", "detail", "cancel",
          "backCar", "orderPage", "leaseCarPay", "againLeaseCarPay", "maintainPay",
          "buyCarPay", "againBuyCarPay", "againMaintainPay", "installmentDetail", "installment"
        }) {
      assertTrue(routes.contains("/api/order/" + method), "订单API缺少旧路由: " + method);
    }
  }

  private static void assertRegisteredPagesExist(Path app) throws IOException {
    Set<String> pages = new LinkedHashSet<>();
    collect(app.resolve("pages.json"), PAGE_PATH, pages);
    assertTrue(!pages.isEmpty(), "pages.json未注册页面: " + app.getFileName());
    for (String page : pages)
      assertTrue(Files.isRegularFile(app.resolve(page + ".vue")), "注册页面文件不存在: " + app.resolve(page + ".vue"));
  }

  private static Set<String> scan(Path root, Pattern pattern, String... suffixes)
      throws IOException {
    Set<String> values = new LinkedHashSet<>();
    try (Stream<Path> files = Files.walk(root)) {
      files
          .filter(Files::isRegularFile)
          .filter(path -> hasSuffix(path, suffixes))
          .filter(path -> !path.toString().contains("/uni_modules/"))
          .filter(path -> !path.toString().contains("/unpackage/"))
          .forEach(path -> collect(path, pattern, values));
    }
    return values;
  }

  private static void collect(Path path, Pattern pattern, Set<String> values) {
    try {
      Matcher matcher = pattern.matcher(new String(Files.readAllBytes(path), StandardCharsets.UTF_8));
      while (matcher.find()) values.add(matcher.group(1));
    } catch (IOException exception) {
      throw new IllegalStateException("读取路由来源失败: " + path, exception);
    }
  }

  private static boolean hasSuffix(Path path, String[] suffixes) {
    String name = path.getFileName().toString();
    for (String suffix : suffixes) if (name.endsWith(suffix)) return true;
    return false;
  }

  private static boolean matches(Set<String> routes, String path) {
    AntPathMatcher matcher = new AntPathMatcher();
    for (String route : routes)
      if (route.equals(path) || matcher.match(route, path)) return true;
    return false;
  }

  private static boolean matchesFrontendApi(Set<String> routes, String sourcePath) {
    String path = sourcePath.split("\\?", 2)[0];
    if (!path.endsWith("/")) return matches(routes, path);
    AntPathMatcher matcher = new AntPathMatcher();
    for (String route : routes) {
      if (route.startsWith(path) || matcher.match(route, path + "1")) return true;
    }
    return false;
  }

  private static Path projectRoot() {
    Path current = Paths.get("").toAbsolutePath().normalize();
    return Files.isDirectory(current.resolve("feimao-user")) ? current : current.getParent();
  }
}
