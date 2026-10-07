package com.feimao.mapper;

import java.math.BigDecimal;
import java.util.Map;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface OrderActionMapper {
  @Select("select number,merchant_id from ymcx_order where id=#{id} and is_delete=1")
  Map<String, Object> selectReturnOrder(@Param("id") long id);

  @Select("select * from ymcx_order where id=#{id} and is_delete=1 for update")
  Map<String, Object> lockOrder(@Param("id") long id);

  @Select("select count(*) from ymcx_order_guest where order_id=#{orderId} and is_delete=1 and is_insure=2 and s_time>#{now}")
  int activeInsuranceCount(@Param("orderId") long orderId, @Param("now") long now);

  @Insert("insert into ymcx_platform_log(user_id,amount,type,log_note,order_id,createtime,is_delete,user_type) values(#{userId},#{amount},5,#{note},#{orderId},#{now},1,1)")
  int insertRefundLog(@Param("userId") long userId, @Param("amount") BigDecimal amount,
      @Param("orderId") long orderId,@Param("note") String note,@Param("now") long now);

  @Update("update ymcx_order set state=6,cancel_time=#{now},cancel_id=#{adminId},cancel_refund_price=#{refund},cancel_refund_number=#{refundNumber},last_all_money=#{remaining},cash_state=if(cash_state=2,3,cash_state),is_insure=if(is_insure=1,1,3),updatetime=#{now} where id=#{id} and state=#{state} and pick_state=1")
  int cancelOrder(@Param("id") long id, @Param("state") int state,
      @Param("adminId") long adminId, @Param("refund") BigDecimal refund,
      @Param("refundNumber") String refundNumber, @Param("remaining") BigDecimal remaining,
      @Param("now") long now);

  @Update("update ymcx_merchant_car set stock=least(total_stock,stock+1),updatetime=#{now} where id=#{id} and is_delete=1")
  int restoreCarStock(@Param("id") long id, @Param("now") long now);
}
