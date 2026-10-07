package com.feimao.controller.common;

import com.feimao.service.InsuranceService;
import com.feimao.service.OrderTaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** HTTP entry points retained for the cron jobs configured by the original PHP service. */
@RestController
@RequestMapping("/api/task")
public class TaskCompatibilityController {
  private final OrderTaskService orderTasks;
  private final InsuranceService insurance;

  public TaskCompatibilityController(OrderTaskService orderTasks, InsuranceService insurance) {
    this.orderTasks = orderTasks;
    this.insurance = insurance;
  }

  @PostMapping("/tenCancelOrder")
  public ResponseEntity<Void> cancelExpiredUnpaidOrders() {
    orderTasks.cancelUnpaidOrders();
    return ResponseEntity.ok().build();
  }

  @PostMapping("/leaseOrderUnderway")
  public ResponseEntity<Void> startPaidLeaseOrders() {
    orderTasks.startPaidLeaseOrders();
    return ResponseEntity.ok().build();
  }

  @PostMapping("/orderGuestInsure")
  public ResponseEntity<Void> insurePendingOrderGuests() {
    insurance.insurePendingGuests();
    return ResponseEntity.ok().build();
  }

  @PostMapping("/orderGuestInsureCancel")
  public ResponseEntity<Void> cancelPendingOrderGuestPolicies() {
    insurance.cancelPendingGuests();
    return ResponseEntity.ok().build();
  }
}
