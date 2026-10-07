package com.feimao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.feimao.entity.BusinessOrder;
import com.feimao.mapper.BusinessOrderMapper;
import com.feimao.service.BusinessOrderService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class BusinessOrderServiceImpl extends ServiceImpl<BusinessOrderMapper, BusinessOrder>
    implements BusinessOrderService {
  @Override
  public List<BusinessOrder> lockExpiredUnpaid(long cutoff) {
    return baseMapper.selectExpiredUnpaidForUpdate(cutoff);
  }

  @Override
  public boolean cancelUnpaid(long id, long now) {
    return baseMapper.cancelUnpaid(id, now) > 0;
  }

  @Override
  public int startPaidLeases(long now, long cutoff) {
    return baseMapper.startPaidLeases(now, cutoff);
  }

  @Override
  public void restoreMerchantCarStock(long merchantCarId, long now) {
    baseMapper.restoreMerchantCarStock(merchantCarId, now);
  }
}
