package com.feimao.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserProfileMapper {
  @Select("select id from ymcx_user where openid=#{openid} limit 1") Long userIdByOpenid(@Param("openid") String openid);
  @Insert("insert into ymcx_user(group_id,nickname,avatar,level,gender,money,score,successions,maxsuccessions,prevtime,logintime,jointime,createtime,updatetime,status,openid,merchant_status,merchant_id,shop_status,shop_id) values(1,'用户','/assets/img/avatar.png',1,0,0,0,1,1,#{now},#{now},#{now},#{now},#{now},'normal',#{openid},1,0,1,0)")
  @Options(useGeneratedKeys=true,keyProperty="v.id") int insertWechatUser(@Param("v") Map<String,Object> values,@Param("openid") String openid,@Param("now") long now);
  @Update("update ymcx_user set nickname=#{nickname} where id=#{id}") int initializeNickname(@Param("id") long id,@Param("nickname") String nickname);
  @Update("update ymcx_user set prevtime=logintime,logintime=#{now},updatetime=#{now} where id=#{id}") int updateLoginTime(@Param("id") long id,@Param("now") long now);
  @Select("select id,id user_id,avatar,nickname,mobile from ymcx_user where id=#{id}") Map<String,Object> loginUser(@Param("id") long id);
  @Select("select id,avatar,nickname,mobile,merchant_status,merchant_id,shop_status,shop_id from ymcx_user where id=#{id}") Map<String,Object> userInfo(@Param("id") long id);
  @Update({"<script>update ymcx_user set updatetime=#{now}",
      "<if test='v.containsKey(\"nickname\")'>,nickname=#{v.nickname}</if>",
      "<if test='v.containsKey(\"avatar\")'>,avatar=#{v.avatar}</if>",
      "<if test='v.containsKey(\"mobile\")'>,mobile=#{v.mobile}</if>",
      "where id=#{id}</script>"})
  int updateProfile(@Param("id") long id,@Param("v") Map<String,String> values,@Param("now") long now);
  @Select("select count(*) from ymcx_address where user_id=#{userId}") int addressCount(@Param("userId") long userId);
  @Select("select * from ymcx_address where user_id=#{userId} order by id desc limit #{limit} offset #{offset}") List<Map<String,Object>> addresses(@Param("userId") long userId,@Param("limit") long limit,@Param("offset") long offset);
  @Insert("insert into ymcx_address(user_id,name,administrative,address,mobile,createtime,updatetime,lng,lat) values(#{userId},#{name},#{administrative},#{address},#{mobile},#{now},#{now},#{lng},#{lat})") int insertAddress(@Param("userId") long userId,@Param("name") String name,@Param("administrative") String administrative,@Param("address") String address,@Param("mobile") String mobile,@Param("lng") double lng,@Param("lat") double lat,@Param("now") long now);
  @Select("select count(*) from ymcx_address where id=#{id} and user_id=#{userId}")
  int addressExists(@Param("id") long id,@Param("userId") long userId);
  @Update({"<script>update ymcx_address set updatetime=#{now}",
      "<if test='v.containsKey(\"name\")'>,name=#{v.name}</if>",
      "<if test='v.containsKey(\"administrative\")'>,administrative=#{v.administrative}</if>",
      "<if test='v.containsKey(\"address\")'>,address=#{v.address}</if>",
      "<if test='v.containsKey(\"mobile\")'>,mobile=#{v.mobile}</if>",
      "<if test='v.containsKey(\"lng\")'>,lng=#{v.lng}</if>",
      "<if test='v.containsKey(\"lat\")'>,lat=#{v.lat}</if>",
      "where id=#{id} and user_id=#{userId}</script>"})
  int updateAddress(@Param("id") long id,@Param("userId") long userId,
      @Param("v") Map<String,Object> values,@Param("now") long now);
  @Delete("delete from ymcx_address where id=#{id} and user_id=#{userId}") int deleteAddress(@Param("id") long id,@Param("userId") long userId);
  @Select("select id coupon_id,price,reduceprice,endtime from ymcx_user_coupon where user_id=#{userId} and state=1") List<Map<String,Object>> coupons(@Param("userId") long userId);
  @Select({"<script><choose><when test='staff'>select staff_authentication,staff_mobile,staff_name,staff_work from ymcx_user</when><otherwise>select authentication,authentication_mobile,authentication_name,authentication_card from ymcx_user</otherwise></choose> where id=#{userId}</script>"}) Map<String,Object> authInfo(@Param("userId") long userId,@Param("staff") boolean staff);
  @Update("update ymcx_user set authentication=2,authentication_mobile=#{mobile},authentication_name=#{name},authentication_card=#{card},updatetime=#{now} where id=#{userId} and authentication in (1,4)") int submitAuthentication(@Param("userId") long userId,@Param("mobile") String mobile,@Param("name") String name,@Param("card") String card,@Param("now") long now);
  @Update("update ymcx_user set staff_authentication=2,staff_mobile=#{mobile},staff_name=#{name},staff_work=#{work},updatetime=#{now} where id=#{userId} and staff_authentication in (1,4)") int submitStaffAuthentication(@Param("userId") long userId,@Param("mobile") String mobile,@Param("name") String name,@Param("work") String work,@Param("now") long now);
  @Select({"<script><choose><when test='staff'>select staff_authentication</when><otherwise>select authentication</otherwise></choose> from ymcx_user where id=#{userId}</script>"})
  @Options(useCache=false, flushCache=Options.FlushCachePolicy.TRUE)
  Integer authenticationStatus(@Param("userId") long userId,@Param("staff") boolean staff);
  @Select("select count(*) from ymcx_order where number=#{number} and state in (2,3,4)") int malfunctionOrderCount(@Param("number") String number);
  @Insert("insert into ymcx_malfunction(user_id,number,image,content,createtime,updatetime) values(#{userId},#{number},#{image},#{content},#{now},#{now})") int insertMalfunction(@Param("userId") long userId,@Param("number") String number,@Param("image") String image,@Param("content") String content,@Param("now") long now);
  @Select("select value from ymcx_config where name=#{name} limit 1") String config(@Param("name") String name);
  @Select({"<script><choose><when test='role == \"merchant\"'>select merchant_status status,merchant_id partner_id from ymcx_user</when><otherwise>select shop_status status,shop_id partner_id from ymcx_user</otherwise></choose> where id=#{userId} for update</script>"}) Map<String,Object> lockApplicationUser(@Param("role") String role,@Param("userId") long userId);
  @Select({"<script><choose><when test='role == \"merchant\"'>select id,name,mobile,administrative,address,intro,lat,lng,examine_note from ymcx_merchant</when><otherwise>select id,name,nickname,mobile,image,address,intro,examine_note from ymcx_shop</otherwise></choose> where id=#{id} and user_id=#{userId} and is_delete=1 and deletetime is null</script>"}) Map<String,Object> applicationDetail(@Param("role") String role,@Param("id") long id,@Param("userId") long userId);
  @Select({"<script><choose><when test='role == \"merchant\"'>select id,examine_status from ymcx_merchant</when><otherwise>select id,examine_status from ymcx_shop</otherwise></choose> where id=#{id} and user_id=#{userId} and is_delete=1 and deletetime is null</script>"}) Map<String,Object> applicationState(@Param("role") String role,@Param("id") long id,@Param("userId") long userId);
  @Insert("insert into ymcx_merchant(user_id,name,mobile,administrative,address,intro,avatar,lat,lng,status,is_delete,examine_status,createtime,updatetime) values(#{v.user_id},#{v.name},#{v.mobile},#{v.administrative},#{v.address},#{v.intro},#{v.avatar},#{v.lat},#{v.lng},1,1,1,#{v.now},#{v.now})")
  @Options(useGeneratedKeys=true,keyProperty="v.id") int insertMerchantApplication(@Param("v") Map<String,Object> values);
  @Insert("insert into ymcx_shop(user_id,name,nickname,mobile,address,image,avatar,intro,status,is_delete,examine_status,createtime,updatetime) values(#{v.user_id},#{v.name},#{v.nickname},#{v.mobile},#{v.address},#{v.image},#{v.avatar},#{v.intro},1,1,1,#{v.now},#{v.now})")
  @Options(useGeneratedKeys=true,keyProperty="v.id") int insertShopApplication(@Param("v") Map<String,Object> values);
  @Update({"<script><choose><when test='role == \"merchant\"'>update ymcx_user set merchant_status=2,merchant_id=#{partnerId},updatetime=#{now}</when><otherwise>update ymcx_user set shop_status=2,shop_id=#{partnerId},updatetime=#{now}</otherwise></choose> where id=#{userId}</script>"}) int bindApplication(@Param("role") String role,@Param("userId") long userId,@Param("partnerId") long partnerId,@Param("now") long now);
  @Update("update ymcx_merchant set name=#{v.name},mobile=#{v.mobile},administrative=#{v.administrative},address=#{v.address},intro=#{v.intro},lat=#{v.lat},lng=#{v.lng},examine_status=1,updatetime=#{v.now} where id=#{id}") int resubmitMerchant(@Param("id") long id,@Param("v") Map<String,Object> values);
  @Update("update ymcx_shop set name=#{v.name},nickname=#{v.nickname},mobile=#{v.mobile},address=#{v.address},image=#{v.image},avatar=#{v.avatar},intro=#{v.intro},examine_status=1,updatetime=#{v.now} where id=#{id}") int resubmitShop(@Param("id") long id,@Param("v") Map<String,Object> values);
  @Update({"<script><choose><when test='role == \"merchant\"'>update ymcx_user set merchant_status=2,updatetime=#{now}</when><otherwise>update ymcx_user set shop_status=2,updatetime=#{now}</otherwise></choose> where id=#{userId}</script>"}) int markApplicationPending(@Param("role") String role,@Param("userId") long userId,@Param("now") long now);
}
