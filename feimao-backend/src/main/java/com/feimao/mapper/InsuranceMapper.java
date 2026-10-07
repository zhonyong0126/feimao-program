package com.feimao.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface InsuranceMapper {
  @Select("select biz_order_sn from ymcx_order_guest where id=#{id} and is_delete=1")
  String selectPolicyNumber(@Param("id") long id);

  @Select("select g.id,g.name,g.mobile,g.idcard,o.starttime,o.endtime,o.lease_type,o.lease_duration,o.pick_address,m.administrative,m.address merchant_address from ymcx_order_guest g join ymcx_order o on o.id=g.order_id and o.is_delete=1 left join ymcx_merchant m on m.id=o.merchant_id where g.is_delete=1 and g.is_insure=1 and o.type=1 and o.state in(2,3,4,5) order by g.id limit 20")
  List<Map<String, Object>> selectPendingInsureGuests();

  @Select("select g.id,g.biz_order_sn from ymcx_order_guest g join ymcx_order o on o.id=g.order_id and o.is_delete=1 where g.is_delete=1 and g.is_insure=2 and g.s_time>#{now} and o.state=6 order by g.id limit 20")
  List<Map<String, Object>> selectPendingCancelGuests(@Param("now") long now);

  @Select("select id,biz_order_sn from ymcx_order_guest where order_id=#{orderId} and is_delete=1 and is_insure=2 and s_time>#{now} order by id")
  List<Map<String, Object>> selectOrderGuestsToCancel(
      @Param("orderId") long orderId, @Param("now") long now);

  @Update("update ymcx_order_guest set biz_order_sn=#{bizOrderSn},num_id=#{numId},is_insure=2,s_time=#{startTime},e_time=#{endTime},data=#{data},updatetime=#{now} where id=#{id} and is_insure=1")
  int markInsured(@Param("id") long id, @Param("bizOrderSn") String bizOrderSn,
      @Param("numId") String numId, @Param("startTime") Object startTime,
      @Param("endTime") Object endTime, @Param("data") String data, @Param("now") long now);

  @Update("update ymcx_order_guest set is_insure=3,cancel_time=#{now},data=#{data},updatetime=#{now} where id=#{id} and is_insure=2")
  int markCancelled(@Param("id") long id, @Param("data") String data, @Param("now") long now);

  @Update("update ymcx_order_guest set data=#{data},updatetime=#{now} where id=#{id}")
  int saveResponse(@Param("id") long id, @Param("data") String data, @Param("now") long now);

  @Select("select count(*) from ymcx_order_guest where biz_order_sn=#{number}")
  int policyNumberCount(@Param("number") String number);
}
