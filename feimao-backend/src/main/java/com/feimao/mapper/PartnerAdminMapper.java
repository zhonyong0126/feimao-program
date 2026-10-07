package com.feimao.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface PartnerAdminMapper {
  @Select({"<script>","select count(*) from ymcx_merchant where is_delete=1 and deletetime is null",
      "<if test='status != null'>and status=#{status}</if><if test='examineStatus != null'>and examine_status=#{examineStatus}</if>",
      "<if test='keyword != null and keyword != \"\"'>and (name like concat('%',#{keyword},'%') or nickname like concat('%',#{keyword},'%') or mobile like concat('%',#{keyword},'%') or username like concat('%',#{keyword},'%'))</if>","</script>"})
  int merchantCount(@Param("keyword") String keyword,@Param("status") Integer status,@Param("examineStatus") Integer examineStatus);
  @Select({"<script>","select p.*,u.nickname user_nickname from ymcx_merchant p left join ymcx_user u on u.id=p.user_id where p.is_delete=1 and p.deletetime is null",
      "<if test='status != null'>and p.status=#{status}</if><if test='examineStatus != null'>and p.examine_status=#{examineStatus}</if>",
      "<if test='keyword != null and keyword != \"\"'>and (p.name like concat('%',#{keyword},'%') or p.nickname like concat('%',#{keyword},'%') or p.mobile like concat('%',#{keyword},'%') or p.username like concat('%',#{keyword},'%'))</if>","order by p.id desc limit #{limit} offset #{offset}","</script>"})
  List<Map<String,Object>> merchants(@Param("keyword") String keyword,@Param("status") Integer status,@Param("examineStatus") Integer examineStatus,@Param("limit") int limit,@Param("offset") int offset);
  @Select({"<script>","select count(*) from ymcx_shop where is_delete=1 and deletetime is null",
      "<if test='status != null'>and status=#{status}</if><if test='examineStatus != null'>and examine_status=#{examineStatus}</if>",
      "<if test='keyword != null and keyword != \"\"'>and (name like concat('%',#{keyword},'%') or nickname like concat('%',#{keyword},'%') or mobile like concat('%',#{keyword},'%') or username like concat('%',#{keyword},'%'))</if>","</script>"})
  int shopCount(@Param("keyword") String keyword,@Param("status") Integer status,@Param("examineStatus") Integer examineStatus);
  @Select({"<script>","select p.*,u.nickname user_nickname from ymcx_shop p left join ymcx_user u on u.id=p.user_id where p.is_delete=1 and p.deletetime is null",
      "<if test='status != null'>and p.status=#{status}</if><if test='examineStatus != null'>and p.examine_status=#{examineStatus}</if>",
      "<if test='keyword != null and keyword != \"\"'>and (p.name like concat('%',#{keyword},'%') or p.nickname like concat('%',#{keyword},'%') or p.mobile like concat('%',#{keyword},'%') or p.username like concat('%',#{keyword},'%'))</if>","order by p.id desc limit #{limit} offset #{offset}","</script>"})
  List<Map<String,Object>> shops(@Param("keyword") String keyword,@Param("status") Integer status,@Param("examineStatus") Integer examineStatus,@Param("limit") int limit,@Param("offset") int offset);
  @Select("select p.*,u.nickname user_nickname,u.mobile user_mobile from ymcx_merchant p left join ymcx_user u on u.id=p.user_id where p.id=#{id} and p.is_delete=1 and p.deletetime is null") Map<String,Object> merchant(@Param("id") long id);
  @Select("select p.*,u.nickname user_nickname,u.mobile user_mobile from ymcx_shop p left join ymcx_user u on u.id=p.user_id where p.id=#{id} and p.is_delete=1 and p.deletetime is null") Map<String,Object> shop(@Param("id") long id);
  @Select("select count(*) from ymcx_user where id=#{id}") int userCount(@Param("id") long id);
  @Select("select id,nickname from ymcx_user where merchant_id=0 order by id desc limit 1000")
  List<Map<String,Object>> merchantUserOptions();
  @Select("select id,nickname from ymcx_user where shop_id=0 order by id desc limit 1000")
  List<Map<String,Object>> shopUserOptions();
  @Select("select count(*) from ymcx_merchant where username=#{username} and id<>#{id} and is_delete=1 and deletetime is null") int merchantUsernameCount(@Param("username") String username,@Param("id") long id);
  @Select("select count(*) from ymcx_shop where username=#{username} and id<>#{id} and is_delete=1 and deletetime is null") int shopUsernameCount(@Param("username") String username,@Param("id") long id);
  @Select("select count(*) from ymcx_user where merchant_id<>0 and merchant_id<>#{partnerId} and id=#{userId}") int otherMerchantBinding(@Param("partnerId") long partnerId,@Param("userId") long userId);
  @Select("select count(*) from ymcx_user where shop_id<>0 and shop_id<>#{partnerId} and id=#{userId}") int otherShopBinding(@Param("partnerId") long partnerId,@Param("userId") long userId);
  @Select("select count(*) from ymcx_merchant_car where merchant_id=#{id} and is_delete=1 and deletetime is null") int activeMerchantCarCount(@Param("id") long id);
  @Select("select last_insert_id()") long lastInsertId();
  @Insert("insert into ymcx_merchant(user_id,username,salt,password,name,nickname,mobile,address,avatar,image,intro,business,administrative,car_num,min_price,pick_type,lng,lat,status,is_delete,createtime,updatetime,examine_status,commission_normal_rate,commission_merchant_rate,commission_shop_rate,service_distance1,service_distance2,service_distance3,service_distance4,service_money1,service_money2,service_money3,service_money4,service_money5) values(#{v.user_id},#{v.username},#{v.salt},#{v.password},#{v.name},#{v.nickname},#{v.mobile},#{v.address},#{v.avatar},#{v.image},#{v.intro},#{v.business},#{v.administrative},#{v.car_num},#{v.min_price},#{v.pick_type},#{v.lng},#{v.lat},#{v.status},1,#{now},#{now},2,#{v.commission_normal_rate},#{v.commission_merchant_rate},#{v.commission_shop_rate},#{v.service_distance1},#{v.service_distance2},#{v.service_distance3},#{v.service_distance4},#{v.service_money1},#{v.service_money2},#{v.service_money3},#{v.service_money4},#{v.service_money5})")
  int insertMerchant(@Param("v") Map<String,Object> values,@Param("now") long now);
  @Insert("insert into ymcx_shop(user_id,username,salt,password,name,nickname,mobile,address,avatar,image,intro,status,is_delete,createtime,updatetime,examine_status,commission_shop_rate) values(#{v.user_id},#{v.username},#{v.salt},#{v.password},#{v.name},#{v.nickname},#{v.mobile},#{v.address},#{v.avatar},#{v.image},#{v.intro},#{v.status},1,#{now},#{now},2,#{v.commission_shop_rate})")
  int insertShop(@Param("v") Map<String,Object> values,@Param("now") long now);
  @Update({"<script>","update ymcx_merchant set updatetime=#{now}",
      "<if test='v.containsKey(\"name\")'>,name=#{v.name}</if><if test='v.containsKey(\"nickname\")'>,nickname=#{v.nickname}</if><if test='v.containsKey(\"mobile\")'>,mobile=#{v.mobile}</if><if test='v.containsKey(\"address\")'>,address=#{v.address}</if><if test='v.containsKey(\"avatar\")'>,avatar=#{v.avatar}</if><if test='v.containsKey(\"image\")'>,image=#{v.image}</if><if test='v.containsKey(\"intro\")'>,intro=#{v.intro}</if><if test='v.containsKey(\"status\")'>,status=#{v.status}</if>",
      "<if test='v.containsKey(\"business\")'>,business=#{v.business}</if><if test='v.containsKey(\"administrative\")'>,administrative=#{v.administrative}</if><if test='v.containsKey(\"pick_type\")'>,pick_type=#{v.pick_type}</if><if test='v.containsKey(\"lng\")'>,lng=#{v.lng}</if><if test='v.containsKey(\"lat\")'>,lat=#{v.lat}</if>",
      "<if test='v.containsKey(\"car_num\")'>,car_num=#{v.car_num}</if><if test='v.containsKey(\"min_price\")'>,min_price=#{v.min_price}</if>",
      "<if test='v.containsKey(\"commission_normal_rate\")'>,commission_normal_rate=#{v.commission_normal_rate}</if><if test='v.containsKey(\"commission_merchant_rate\")'>,commission_merchant_rate=#{v.commission_merchant_rate}</if><if test='v.containsKey(\"commission_shop_rate\")'>,commission_shop_rate=#{v.commission_shop_rate}</if>",
      "<if test='v.containsKey(\"service_distance1\")'>,service_distance1=#{v.service_distance1}</if><if test='v.containsKey(\"service_distance2\")'>,service_distance2=#{v.service_distance2}</if><if test='v.containsKey(\"service_distance3\")'>,service_distance3=#{v.service_distance3}</if><if test='v.containsKey(\"service_distance4\")'>,service_distance4=#{v.service_distance4}</if>",
      "<if test='v.containsKey(\"service_money1\")'>,service_money1=#{v.service_money1}</if><if test='v.containsKey(\"service_money2\")'>,service_money2=#{v.service_money2}</if><if test='v.containsKey(\"service_money3\")'>,service_money3=#{v.service_money3}</if><if test='v.containsKey(\"service_money4\")'>,service_money4=#{v.service_money4}</if><if test='v.containsKey(\"service_money5\")'>,service_money5=#{v.service_money5}</if>",
      "<if test='v.containsKey(\"user_id\")'>,user_id=#{v.user_id}</if><if test='v.containsKey(\"username\")'>,username=#{v.username}</if><if test='v.containsKey(\"salt\")'>,salt=#{v.salt}</if><if test='v.containsKey(\"password\")'>,password=#{v.password}</if>",
      "where id=#{id} and is_delete=1 and deletetime is null","</script>"})
  int updateMerchant(@Param("id") long id,@Param("v") Map<String,Object> values,@Param("now") long now);
  @Update({"<script>","update ymcx_shop set updatetime=#{now}",
      "<if test='v.containsKey(\"name\")'>,name=#{v.name}</if><if test='v.containsKey(\"nickname\")'>,nickname=#{v.nickname}</if><if test='v.containsKey(\"mobile\")'>,mobile=#{v.mobile}</if><if test='v.containsKey(\"address\")'>,address=#{v.address}</if><if test='v.containsKey(\"avatar\")'>,avatar=#{v.avatar}</if><if test='v.containsKey(\"image\")'>,image=#{v.image}</if><if test='v.containsKey(\"intro\")'>,intro=#{v.intro}</if><if test='v.containsKey(\"status\")'>,status=#{v.status}</if><if test='v.containsKey(\"commission_shop_rate\")'>,commission_shop_rate=#{v.commission_shop_rate}</if>",
      "<if test='v.containsKey(\"user_id\")'>,user_id=#{v.user_id}</if><if test='v.containsKey(\"username\")'>,username=#{v.username}</if><if test='v.containsKey(\"salt\")'>,salt=#{v.salt}</if><if test='v.containsKey(\"password\")'>,password=#{v.password}</if>",
      "where id=#{id} and is_delete=1 and deletetime is null","</script>"})
  int updateShop(@Param("id") long id,@Param("v") Map<String,Object> values,@Param("now") long now);
  @Select("select user_id,qrimage from ymcx_merchant where id=#{id} and is_delete=1 and deletetime is null") Map<String,Object> merchantMetadata(@Param("id") long id);
  @Select("select user_id,qrimage from ymcx_shop where id=#{id} and is_delete=1 and deletetime is null") Map<String,Object> shopMetadata(@Param("id") long id);
  @Update("update ymcx_user set merchant_id=0,merchant_status=1,updatetime=#{now} where id=#{userId} and merchant_id=#{partnerId}")
  int unbindMerchant(@Param("userId") long userId,@Param("partnerId") long partnerId,@Param("now") long now);
  @Update("update ymcx_user set shop_id=0,shop_status=1,updatetime=#{now} where id=#{userId} and shop_id=#{partnerId}")
  int unbindShop(@Param("userId") long userId,@Param("partnerId") long partnerId,@Param("now") long now);
  @Update("update ymcx_user set merchant_id=#{partnerId},merchant_status=3,updatetime=#{now} where id=#{userId}")
  int bindMerchant(@Param("userId") long userId,@Param("partnerId") long partnerId,@Param("now") long now);
  @Update("update ymcx_user set shop_id=#{partnerId},shop_status=3,updatetime=#{now} where id=#{userId}")
  int bindShop(@Param("userId") long userId,@Param("partnerId") long partnerId,@Param("now") long now);
  @Update("update ymcx_merchant set is_delete=2,deletetime=#{now},updatetime=#{now} where id=#{id} and is_delete=1 and deletetime is null")
  int deleteMerchant(@Param("id") long id,@Param("now") long now);
  @Update("update ymcx_shop set is_delete=2,deletetime=#{now},updatetime=#{now} where id=#{id} and is_delete=1 and deletetime is null")
  int deleteShop(@Param("id") long id,@Param("now") long now);
  @Update("update ymcx_merchant_car set merchant_longitude=(select lng from ymcx_merchant where id=#{id} and deletetime is null),merchant_latitude=(select lat from ymcx_merchant where id=#{id} and deletetime is null) where merchant_id=#{id} and deletetime is null")
  int syncMerchantCarCoordinates(@Param("id") long id);
  @Select("select count(*) from ymcx_merchant where is_delete=2") int recycledMerchantCount();
  @Select("select count(*) from ymcx_shop where is_delete=2") int recycledShopCount();
  @Select("select p.*,u.nickname user_nickname from ymcx_merchant p left join ymcx_user u on u.id=p.user_id where p.is_delete=2 order by p.id desc limit #{limit} offset #{offset}")
  List<Map<String,Object>> recycledMerchants(@Param("limit") int limit,@Param("offset") int offset);
  @Select("select p.*,u.nickname user_nickname from ymcx_shop p left join ymcx_user u on u.id=p.user_id where p.is_delete=2 order by p.id desc limit #{limit} offset #{offset}")
  List<Map<String,Object>> recycledShops(@Param("limit") int limit,@Param("offset") int offset);
  @Update("update ymcx_merchant set is_delete=1,deletetime=null,updatetime=#{now} where id=#{id} and is_delete=2")
  int restoreMerchant(@Param("id") long id,@Param("now") long now);
  @Update("update ymcx_shop set is_delete=1,deletetime=null,updatetime=#{now} where id=#{id} and is_delete=2")
  int restoreShop(@Param("id") long id,@Param("now") long now);
  @Delete("delete from ymcx_merchant where id=#{id} and is_delete=2") int destroyMerchant(@Param("id") long id);
  @Delete("delete from ymcx_shop where id=#{id} and is_delete=2") int destroyShop(@Param("id") long id);
}
