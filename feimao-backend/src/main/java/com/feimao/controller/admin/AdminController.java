package com.feimao.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.feimao.common.Result;
import com.feimao.entity.*;
import com.feimao.service.*;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台管理接口
 *
 * @author feimao
 */
@RestController
@RequestMapping("/api/admin")

public class AdminController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private DriverService driverService;

    @Autowired
    private DriverAdminService driverAdminService;

    @Autowired
    private StaffService staffService;

    @Autowired
    private StaffPasswordService staffPasswordService;

    @Autowired
    private StaffDispatchService staffDispatchService;

    @Autowired
    private CustomerService customerService;

    // ==================== 首页数据 ====================

    /**
     * 首页数据概览
     */
    @GetMapping("/dashboard")
    
    public Result<Map<String, Object>> dashboard() {
        Map<String, Object> data = new HashMap<>();
        
        // 总订单数
        data.put("totalOrders", orderService.count(new LambdaQueryWrapper<Order>()
                .eq(Order::getDeleted, 0)));
        // 今日订单数
        LocalDateTime today = LocalDate.now().atStartOfDay();
        data.put("todayOrders", orderService.count(new LambdaQueryWrapper<Order>()
                .eq(Order::getDeleted, 0)
                .ge(Order::getCreateTime, today)
                .lt(Order::getCreateTime, today.plusDays(1))));
        // 总司机数
        data.put("totalDrivers", driverService.count(new LambdaQueryWrapper<Driver>()
                .eq(Driver::getDeleted, 0).eq(Driver::getStatus, 1)));
        // 总员工数
        data.put("totalStaff", staffService.count(new LambdaQueryWrapper<Staff>()
                .eq(Staff::getDeleted, 0).eq(Staff::getStatus, 1)));
        // 总客户数
        data.put("totalCustomers", customerService.count(new LambdaQueryWrapper<Customer>()
                .eq(Customer::getDeleted, 0)));
        // 待处理订单
        data.put("pendingOrders", orderService.count(new LambdaQueryWrapper<Order>()
                .in(Order::getStatus, 0, 1).eq(Order::getDeleted, 0)));
        
        return Result.success(data);
    }

    // ==================== 订单管理 ====================

    @GetMapping("/orders")
    
    public Result<Page<Order>> orders(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        if (page == null || page < 1 || limit == null || limit < 1 || limit > 100) {
            return Result.error("分页参数错误");
        }
        if (status != null && (status < 0 || status > 5)) {
            return Result.error("订单状态参数错误");
        }
        LocalDate start;
        LocalDate end;
        try {
            start = startDate == null || startDate.trim().isEmpty()
                    ? null : LocalDate.parse(startDate.trim());
            end = endDate == null || endDate.trim().isEmpty()
                    ? null : LocalDate.parse(endDate.trim());
        } catch (Exception exception) {
            return Result.error("订单日期格式错误");
        }
        if (start != null && end != null && start.isAfter(end)) {
            return Result.error("开始日期不能晚于结束日期");
        }

        Page<Order> pageObj = new Page<>(page, limit);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getDeleted, 0);
        
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(query -> query.like(Order::getOrderNo, keyword)
                    .or()
                    .like(Order::getGoodsName, keyword));
        }
        if (start != null) {
            wrapper.ge(Order::getCreateTime, start.atStartOfDay());
        }
        if (end != null) {
            wrapper.lt(Order::getCreateTime, end.plusDays(1).atStartOfDay());
        }
        
        wrapper.orderByDesc(Order::getCreateTime);
        return Result.success(orderService.page(pageObj, wrapper));
    }

    @GetMapping("/order/{id}")
    
    public Result<Order> orderDetail(@PathVariable Long id) {
        return Result.success(orderService.getById(id));
    }

    @PostMapping("/order/cancel/{id}")
    public Result<Boolean> cancelOrder(@PathVariable Long id) {
        return staffDispatchService.cancel(id)
                ? Result.success(true)
                : Result.error("订单状态不允许取消或已被处理");
    }

    // ==================== 司机管理 ====================

    @GetMapping("/drivers")
    
    public Result<Page<Driver>> drivers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer authStatus,
            @RequestParam(required = false) Integer onlineStatus,
            @RequestParam(required = false) String keyword) {
        
        Page<Driver> pageObj = new Page<>(page, limit);
        LambdaQueryWrapper<Driver> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Driver::getDeleted, 0);
        
        if (status != null) {
            wrapper.eq(Driver::getStatus, status);
        }
        if (authStatus != null) {
            wrapper.eq(Driver::getAuthStatus, authStatus);
        }
        if (onlineStatus != null) {
            wrapper.eq(Driver::getOnlineStatus, onlineStatus);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(query -> query.like(Driver::getName, keyword)
                    .or()
                    .like(Driver::getMobile, keyword)
                    .or()
                    .like(Driver::getPlateNumber, keyword));
        }
        
        wrapper.orderByDesc(Driver::getCreateTime);
        return Result.success(driverService.page(pageObj, wrapper));
    }

    @GetMapping("/driver/{id}")
    
    public Result<Driver> driverDetail(@PathVariable Long id) {
        return Result.success(driverService.getById(id));
    }

    @PostMapping("/driver/status")
    
    public Result<Boolean> updateDriverStatus(@RequestBody Map<String, Object> body) {
        try {
            long id = Long.parseLong(String.valueOf(body.get("id")));
            int status = Integer.parseInt(String.valueOf(body.get("status")));
            if (status != 0 && status != 1) {
                return Result.error("司机状态参数错误");
            }

            return driverAdminService.changeStatus(id, status)
                    ? Result.success(true)
                    : Result.error("司机不存在、正在运输或存在履约中的订单");
        } catch (Exception exception) {
            return Result.error("司机参数错误");
        }
    }

    @PostMapping("/driver/auth")
    
    public Result<Boolean> authDriver(@RequestBody Map<String, Object> body) {
        try {
            long id = Long.parseLong(String.valueOf(body.get("id")));
            int authStatus = Integer.parseInt(String.valueOf(body.get("authStatus")));
            if (authStatus != 2 && authStatus != 3) {
                return Result.error("认证状态参数错误");
            }

            return driverAdminService.review(id, authStatus)
                    ? Result.success(true)
                    : Result.error("司机不存在、正在运输或存在履约中的订单");
        } catch (Exception exception) {
            return Result.error("司机参数错误");
        }
    }

    // ==================== 派单 ====================

    @PostMapping("/order/dispatch")
    
    public Result<Boolean> dispatchOrder(@RequestBody Map<String, Object> body) {
        try {
            long orderId = Long.parseLong(String.valueOf(body.get("orderId")));
            long driverId = Long.parseLong(String.valueOf(body.get("driverId")));
            return staffDispatchService.adminDispatch(orderId, driverId)
                    ? Result.success(true)
                    : Result.error("订单已被处理或司机当前不可派单");
        } catch (Exception exception) {
            return Result.error("订单或司机参数错误");
        }
    }

    // ==================== 员工管理 ====================

    @GetMapping("/staffs")
    
    public Result<Page<Staff>> staffs(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        
        Page<Staff> pageObj = new Page<>(page, limit);
        LambdaQueryWrapper<Staff> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Staff::getDeleted, 0);
        
        if (status != null) {
            wrapper.eq(Staff::getStatus, status);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(query -> query.like(Staff::getName, keyword)
                    .or()
                    .like(Staff::getMobile, keyword));
        }
        
        wrapper.orderByDesc(Staff::getCreateTime);
        return Result.success(staffService.page(pageObj, wrapper));
    }

    @PostMapping("/staff/add")
    
    public Result<Boolean> addStaff(@RequestBody Staff staff) {
        String validation = validateStaff(staff, false);
        if (validation != null) {
            return Result.error(validation);
        }
        if (staff.getPassword() == null
                || staff.getPassword().trim().isEmpty()
                || staff.getPassword().length() < 8) {
            return Result.error("员工初始密码不能少于8位");
        }
        if (staffExists(staff.getStaffNo(), staff.getMobile(), null)) {
            return Result.error("员工工号或手机号已存在");
        }
        staff.setStaffNo(staff.getStaffNo().trim());
        staff.setName(staff.getName().trim());
        staff.setMobile(staff.getMobile().trim());
        staff.setPassword(staffPasswordService.encode(staff.getPassword()));
        staff.setDeleted(0);
        try {
            return staffService.save(staff)
                    ? Result.success(true)
                    : Result.error("员工新增失败");
        } catch (DataIntegrityViolationException exception) {
            return Result.error("员工工号或手机号已存在");
        }
    }

    @PostMapping("/staff/update")
    
    public Result<Boolean> updateStaff(@RequestBody Staff staff) {
        String validation = validateStaff(staff, true);
        if (validation != null) {
            return Result.error(validation);
        }
        if (staffService.getById(staff.getId()) == null) {
            return Result.error("员工不存在");
        }
        if (staffExists(staff.getStaffNo(), staff.getMobile(), staff.getId())) {
            return Result.error("员工工号或手机号已存在");
        }
        if (staff.getPassword() != null && !staff.getPassword().trim().isEmpty()) {
            if (staff.getPassword().length() < 8) {
                return Result.error("员工密码不能少于8位");
            }
            staff.setPassword(staffPasswordService.encode(staff.getPassword()));
        } else {
            staff.setPassword(null);
        }
        staff.setStaffNo(staff.getStaffNo().trim());
        staff.setName(staff.getName().trim());
        staff.setMobile(staff.getMobile().trim());
        staff.setDeleted(null);
        try {
            return staffService.updateById(staff)
                    ? Result.success(true)
                    : Result.error("员工更新失败");
        } catch (DataIntegrityViolationException exception) {
            return Result.error("员工工号或手机号已存在");
        }
    }

    @PostMapping("/staff/delete/{id}")
    
    public Result<Boolean> deleteStaff(@PathVariable Long id) {
        if (staffService.getById(id) == null) {
            return Result.error("员工不存在");
        }
        return staffService.removeById(id)
                ? Result.success(true)
                : Result.error("员工删除失败");
    }

    private String validateStaff(Staff staff, boolean updating) {
        if (staff == null || (updating && staff.getId() == null)) {
            return "员工参数错误";
        }
        if (staff.getStaffNo() == null || staff.getStaffNo().trim().isEmpty()) {
            return "员工工号不能为空";
        }
        if (staff.getStaffNo().trim().length() > 50) {
            return "员工工号不能超过50个字符";
        }
        if (staff.getName() == null || staff.getName().trim().isEmpty()) {
            return "员工姓名不能为空";
        }
        if (staff.getName().trim().length() > 50) {
            return "员工姓名不能超过50个字符";
        }
        if (staff.getMobile() == null || !staff.getMobile().trim().matches("^1\\d{10}$")) {
            return "员工手机号格式错误";
        }
        if (staff.getRole() == null || (staff.getRole() != 1 && staff.getRole() != 2)) {
            return "员工角色参数错误";
        }
        if (staff.getStatus() == null || (staff.getStatus() != 0 && staff.getStatus() != 1)) {
            return "员工状态参数错误";
        }
        return null;
    }

    private boolean staffExists(String staffNo, String mobile, Long excludedId) {
        LambdaQueryWrapper<Staff> wrapper = new LambdaQueryWrapper<Staff>()
                .eq(Staff::getDeleted, 0)
                .and(query -> query.eq(Staff::getStaffNo, staffNo.trim())
                        .or()
                        .eq(Staff::getMobile, mobile.trim()));
        if (excludedId != null) {
            wrapper.ne(Staff::getId, excludedId);
        }
        return staffService.count(wrapper) > 0;
    }

    // ==================== 客户管理 ====================

    @GetMapping("/customers")
    
    public Result<Page<Customer>> customers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) String keyword) {
        
        Page<Customer> pageObj = new Page<>(page, limit);
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Customer::getDeleted, 0);
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(query -> query.like(Customer::getName, keyword)
                    .or()
                    .like(Customer::getMobile, keyword));
        }
        
        wrapper.orderByDesc(Customer::getCreateTime);
        return Result.success(customerService.page(pageObj, wrapper));
    }

    // ==================== 统计分析 ====================

    @GetMapping("/stats/orders")
    
    public Result<List<Map<String, Object>>> orderStats(@RequestParam(defaultValue = "7") Integer days) {
        int range = days == null ? 7 : Math.max(1, Math.min(days, 366));
        LocalDate firstDay = LocalDate.now().minusDays(range - 1L);
        List<Map<String, Object>> result = new ArrayList<>(range);

        for (int offset = 0; offset < range; offset++) {
            LocalDate date = firstDay.plusDays(offset);
            long count = orderService.count(new LambdaQueryWrapper<Order>()
                    .eq(Order::getDeleted, 0)
                    .ge(Order::getCreateTime, date.atStartOfDay())
                    .lt(Order::getCreateTime, date.plusDays(1).atStartOfDay()));
            Map<String, Object> item = new HashMap<>();
            item.put("date", date.toString());
            item.put("count", count);
            result.add(item);
        }
        return Result.success(result);
    }

}
