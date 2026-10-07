package com.feimao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.feimao.entity.BusinessOrder;
import java.util.List;

public interface BusinessOrderService extends IService<BusinessOrder> {
  List<BusinessOrder> lockExpiredUnpaid(long cutoff);

  boolean cancelUnpaid(long id, long now);

  int startPaidLeases(long now, long cutoff);

  void restoreMerchantCarStock(long merchantCarId, long now);
}
