package com.feimao.controller.admin;

import com.feimao.common.Result;
import com.feimao.service.DriverWithdrawAdminService;
import java.util.Collections;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/driver-withdrawals")
public class DriverWithdrawAdminController {
  private final DriverWithdrawAdminService withdrawals;

  public DriverWithdrawAdminController(DriverWithdrawAdminService withdrawals) {
    this.withdrawals = withdrawals;
  }

  @GetMapping
  public Result<Map<String, Object>> list(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int limit,
      @RequestParam(required = false) Integer status) {
    if (page < 1 || limit < 1 || limit > 100) return Result.error("分页参数错误");
    if (status != null && (status < 0 || status > 2)) return Result.error("提现状态参数错误");
    return Result.success(withdrawals.list(page, limit, status));
  }

  @PostMapping("/approve")
  public Result<Map<String, Object>> approve(@RequestBody Map<String, Object> body) {
    try {
      long id = Long.parseLong(String.valueOf(body.get("id")));
      String batchId = withdrawals.approve(id, text(body.get("note")));
      return Result.success(Collections.singletonMap("batchId", batchId));
    } catch (ArithmeticException | IllegalArgumentException | IllegalStateException exception) {
      return Result.error(exception.getMessage());
    }
  }

  @PostMapping("/reject")
  public Result<Boolean> reject(@RequestBody Map<String, Object> body) {
    try {
      long id = Long.parseLong(String.valueOf(body.get("id")));
      String reason = text(body.get("reason"));
      if (reason == null || reason.trim().isEmpty()) return Result.error("请输入拒绝原因");
      return withdrawals.reject(id, reason)
          ? Result.success(true)
          : Result.error("提现记录不存在或已审核");
    } catch (Exception exception) {
      return Result.error("提现参数错误");
    }
  }

  private String text(Object value) {
    return value == null ? null : String.valueOf(value);
  }
}
