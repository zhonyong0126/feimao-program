package com.feimao.controller.staff;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.feimao.common.Result;
import com.feimao.entity.Driver;
import com.feimao.entity.Customer;
import com.feimao.entity.Order;
import com.feimao.entity.Staff;
import com.feimao.service.DriverService;
import com.feimao.service.CustomerService;
import com.feimao.service.OrderService;
import com.feimao.service.StaffService;
import com.feimao.service.StaffDispatchService;
import com.feimao.service.StaffPasswordService;
import com.feimao.utils.JwtUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.UUID;

/**
 * 员工端接口（调度员）
 *
 * @author feimao
 */
@RestController
@RequestMapping("/api/staff")

public class StaffController {

    @Autowired
    private StaffService staffService;

    @Autowired
    private DriverService driverService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private StaffDispatchService staffDispatchService;

    @Autowired
    private StaffPasswordService staffPasswords;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 登录
     */
    @PostMapping("/login")
    
    public Result<Map<String, Object>> login(@RequestBody Map<String, Object> body) {
        String username = body.get("username") == null ? "" : body.get("username").toString().trim();
        String password = body.get("password") == null ? "" : body.get("password").toString();
        if (username.isEmpty() || password.isEmpty()) return Result.error("请输入账号和密码");
        // 支持手机号和工号（工号大小写不敏感）
        // 1. 按手机号精确查
        Staff staff = staffService.getOne(new LambdaQueryWrapper<Staff>()
                .eq(Staff::getMobile, username)
                .eq(Staff::getDeleted, 0));

        // 2. 按工号查（大小写不敏感）
        if (staff == null) {
            staff = staffService.getOne(new QueryWrapper<Staff>()
                    .eq("deleted", 0)
                    .apply("UPPER(staff_no) = UPPER({0})", username)
                    .last("LIMIT 1"));
        }

        // 3. 密码比对
        if (staff == null || !staffPasswords.matches(password, staff.getPassword())) {
            return Result.error("账号或密码错误");
        }

        if (staff.getStatus() != 1) {
            return Result.error("账号已被禁用");
        }
        if (staffPasswords.legacy(staff.getPassword())) {
            Staff upgraded = new Staff();
            upgraded.setId(staff.getId());
            upgraded.setPassword(staffPasswords.encode(password));
            staffService.updateById(upgraded);
        }
        
        String token = jwtUtils.generateToken(staff.getId(), "staff");
        redisTemplate.opsForValue().set("staff:token:" + staff.getId(), token, 7, TimeUnit.DAYS);
        
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("staff", staff);
        
        return Result.success(data);
    }

    @PostMapping("/logout")
    public Result<Boolean> logout(@RequestHeader String authorization) {
        String token = authorization.replace("Bearer ", "");
        Long staffId = jwtUtils.getUserId(token);
        String key = "staff:token:" + staffId;
        if (token.equals(redisTemplate.opsForValue().get(key))) {
            redisTemplate.delete(key);
        }
        return Result.success(true);
    }

    @PostMapping("/order/create")
    public Result<Order> createOrder(
            @RequestHeader String authorization, @RequestBody Order body) {
        Long staffId = jwtUtils.getUserId(authorization.replace("Bearer ", ""));
        Staff currentStaff = staffService.getById(staffId);
        if (currentStaff == null || currentStaff.getStatus() == null || currentStaff.getStatus() != 1) {
            return Result.error("员工账号不可用");
        }
        if (body.getStartAddress() == null || body.getStartAddress().trim().isEmpty()
                || body.getEndAddress() == null || body.getEndAddress().trim().isEmpty()
                || body.getGoodsName() == null || body.getGoodsName().trim().isEmpty()) {
            return Result.error("请完整填写起点、终点和货物名称");
        }
        if (body.getStartAddress().trim().length() > 255
                || body.getEndAddress().trim().length() > 255) {
            return Result.error("起点或终点地址不能超过255个字符");
        }
        if (body.getGoodsName().trim().length() > 100) {
            return Result.error("货物名称不能超过100个字符");
        }
        if (body.getRemark() != null && body.getRemark().trim().length() > 500) {
            return Result.error("订单备注不能超过500个字符");
        }
        if (!validDecimal(body.getGoodsWeight())
                || !validDecimal(body.getGoodsVolume())
                || !validDecimal(body.getFreight())
                || !validDecimal(body.getDistance())) {
            return Result.error("重量、体积、距离和运费必须是非负且最多两位小数");
        }
        if (body.getCustomerId() != null && customerService.getById(body.getCustomerId()) == null) {
            return Result.error("客户不存在");
        }
        Order order = new Order();
        order.setOrderNo("FM" + UUID.randomUUID().toString().replace("-", "").toUpperCase());
        order.setCustomerId(body.getCustomerId());
        order.setStaffId(staffId);
        order.setStartAddress(body.getStartAddress().trim());
        order.setEndAddress(body.getEndAddress().trim());
        order.setGoodsName(body.getGoodsName().trim());
        order.setGoodsWeight(body.getGoodsWeight());
        order.setGoodsVolume(body.getGoodsVolume());
        order.setFreight(body.getFreight());
        order.setDistance(body.getDistance());
        order.setRemark(body.getRemark() == null ? null : body.getRemark().trim());
        order.setStatus(0);
        order.setPayStatus(0);
        order.setDeleted(0);
        order.setCreateTime(LocalDateTime.now());
        return orderService.save(order)
                ? Result.success(order)
                : Result.error("订单创建失败");
    }

    private boolean validDecimal(BigDecimal value) {
        return value == null
                || (value.signum() >= 0
                && value.scale() <= 2
                && value.compareTo(new BigDecimal("99999999.99")) <= 0);
    }

    @GetMapping("/order/{id}")
    public Result<Order> orderDetail(@PathVariable Long id) {
        Order order = orderService.getById(id);
        return order == null || Integer.valueOf(1).equals(order.getDeleted())
                ? Result.error("订单不存在") : Result.success(order);
    }

    @PostMapping("/order/cancel/{id}")
    public Result<Boolean> cancelOrder(@PathVariable Long id) {
        return staffDispatchService.cancel(id)
                ? Result.success(true) : Result.error("订单状态不允许取消或已被处理");
    }

    /**
     * 订单列表
     */
    @GetMapping("/orders")
    
    public Result<Page<Order>> orders(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        
        Page<Order> pageObj = new Page<>(page, limit);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getDeleted, 0);
        
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(search -> search.like(Order::getOrderNo, keyword)
                    .or()
                    .like(Order::getGoodsName, keyword));
        }
        
        wrapper.orderByDesc(Order::getCreateTime);
        return Result.success(orderService.page(pageObj, wrapper));
    }

    /**
     * 派单
     */
    @PostMapping("/order/dispatch")
    
    public Result<Boolean> dispatch(
            @RequestHeader String authorization,
            @RequestBody Map<String, Object> body) {
        
        String token = authorization.replace("Bearer ", "");
        Long staffId = jwtUtils.getUserId(token);
        Long orderId;
        Long driverId;
        try {
            orderId = Long.valueOf(String.valueOf(body.get("orderId")));
            driverId = Long.valueOf(String.valueOf(body.get("driverId")));
        } catch (Exception exception) {
            return Result.error("订单或司机参数错误");
        }
        
        return staffDispatchService.dispatch(staffId, orderId, driverId)
                ? Result.success(true)
                : Result.error("订单已被处理或司机当前不可派单");
    }

    /**
     * 司机列表
     */
    @GetMapping("/drivers")
    
    public Result<Page<Driver>> drivers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) Integer onlineStatus,
            @RequestParam(required = false) String keyword) {
        
        Page<Driver> pageObj = new Page<>(page, limit);
        LambdaQueryWrapper<Driver> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Driver::getDeleted, 0)
                .eq(Driver::getStatus, 1)
                .eq(Driver::getAuthStatus, 2); // 已认证
        
        if (onlineStatus != null) {
            wrapper.eq(Driver::getOnlineStatus, onlineStatus);
        }
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(search -> search.like(Driver::getName, keyword)
                    .or()
                    .like(Driver::getMobile, keyword)
                    .or()
                    .like(Driver::getPlateNumber, keyword));
        }
        
        wrapper.orderByDesc(Driver::getCreateTime);
        return Result.success(driverService.page(pageObj, wrapper));
    }

    @GetMapping("/drivers/online")
    public Result<List<Driver>> onlineDrivers() {
        return Result.success(driverService.list(new LambdaQueryWrapper<Driver>()
                .eq(Driver::getDeleted, 0)
                .eq(Driver::getStatus, 1)
                .eq(Driver::getAuthStatus, 2)
                .eq(Driver::getOnlineStatus, 1)
                .orderByDesc(Driver::getCreateTime)));
    }

    @GetMapping("/customers")
    public Result<Page<Customer>> customers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) String keyword) {
        Page<Customer> pageObj = new Page<>(page, limit);
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Customer::getDeleted, 0);
        if (keyword != null && !keyword.trim().isEmpty()) {
            String search = keyword.trim();
            wrapper.and(value -> value.like(Customer::getName, search)
                    .or().like(Customer::getMobile, search)
                    .or().like(Customer::getContact, search));
        }
        wrapper.orderByDesc(Customer::getCreateTime);
        return Result.success(customerService.page(pageObj, wrapper));
    }

    @PostMapping("/customer/create")
    public Result<Customer> createCustomer(@RequestBody Customer body) {
        String validation = validateCustomer(body, false);
        if (validation != null) return Result.error(validation);
        Customer customer = customerValue(body);
        customer.setDeleted(0);
        return customerService.save(customer)
                ? Result.success(customer)
                : Result.error("客户新增失败");
    }

    @PostMapping("/customer/update")
    public Result<Boolean> updateCustomer(@RequestBody Customer body) {
        String validation = validateCustomer(body, true);
        if (validation != null) return Result.error(validation);
        if (customerService.getById(body.getId()) == null) return Result.error("客户不存在");
        Customer customer = customerValue(body);
        customer.setId(body.getId());
        return customerService.updateById(customer)
                ? Result.success(true)
                : Result.error("客户更新失败");
    }

    private String validateCustomer(Customer customer, boolean updating) {
        if (customer == null || (updating && customer.getId() == null)) return "客户参数错误";
        if (customer.getName() == null || customer.getName().trim().isEmpty()) {
            return "客户名称不能为空";
        }
        if (customer.getName().trim().length() > 100) return "客户名称不能超过100个字符";
        if (customer.getContact() != null && customer.getContact().trim().length() > 50) {
            return "联系人不能超过50个字符";
        }
        if (customer.getMobile() != null && customer.getMobile().trim().length() > 20) {
            return "联系电话不能超过20个字符";
        }
        if (customer.getAddress() != null && customer.getAddress().trim().length() > 255) {
            return "客户地址不能超过255个字符";
        }
        return null;
    }

    private Customer customerValue(Customer source) {
        Customer customer = new Customer();
        customer.setName(source.getName().trim());
        customer.setContact(trimToNull(source.getContact()));
        customer.setMobile(trimToNull(source.getMobile()));
        customer.setAddress(trimToNull(source.getAddress()));
        return customer;
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        return value.trim();
    }

    /**
     * 订单统计
     */
    @GetMapping({"/stats", "/stats/today"})
    
    public Result<Map<String, Object>> stats() {
        Map<String, Object> stats = new HashMap<>();
        long total = orderService.count(new LambdaQueryWrapper<Order>()
                .eq(Order::getDeleted, 0));
        long pending = orderService.count(new LambdaQueryWrapper<Order>()
                .in(Order::getStatus, 0, 1).eq(Order::getDeleted, 0));
        stats.put("totalOrders", total);
        stats.put("pending", pending);
        stats.put("pendingOrders", pending);
        stats.put("inProgress", orderService.count(new LambdaQueryWrapper<Order>()
                .in(Order::getStatus, 2, 3).eq(Order::getDeleted, 0)));
        stats.put("completed", orderService.count(new LambdaQueryWrapper<Order>()
                .eq(Order::getStatus, 4).eq(Order::getDeleted, 0)));
        stats.put("onlineDrivers", driverService.count(new LambdaQueryWrapper<Driver>()
                .eq(Driver::getOnlineStatus, 1).eq(Driver::getDeleted, 0).eq(Driver::getStatus, 1)));
        return Result.success(stats);
    }

}
