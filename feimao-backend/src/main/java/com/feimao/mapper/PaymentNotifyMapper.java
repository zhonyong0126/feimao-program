package com.feimao.mapper;

import java.math.BigDecimal;
import java.util.Map;
import org.apache.ibatis.annotations.*;

@Mapper
public interface PaymentNotifyMapper {
  @Select("select id,user_id,type,state,price,discounts_price,coupon_id from ymcx_order where number=#{number} and is_delete=1 for update")
  Map<String, Object> lockOrder(@Param("number") String number);

  @Update("update ymcx_order set state=2,pay_price=#{payPrice},last_all_money=if(type=1,#{payPrice},last_all_money),paytime=#{paidAt},transaction_id=#{transactionId},pay_data=#{payData},is_insure=if(type=1,2,is_insure),updatetime=#{now} where id=#{id} and state=1")
  int markOrderPaid(@Param("id") long id, @Param("payPrice") BigDecimal payPrice,
      @Param("transactionId") String transactionId, @Param("payData") String payData,
      @Param("paidAt") long paidAt,@Param("now") long now);

  @Insert("insert into ymcx_platform_log(user_id,order_id,amount,type,log_note,user_type,createtime,is_delete) select #{userId},#{orderId},#{amount},1,'用户租车订单支付',1,#{now},1 from dual where not exists (select 1 from ymcx_platform_log where is_delete=1 and deletetime is null and user_id=#{userId} and order_id=#{orderId} and user_type=1 and type=1)")
  int insertLeasePaymentLog(@Param("userId") long userId,@Param("orderId") long orderId,
      @Param("amount") BigDecimal amount,@Param("now") long now);

  @Update("update ymcx_user_coupon set state=2,usetime=#{now},updatetime=#{now} where id=#{id} and user_id=#{userId} and state=1 and deletetime is null and (endtime is null or endtime>#{now})")
  int useCoupon(@Param("id") long id, @Param("userId") long userId, @Param("now") long now);

  @Select("select id,number,num,price,state from ymcx_order_child where child_number=#{number} for update")
  Map<String, Object> lockInstallment(@Param("number") String number);

  @Select("select id,lease_duration from ymcx_order where number=#{number} for update")
  Map<String, Object> lockInstallmentParent(@Param("number") String number);

  @Update("update ymcx_order_child set state=2,pay_price=#{payPrice},transaction_id=#{transactionId},paytime=#{paidAt},updatetime=#{now} where id=#{id} and state=1")
  int markInstallmentPaid(@Param("id") long id, @Param("payPrice") BigDecimal payPrice,
      @Param("transactionId") String transactionId,@Param("paidAt") long paidAt,
      @Param("now") long now);

  @Update("update ymcx_order set pay_price=coalesce(pay_price,0)+#{payPrice},state=case when lease_duration=#{childNum} then 4 else state end,updatetime=#{now} where number=#{number}")
  int addInstallmentPayment(@Param("number") String number,
      @Param("payPrice") BigDecimal payPrice, @Param("childNum") int childNum,
      @Param("now") long now);
}
