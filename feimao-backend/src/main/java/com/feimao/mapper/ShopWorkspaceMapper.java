package com.feimao.mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ShopWorkspaceMapper {
  @Select("select id,avatar,name,mobile,password,salt,status from ymcx_shop where username=#{username} and is_delete=1 and deletetime is null and examine_status=2")
  Map<String, Object> selectLogin(@Param("username") String username);
  @Select("select id,name,nickname,mobile,address,avatar,image,intro,money,qrimage from ymcx_shop where id=#{id} and is_delete=1 and deletetime is null")
  Map<String, Object> selectProfile(@Param("id") long id);
  @Select("select count(*) from ymcx_shop where id=#{id} and is_delete=1 and deletetime is null and status=1")
  int activeCount(@Param("id") long id);
  @Update({"<script>", "update ymcx_shop set updatetime=#{now}",
      "<if test='fields.containsKey(\"name\")'>,name=#{fields.name}</if>",
      "<if test='fields.containsKey(\"nickname\")'>,nickname=#{fields.nickname}</if>",
      "<if test='fields.containsKey(\"mobile\")'>,mobile=#{fields.mobile}</if>",
      "<if test='fields.containsKey(\"address\")'>,address=#{fields.address}</if>",
      "<if test='fields.containsKey(\"avatar\")'>,avatar=#{fields.avatar}</if>",
      "<if test='fields.containsKey(\"image\")'>,image=#{fields.image}</if>",
      "<if test='fields.containsKey(\"intro\")'>,intro=#{fields.intro}</if>",
      "where id=#{id} and is_delete=1 and deletetime is null", "</script>"})
  int updateProfile(@Param("id") long id, @Param("fields") Map<String, Object> fields, @Param("now") long now);
  @Select("select id,amount,status,createtime from ymcx_shop_cash where shop_id=#{shopId} and is_delete=1 and deletetime is null order by createtime desc limit #{limit} offset #{offset}")
  List<Map<String, Object>> selectCashRows(@Param("shopId") long shopId, @Param("limit") long limit, @Param("offset") long offset);
  @Select("select count(*) from ymcx_shop_cash where shop_id=#{shopId} and is_delete=1 and deletetime is null") int cashCount(@Param("shopId") long shopId);
  @Select("select money from ymcx_shop where id=#{shopId} and is_delete=1 and deletetime is null for update") BigDecimal lockBalance(@Param("shopId") long shopId);
  @Insert("insert into ymcx_shop_cash(out_trade_no,shop_id,amount,status,createtime,updatetime,is_delete) values(#{number},#{shopId},#{amount},1,#{now},#{now},1)")
  int insertCash(@Param("number") String number, @Param("shopId") long shopId,
      @Param("amount") BigDecimal amount, @Param("now") long now);
  @Select("select id from ymcx_shop_cash where out_trade_no=#{number}") Long selectCashId(@Param("number") String number);
  @Select("select count(*) from ymcx_shop_cash where out_trade_no=#{number}")
  int cashNumberCount(@Param("number") String number);
  @Update("update ymcx_shop set money=#{money},updatetime=#{now} where id=#{shopId} and is_delete=1 and deletetime is null")
  int updateBalance(@Param("shopId") long shopId, @Param("money") BigDecimal money, @Param("now") long now);
  @Insert("insert into ymcx_shop_money_log(shop_id,money,`before`,`after`,type,memo,createtime,updatetime,is_delete,order_id) values(#{shopId},#{amount},#{before},#{after},2,'提现申请消耗',#{now},#{now},1,#{cashId})")
  int insertCashLog(@Param("shopId") long shopId, @Param("amount") BigDecimal amount,
      @Param("before") BigDecimal before, @Param("after") BigDecimal after,
      @Param("now") long now, @Param("cashId") long cashId);
  @Select("select id,name,avatar,mobile,qrimage,money from ymcx_shop where id=#{shopId} and is_delete=1 and deletetime is null") Map<String, Object> selectDashboard(@Param("shopId") long shopId);
  @Select("select o.id,o.number,o.lease_money price,o.createtime,u.nickname user_name,u.avatar user_avatar from ymcx_order o left join ymcx_user u on u.id=o.user_id where o.source_id=#{shopId} and o.source_type=3 and o.state in(2,3,4,5) and o.is_delete=1 order by o.createtime desc limit #{limit} offset #{offset}")
  List<Map<String, Object>> selectOrders(@Param("shopId") long shopId, @Param("limit") long limit, @Param("offset") long offset);
  @Select("select count(*) from ymcx_order where source_id=#{shopId} and source_type=3 and state in(2,3,4,5) and is_delete=1") int orderCount(@Param("shopId") long shopId);
  @Select("select l.id,l.money price,l.createtime,o.number,u.nickname user_name,u.avatar user_avatar from ymcx_shop_money_log l left join ymcx_order o on o.id=l.order_id left join ymcx_user u on u.id=o.user_id where l.shop_id=#{shopId} and l.type=1 and l.is_delete=1 and l.deletetime is null order by l.createtime desc limit #{limit} offset #{offset}")
  List<Map<String, Object>> selectIncome(@Param("shopId") long shopId, @Param("limit") long limit, @Param("offset") long offset);
  @Select("select count(*) from ymcx_shop_money_log where shop_id=#{shopId} and type=1 and is_delete=1 and deletetime is null") int incomeCount(@Param("shopId") long shopId);
}
