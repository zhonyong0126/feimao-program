package com.feimao.mapper;

import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OrderNotificationMapper {
  @Select(
      "select o.pick_type,m.mobile from ymcx_order o"
          + " join ymcx_merchant m on m.id=o.merchant_id and m.deletetime is null"
          + " where o.id=#{orderId} and o.type=1 and o.state in(2,3) limit 1")
  Map<String, Object> selectPaidRentalContact(@Param("orderId") long orderId);

  @Select(
      "select m.mobile from ymcx_order o"
          + " join ymcx_merchant m on m.id=o.merchant_id and m.is_delete=1 and m.deletetime is null"
          + " where o.id=#{orderId} limit 1")
  Map<String, Object> selectReturnRequestedContact(@Param("orderId") long orderId);
}
