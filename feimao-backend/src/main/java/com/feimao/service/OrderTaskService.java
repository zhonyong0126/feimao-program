package com.feimao.service;

import com.feimao.entity.BusinessOrder;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderTaskService {
  private final BusinessOrderService orders;

  public OrderTaskService(BusinessOrderService orders) {
    this.orders = orders;
  }

  @Scheduled(fixedDelay = 600000, initialDelay = 60000)
  @Transactional
  public void cancelUnpaidOrders() {
    long now = System.currentTimeMillis() / 1000;
    List<BusinessOrder> expired = orders.lockExpiredUnpaid(now - 1800);
    for (BusinessOrder order : expired) {
      boolean changed = orders.cancelUnpaid(order.getId(), now);
      if (changed && order.getMerchantCarId() != null)
        orders.restoreMerchantCarStock(order.getMerchantCarId(), now);
    }
  }

  @Scheduled(fixedDelay = 120000, initialDelay = 60000)
  public void startPaidLeaseOrders() {
    long now = System.currentTimeMillis() / 1000;
    orders.startPaidLeases(now, now - 60);
  }
}
