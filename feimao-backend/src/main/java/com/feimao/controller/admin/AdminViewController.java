package com.feimao.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.feimao.entity.*;
import com.feimao.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;

/**
 * 后台管理视图控制器
 */
@Controller
@RequestMapping("/admin")
public class AdminViewController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private DriverService driverService;
    @Autowired
    private StaffService staffService;
    @Autowired
    private CustomerService customerService;

    // ==================== 登录页 ====================
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // ==================== 首页 ====================
    @GetMapping({"", "/", "/index"})
    public String index(Model model) {
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("totalOrders", orderService.count(new LambdaQueryWrapper<Order>()
                .eq(Order::getDeleted, 0)));
        dashboard.put("pendingOrders", orderService.count(new LambdaQueryWrapper<Order>()
                .in(Order::getStatus, 0, 1).eq(Order::getDeleted, 0)));
        dashboard.put("totalDrivers", driverService.count(new LambdaQueryWrapper<Driver>()
                .eq(Driver::getDeleted, 0).eq(Driver::getStatus, 1)));
        dashboard.put("totalStaff", staffService.count(new LambdaQueryWrapper<Staff>()
                .eq(Staff::getDeleted, 0).eq(Staff::getStatus, 1)));
        dashboard.put("totalCustomers", customerService.count(new LambdaQueryWrapper<Customer>()
                .eq(Customer::getDeleted, 0)));
        model.addAttribute("dashboard", dashboard);
        model.addAttribute("active", "dashboard");
        return "index";
    }

    // ==================== 订单管理 ====================
    @GetMapping("/order/list")
    public String orderList(@RequestParam(defaultValue = "1") Integer page,
                            @RequestParam(defaultValue = "10") Integer limit,
                            @RequestParam(required = false) Integer status,
                            @RequestParam(required = false) String keyword,
                            @RequestParam(required = false) String startDate,
                            @RequestParam(required = false) String endDate,
                            Model model) {
        if (page == null || page < 1 || limit == null || limit < 1 || limit > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "分页参数错误");
        }
        if (status != null && (status < 0 || status > 5)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "订单状态参数错误");
        }
        LocalDate start;
        LocalDate end;
        try {
            start = startDate == null || startDate.trim().isEmpty()
                    ? null : LocalDate.parse(startDate.trim());
            end = endDate == null || endDate.trim().isEmpty()
                    ? null : LocalDate.parse(endDate.trim());
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "订单日期格式错误");
        }
        if (start != null && end != null && start.isAfter(end)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "开始日期不能晚于结束日期");
        }
        Page<Order> pageObj = new Page<>(page, limit);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getDeleted, 0);
        if (status != null) wrapper.eq(Order::getStatus, status);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Order::getOrderNo, keyword).or().like(Order::getGoodsName, keyword));
        }
        if (start != null) wrapper.ge(Order::getCreateTime, start.atStartOfDay());
        if (end != null) wrapper.lt(Order::getCreateTime, end.plusDays(1).atStartOfDay());
        wrapper.orderByDesc(Order::getCreateTime);
        model.addAttribute("page", orderService.page(pageObj, wrapper));
        model.addAttribute("status", status);
        model.addAttribute("keyword", keyword);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("active", "order");
        return "order/list";
    }

    @GetMapping("/order/detail")
    public String orderDetail(@RequestParam Long id, Model model) {
        Order order = orderService.getById(id);
        if (order == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "订单不存在");
        }
        model.addAttribute("order", order);
        if (order.getCustomerId() != null) {
            model.addAttribute("customer", customerService.getById(order.getCustomerId()));
        }
        if (order.getDriverId() != null) {
            model.addAttribute("driver", driverService.getById(order.getDriverId()));
        }
        if (order.getStaffId() != null) {
            model.addAttribute("dispatchStaff", staffService.getById(order.getStaffId()));
        }
        model.addAttribute("active", "order");
        return "order/detail";
    }

    @GetMapping("/order/dispatch")
    public String orderDispatch(@RequestParam Long id, Model model) {
        Order order = orderService.getById(id);
        if (order == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "订单不存在");
        }
        if (order.getDriverId() != null || (order.getStatus() != 0 && order.getStatus() != 1)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "当前订单不可派单");
        }
        model.addAttribute("order", order);
        // 查询空闲司机
        List<Driver> drivers = driverService.list(new LambdaQueryWrapper<Driver>()
                .eq(Driver::getDeleted, 0)
                .eq(Driver::getStatus, 1)
                .eq(Driver::getAuthStatus, 2)
                .eq(Driver::getOnlineStatus, 1)
                .orderByDesc(Driver::getCreateTime));
        model.addAttribute("drivers", drivers);
        model.addAttribute("active", "order");
        return "order/dispatch";
    }

    // ==================== 司机管理 ====================
    @GetMapping("/driver/list")
    public String driverList(@RequestParam(defaultValue = "1") Integer page,
                             @RequestParam(defaultValue = "10") Integer limit,
                             @RequestParam(required = false) Integer status,
                             @RequestParam(required = false) Integer authStatus,
                             @RequestParam(required = false) Integer onlineStatus,
                             @RequestParam(required = false) String keyword,
                             Model model) {
        Page<Driver> pageObj = new Page<>(page, limit);
        LambdaQueryWrapper<Driver> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Driver::getDeleted, 0);
        if (status != null) wrapper.eq(Driver::getStatus, status);
        if (authStatus != null) wrapper.eq(Driver::getAuthStatus, authStatus);
        if (onlineStatus != null) wrapper.eq(Driver::getOnlineStatus, onlineStatus);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Driver::getName, keyword)
                    .or().like(Driver::getMobile, keyword)
                    .or().like(Driver::getPlateNumber, keyword));
        }
        wrapper.orderByDesc(Driver::getCreateTime);
        model.addAttribute("page", driverService.page(pageObj, wrapper));
        model.addAttribute("status", status);
        model.addAttribute("authStatus", authStatus);
        model.addAttribute("onlineStatus", onlineStatus);
        model.addAttribute("keyword", keyword);
        model.addAttribute("active", "driver");
        return "driver/list";
    }

    @GetMapping("/driver/detail")
    public String driverDetail(@RequestParam Long id, Model model) {
        Driver driver = driverService.getById(id);
        if (driver == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "司机不存在");
        }
        model.addAttribute("d", driver);
        model.addAttribute("active", "driver");
        return "driver/detail";
    }

    @GetMapping("/driver/withdrawals")
    public String driverWithdrawals(Model model) {
        model.addAttribute("active", "driver-withdrawals");
        return "driver/withdrawals";
    }

    // ==================== 员工管理 ====================
    @GetMapping("/staff/list")
    public String staffList(@RequestParam(defaultValue = "1") Integer page,
                            @RequestParam(defaultValue = "10") Integer limit,
                            @RequestParam(required = false) Integer status,
                            @RequestParam(required = false) String keyword,
                            Model model) {
        Page<Staff> pageObj = new Page<>(page, limit);
        LambdaQueryWrapper<Staff> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Staff::getDeleted, 0);
        if (status != null) wrapper.eq(Staff::getStatus, status);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Staff::getName, keyword).or().like(Staff::getMobile, keyword));
        }
        wrapper.orderByDesc(Staff::getCreateTime);
        model.addAttribute("page", staffService.page(pageObj, wrapper));
        model.addAttribute("status", status);
        model.addAttribute("keyword", keyword);
        model.addAttribute("active", "staff");
        return "staff/list";
    }

    // ==================== 客户管理 ====================
    @GetMapping("/customer/list")
    public String customerList(@RequestParam(defaultValue = "1") Integer page,
                               @RequestParam(defaultValue = "10") Integer limit,
                               @RequestParam(required = false) String keyword,
                               Model model) {
        Page<Customer> pageObj = new Page<>(page, limit);
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Customer::getDeleted, 0);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Customer::getName, keyword).or().like(Customer::getMobile, keyword));
        }
        wrapper.orderByDesc(Customer::getCreateTime);
        model.addAttribute("page", customerService.page(pageObj, wrapper));
        model.addAttribute("keyword", keyword);
        model.addAttribute("active", "customer");
        return "customer/list";
    }
}
