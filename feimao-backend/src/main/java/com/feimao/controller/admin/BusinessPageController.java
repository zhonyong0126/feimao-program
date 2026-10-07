package com.feimao.controller.admin;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class BusinessPageController {
  private static final Map<String, ResourcePage> RESOURCES;

  static {
    Map<String, ResourcePage> resources = new LinkedHashMap<>();
    resources.put("banner", new ResourcePage("轮播图管理", true));
    resources.put("brand", new ResourcePage("品牌管理", true));
    resources.put("platform-car", new ResourcePage("平台车辆管理", true));
    resources.put("maintain", new ResourcePage("维修项目", true));
    resources.put("coupon", new ResourcePage("优惠券", true));
    resources.put("category", new ResourcePage("分类管理", true));
    resources.put("malfunction", new ResourcePage("故障上报", false));
    RESOURCES = Collections.unmodifiableMap(resources);
  }

  @GetMapping("/resources/{resource}")
  public String resource(@PathVariable String resource, Model model) {
    ResourcePage page = RESOURCES.get(resource);
    if (page == null) return "redirect:/admin/";
    model.addAttribute("resource", resource);
    model.addAttribute("resourceTitle", page.title);
    model.addAttribute("resourceMutable", page.mutable);
    model.addAttribute("active", "business-" + resource);
    return "resource/list";
  }

  @GetMapping("/applications/{role}")
  public String applications(@PathVariable String role, Model model) {
    if (!"merchant".equals(role) && !"shop".equals(role)) return "redirect:/admin/";
    String title = "merchant".equals(role) ? "商户入驻审核" : "门店入驻审核";
    model.addAttribute("role", role);
    model.addAttribute("applicationTitle", title);
    model.addAttribute("active", "business-application-" + role);
    return "application/list";
  }

  @GetMapping("/partners/{role}")
  public String partners(@PathVariable String role, Model model) {
    if (!"merchant".equals(role) && !"shop".equals(role)) return "redirect:/admin/";
    String title = "merchant".equals(role) ? "门店管理" : "酒店管理";
    model.addAttribute("role", role);
    model.addAttribute("partnerTitle", title);
    model.addAttribute("active", "business-partner-" + role);
    return "partner/list";
  }

  @GetMapping("/merchant-services/{service}")
  public String merchantServices(@PathVariable String service, Model model) {
    if (!"cars".equals(service) && !"maintains".equals(service)) return "redirect:/admin/";
    String title = "cars".equals(service) ? "门店车辆管理" : "门店维修项目";
    model.addAttribute("service", service);
    model.addAttribute("serviceTitle", title);
    model.addAttribute("active", "business-merchant-" + service);
    return "merchant-service/list";
  }

  @GetMapping("/orders/{section}")
  public String orderData(@PathVariable String section, Model model) {
    Map<String, String> titles = new LinkedHashMap<>();
    titles.put("orders", "订单列表");
    titles.put("guests", "订单乘坐人管理");
    titles.put("installments", "订单分期记录");
    titles.put("coupons", "用户优惠券");
    if (!titles.containsKey(section)) return "redirect:/admin/";
    model.addAttribute("section", section);
    model.addAttribute("sectionTitle", titles.get(section));
    model.addAttribute("active", "business-order-" + section);
    return "order-data/list";
  }

  @GetMapping("/funds/{section}")
  public String funds(@PathVariable String section, Model model) {
    Map<String, String> titles = new LinkedHashMap<>();
    titles.put("cash-merchant", "门店提现订单");
    titles.put("cash-shop", "酒店提现订单");
    titles.put("finance-merchant", "商户资金流水");
    titles.put("finance-shop", "门店资金流水");
    titles.put("finance-platform", "平台流水管理");
    titles.put("finance-system", "平台收益管理");
    if (!titles.containsKey(section)) return "redirect:/admin/";
    model.addAttribute("section", section);
    model.addAttribute("fundTitle", titles.get(section));
    model.addAttribute("active", "business-fund-" + section);
    return "fund/list";
  }

  @GetMapping("/access/{section}")
  public String access(@PathVariable String section, Model model) {
    Map<String, String> titles = new LinkedHashMap<>();
    titles.put("users", "会员管理");
    titles.put("user-groups", "会员组管理");
    titles.put("user-rules", "会员权限规则");
    titles.put("admins", "管理员管理");
    titles.put("admin-groups", "角色组");
    titles.put("admin-rules", "菜单规则");
    titles.put("logs", "管理员日志");
    if (!titles.containsKey(section)) return "redirect:/admin/";
    model.addAttribute("section", section);
    model.addAttribute("accessTitle", titles.get(section));
    model.addAttribute("active", "business-access-" + section);
    return "access/list";
  }

  @GetMapping("/system/{section}")
  public String system(@PathVariable String section, Model model) {
    Map<String, String> titles = new LinkedHashMap<>();
    titles.put("config", "系统配置");
    titles.put("attachments", "附件管理");
    titles.put("addresses", "用户地址");
    titles.put("identity", "实名认证审核");
    titles.put("staff", "员工认证审核");
    titles.put("sign-logs", "签到记录");
    if (!titles.containsKey(section)) return "redirect:/admin/";
    model.addAttribute("section", section);
    model.addAttribute("systemTitle", titles.get(section));
    model.addAttribute("active", "business-system-" + section);
    return "system/list";
  }

  @GetMapping("/system/attachments/select")
  public String attachmentSelect(Model model) {
    model.addAttribute("active", "business-system-attachments");
    return "system/attachment-select";
  }

  @GetMapping("/profile")
  public String profile(Model model) {
    model.addAttribute("active", "business-profile");
    return "profile/index";
  }

  private static final class ResourcePage {
    private final String title;
    private final boolean mutable;

    private ResourcePage(String title, boolean mutable) {
      this.title = title;
      this.mutable = mutable;
    }
  }
}
