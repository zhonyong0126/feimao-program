package com.feimao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.feimao.entity.BusinessOrder;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;

@Mapper
public interface BusinessOrderMapper extends BaseMapper<BusinessOrder> {
  @Select(
      "select id,type,merchant_car_id from ymcx_order where createtime<#{cutoff} and state=1 and"
          + " is_delete=1 for update")
  List<BusinessOrder> selectExpiredUnpaidForUpdate(@Param("cutoff") long cutoff);

  @Update(
      "update ymcx_order set state=6,updatetime=#{now} where id=#{id} and"
          + " state=1")
  int cancelUnpaid(@Param("id") long id, @Param("now") long now);

  @Update(
      "update ymcx_order set state=3,updatetime=#{now} where type=1 and state=2 and paytime is not"
          + " null and paytime<#{cutoff}")
  int startPaidLeases(@Param("now") long now, @Param("cutoff") long cutoff);

  @Update(
      "update ymcx_merchant_car set stock=stock+1,updatetime=#{now} where"
          + " id=#{merchantCarId} and deletetime is null")
  int restoreMerchantCarStock(
      @Param("merchantCarId") long merchantCarId, @Param("now") long now);

  @Select("select mc.id,mc.merchant_id,mc.platform_car_id,mc.status,mc.cash_price,mc.month_price,mc.day_price,mc.hour_price,p.name platform_car_name,p.name,p.image,p.brand_id,p.specification,m.pick_type,b.name brand from ymcx_merchant_car mc join ymcx_platform_car p on p.id=mc.platform_car_id and p.deletetime is null left join ymcx_merchant m on m.id=mc.merchant_id left join ymcx_brand b on b.id=p.brand_id and b.deletetime is null where mc.id=#{id} and mc.is_delete=1 and mc.deletetime is null and mc.status=1") Map<String,Object> rentableCar(@Param("id") long id);
  @Select("select mc.id,mc.merchant_id,mc.platform_car_id,mc.status,mc.cash_price,mc.month_price,mc.day_price,mc.hour_price,mc.stock,p.name,p.image from ymcx_merchant_car mc join ymcx_platform_car p on p.id=mc.platform_car_id and p.deletetime is null where mc.id=#{id} and mc.is_delete=1 and mc.deletetime is null and mc.status=1 for update") Map<String,Object> lockRentableCar(@Param("id") long id);
  @Select("select * from ymcx_merchant where id=#{id} and is_delete=1 and deletetime is null and status=1 and examine_status=2") Map<String,Object> activeMerchant(@Param("id") long id);
  @Select("select id,name,administrative,address,mobile,lng,lat from ymcx_address where id=#{id} and user_id=#{userId}") Map<String,Object> userAddress(@Param("id") long id,@Param("userId") long userId);
  @Select("select name,value from ymcx_config where name in ('lease_agreement','insure_agreement')") List<Map<String,Object>> leaseConfigs();
  @Select("select value from ymcx_config where name=#{name} limit 1") String configValue(@Param("name") String name);
  @Select("select count(*) from ymcx_order where number=#{number} and user_id=#{userId} and is_delete=1") int userOrderCount(@Param("number") String number,@Param("userId") long userId);
  @Select("select id,num,price,state,paytime from ymcx_order_child where number=#{number} order by num") List<Map<String,Object>> installmentDetails(@Param("number") String number);
  @Select("select id,number,child_number,num,price,state from ymcx_order_child where id=#{childId} for update") Map<String,Object> lockInstallment(@Param("childId") long childId);
  @Update("update ymcx_order_child set child_number=#{childNumber},updatetime=#{now} where id=#{id}") int assignInstallmentNumber(@Param("id") long id,@Param("childNumber") String childNumber,@Param("now") long now);
  @Select("select openid from ymcx_user where id=#{userId}") String userOpenid(@Param("userId") long userId);
  @Select("select o.number,o.type,o.order_name,o.price,o.discounts_price,o.state,u.openid from ymcx_order o join ymcx_user u on u.id=o.user_id where o.number=#{number}") Map<String,Object> paymentOrder(@Param("number") String number);
  @Select("select o.number,o.type,o.order_name,o.price,o.discounts_price,o.state,u.openid from ymcx_order o join ymcx_user u on u.id=o.user_id where o.number=#{number} and o.user_id=#{userId} and o.is_delete=1") Map<String,Object> userPaymentOrder(@Param("number") String number,@Param("userId") long userId);
  @Select({"<script>select o.id,o.merchant_id,o.number,o.service_name,o.service_image,o.num,o.pick_type,o.lease_type,o.lease_duration,o.lease_price,o.cash_price,o.type,o.pick_state,o.state,o.price,m.name merchant_name,concat(coalesce(m.administrative,''),coalesce(m.address,'')) merchant_address from ymcx_order o left join ymcx_merchant m on m.id=o.merchant_id and m.deletetime is null where o.is_delete=1 and o.user_id=#{userId}<if test='type &gt; 0'>and o.type=#{type}</if><if test='state &gt; 0'>and o.state=#{state}</if> order by o.createtime desc limit #{limit} offset #{offset}</script>"}) List<Map<String,Object>> userOrders(@Param("userId") long userId,@Param("type") int type,@Param("state") int state,@Param("limit") long limit,@Param("offset") long offset);
  @Select({"<script>select count(*) from ymcx_order o where o.is_delete=1 and o.user_id=#{userId}<if test='type &gt; 0'>and o.type=#{type}</if><if test='state &gt; 0'>and o.state=#{state}</if></script>"}) int userOrdersCount(@Param("userId") long userId,@Param("type") int type,@Param("state") int state);
  @Select("select m.name merchant_name,m.business merchant_business,m.administrative merchant_administrative,m.address merchant_address,m.mobile merchant_mobile,m.lat,m.lng,o.id,o.merchant_car_id,o.number,o.state,o.lease_money,o.pick_single,o.pick_price,o.insure_money,o.price,o.pick_type,o.service_name,o.service_image,o.num,o.pick_address,o.address_name,o.address_phone,o.type,o.createtime,o.discounts_price,o.pay_price,o.starttime,o.endtime,o.backtime,o.back_image,o.back_content,o.pick_state,o.cash_price,o.name,o.mobile,o.idcard,o.merchant_service_id,o.lease_type,o.lease_duration,o.lease_price,o.take_back_time,o.cancel_time,o.cancel_refund_price,concat('车辆品牌:',coalesce(b.name,'')) introductory from ymcx_order o join ymcx_merchant m on m.id=o.merchant_id and m.deletetime is null left join ymcx_merchant_car mc on mc.id=o.merchant_car_id and mc.deletetime is null left join ymcx_platform_car pc on pc.id=mc.platform_car_id and pc.deletetime is null left join ymcx_brand b on b.id=pc.brand_id and b.deletetime is null where o.number=#{number}") Map<String,Object> userOrder(@Param("number") String number,@Param("userId") long userId);
  @Select("select name,mobile,idcard from ymcx_order_guest where order_id=#{orderId} and is_delete=1 and deletetime is null order by id") List<Map<String,Object>> userOrderGuests(@Param("orderId") long orderId);
  @Select("select id,type,state,merchant_car_id from ymcx_order where number=#{number} and user_id=#{userId} and is_delete=1 for update") Map<String,Object> lockUserOrderForCancel(@Param("number") String number,@Param("userId") long userId);
  @Update("update ymcx_order set state=6,updatetime=#{now} where id=#{id} and state=1") int cancelUserOrder(@Param("id") long id,@Param("now") long now);
  @Select("select id,state,pick_state from ymcx_order where number=#{number} and user_id=#{userId} and is_delete=1 for update") Map<String,Object> lockUserOrderForReturn(@Param("number") String number,@Param("userId") long userId);
  @Update("update ymcx_order set pick_state=1,state=4,backtime=#{now},back_image=#{image},back_content=#{content},updatetime=#{now} where id=#{id}") int returnUserCar(@Param("id") long id,@Param("image") String image,@Param("content") String content,@Param("now") long now);
  @Select("select (select count(*) from ymcx_order where number=#{number})+(select count(*) from ymcx_order_child where child_number=#{number})") int orderNumberCount(@Param("number") String number);
  @Insert("insert into ymcx_order(user_id,merchant_id,number,type,order_name,service_name,service_image,merchant_car_id,pick_type,address_id,address_name,address_phone,pick_address,pick_point,price,num,name,mobile,idcard,lease_type,lease_duration,lease_price,take_back_time,cash_price,lease_money,pick_single,pick_price,insure_money,cash_state,state,pick_state,starttime,endtime,source_type,source_id,createtime,updatetime,is_delete) values(#{v.user_id},#{v.merchant_id},#{v.number},1,#{v.order_name},#{v.service_name},#{v.service_image},#{v.merchant_car_id},#{v.pick_type},#{v.address_id},#{v.address_name},#{v.address_phone},#{v.pick_address},#{v.pick_point},#{v.price},#{v.num},#{v.name},#{v.mobile},#{v.idcard},#{v.lease_type},#{v.lease_duration},#{v.lease_price},#{v.take_back_time},#{v.cash_price},#{v.lease_money},#{v.pick_single},#{v.pick_price},#{v.insure_money},#{v.cash_state},1,1,#{v.starttime},#{v.endtime},#{v.source_type},#{v.source_id},#{v.createtime},#{v.updatetime},1)")
  @Options(useGeneratedKeys=true,keyProperty="v.id") int insertLeaseOrder(@Param("v") Map<String,Object> values);
  @Insert("insert into ymcx_order_guest(user_id,order_id,name,mobile,idcard,createtime,updatetime,is_delete,insure_money) values(#{userId},#{orderId},#{name},#{mobile},#{idcard},#{now},#{now},1,#{insure})") int insertOrderGuest(@Param("userId") long userId,@Param("orderId") long orderId,@Param("name") String name,@Param("mobile") String mobile,@Param("idcard") String idcard,@Param("insure") java.math.BigDecimal insure,@Param("now") long now);
  @Update("update ymcx_merchant_car set stock=stock-1,updatetime=#{now} where id=#{id} and deletetime is null and stock>0") int decrementStock(@Param("id") long id,@Param("now") long now);
  @Select({"<script><choose><when test='type == 2'>select mc.id,mc.price,mc.merchant_id,p.name,p.image from ymcx_merchant_car mc join ymcx_platform_car p on p.id=mc.platform_car_id and p.deletetime is null where mc.id=#{id} and mc.deletetime is null</when><otherwise>select mm.id,mm.price,mm.merchant_id,m.name,m.image from ymcx_merchant_maintain mm join ymcx_maintain m on m.id=mm.maintain_id where mm.id=#{id} and mm.deletetime is null and m.deletetime is null</otherwise></choose></script>"}) Map<String,Object> simpleService(@Param("type") int type,@Param("id") long id);
  @Select("select c.id,c.price,c.reduceprice from ymcx_user_coupon c where c.id=#{id} and c.state=1 and c.user_id=#{userId} and c.deletetime is null for update") Map<String,Object> usableCoupon(@Param("id") long id,@Param("userId") long userId);
  @Insert("insert into ymcx_order(user_id,merchant_id,number,type,order_name,service_name,service_image,merchant_car_id,merchant_service_id,pick_type,pick_address,price,num,name,mobile,take_back_time,coupon_id,discounts_price,state,createtime,updatetime,is_delete) values(#{v.user_id},#{v.merchant_id},#{v.number},#{v.type},#{v.order_name},#{v.service_name},#{v.service_image},#{v.merchant_car_id},#{v.merchant_service_id},#{v.pick_type},#{v.pick_address},#{v.price},#{v.num},#{v.name},#{v.mobile},#{v.take_back_time},#{v.coupon_id},#{v.discounts_price},1,#{v.createtime},#{v.updatetime},1)")
  @Options(useGeneratedKeys=true,keyProperty="v.id") int insertSimpleOrder(@Param("v") Map<String,Object> values);
}
