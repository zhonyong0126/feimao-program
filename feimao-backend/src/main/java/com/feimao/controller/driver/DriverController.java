package com.feimao.controller.driver;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.feimao.common.Result;
import com.feimao.entity.Driver;
import com.feimao.entity.Order;
import com.feimao.service.DriverService;
import com.feimao.service.DriverFinanceService;
import com.feimao.service.DriverOrderService;
import com.feimao.service.DriverProfileService;
import com.feimao.service.OrderService;
import com.feimao.service.WechatLoginService;
import com.feimao.utils.JwtUtils;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 司机端接口
 */
@RestController
@RequestMapping("/api/driver")

public class DriverController {

    @Autowired
    private DriverService driverService;

    @Autowired
    private DriverFinanceService driverFinanceService;

    @Autowired
    private DriverOrderService driverOrderService;

    @Autowired
    private DriverProfileService driverProfileService;

    @Autowired
    private WechatLoginService wechatLoginService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    /**
     * 微信登录
     */
    @PostMapping("/login/wx")
    
    public Result<Map<String, Object>> wxLogin(@RequestBody Map<String, Object> body) {
        String code = body.get("code") == null ? "" : body.get("code").toString().trim();
        if (code.isEmpty()) return Result.error("微信登录凭证不能为空");
        String openid = wechatLoginService.exchangeCode(code);
        
        Driver driver = driverService.getByOpenid(openid);
        boolean isNew = false;
        
        if (driver == null) {
            isNew = true;
            driver = new Driver();
            driver.setOpenid(openid);
            driver.setOnlineStatus(0);
            driver.setAuthStatus(0);
            driver.setStatus(1);
            driver.setBalance(BigDecimal.ZERO);
            driver.setDeleted(0);
            driverService.save(driver);
        }
        
        String token = jwtUtils.generateToken(driver.getId(), "driver");
        
        if (redisTemplate != null) {
            redisTemplate.opsForValue().set("driver:token:" + driver.getId(), token, 7, TimeUnit.DAYS);
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("isNew", isNew);
        data.put("driver", driver);
        
        return Result.success(data);
    }

    @PostMapping("/logout")
    public Result<Boolean> logout(@RequestHeader String authorization) {
        String token = authorization.replace("Bearer ", "");
        Long driverId = jwtUtils.getUserId(token);
        if (redisTemplate != null) {
            String key = "driver:token:" + driverId;
            if (token.equals(redisTemplate.opsForValue().get(key))) {
                redisTemplate.delete(key);
            }
        }
        return Result.success(true);
    }

    /**
     * 获取司机信息
     */
    @GetMapping("/info")
    
    public Result<Driver> info(@RequestHeader String authorization) {
        String token = authorization.replace("Bearer ", "");
        Long driverId = jwtUtils.getUserId(token);
        Driver driver = driverService.getById(driverId);
        return Result.success(driver);
    }

    /**
     * 更新司机信息
     */
    @PostMapping("/update")
    
    public Result<Boolean> update(@RequestHeader String authorization, @RequestBody Driver driver) {
        String token = authorization.replace("Bearer ", "");
        Long driverId = jwtUtils.getUserId(token);
        Driver changed = new Driver();
        changed.setId(driverId);
        changed.setName(driver.getName());
        changed.setMobile(driver.getMobile());
        changed.setAvatar(driver.getAvatar());
        return Result.success(driverService.updateById(changed));
    }

    @PostMapping("/auth/submit")
    public Result<Boolean> submitAuth(
            @RequestHeader String authorization, @RequestBody Driver driver) {
        Long driverId = jwtUtils.getUserId(authorization.replace("Bearer ", ""));
        if (StrUtil.hasBlank(driver.getName(), driver.getMobile(), driver.getIdCard(), driver.getDriverLicense())) {
            return Result.error("请完整填写实名认证资料");
        }
        if (!driver.getMobile().matches("^1\\d{10}$")) return Result.error("手机号格式错误");
        if (!driver.getIdCard().matches("^[0-9]{17}[0-9Xx]$")) return Result.error("身份证号格式错误");
        try {
            return driverProfileService.submitAuthentication(driverId, driver)
                    ? Result.success(true)
                    : Result.error("账号不可用或存在履约中的订单");
        } catch (DataIntegrityViolationException exception) {
            return Result.error("手机号已被其他司机使用");
        }
    }

    @PostMapping("/vehicle/update")
    public Result<Boolean> updateVehicle(
            @RequestHeader String authorization, @RequestBody Driver driver) {
        Long driverId = jwtUtils.getUserId(authorization.replace("Bearer ", ""));
        if (StrUtil.hasBlank(driver.getPlateNumber(), driver.getVehicleType(), driver.getVehicleLicense())) {
            return Result.error("请完整填写车辆资料");
        }
        return driverProfileService.submitVehicle(driverId, driver)
                ? Result.success(true)
                : Result.error("账号不可用或存在履约中的订单");
    }

    /**
     * 切换司机在线状态
     */
    @PostMapping("/online-status")
    public Result<Boolean> updateOnlineStatus(
            @RequestHeader String authorization, @RequestBody Map<String, Integer> body) {
        String token = authorization.replace("Bearer ", "");
        Long driverId = jwtUtils.getUserId(token);
        Integer onlineStatus = body.get("onlineStatus");
        if (onlineStatus == null || (onlineStatus != 0 && onlineStatus != 1)) {
            return Result.error("在线状态参数错误");
        }
        Driver current = driverService.getById(driverId);
        if (current == null
                || Objects.equals(current.getDeleted(), 1)
                || !Objects.equals(current.getStatus(), 1)) {
            return Result.error("司机账号不可用");
        }
        if (!Objects.equals(current.getAuthStatus(), 2)) {
            return Result.error("司机尚未通过认证");
        }
        if (Objects.equals(current.getOnlineStatus(), 2)) {
            return Result.error("运输中不能切换在线状态");
        }
        return driverOrderService.changeOnlineStatus(driverId, onlineStatus)
                ? Result.success(true)
                : Result.error("存在履约中的订单或状态已变化，请刷新后重试");
    }

    /**
     * 订单大厅（可抢单）
     */
    @GetMapping("/orders/grab")
    
    public Result<Page<Order>> grabList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit) {
        Page<Order> pageObj = new Page<>(page, limit);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getStatus, 1)
                .isNull(Order::getDriverId)
                .eq(Order::getDeleted, 0)
                .orderByDesc(Order::getCreateTime);
        return Result.success(orderService.page(pageObj, wrapper));
    }

    /**
     * 我的订单
     */
    @GetMapping("/orders/my")
    
    public Result<Page<Order>> myOrders(
            @RequestHeader String authorization,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) Integer status) {
        String token = authorization.replace("Bearer ", "");
        Long driverId = jwtUtils.getUserId(token);
        
        Page<Order> pageObj = new Page<>(page, limit);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getDriverId, driverId)
                .eq(Order::getDeleted, 0);
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreateTime);
        return Result.success(orderService.page(pageObj, wrapper));
    }

    /**
     * 订单详情
     */
    @GetMapping("/order/{id}")
    public Result<Order> orderDetail(
            @RequestHeader String authorization, @PathVariable Long id) {
        Long driverId = jwtUtils.getUserId(authorization.replace("Bearer ", ""));
        Order order = orderService.getById(id);
        if (order == null || !Objects.equals(order.getDriverId(), driverId)) {
            return Result.error("订单不存在或不属于当前司机");
        }
        return Result.success(order);
    }

    /**
     * 抢单
     */
    @PostMapping("/order/grab/{id}")
    
    public Result<Boolean> grabOrder(@RequestHeader String authorization, @PathVariable Long id) {
        String token = authorization.replace("Bearer ", "");
        Long driverId = jwtUtils.getUserId(token);
        
        return driverOrderService.grab(driverId, id)
                ? Result.success(true)
                : Result.error("订单已被抢走或当前司机不可接单");
    }

    /**
     * 开始运输
     */
    @PostMapping("/order/start/{id}")
    public Result<Boolean> startTransport(
            @RequestHeader String authorization, @PathVariable Long id) {
        Long driverId = jwtUtils.getUserId(authorization.replace("Bearer ", ""));
        Order order = orderService.getById(id);
        if (order == null || !Objects.equals(order.getDriverId(), driverId)) {
            return Result.error("订单不存在或不属于当前司机");
        }
        if (!Objects.equals(order.getStatus(), 2)) {
            return Result.error("当前订单状态不能开始运输");
        }
        return driverOrderService.start(driverId, id)
                ? Result.success(true)
                : Result.error("订单状态已发生变化，请刷新后重试");
    }

    /**
     * 确认到达目的地
     */
    @PostMapping("/order/arrive/{id}")
    public Result<Boolean> arrive(
            @RequestHeader String authorization, @PathVariable Long id) {
        Long driverId = jwtUtils.getUserId(authorization.replace("Bearer ", ""));
        Order order = orderService.getById(id);
        if (order == null || !Objects.equals(order.getDriverId(), driverId)) {
            return Result.error("订单不存在或不属于当前司机");
        }
        if (!Objects.equals(order.getStatus(), 3)) {
            return Result.error("当前订单状态不能确认到达");
        }
        return driverOrderService.arrive(driverId, id)
                ? Result.success(true)
                : Result.error("订单已确认到达，请刷新后重试");
    }

    /**
     * 完成订单
     */
    @PostMapping("/order/complete/{id}")
    public Result<Boolean> completeOrder(
            @RequestHeader String authorization, @PathVariable Long id) {
        Long driverId = jwtUtils.getUserId(authorization.replace("Bearer ", ""));
        Order order = orderService.getById(id);
        if (order == null || !Objects.equals(order.getDriverId(), driverId)) {
            return Result.error("订单不存在或不属于当前司机");
        }
        if (!Objects.equals(order.getStatus(), 3) || order.getArriveTime() == null) {
            return Result.error("请先确认到达目的地");
        }
        return driverFinanceService.complete(driverId, id)
                ? Result.success(true)
                : Result.error("订单状态已发生变化，请刷新后重试");
    }

    @GetMapping("/income/list")
    public Result<Map<String, Object>> incomeList(
            @RequestHeader String authorization,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit) {
        Long driverId = jwtUtils.getUserId(authorization.replace("Bearer ", ""));
        return Result.success(driverFinanceService.income(driverId, page, limit));
    }

    @PostMapping("/income/withdraw")
    public Result<Boolean> withdraw(
            @RequestHeader String authorization, @RequestBody Map<String, Object> body) {
        Long driverId = jwtUtils.getUserId(authorization.replace("Bearer ", ""));
        BigDecimal amount;
        try {
            amount = new BigDecimal(String.valueOf(body.get("amount"))).setScale(2);
        } catch (Exception exception) {
            return Result.error("提现金额格式错误");
        }
        if (amount.signum() <= 0) return Result.error("提现金额必须大于0");
        return driverFinanceService.withdraw(driverId, amount)
                ? Result.success(true)
                : Result.error("余额不足或司机账号不可用");
    }

    /**
     * 今日统计
     */
    @GetMapping("/stats/today")
    
    public Result<Map<String, Object>> todayStats(@RequestHeader String authorization) {
        String token = authorization.replace("Bearer ", "");
        Long driverId = jwtUtils.getUserId(token);
        
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        
        long todayOrders = orderService.count(new LambdaQueryWrapper<Order>()
                .eq(Order::getDriverId, driverId)
                .ge(Order::getCreateTime, todayStart)
                .eq(Order::getDeleted, 0));
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("todayOrders", todayOrders);
        stats.put("todayIncome", driverFinanceService.todayIncome(driverId));
        stats.put("pendingOrders", orderService.count(new LambdaQueryWrapper<Order>()
                .eq(Order::getDriverId, driverId)
                .in(Order::getStatus, 2, 3)
                .eq(Order::getDeleted, 0)));
        
        return Result.success(stats);
    }

}
