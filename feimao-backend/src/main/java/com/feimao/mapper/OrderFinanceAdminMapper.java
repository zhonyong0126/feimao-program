package com.feimao.mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface OrderFinanceAdminMapper {
  @Select({"<script>select count(*) from ymcx_order o left join ymcx_user u on u.id=o.user_id left join ymcx_merchant m on m.id=o.merchant_id where o.is_delete=1",
      "<if test='type != null'>and o.type=#{type}</if><if test='state != null'>and o.state=#{state}</if><if test='merchantId != null and merchantId &gt; 0'>and o.merchant_id=#{merchantId}</if>",
      "<if test='leaseType != null'>and o.lease_type=#{leaseType}</if><if test='sourceType != null'>and o.source_type=#{sourceType}</if>",
      "<if test='priceMin != null'>and o.price&gt;=#{priceMin}</if><if test='priceMax != null'>and o.price&lt;=#{priceMax}</if><if test='discountMin != null'>and o.discounts_price&gt;=#{discountMin}</if><if test='discountMax != null'>and o.discounts_price&lt;=#{discountMax}</if>",
      "<if test='createdFrom != null'>and o.createtime&gt;=#{createdFrom}</if><if test='createdTo != null'>and o.createtime&lt;=#{createdTo}</if>",
      "<if test='ranges.startFrom != null'>and o.starttime&gt;=#{ranges.startFrom}</if><if test='ranges.startTo != null'>and o.starttime&lt;=#{ranges.startTo}</if><if test='ranges.endFrom != null'>and o.endtime&gt;=#{ranges.endFrom}</if><if test='ranges.endTo != null'>and o.endtime&lt;=#{ranges.endTo}</if>",
      "<if test='ranges.takeBackFrom != null'>and o.take_back_time&gt;=#{ranges.takeBackFrom}</if><if test='ranges.takeBackTo != null'>and o.take_back_time&lt;=#{ranges.takeBackTo}</if><if test='ranges.backFrom != null'>and o.backtime&gt;=#{ranges.backFrom}</if><if test='ranges.backTo != null'>and o.backtime&lt;=#{ranges.backTo}</if>",
      "<if test='orderNumber != null and orderNumber != \"\"'>and o.number like concat('%',#{orderNumber},'%')</if><if test='userName != null and userName != \"\"'>and u.nickname like concat('%',#{userName},'%')</if><if test='merchantName != null and merchantName != \"\"'>and m.name like concat('%',#{merchantName},'%')</if>",
      "<if test='orderName != null and orderName != \"\"'>and o.order_name like concat('%',#{orderName},'%')</if><if test='customerName != null and customerName != \"\"'>and o.name like concat('%',#{customerName},'%')</if><if test='mobile != null and mobile != \"\"'>and o.mobile like concat('%',#{mobile},'%')</if><if test='leaseDuration != null and leaseDuration != \"\"'>and cast(o.lease_duration as char) like concat('%',#{leaseDuration},'%')</if>",
      "<if test='keyword != null and keyword != \"\"'>and (o.number like concat('%',#{keyword},'%') or o.order_name like concat('%',#{keyword},'%') or o.name like concat('%',#{keyword},'%') or o.mobile like concat('%',#{keyword},'%') or u.nickname like concat('%',#{keyword},'%') or m.name like concat('%',#{keyword},'%'))</if></script>"})
  int orderCount(@Param("keyword") String keyword,@Param("orderNumber") String orderNumber,@Param("userName") String userName,@Param("merchantName") String merchantName,@Param("orderName") String orderName,@Param("customerName") String customerName,@Param("mobile") String mobile,@Param("leaseDuration") String leaseDuration,@Param("type") Integer type,@Param("state") Integer state,@Param("merchantId") Long merchantId,@Param("leaseType") Integer leaseType,@Param("sourceType") Integer sourceType,@Param("priceMin") BigDecimal priceMin,@Param("priceMax") BigDecimal priceMax,@Param("discountMin") BigDecimal discountMin,@Param("discountMax") BigDecimal discountMax,@Param("createdFrom") Long createdFrom,@Param("createdTo") Long createdTo,@Param("ranges") Map<String,Long> ranges);
  @Select({"<script>select o.*,u.nickname user_nickname,m.name merchant_name from ymcx_order o left join ymcx_user u on u.id=o.user_id left join ymcx_merchant m on m.id=o.merchant_id where o.is_delete=1",
      "<if test='type != null'>and o.type=#{type}</if><if test='state != null'>and o.state=#{state}</if><if test='merchantId != null and merchantId &gt; 0'>and o.merchant_id=#{merchantId}</if>",
      "<if test='leaseType != null'>and o.lease_type=#{leaseType}</if><if test='sourceType != null'>and o.source_type=#{sourceType}</if>",
      "<if test='priceMin != null'>and o.price&gt;=#{priceMin}</if><if test='priceMax != null'>and o.price&lt;=#{priceMax}</if><if test='discountMin != null'>and o.discounts_price&gt;=#{discountMin}</if><if test='discountMax != null'>and o.discounts_price&lt;=#{discountMax}</if>",
      "<if test='createdFrom != null'>and o.createtime&gt;=#{createdFrom}</if><if test='createdTo != null'>and o.createtime&lt;=#{createdTo}</if>",
      "<if test='ranges.startFrom != null'>and o.starttime&gt;=#{ranges.startFrom}</if><if test='ranges.startTo != null'>and o.starttime&lt;=#{ranges.startTo}</if><if test='ranges.endFrom != null'>and o.endtime&gt;=#{ranges.endFrom}</if><if test='ranges.endTo != null'>and o.endtime&lt;=#{ranges.endTo}</if>",
      "<if test='ranges.takeBackFrom != null'>and o.take_back_time&gt;=#{ranges.takeBackFrom}</if><if test='ranges.takeBackTo != null'>and o.take_back_time&lt;=#{ranges.takeBackTo}</if><if test='ranges.backFrom != null'>and o.backtime&gt;=#{ranges.backFrom}</if><if test='ranges.backTo != null'>and o.backtime&lt;=#{ranges.backTo}</if>",
      "<if test='orderNumber != null and orderNumber != \"\"'>and o.number like concat('%',#{orderNumber},'%')</if><if test='userName != null and userName != \"\"'>and u.nickname like concat('%',#{userName},'%')</if><if test='merchantName != null and merchantName != \"\"'>and m.name like concat('%',#{merchantName},'%')</if>",
      "<if test='orderName != null and orderName != \"\"'>and o.order_name like concat('%',#{orderName},'%')</if><if test='customerName != null and customerName != \"\"'>and o.name like concat('%',#{customerName},'%')</if><if test='mobile != null and mobile != \"\"'>and o.mobile like concat('%',#{mobile},'%')</if><if test='leaseDuration != null and leaseDuration != \"\"'>and cast(o.lease_duration as char) like concat('%',#{leaseDuration},'%')</if>",
      "<if test='keyword != null and keyword != \"\"'>and (o.number like concat('%',#{keyword},'%') or o.order_name like concat('%',#{keyword},'%') or o.name like concat('%',#{keyword},'%') or o.mobile like concat('%',#{keyword},'%') or u.nickname like concat('%',#{keyword},'%') or m.name like concat('%',#{keyword},'%'))</if>",
      "order by o.id desc limit #{limit} offset #{offset}</script>"})
  List<Map<String,Object>> orders(@Param("keyword") String keyword,@Param("orderNumber") String orderNumber,@Param("userName") String userName,@Param("merchantName") String merchantName,@Param("orderName") String orderName,@Param("customerName") String customerName,@Param("mobile") String mobile,@Param("leaseDuration") String leaseDuration,@Param("type") Integer type,@Param("state") Integer state,@Param("merchantId") Long merchantId,@Param("leaseType") Integer leaseType,@Param("sourceType") Integer sourceType,@Param("priceMin") BigDecimal priceMin,@Param("priceMax") BigDecimal priceMax,@Param("discountMin") BigDecimal discountMin,@Param("discountMax") BigDecimal discountMax,@Param("createdFrom") Long createdFrom,@Param("createdTo") Long createdTo,@Param("ranges") Map<String,Long> ranges,@Param("limit") int limit,@Param("offset") int offset);
  @Select({"<script>select coalesce(sum(o.price),0) total_price,coalesce(sum(o.pay_price),0) pay_price,count(*) order_count from ymcx_order o left join ymcx_user u on u.id=o.user_id left join ymcx_merchant m on m.id=o.merchant_id where o.is_delete=1",
      "<if test='type != null'>and o.type=#{type}</if><if test='state != null'>and o.state=#{state}</if><if test='merchantId != null and merchantId &gt; 0'>and o.merchant_id=#{merchantId}</if>",
      "<if test='leaseType != null'>and o.lease_type=#{leaseType}</if><if test='sourceType != null'>and o.source_type=#{sourceType}</if>",
      "<if test='priceMin != null'>and o.price&gt;=#{priceMin}</if><if test='priceMax != null'>and o.price&lt;=#{priceMax}</if><if test='discountMin != null'>and o.discounts_price&gt;=#{discountMin}</if><if test='discountMax != null'>and o.discounts_price&lt;=#{discountMax}</if>",
      "<if test='createdFrom != null'>and o.createtime&gt;=#{createdFrom}</if><if test='createdTo != null'>and o.createtime&lt;=#{createdTo}</if>",
      "<if test='ranges.startFrom != null'>and o.starttime&gt;=#{ranges.startFrom}</if><if test='ranges.startTo != null'>and o.starttime&lt;=#{ranges.startTo}</if><if test='ranges.endFrom != null'>and o.endtime&gt;=#{ranges.endFrom}</if><if test='ranges.endTo != null'>and o.endtime&lt;=#{ranges.endTo}</if>",
      "<if test='ranges.takeBackFrom != null'>and o.take_back_time&gt;=#{ranges.takeBackFrom}</if><if test='ranges.takeBackTo != null'>and o.take_back_time&lt;=#{ranges.takeBackTo}</if><if test='ranges.backFrom != null'>and o.backtime&gt;=#{ranges.backFrom}</if><if test='ranges.backTo != null'>and o.backtime&lt;=#{ranges.backTo}</if>",
      "<if test='orderNumber != null and orderNumber != \"\"'>and o.number like concat('%',#{orderNumber},'%')</if><if test='userName != null and userName != \"\"'>and u.nickname like concat('%',#{userName},'%')</if><if test='merchantName != null and merchantName != \"\"'>and m.name like concat('%',#{merchantName},'%')</if>",
      "<if test='orderName != null and orderName != \"\"'>and o.order_name like concat('%',#{orderName},'%')</if><if test='customerName != null and customerName != \"\"'>and o.name like concat('%',#{customerName},'%')</if><if test='mobile != null and mobile != \"\"'>and o.mobile like concat('%',#{mobile},'%')</if><if test='leaseDuration != null and leaseDuration != \"\"'>and cast(o.lease_duration as char) like concat('%',#{leaseDuration},'%')</if>",
      "<if test='keyword != null and keyword != \"\"'>and (o.number like concat('%',#{keyword},'%') or o.order_name like concat('%',#{keyword},'%') or o.name like concat('%',#{keyword},'%') or o.mobile like concat('%',#{keyword},'%') or u.nickname like concat('%',#{keyword},'%') or m.name like concat('%',#{keyword},'%'))</if></script>"})
  Map<String,Object> orderSummary(@Param("keyword") String keyword,@Param("orderNumber") String orderNumber,@Param("userName") String userName,@Param("merchantName") String merchantName,@Param("orderName") String orderName,@Param("customerName") String customerName,@Param("mobile") String mobile,@Param("leaseDuration") String leaseDuration,@Param("type") Integer type,@Param("state") Integer state,@Param("merchantId") Long merchantId,@Param("leaseType") Integer leaseType,@Param("sourceType") Integer sourceType,@Param("priceMin") BigDecimal priceMin,@Param("priceMax") BigDecimal priceMax,@Param("discountMin") BigDecimal discountMin,@Param("discountMax") BigDecimal discountMax,@Param("createdFrom") Long createdFrom,@Param("createdTo") Long createdTo,@Param("ranges") Map<String,Long> ranges);
  @Select("select o.*,u.nickname user_nickname,u.avatar user_avatar,m.name merchant_name,m.mobile merchant_mobile from ymcx_order o left join ymcx_user u on u.id=o.user_id left join ymcx_merchant m on m.id=o.merchant_id where o.id=#{id} and o.is_delete=1") Map<String,Object> order(@Param("id") long id);
  @Delete("delete from ymcx_order where id=#{id}") int deleteOrder(@Param("id") long id,@Param("now") long now);
  @Delete({"<script>delete from ymcx_order where id in",
      "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach></script>"})
  int deleteOrders(@Param("ids") List<Long> ids,@Param("now") long now);
  @Select("select * from ymcx_order_guest where order_id=#{id} and is_delete=1 and deletetime is null order by id") List<Map<String,Object>> orderGuests(@Param("id") long id);
  @Select("select * from ymcx_order_child where number=#{number} order by num,id") List<Map<String,Object>> orderInstallments(@Param("number") String number);

  @Select({"<script>select count(*) from ymcx_order_guest g left join ymcx_order o on o.id=g.order_id left join ymcx_user u on u.id=g.user_id where g.is_delete=1 and g.deletetime is null", "<if test='orderId != null and orderId &gt; 0'>and g.order_id=#{orderId}</if><if test='userName != null and userName != &quot;&quot;'>and u.nickname like concat('%',#{userName},'%')</if><if test='orderNumber != null and orderNumber != &quot;&quot;'>and o.number=#{orderNumber}</if><if test='name != null and name != &quot;&quot;'>and g.name like concat('%',#{name},'%')</if><if test='mobile != null and mobile != &quot;&quot;'>and g.mobile like concat('%',#{mobile},'%')</if><if test='idcard != null and idcard != &quot;&quot;'>and g.idcard like concat('%',#{idcard},'%')</if><if test='bizOrderSn != null and bizOrderSn != &quot;&quot;'>and g.biz_order_sn like concat('%',#{bizOrderSn},'%')</if><if test='insureState != null'>and g.is_insure=#{insureState}</if><if test='createdFrom != null'>and g.createtime&gt;=#{createdFrom}</if><if test='createdTo != null'>and g.createtime&lt;=#{createdTo}</if>", "</script>"})
  int guestCount(@Param("orderId") Long orderId,@Param("userName") String userName,@Param("orderNumber") String orderNumber,@Param("name") String name,@Param("mobile") String mobile,@Param("idcard") String idcard,@Param("bizOrderSn") String bizOrderSn,@Param("insureState") Integer insureState,@Param("createdFrom") Long createdFrom,@Param("createdTo") Long createdTo);
  @Select({"<script>select coalesce(sum(g.insure_money),0) from ymcx_order_guest g left join ymcx_order o on o.id=g.order_id left join ymcx_user u on u.id=g.user_id where g.is_delete=1 and g.deletetime is null", "<if test='orderId != null and orderId &gt; 0'>and g.order_id=#{orderId}</if><if test='userName != null and userName != &quot;&quot;'>and u.nickname like concat('%',#{userName},'%')</if><if test='orderNumber != null and orderNumber != &quot;&quot;'>and o.number=#{orderNumber}</if><if test='name != null and name != &quot;&quot;'>and g.name like concat('%',#{name},'%')</if><if test='mobile != null and mobile != &quot;&quot;'>and g.mobile like concat('%',#{mobile},'%')</if><if test='idcard != null and idcard != &quot;&quot;'>and g.idcard like concat('%',#{idcard},'%')</if><if test='bizOrderSn != null and bizOrderSn != &quot;&quot;'>and g.biz_order_sn like concat('%',#{bizOrderSn},'%')</if><if test='insureState != null'>and g.is_insure=#{insureState}</if><if test='createdFrom != null'>and g.createtime&gt;=#{createdFrom}</if><if test='createdTo != null'>and g.createtime&lt;=#{createdTo}</if>", "</script>"})
  BigDecimal guestInsuranceSum(@Param("orderId") Long orderId,@Param("userName") String userName,@Param("orderNumber") String orderNumber,@Param("name") String name,@Param("mobile") String mobile,@Param("idcard") String idcard,@Param("bizOrderSn") String bizOrderSn,@Param("insureState") Integer insureState,@Param("createdFrom") Long createdFrom,@Param("createdTo") Long createdTo);
  @Select({"<script>select g.*,o.number order_number,u.nickname user_nickname from ymcx_order_guest g left join ymcx_order o on o.id=g.order_id left join ymcx_user u on u.id=g.user_id where g.is_delete=1 and g.deletetime is null", "<if test='orderId != null and orderId &gt; 0'>and g.order_id=#{orderId}</if><if test='userName != null and userName != &quot;&quot;'>and u.nickname like concat('%',#{userName},'%')</if><if test='orderNumber != null and orderNumber != &quot;&quot;'>and o.number=#{orderNumber}</if><if test='name != null and name != &quot;&quot;'>and g.name like concat('%',#{name},'%')</if><if test='mobile != null and mobile != &quot;&quot;'>and g.mobile like concat('%',#{mobile},'%')</if><if test='idcard != null and idcard != &quot;&quot;'>and g.idcard like concat('%',#{idcard},'%')</if><if test='bizOrderSn != null and bizOrderSn != &quot;&quot;'>and g.biz_order_sn like concat('%',#{bizOrderSn},'%')</if><if test='insureState != null'>and g.is_insure=#{insureState}</if><if test='createdFrom != null'>and g.createtime&gt;=#{createdFrom}</if><if test='createdTo != null'>and g.createtime&lt;=#{createdTo}</if>", "order by g.id desc limit #{limit} offset #{offset}</script>"})
  List<Map<String,Object>> guests(@Param("orderId") Long orderId,@Param("userName") String userName,@Param("orderNumber") String orderNumber,@Param("name") String name,@Param("mobile") String mobile,@Param("idcard") String idcard,@Param("bizOrderSn") String bizOrderSn,@Param("insureState") Integer insureState,@Param("createdFrom") Long createdFrom,@Param("createdTo") Long createdTo,@Param("limit") int limit,@Param("offset") int offset);

  @Select({"<script>select count(*) from ymcx_order_child c where 1=1", "<if test='userId != null'>and c.user_id=#{userId}</if><if test='number != null and number != &quot;&quot;'>and c.number=#{number}</if><if test='childNumber != null and childNumber != &quot;&quot;'>and c.child_number=#{childNumber}</if><if test='num != null'>and c.num=#{num}</if><if test='priceMin != null'>and c.price&gt;=#{priceMin}</if><if test='priceMax != null'>and c.price&lt;=#{priceMax}</if><if test='payPriceMin != null'>and c.pay_price&gt;=#{payPriceMin}</if><if test='payPriceMax != null'>and c.pay_price&lt;=#{payPriceMax}</if><if test='transactionId != null and transactionId != &quot;&quot;'>and c.transaction_id like concat('%',#{transactionId},'%')</if><if test='times.payFrom != null'>and c.paytime&gt;=#{times.payFrom}</if><if test='times.payTo != null'>and c.paytime&lt;=#{times.payTo}</if><if test='times.createdFrom != null'>and c.createtime&gt;=#{times.createdFrom}</if><if test='times.createdTo != null'>and c.createtime&lt;=#{times.createdTo}</if><if test='times.updatedFrom != null'>and c.updatetime&gt;=#{times.updatedFrom}</if><if test='times.updatedTo != null'>and c.updatetime&lt;=#{times.updatedTo}</if>", "</script>"})
  int installmentCount(@Param("userId") Long userId,@Param("number") String number,@Param("childNumber") String childNumber,@Param("num") Integer num,@Param("priceMin") BigDecimal priceMin,@Param("priceMax") BigDecimal priceMax,@Param("payPriceMin") BigDecimal payPriceMin,@Param("payPriceMax") BigDecimal payPriceMax,@Param("transactionId") String transactionId,@Param("times") Map<String,Long> times);
  @Select({"<script>select c.* from ymcx_order_child c where 1=1", "<if test='userId != null'>and c.user_id=#{userId}</if><if test='number != null and number != &quot;&quot;'>and c.number=#{number}</if><if test='childNumber != null and childNumber != &quot;&quot;'>and c.child_number=#{childNumber}</if><if test='num != null'>and c.num=#{num}</if><if test='priceMin != null'>and c.price&gt;=#{priceMin}</if><if test='priceMax != null'>and c.price&lt;=#{priceMax}</if><if test='payPriceMin != null'>and c.pay_price&gt;=#{payPriceMin}</if><if test='payPriceMax != null'>and c.pay_price&lt;=#{payPriceMax}</if><if test='transactionId != null and transactionId != &quot;&quot;'>and c.transaction_id like concat('%',#{transactionId},'%')</if><if test='times.payFrom != null'>and c.paytime&gt;=#{times.payFrom}</if><if test='times.payTo != null'>and c.paytime&lt;=#{times.payTo}</if><if test='times.createdFrom != null'>and c.createtime&gt;=#{times.createdFrom}</if><if test='times.createdTo != null'>and c.createtime&lt;=#{times.createdTo}</if><if test='times.updatedFrom != null'>and c.updatetime&gt;=#{times.updatedFrom}</if><if test='times.updatedTo != null'>and c.updatetime&lt;=#{times.updatedTo}</if>", "order by c.id desc limit #{limit} offset #{offset}</script>"})
  List<Map<String,Object>> installments(@Param("userId") Long userId,@Param("number") String number,@Param("childNumber") String childNumber,@Param("num") Integer num,@Param("priceMin") BigDecimal priceMin,@Param("priceMax") BigDecimal priceMax,@Param("payPriceMin") BigDecimal payPriceMin,@Param("payPriceMax") BigDecimal payPriceMax,@Param("transactionId") String transactionId,@Param("times") Map<String,Long> times,@Param("limit") int limit,@Param("offset") int offset);
  @Select("select id,user_id,type,lease_type,state from ymcx_order where number=#{number} and is_delete=1") Map<String,Object> installmentOrder(@Param("number") String number);
  @Select("select count(*) from ymcx_order_child where number=#{number} and num=#{num} and id<>#{excludedId}") int duplicateInstallment(@Param("number") String number,@Param("num") int num,@Param("excludedId") long excludedId);
  @Insert("insert into ymcx_order_child(user_id,number,child_number,num,price,pay_price,transaction_id,paytime,state,createtime,updatetime) values(#{v.user_id},#{v.number},#{v.child_number},#{v.num},#{v.price},#{v.pay_price},#{v.transaction_id},#{v.paytime},1,#{v.createtime},#{v.updatetime})")
  @Options(useGeneratedKeys=true,keyProperty="v.id") int insertInstallment(@Param("v") Map<String,Object> values);
  @Update("update ymcx_order_child set user_id=#{v.user_id},number=#{v.number},child_number=#{v.child_number},num=#{v.num},price=#{v.price},pay_price=#{v.pay_price},transaction_id=#{v.transaction_id},paytime=#{v.paytime},updatetime=#{v.updatetime} where id=#{id}") int updateInstallment(@Param("id") long id,@Param("v") Map<String,Object> values);
  @Select("select id,number,state from ymcx_order_child where id=#{id}") Map<String,Object> installment(@Param("id") long id);
  @Delete("delete from ymcx_order_child where id=#{id}") int deleteInstallment(@Param("id") long id);

  @Select({"<script>select count(*) from ymcx_user_coupon c left join ymcx_user u on u.id=c.user_id where c.deletetime is null", "<if test='userId != null'>and c.user_id=#{userId}</if><if test='state != null'>and c.state=#{state}</if><if test='priceMin != null'>and c.price&gt;=#{priceMin}</if><if test='priceMax != null'>and c.price&lt;=#{priceMax}</if><if test='reduceMin != null'>and c.reduceprice&gt;=#{reduceMin}</if><if test='reduceMax != null'>and c.reduceprice&lt;=#{reduceMax}</if><if test='userName != null and userName != &quot;&quot;'>and u.nickname like concat('%',#{userName},'%')</if><if test='times.endFrom != null'>and c.endtime&gt;=#{times.endFrom}</if><if test='times.endTo != null'>and c.endtime&lt;=#{times.endTo}</if><if test='times.createdFrom != null'>and c.createtime&gt;=#{times.createdFrom}</if><if test='times.createdTo != null'>and c.createtime&lt;=#{times.createdTo}</if><if test='times.updatedFrom != null'>and c.updatetime&gt;=#{times.updatedFrom}</if><if test='times.updatedTo != null'>and c.updatetime&lt;=#{times.updatedTo}</if><if test='times.usedFrom != null'>and c.usetime&gt;=#{times.usedFrom}</if><if test='times.usedTo != null'>and c.usetime&lt;=#{times.usedTo}</if>", "</script>"})
  int userCouponCount(@Param("userId") Long userId,@Param("state") Integer state,@Param("priceMin") BigDecimal priceMin,@Param("priceMax") BigDecimal priceMax,@Param("reduceMin") BigDecimal reduceMin,@Param("reduceMax") BigDecimal reduceMax,@Param("userName") String userName,@Param("times") Map<String,Long> times);
  @Select({"<script>select c.*,u.nickname user_nickname from ymcx_user_coupon c left join ymcx_user u on u.id=c.user_id where c.deletetime is null", "<if test='userId != null'>and c.user_id=#{userId}</if><if test='state != null'>and c.state=#{state}</if><if test='priceMin != null'>and c.price&gt;=#{priceMin}</if><if test='priceMax != null'>and c.price&lt;=#{priceMax}</if><if test='reduceMin != null'>and c.reduceprice&gt;=#{reduceMin}</if><if test='reduceMax != null'>and c.reduceprice&lt;=#{reduceMax}</if><if test='userName != null and userName != &quot;&quot;'>and u.nickname like concat('%',#{userName},'%')</if><if test='times.endFrom != null'>and c.endtime&gt;=#{times.endFrom}</if><if test='times.endTo != null'>and c.endtime&lt;=#{times.endTo}</if><if test='times.createdFrom != null'>and c.createtime&gt;=#{times.createdFrom}</if><if test='times.createdTo != null'>and c.createtime&lt;=#{times.createdTo}</if><if test='times.updatedFrom != null'>and c.updatetime&gt;=#{times.updatedFrom}</if><if test='times.updatedTo != null'>and c.updatetime&lt;=#{times.updatedTo}</if><if test='times.usedFrom != null'>and c.usetime&gt;=#{times.usedFrom}</if><if test='times.usedTo != null'>and c.usetime&lt;=#{times.usedTo}</if>", "order by c.id desc limit #{limit} offset #{offset}</script>"})
  List<Map<String,Object>> userCoupons(@Param("userId") Long userId,@Param("state") Integer state,@Param("priceMin") BigDecimal priceMin,@Param("priceMax") BigDecimal priceMax,@Param("reduceMin") BigDecimal reduceMin,@Param("reduceMax") BigDecimal reduceMax,@Param("userName") String userName,@Param("times") Map<String,Long> times,@Param("limit") int limit,@Param("offset") int offset);
  @Select({"<script>select count(*) from ymcx_sign_log s where 1=1","<if test='userId != null'>and s.user_id=#{userId}</if><if test='createdFrom != null and createdFrom != &quot;&quot;'>and s.createtime&gt;=#{createdFrom}</if><if test='createdTo != null and createdTo != &quot;&quot;'>and s.createtime&lt;=#{createdTo}</if></script>"}) int signLogCount(@Param("userId") Long userId,@Param("createdFrom") String createdFrom,@Param("createdTo") String createdTo);
  @Select({"<script>select s.* from ymcx_sign_log s where 1=1","<if test='userId != null'>and s.user_id=#{userId}</if><if test='createdFrom != null and createdFrom != &quot;&quot;'>and s.createtime&gt;=#{createdFrom}</if><if test='createdTo != null and createdTo != &quot;&quot;'>and s.createtime&lt;=#{createdTo}</if>","order by s.id desc limit #{limit} offset #{offset}</script>"}) List<Map<String,Object>> signLogs(@Param("userId") Long userId,@Param("createdFrom") String createdFrom,@Param("createdTo") String createdTo,@Param("limit") int limit,@Param("offset") int offset);
  @Insert("insert into ymcx_sign_log(user_id,score,createtime) values(#{userId},#{score},current_timestamp)")
  @Options(useGeneratedKeys=true,keyProperty="values.id")
  int insertSignLog(@Param("userId") long userId,@Param("score") int score,@Param("values") Map<String,Object> values);
  @Update("update ymcx_sign_log set user_id=#{userId},score=#{score} where id=#{id}") int updateSignLog(@Param("id") long id,@Param("userId") long userId,@Param("score") int score);
  @Delete("delete from ymcx_sign_log where id=#{id}") int deleteSignLog(@Param("id") long id);
  @Select({"<script>select count(*) from ymcx_admin_log where 1=1","<if test='allowedIds != null'>and admin_id in <foreach collection='allowedIds' item='id' open='(' separator=',' close=')'>#{id}</foreach></if>","<if test='adminId != null'>and admin_id=#{adminId}</if><if test='username != null and username != \"\"'>and username=#{username}</if><if test='title != null and title != \"\"'>and title like concat('%',#{title},'%')</if><if test='url != null and url != \"\"'>and url like concat('%',#{url},'%')</if><if test='ip != null and ip != \"\"'>and ip=#{ip}</if><if test='createdFrom != null and createdFrom != \"\"'>and createtime&gt;=#{createdFrom}</if><if test='createdTo != null and createdTo != \"\"'>and createtime&lt;=#{createdTo}</if></script>"}) int operationLogCount(@Param("allowedIds") List<Long> allowedIds,@Param("adminId") Long adminId,@Param("username") String username,@Param("title") String title,@Param("url") String url,@Param("ip") String ip,@Param("createdFrom") Long createdFrom,@Param("createdTo") Long createdTo);
  @Select({"<script>select * from ymcx_admin_log where 1=1","<if test='allowedIds != null'>and admin_id in <foreach collection='allowedIds' item='id' open='(' separator=',' close=')'>#{id}</foreach></if>","<if test='adminId != null'>and admin_id=#{adminId}</if><if test='username != null and username != \"\"'>and username=#{username}</if><if test='title != null and title != \"\"'>and title like concat('%',#{title},'%')</if><if test='url != null and url != \"\"'>and url like concat('%',#{url},'%')</if><if test='ip != null and ip != \"\"'>and ip=#{ip}</if><if test='createdFrom != null'>and createtime&gt;=#{createdFrom}</if><if test='createdTo != null'>and createtime&lt;=#{createdTo}</if>","order by id desc limit #{limit} offset #{offset}</script>"}) List<Map<String,Object>> operationLogs(@Param("allowedIds") List<Long> allowedIds,@Param("adminId") Long adminId,@Param("username") String username,@Param("title") String title,@Param("url") String url,@Param("ip") String ip,@Param("createdFrom") Long createdFrom,@Param("createdTo") Long createdTo,@Param("limit") int limit,@Param("offset") int offset);
  @Select("select group_id from ymcx_auth_group_access where uid=#{adminId}") List<Long> adminGroupIds(@Param("adminId") long adminId);
  @Select("select id,pid from ymcx_auth_group") List<Map<String,Object>> adminGroupTree();
  @Select({"<script>select distinct uid from ymcx_auth_group_access where group_id in <foreach collection='groupIds' item='id' open='(' separator=',' close=')'>#{id}</foreach></script>"}) List<Long> adminsInGroups(@Param("groupIds") List<Long> groupIds);
  @Select("select id,user_id,type from ymcx_order where id=#{id} and is_delete=1")
  Map<String, Object> orderForGuest(@Param("id") long id);

  @Insert(
      "insert into ymcx_order_guest(user_id,order_id,name,mobile,idcard,createtime,updatetime,is_delete,is_insure,insure_money) values(#{v.user_id},#{v.order_id},#{v.name},#{v.mobile},#{v.idcard},#{v.createtime},#{v.updatetime},1,1,#{v.insure_money})")
  @Options(useGeneratedKeys = true, keyProperty = "v.id")
  int insertGuest(@Param("v") Map<String, Object> values);

  @Update(
      "update ymcx_order_guest set name=#{name},mobile=#{mobile},idcard=#{idcard},updatetime=#{now} where id=#{id} and is_delete=1 and deletetime is null")
  int updateGuest(
      @Param("id") long id,
      @Param("name") String name,
      @Param("mobile") String mobile,
      @Param("idcard") String idcard,
      @Param("money") BigDecimal money,
      @Param("now") long now);

  @Update(
      "update ymcx_order_guest set is_delete=2,deletetime=#{now},updatetime=#{now} where id=#{id} and is_delete=1 and deletetime is null")
  int deleteGuest(@Param("id") long id, @Param("now") long now);

  @Select("select count(*) from ymcx_order_guest where deletetime is not null")
  int recycledGuestCount();

  @Select(
      "select g.*,o.number order_number,u.nickname user_nickname from ymcx_order_guest g left join ymcx_order o on o.id=g.order_id left join ymcx_user u on u.id=g.user_id where g.deletetime is not null order by g.deletetime desc,g.id desc limit #{limit} offset #{offset}")
  List<Map<String, Object>> recycledGuests(
      @Param("limit") int limit, @Param("offset") int offset);

  @Update(
      "update ymcx_order_guest set is_delete=1,deletetime=null,updatetime=#{now} where id=#{id} and deletetime is not null")
  int restoreGuest(@Param("id") long id, @Param("now") long now);

  @Update({
    "<script>update ymcx_order_guest set is_delete=1,deletetime=null,updatetime=#{now} where deletetime is not null",
    "<if test='ids != null and !ids.isEmpty()'>and id in <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach></if>",
    "</script>"
  })
  int restoreGuests(@Param("ids") List<Long> ids, @Param("now") long now);

  @Delete("delete from ymcx_order_guest where id=#{id} and deletetime is not null")
  int destroyGuest(@Param("id") long id);

  @Delete({
    "<script>delete from ymcx_order_guest where deletetime is not null",
    "<if test='ids != null and !ids.isEmpty()'>and id in <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach></if>",
    "</script>"
  })
  int destroyGuests(@Param("ids") List<Long> ids);

  @Select("select count(*) from ymcx_user where id=#{id}")
  int userExists(@Param("id") long id);

  @Insert(
      "insert into ymcx_user_coupon(user_id,price,reduceprice,state,endtime,usetime,createtime,updatetime) values(#{v.user_id},#{v.price},#{v.reduceprice},#{v.state},#{v.endtime},#{v.usetime},#{v.createtime},#{v.updatetime})")
  @Options(useGeneratedKeys = true, keyProperty = "v.id")
  int insertUserCoupon(@Param("v") Map<String, Object> values);

  @Update(
      "update ymcx_user_coupon set user_id=#{v.user_id},price=#{v.price},reduceprice=#{v.reduceprice},state=#{v.state},endtime=#{v.endtime},usetime=#{v.usetime},updatetime=#{v.updatetime} where id=#{id} and deletetime is null")
  int updateUserCoupon(@Param("id") long id, @Param("v") Map<String, Object> values);

  @Update(
      "update ymcx_user_coupon set deletetime=#{now},updatetime=#{now} where id=#{id} and deletetime is null")
  int deleteUserCoupon(@Param("id") long id, @Param("now") long now);
  @Select("select count(*) from ymcx_user_coupon where deletetime is not null")
  int recycledUserCouponCount();
  @Select("select c.*,u.nickname user_nickname from ymcx_user_coupon c left join ymcx_user u on u.id=c.user_id where c.deletetime is not null order by c.id desc limit #{limit} offset #{offset}")
  List<Map<String,Object>> recycledUserCoupons(@Param("limit") int limit,@Param("offset") int offset);
  @Update("update ymcx_user_coupon set deletetime=null,updatetime=#{now} where id=#{id} and deletetime is not null")
  int restoreUserCoupon(@Param("id") long id,@Param("now") long now);
  @Delete("delete from ymcx_user_coupon where id=#{id} and deletetime is not null")
  int destroyUserCoupon(@Param("id") long id);
  @Update({"<script>update ymcx_user_coupon set deletetime=#{now},updatetime=#{now} where deletetime is null and id in", "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach></script>"})
  int deleteUserCoupons(@Param("ids") List<Long> ids,@Param("now") long now);
  @Update({"<script>update ymcx_user_coupon set deletetime=null,updatetime=#{now} where deletetime is not null and id in", "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach></script>"})
  int restoreUserCoupons(@Param("ids") List<Long> ids,@Param("now") long now);
  @Update("update ymcx_user_coupon set deletetime=null,updatetime=#{now} where deletetime is not null")
  int restoreAllUserCoupons(@Param("now") long now);
  @Delete({"<script>delete from ymcx_user_coupon where deletetime is not null and id in", "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach></script>"})
  int destroyUserCoupons(@Param("ids") List<Long> ids);
  @Delete("delete from ymcx_user_coupon where deletetime is not null")
  int destroyAllUserCoupons();

  @Select({
    "<script>select * from ymcx_admin_log where id=#{id}",
    "<if test='allowedIds != null'>and admin_id in <foreach collection='allowedIds' item='aid' open='(' separator=',' close=')'>#{aid}</foreach></if>",
    "</script>"
  })
  Map<String, Object> operationLog(@Param("id") long id, @Param("allowedIds") List<Long> allowedIds);

  @Delete({
    "<script>delete from ymcx_admin_log where id in",
    "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>",
    "<if test='allowedIds != null'>and admin_id in <foreach collection='allowedIds' item='aid' open='(' separator=',' close=')'>#{aid}</foreach></if>",
    "</script>"
  })
  int deleteOperationLogs(@Param("ids") List<Long> ids, @Param("allowedIds") List<Long> allowedIds);

  @Select({"<script><choose>",
      "<when test='kind == \"merchant\"'>select count(*) from ymcx_merchant_money_log l where l.is_delete=1 and l.deletetime is null <if test='ownerId != null and ownerId &gt; 0'>and l.merchant_id=#{ownerId}</if></when>",
      "<when test='kind == \"shop\"'>select count(*) from ymcx_shop_money_log l where l.is_delete=1 and l.deletetime is null <if test='ownerId != null and ownerId &gt; 0'>and l.shop_id=#{ownerId}</if></when>",
      "<when test='kind == \"system\"'>select count(*) from ymcx_system_money_log l where l.is_delete=1 and l.deletetime is null</when>",
      "<otherwise>select count(*) from ymcx_platform_log l where l.is_delete=1 and l.deletetime is null <if test='ownerId != null and ownerId &gt; 0'>and l.user_id=#{ownerId}</if></otherwise>",
      "</choose><if test='type != null'>and l.type=#{type}</if></script>"})
  int financeCount(@Param("kind") String kind,@Param("ownerId") Long ownerId,@Param("type") Integer type);

  @Select({"<script><choose>",
      "<when test='kind == \"merchant\"'>select coalesce(sum(l.money),0) from ymcx_merchant_money_log l where l.is_delete=1 and l.deletetime is null <if test='ownerId != null and ownerId &gt; 0'>and l.merchant_id=#{ownerId}</if></when>",
      "<when test='kind == \"shop\"'>select coalesce(sum(l.money),0) from ymcx_shop_money_log l where l.is_delete=1 and l.deletetime is null <if test='ownerId != null and ownerId &gt; 0'>and l.shop_id=#{ownerId}</if></when>",
      "<when test='kind == \"system\"'>select coalesce(sum(l.money),0) from ymcx_system_money_log l where l.is_delete=1 and l.deletetime is null</when>",
      "<otherwise>select coalesce(sum(l.amount),0) from ymcx_platform_log l where l.is_delete=1 and l.deletetime is null <if test='ownerId != null and ownerId &gt; 0'>and l.user_id=#{ownerId}</if></otherwise>",
      "</choose><if test='type != null'>and l.type=#{type}</if></script>"})
  BigDecimal financeAmount(@Param("kind") String kind,@Param("ownerId") Long ownerId,@Param("type") Integer type);

  @Select({"<script><choose>",
      "<when test='kind == \"merchant\"'>select l.*,x.name owner_name,o.number order_number from ymcx_merchant_money_log l left join ymcx_merchant x on x.id=l.merchant_id and x.deletetime is null left join ymcx_order o on o.id=l.order_id where l.is_delete=1 and l.deletetime is null <if test='ownerId != null and ownerId &gt; 0'>and l.merchant_id=#{ownerId}</if></when>",
      "<when test='kind == \"shop\"'>select l.*,x.name owner_name,o.number order_number from ymcx_shop_money_log l left join ymcx_shop x on x.id=l.shop_id and x.deletetime is null left join ymcx_order o on o.id=l.order_id where l.is_delete=1 and l.deletetime is null <if test='ownerId != null and ownerId &gt; 0'>and l.shop_id=#{ownerId}</if></when>",
      "<when test='kind == \"system\"'>select l.*,o.number order_number from ymcx_system_money_log l left join ymcx_order o on o.id=l.order_id where l.is_delete=1 and l.deletetime is null</when>",
      "<otherwise>select l.*,o.number order_number from ymcx_platform_log l left join ymcx_order o on o.id=l.order_id where l.is_delete=1 and l.deletetime is null <if test='ownerId != null and ownerId &gt; 0'>and l.user_id=#{ownerId}</if></otherwise>",
      "</choose><if test='type != null'>and l.type=#{type}</if> order by l.id desc limit #{limit} offset #{offset}</script>"})
  List<Map<String,Object>> financeRows(@Param("kind") String kind,@Param("ownerId") Long ownerId,@Param("type") Integer type,@Param("limit") int limit,@Param("offset") int offset);

  @Select({
      "<script>",
      "select count(*) from ymcx_platform_log l",
      "left join ymcx_order o on l.type in (1,2,5) and o.id=cast(l.order_id as unsigned)",
      "left join ymcx_merchant_cash mc on l.type=3 and mc.id=cast(l.order_id as unsigned)",
      "left join ymcx_shop_cash sc on l.type=4 and sc.id=cast(l.order_id as unsigned)",
      "left join ymcx_user u on l.user_type=1 and u.id=l.user_id",
      "left join ymcx_merchant m on l.user_type=2 and m.id=l.user_id",
      "left join ymcx_shop s on l.user_type=3 and s.id=l.user_id",
      "where l.is_delete=1 and l.deletetime is null",
      "<if test='ownerId != null and ownerId &gt; 0'>and l.user_id=#{ownerId}</if>",
      "<if test='type != null'>and l.type=#{type}</if>",
      "<if test='nickname != null and nickname != \"\"'>and (case l.user_type when 1 then u.nickname when 2 then m.name when 3 then s.name else '' end) like concat('%',#{nickname},'%')</if>",
      "<if test='outTradeNo != null and outTradeNo != \"\"'>and (case when l.type in (1,2,5) then o.number when l.type=3 then mc.out_trade_no when l.type=4 then sc.out_trade_no else '' end) like concat('%',#{outTradeNo},'%')</if>",
      "<if test='amountMin != null'>and l.amount &gt;= #{amountMin}</if>",
      "<if test='amountMax != null'>and l.amount &lt;= #{amountMax}</if>",
      "<if test='createdFrom != null'>and l.createtime &gt;= #{createdFrom}</if>",
      "<if test='createdTo != null'>and l.createtime &lt;= #{createdTo}</if>",
      "</script>"
  })
  int platformFinanceCount(
      @Param("ownerId") Long ownerId,
      @Param("type") Integer type,
      @Param("nickname") String nickname,
      @Param("outTradeNo") String outTradeNo,
      @Param("amountMin") BigDecimal amountMin,
      @Param("amountMax") BigDecimal amountMax,
      @Param("createdFrom") Long createdFrom,
      @Param("createdTo") Long createdTo);

  @Select({
      "<script>",
      "select l.* ,",
      "case l.user_type when 1 then u.nickname when 2 then m.name when 3 then s.name else '' end nickname,",
      "case l.user_type when 1 then u.nickname when 2 then m.name when 3 then s.name else '' end owner_name,",
      "case when l.type in (1,2,5) then o.number when l.type=3 then mc.out_trade_no when l.type=4 then sc.out_trade_no else '' end out_trade_no,",
      "case when l.type in (1,2,5) then o.number when l.type=3 then mc.out_trade_no when l.type=4 then sc.out_trade_no else '' end order_number",
      "from ymcx_platform_log l",
      "left join ymcx_order o on l.type in (1,2,5) and o.id=cast(l.order_id as unsigned)",
      "left join ymcx_merchant_cash mc on l.type=3 and mc.id=cast(l.order_id as unsigned)",
      "left join ymcx_shop_cash sc on l.type=4 and sc.id=cast(l.order_id as unsigned)",
      "left join ymcx_user u on l.user_type=1 and u.id=l.user_id",
      "left join ymcx_merchant m on l.user_type=2 and m.id=l.user_id",
      "left join ymcx_shop s on l.user_type=3 and s.id=l.user_id",
      "where l.is_delete=1 and l.deletetime is null",
      "<if test='ownerId != null and ownerId &gt; 0'>and l.user_id=#{ownerId}</if>",
      "<if test='type != null'>and l.type=#{type}</if>",
      "<if test='nickname != null and nickname != \"\"'>and (case l.user_type when 1 then u.nickname when 2 then m.name when 3 then s.name else '' end) like concat('%',#{nickname},'%')</if>",
      "<if test='outTradeNo != null and outTradeNo != \"\"'>and (case when l.type in (1,2,5) then o.number when l.type=3 then mc.out_trade_no when l.type=4 then sc.out_trade_no else '' end) like concat('%',#{outTradeNo},'%')</if>",
      "<if test='amountMin != null'>and l.amount &gt;= #{amountMin}</if>",
      "<if test='amountMax != null'>and l.amount &lt;= #{amountMax}</if>",
      "<if test='createdFrom != null'>and l.createtime &gt;= #{createdFrom}</if>",
      "<if test='createdTo != null'>and l.createtime &lt;= #{createdTo}</if>",
      "order by l.id desc limit #{limit} offset #{offset}",
      "</script>"
  })
  List<Map<String,Object>> platformFinanceRows(
      @Param("ownerId") Long ownerId,
      @Param("type") Integer type,
      @Param("nickname") String nickname,
      @Param("outTradeNo") String outTradeNo,
      @Param("amountMin") BigDecimal amountMin,
      @Param("amountMax") BigDecimal amountMax,
      @Param("createdFrom") Long createdFrom,
      @Param("createdTo") Long createdTo,
      @Param("limit") int limit,
      @Param("offset") int offset);

  @Select({
      "<script>",
      "select coalesce(sum(l.amount),0) money,",
      "coalesce(sum(case when l.type=1 then l.amount else 0 end),0) in_money,",
      "coalesce(sum(case when l.type in (2,3,4,5) then l.amount else 0 end),0) out_money,",
      "coalesce(sum(case when l.type in (3,4) then l.amount else 0 end),0) cash_money",
      "from ymcx_platform_log l",
      "left join ymcx_order o on l.type in (1,2,5) and o.id=cast(l.order_id as unsigned)",
      "left join ymcx_merchant_cash mc on l.type=3 and mc.id=cast(l.order_id as unsigned)",
      "left join ymcx_shop_cash sc on l.type=4 and sc.id=cast(l.order_id as unsigned)",
      "left join ymcx_user u on l.user_type=1 and u.id=l.user_id",
      "left join ymcx_merchant m on l.user_type=2 and m.id=l.user_id",
      "left join ymcx_shop s on l.user_type=3 and s.id=l.user_id",
      "where l.is_delete=1 and l.deletetime is null",
      "<if test='ownerId != null and ownerId &gt; 0'>and l.user_id=#{ownerId}</if>",
      "<if test='type != null'>and l.type=#{type}</if>",
      "<if test='nickname != null and nickname != \"\"'>and (case l.user_type when 1 then u.nickname when 2 then m.name when 3 then s.name else '' end) like concat('%',#{nickname},'%')</if>",
      "<if test='outTradeNo != null and outTradeNo != \"\"'>and (case when l.type in (1,2,5) then o.number when l.type=3 then mc.out_trade_no when l.type=4 then sc.out_trade_no else '' end) like concat('%',#{outTradeNo},'%')</if>",
      "<if test='amountMin != null'>and l.amount &gt;= #{amountMin}</if>",
      "<if test='amountMax != null'>and l.amount &lt;= #{amountMax}</if>",
      "<if test='createdFrom != null'>and l.createtime &gt;= #{createdFrom}</if>",
      "<if test='createdTo != null'>and l.createtime &lt;= #{createdTo}</if>",
      "</script>"
  })
  Map<String,Object> platformFinanceSummary(
      @Param("ownerId") Long ownerId,
      @Param("type") Integer type,
      @Param("nickname") String nickname,
      @Param("outTradeNo") String outTradeNo,
      @Param("amountMin") BigDecimal amountMin,
      @Param("amountMax") BigDecimal amountMax,
      @Param("createdFrom") Long createdFrom,
      @Param("createdTo") Long createdTo);

  @Select({
      "<script>select count(*) from ymcx_system_money_log l left join ymcx_order o on o.id=l.order_id",
      "where l.is_delete=1 and l.deletetime is null",
      "<if test='type != null'>and l.type=#{type}</if>",
      "<if test='orderNumber != null and orderNumber != \"\"'>and o.number like concat('%',#{orderNumber},'%')</if>",
      "<if test='amountMin != null'>and l.money &gt;= #{amountMin}</if>",
      "<if test='amountMax != null'>and l.money &lt;= #{amountMax}</if>",
      "<if test='createdFrom != null'>and l.createtime &gt;= #{createdFrom}</if>",
      "<if test='createdTo != null'>and l.createtime &lt;= #{createdTo}</if>",
      "</script>"
  })
  int systemFinanceCount(
      @Param("type") Integer type,
      @Param("orderNumber") String orderNumber,
      @Param("amountMin") BigDecimal amountMin,
      @Param("amountMax") BigDecimal amountMax,
      @Param("createdFrom") Long createdFrom,
      @Param("createdTo") Long createdTo);

  @Select({
      "<script>select l.*,o.number order_number from ymcx_system_money_log l left join ymcx_order o on o.id=l.order_id",
      "where l.is_delete=1 and l.deletetime is null",
      "<if test='type != null'>and l.type=#{type}</if>",
      "<if test='orderNumber != null and orderNumber != \"\"'>and o.number like concat('%',#{orderNumber},'%')</if>",
      "<if test='amountMin != null'>and l.money &gt;= #{amountMin}</if>",
      "<if test='amountMax != null'>and l.money &lt;= #{amountMax}</if>",
      "<if test='createdFrom != null'>and l.createtime &gt;= #{createdFrom}</if>",
      "<if test='createdTo != null'>and l.createtime &lt;= #{createdTo}</if>",
      "order by l.id desc limit #{limit} offset #{offset}</script>"
  })
  List<Map<String,Object>> systemFinanceRows(
      @Param("type") Integer type,
      @Param("orderNumber") String orderNumber,
      @Param("amountMin") BigDecimal amountMin,
      @Param("amountMax") BigDecimal amountMax,
      @Param("createdFrom") Long createdFrom,
      @Param("createdTo") Long createdTo,
      @Param("limit") int limit,
      @Param("offset") int offset);

  @Select({
      "<script>select coalesce(sum(l.money),0) from ymcx_system_money_log l left join ymcx_order o on o.id=l.order_id",
      "where l.is_delete=1 and l.deletetime is null",
      "<if test='type != null'>and l.type=#{type}</if>",
      "<if test='orderNumber != null and orderNumber != \"\"'>and o.number like concat('%',#{orderNumber},'%')</if>",
      "<if test='amountMin != null'>and l.money &gt;= #{amountMin}</if>",
      "<if test='amountMax != null'>and l.money &lt;= #{amountMax}</if>",
      "<if test='createdFrom != null'>and l.createtime &gt;= #{createdFrom}</if>",
      "<if test='createdTo != null'>and l.createtime &lt;= #{createdTo}</if>",
      "</script>"
  })
  BigDecimal systemFinanceAmount(
      @Param("type") Integer type,
      @Param("orderNumber") String orderNumber,
      @Param("amountMin") BigDecimal amountMin,
      @Param("amountMax") BigDecimal amountMax,
      @Param("createdFrom") Long createdFrom,
      @Param("createdTo") Long createdTo);
  @Select({"<script><choose>",
      "<when test='kind == \"merchant\"'>select * from ymcx_merchant_money_log where id=#{id} and is_delete=1 and deletetime is null</when>",
      "<when test='kind == \"shop\"'>select * from ymcx_shop_money_log where id=#{id} and is_delete=1 and deletetime is null</when>",
      "<when test='kind == \"system\"'>select * from ymcx_system_money_log where id=#{id} and is_delete=1 and deletetime is null</when>",
      "<otherwise>select * from ymcx_platform_log where id=#{id} and is_delete=1 and deletetime is null</otherwise>",
      "</choose></script>"})
  Map<String,Object> financeRow(@Param("kind") String kind,@Param("id") long id);
  @Select({"<script><choose><when test='kind == \"merchant\"'>select count(*) from ymcx_merchant where id=#{id} and is_delete=1 and deletetime is null</when><otherwise>select count(*) from ymcx_shop where id=#{id} and is_delete=1 and deletetime is null</otherwise></choose></script>"})
  int financeOwnerExists(@Param("kind") String kind,@Param("id") long id);
  @Insert({"<script><choose>",
      "<when test='kind == \"merchant\"'>insert into ymcx_merchant_money_log(merchant_id,money,`before`,`after`,type,money_type,memo,createtime,updatetime,is_delete,order_id) values(#{v.owner_id},#{v.money},#{v.before},#{v.after},#{v.type},#{v.money_type},#{v.memo},#{v.now},#{v.now},#{v.is_delete},#{v.order_id})</when>",
      "<otherwise>insert into ymcx_shop_money_log(shop_id,money,`before`,`after`,type,memo,createtime,updatetime,is_delete,order_id) values(#{v.owner_id},#{v.money},#{v.before},#{v.after},#{v.type},#{v.memo},#{v.now},#{v.now},#{v.is_delete},#{v.order_id})</otherwise>",
      "</choose></script>"})
  @Options(useGeneratedKeys=true,keyProperty="v.id")
  int insertFinanceRow(@Param("kind") String kind,@Param("v") Map<String,Object> values);
  @Update({"<script><choose>",
      "<when test='kind == \"merchant\"'>update ymcx_merchant_money_log set merchant_id=#{v.owner_id},money=#{v.money},`before`=#{v.before},`after`=#{v.after},type=#{v.type},money_type=#{v.money_type},memo=#{v.memo},updatetime=#{v.now},is_delete=#{v.is_delete},order_id=#{v.order_id} where id=#{v.id} and is_delete=1 and deletetime is null</when>",
      "<otherwise>update ymcx_shop_money_log set shop_id=#{v.owner_id},money=#{v.money},`before`=#{v.before},`after`=#{v.after},type=#{v.type},memo=#{v.memo},updatetime=#{v.now},is_delete=#{v.is_delete},order_id=#{v.order_id} where id=#{v.id} and is_delete=1 and deletetime is null</otherwise>",
      "</choose></script>"})
  int updateFinanceRow(@Param("kind") String kind,@Param("v") Map<String,Object> values);
  @Update({"<script><choose>",
      "<when test='kind == \"merchant\"'>update ymcx_merchant_money_log set is_delete=2,deletetime=#{now},updatetime=#{now} where id=#{id} and is_delete=1 and deletetime is null</when>",
      "<when test='kind == \"shop\"'>update ymcx_shop_money_log set is_delete=2,deletetime=#{now},updatetime=#{now} where id=#{id} and is_delete=1 and deletetime is null</when>",
      "<when test='kind == \"system\"'>update ymcx_system_money_log set is_delete=2,deletetime=#{now},updatetime=#{now} where id=#{id} and is_delete=1 and deletetime is null</when>",
      "<otherwise>update ymcx_platform_log set is_delete=2,deletetime=#{now} where id=#{id} and is_delete=1 and deletetime is null</otherwise>",
      "</choose></script>"})
  int deleteFinanceRow(@Param("kind") String kind,@Param("id") long id,@Param("now") long now);
  @Update({"<script><choose>",
      "<when test='kind == \"merchant\"'>update ymcx_merchant_money_log set is_delete=2,deletetime=#{now},updatetime=#{now} where is_delete=1 and deletetime is null and id in <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach></when>",
      "<when test='kind == \"shop\"'>update ymcx_shop_money_log set is_delete=2,deletetime=#{now},updatetime=#{now} where is_delete=1 and deletetime is null and id in <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach></when>",
      "<when test='kind == \"system\"'>update ymcx_system_money_log set is_delete=2,deletetime=#{now},updatetime=#{now} where is_delete=1 and deletetime is null and id in <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach></when>",
      "<otherwise>update ymcx_platform_log set is_delete=2,deletetime=#{now} where is_delete=1 and deletetime is null and id in <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach></otherwise>",
      "</choose></script>"})
  int deleteFinanceRows(@Param("kind") String kind,@Param("ids") List<Long> ids,@Param("now") long now);
  @Select({"<script><choose>",
      "<when test='kind == \"merchant\"'>select count(*) from ymcx_merchant_money_log where is_delete=2</when>",
      "<when test='kind == \"shop\"'>select count(*) from ymcx_shop_money_log where is_delete=2</when>",
      "<when test='kind == \"system\"'>select count(*) from ymcx_system_money_log where is_delete=2</when>",
      "<otherwise>select count(*) from ymcx_platform_log where is_delete=2</otherwise>",
      "</choose></script>"})
  int recycledFinanceCount(@Param("kind") String kind);
  @Select({"<script><choose>",
      "<when test='kind == \"merchant\"'>select l.*,x.name owner_name from ymcx_merchant_money_log l left join ymcx_merchant x on x.id=l.merchant_id where l.is_delete=2</when>",
      "<when test='kind == \"shop\"'>select l.*,x.name owner_name from ymcx_shop_money_log l left join ymcx_shop x on x.id=l.shop_id where l.is_delete=2</when>",
      "<when test='kind == \"system\"'>select l.*,o.number order_number from ymcx_system_money_log l left join ymcx_order o on o.id=l.order_id where l.is_delete=2</when>",
      "<otherwise>select l.*,case l.user_type when 1 then u.nickname when 2 then m.name when 3 then s.name else '' end nickname,case l.user_type when 1 then u.nickname when 2 then m.name when 3 then s.name else '' end owner_name,case when l.type in (1,2,5) then o.number when l.type=3 then mc.out_trade_no when l.type=4 then sc.out_trade_no else '' end out_trade_no,case when l.type in (1,2,5) then o.number when l.type=3 then mc.out_trade_no when l.type=4 then sc.out_trade_no else '' end order_number from ymcx_platform_log l left join ymcx_order o on l.type in (1,2,5) and o.id=cast(l.order_id as unsigned) left join ymcx_merchant_cash mc on l.type=3 and mc.id=cast(l.order_id as unsigned) left join ymcx_shop_cash sc on l.type=4 and sc.id=cast(l.order_id as unsigned) left join ymcx_user u on l.user_type=1 and u.id=l.user_id left join ymcx_merchant m on l.user_type=2 and m.id=l.user_id left join ymcx_shop s on l.user_type=3 and s.id=l.user_id where l.is_delete=2</otherwise>",
      "</choose> order by l.id desc limit #{limit} offset #{offset}</script>"})
  List<Map<String,Object>> recycledFinanceRows(@Param("kind") String kind,@Param("limit") int limit,@Param("offset") int offset);
  @Update({"<script><choose>",
      "<when test='kind == \"merchant\"'>update ymcx_merchant_money_log set is_delete=1,deletetime=null,updatetime=#{now} where id=#{id} and is_delete=2</when>",
      "<when test='kind == \"shop\"'>update ymcx_shop_money_log set is_delete=1,deletetime=null,updatetime=#{now} where id=#{id} and is_delete=2</when>",
      "<when test='kind == \"system\"'>update ymcx_system_money_log set is_delete=1,deletetime=null,updatetime=#{now} where id=#{id} and is_delete=2</when>",
      "<otherwise>update ymcx_platform_log set is_delete=1,deletetime=null where id=#{id} and is_delete=2</otherwise>",
      "</choose></script>"})
  int restoreFinanceRow(@Param("kind") String kind,@Param("id") long id,@Param("now") long now);
  @Update({"<script><choose>",
      "<when test='kind == \"merchant\"'>update ymcx_merchant_money_log set is_delete=1,deletetime=null,updatetime=#{now} where is_delete=2 and id in <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach></when>",
      "<when test='kind == \"shop\"'>update ymcx_shop_money_log set is_delete=1,deletetime=null,updatetime=#{now} where is_delete=2 and id in <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach></when>",
      "<when test='kind == \"system\"'>update ymcx_system_money_log set is_delete=1,deletetime=null,updatetime=#{now} where is_delete=2 and id in <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach></when>",
      "<otherwise>update ymcx_platform_log set is_delete=1,deletetime=null where is_delete=2 and id in <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach></otherwise>",
      "</choose></script>"})
  int restoreFinanceRows(@Param("kind") String kind,@Param("ids") List<Long> ids,@Param("now") long now);
  @Delete({"<script><choose>",
      "<when test='kind == \"merchant\"'>delete from ymcx_merchant_money_log where id=#{id} and is_delete=2</when>",
      "<when test='kind == \"shop\"'>delete from ymcx_shop_money_log where id=#{id} and is_delete=2</when>",
      "<when test='kind == \"system\"'>delete from ymcx_system_money_log where id=#{id} and is_delete=2</when>",
      "<otherwise>delete from ymcx_platform_log where id=#{id} and is_delete=2</otherwise>",
      "</choose></script>"})
  int destroyFinanceRow(@Param("kind") String kind,@Param("id") long id);
  @Delete({"<script><choose>",
      "<when test='kind == \"merchant\"'>delete from ymcx_merchant_money_log where is_delete=2 and id in <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach></when>",
      "<when test='kind == \"shop\"'>delete from ymcx_shop_money_log where is_delete=2 and id in <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach></when>",
      "<when test='kind == \"system\"'>delete from ymcx_system_money_log where is_delete=2 and id in <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach></when>",
      "<otherwise>delete from ymcx_platform_log where is_delete=2 and id in <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach></otherwise>",
      "</choose></script>"})
  int destroyFinanceRows(@Param("kind") String kind,@Param("ids") List<Long> ids);
  @Select("select value from ymcx_config where name='system_money' limit 1") String systemMoney();
}
