package com.feimao.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface MerchantServiceAdminMapper {
  @Select({
    "<script>",
    "select count(*) from ymcx_merchant_car mc",
    "left join ymcx_merchant m on m.id=mc.merchant_id and m.is_delete=1 and m.deletetime is null",
    "left join ymcx_platform_car pc on pc.id=mc.platform_car_id and pc.is_delete=1 and pc.deletetime is null",
    "left join ymcx_brand b on b.id=pc.brand_id and b.is_delete=1 and b.deletetime is null",
    "where mc.is_delete=1 and mc.deletetime is null",
    "<if test='merchantId != null and merchantId &gt; 0'>and mc.merchant_id=#{merchantId}</if>",
    "<if test='brandId != null and brandId &gt; 0'>and pc.brand_id=#{brandId}</if>",
    "<if test='status != null'>and mc.status=#{status}</if>",
    "<if test='createdFrom != null and createdFrom &gt; 0'>and mc.createtime &gt;= #{createdFrom}</if>",
    "<if test='createdTo != null and createdTo &gt; 0'>and mc.createtime &lt;= #{createdTo}</if>",
    "<if test='keyword != null and keyword != \"\"'>and (mc.name like concat('%',#{keyword},'%') or m.name like concat('%',#{keyword},'%') or pc.name like concat('%',#{keyword},'%') or b.name like concat('%',#{keyword},'%'))</if>",
    "</script>"
  })
  int carCount(
      @Param("merchantId") Long merchantId,
      @Param("brandId") Long brandId,
      @Param("status") Integer status,
      @Param("createdFrom") Long createdFrom,
      @Param("createdTo") Long createdTo,
      @Param("keyword") String keyword);

  @Select({
    "<script>",
    "select mc.*,m.name merchant_name,pc.name platform_car_name,pc.image,pc.brand_id,",
    "b.name brand_name,(select c.`value` from ymcx_config c where c.name='insure_price' limit 1) insure_price",
    "from ymcx_merchant_car mc",
    "left join ymcx_merchant m on m.id=mc.merchant_id and m.is_delete=1 and m.deletetime is null",
    "left join ymcx_platform_car pc on pc.id=mc.platform_car_id and pc.is_delete=1 and pc.deletetime is null",
    "left join ymcx_brand b on b.id=pc.brand_id and b.is_delete=1 and b.deletetime is null",
    "where mc.is_delete=1 and mc.deletetime is null",
    "<if test='merchantId != null and merchantId &gt; 0'>and mc.merchant_id=#{merchantId}</if>",
    "<if test='brandId != null and brandId &gt; 0'>and pc.brand_id=#{brandId}</if>",
    "<if test='status != null'>and mc.status=#{status}</if>",
    "<if test='createdFrom != null and createdFrom &gt; 0'>and mc.createtime &gt;= #{createdFrom}</if>",
    "<if test='createdTo != null and createdTo &gt; 0'>and mc.createtime &lt;= #{createdTo}</if>",
    "<if test='keyword != null and keyword != \"\"'>and (mc.name like concat('%',#{keyword},'%') or m.name like concat('%',#{keyword},'%') or pc.name like concat('%',#{keyword},'%') or b.name like concat('%',#{keyword},'%'))</if>",
    "order by mc.id desc limit #{limit} offset #{offset}",
    "</script>"
  })
  List<Map<String, Object>> selectCars(
      @Param("merchantId") Long merchantId,
      @Param("brandId") Long brandId,
      @Param("status") Integer status,
      @Param("createdFrom") Long createdFrom,
      @Param("createdTo") Long createdTo,
      @Param("keyword") String keyword,
      @Param("limit") int limit,
      @Param("offset") int offset);

  @Select({
    "<script>",
    "select pc.id,pc.name,pc.brand_id,pc.image,pc.specification,b.name brand_name",
    "from ymcx_platform_car pc",
    "left join ymcx_brand b on b.id=pc.brand_id and b.is_delete=1 and b.deletetime is null",
    "where pc.is_delete=1 and pc.deletetime is null",
    "<if test='brandId != null and brandId &gt; 0'>and pc.brand_id=#{brandId}</if>",
    "order by pc.weigh,pc.id",
    "</script>"
  })
  List<Map<String, Object>> selectPlatformCars(@Param("brandId") Long brandId);

  @Select({
    "<script>",
    "select count(*) from ymcx_merchant_maintain mm left join ymcx_merchant m on m.id=mm.merchant_id left join ymcx_maintain mt on mt.id=mm.maintain_id where mm.deletetime is null",
    "<if test='merchantId != null and merchantId &gt; 0'>and mm.merchant_id=#{merchantId}</if>",
    "</script>"
  })
  int maintainCount(@Param("merchantId") Long merchantId);

  @Select({
    "<script>",
    "select mm.*,m.name merchant_name,mt.name maintain_name,mt.image,mt.introductory from ymcx_merchant_maintain mm left join ymcx_merchant m on m.id=mm.merchant_id left join ymcx_maintain mt on mt.id=mm.maintain_id where mm.deletetime is null",
    "<if test='merchantId != null and merchantId &gt; 0'>and mm.merchant_id=#{merchantId}</if>",
    "order by mm.id desc limit #{limit} offset #{offset}",
    "</script>"
  })
  List<Map<String, Object>> selectMaintains(
      @Param("merchantId") Long merchantId,
      @Param("limit") int limit,
      @Param("offset") int offset);

  @Select({
    "<script>",
    "select count(*) from ymcx_maintain where deletetime is null",
    "<if test='keyword != null and keyword != \"\"'>and (name like concat('%',#{keyword},'%') or introductory like concat('%',#{keyword},'%'))</if>",
    "</script>"
  })
  int maintainOptionCount(@Param("keyword") String keyword);

  @Select({
    "<script>",
    "select id,name,image,introductory from ymcx_maintain where deletetime is null",
    "<if test='keyword != null and keyword != \"\"'>and (name like concat('%',#{keyword},'%') or introductory like concat('%',#{keyword},'%'))</if>",
    "order by id desc limit #{limit} offset #{offset}",
    "</script>"
  })
  List<Map<String, Object>> selectMaintainOptions(
      @Param("keyword") String keyword,
      @Param("limit") int limit,
      @Param("offset") int offset);

  @Select("select count(*) from ymcx_merchant where id=#{id} and is_delete=1 and deletetime is null")
  int activeMerchant(@Param("id") long id);

  @Select("select count(*) from ymcx_platform_car where id=#{id} and is_delete=1 and deletetime is null")
  int activePlatformCar(@Param("id") long id);

  @Select("select count(*) from ymcx_merchant_car where merchant_id=#{merchantId} and platform_car_id=#{carId} and is_delete=1 and deletetime is null and id != #{excludedId}")
  int duplicateCar(
      @Param("merchantId") long merchantId,
      @Param("carId") long carId,
      @Param("excludedId") long excludedId);

  @Insert({
    "<script>",
    "insert into ymcx_merchant_car(merchant_id,platform_car_id,total_stock,stock,admin_id,is_delete,createtime,updatetime",
    "<if test='v.containsKey(\"name\")'>,name</if><if test='v.containsKey(\"intro\")'>,intro</if><if test='v.containsKey(\"images\")'>,images</if><if test='v.containsKey(\"price\")'>,price</if><if test='v.containsKey(\"cash_price\")'>,cash_price</if><if test='v.containsKey(\"month_price\")'>,month_price</if><if test='v.containsKey(\"day_price\")'>,day_price</if><if test='v.containsKey(\"hour_price\")'>,hour_price</if><if test='v.containsKey(\"installment\")'>,installment</if><if test='v.containsKey(\"status\")'>,status</if><if test='v.containsKey(\"merchant_longitude\")'>,merchant_longitude</if><if test='v.containsKey(\"merchant_latitude\")'>,merchant_latitude</if>) values(#{merchantId},#{carId},#{stock},#{stock},#{adminId},1,#{now},#{now}",
    "<if test='v.containsKey(\"name\")'>,#{v.name}</if><if test='v.containsKey(\"intro\")'>,#{v.intro}</if><if test='v.containsKey(\"images\")'>,#{v.images}</if><if test='v.containsKey(\"price\")'>,#{v.price}</if><if test='v.containsKey(\"cash_price\")'>,#{v.cash_price}</if><if test='v.containsKey(\"month_price\")'>,#{v.month_price}</if><if test='v.containsKey(\"day_price\")'>,#{v.day_price}</if><if test='v.containsKey(\"hour_price\")'>,#{v.hour_price}</if><if test='v.containsKey(\"installment\")'>,#{v.installment}</if><if test='v.containsKey(\"status\")'>,#{v.status}</if><if test='v.containsKey(\"merchant_longitude\")'>,#{v.merchant_longitude}</if><if test='v.containsKey(\"merchant_latitude\")'>,#{v.merchant_latitude}</if>)",
    "</script>"
  })
  int insertCar(
      @Param("merchantId") long merchantId,
      @Param("carId") long carId,
      @Param("stock") int stock,
      @Param("adminId") long adminId,
      @Param("now") long now,
      @Param("v") Map<String, Object> values);

  @Select("select last_insert_id()")
  Long lastInsertId();

  @Select("select * from ymcx_merchant_car where id=#{id} and is_delete=1 and deletetime is null for update")
  Map<String, Object> lockCar(@Param("id") long id);

  @Update({
    "<script>",
    "update ymcx_merchant_car set merchant_id=#{merchantId},platform_car_id=#{carId},total_stock=#{total},stock=#{stock},updatetime=#{now}",
    "<if test='v.containsKey(\"name\")'>,name=#{v.name}</if><if test='v.containsKey(\"intro\")'>,intro=#{v.intro}</if><if test='v.containsKey(\"images\")'>,images=#{v.images}</if><if test='v.containsKey(\"price\")'>,price=#{v.price}</if><if test='v.containsKey(\"cash_price\")'>,cash_price=#{v.cash_price}</if><if test='v.containsKey(\"month_price\")'>,month_price=#{v.month_price}</if><if test='v.containsKey(\"day_price\")'>,day_price=#{v.day_price}</if><if test='v.containsKey(\"hour_price\")'>,hour_price=#{v.hour_price}</if><if test='v.containsKey(\"installment\")'>,installment=#{v.installment}</if><if test='v.containsKey(\"status\")'>,status=#{v.status}</if><if test='v.containsKey(\"merchant_longitude\")'>,merchant_longitude=#{v.merchant_longitude}</if><if test='v.containsKey(\"merchant_latitude\")'>,merchant_latitude=#{v.merchant_latitude}</if>",
    "where id=#{id} and is_delete=1 and deletetime is null",
    "</script>"
  })
  int updateCar(
      @Param("id") long id,
      @Param("merchantId") long merchantId,
      @Param("carId") long carId,
      @Param("total") int total,
      @Param("stock") int stock,
      @Param("now") long now,
      @Param("v") Map<String, Object> values);

  @Update("update ymcx_merchant_car set deletetime=#{now},updatetime=#{now} where id=#{id} and is_delete=1 and deletetime is null")
  int deleteCar(@Param("id") long id, @Param("now") long now);

  @Select("select count(*) from ymcx_merchant_car where deletetime is not null")
  int recycledCarCount();

  @Select("select * from ymcx_merchant_car where deletetime is not null order by id desc limit #{limit} offset #{offset}")
  List<Map<String, Object>> selectRecycledCars(
      @Param("limit") int limit, @Param("offset") int offset);

  @Update({
    "<script>",
    "update ymcx_merchant_car set deletetime=null,updatetime=#{now}",
    "where deletetime is not null",
    "<if test='ids != null and ids.size() &gt; 0'>",
    "and id in <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>",
    "</if>",
    "</script>"
  })
  int restoreRecycledCars(@Param("ids") List<Long> ids, @Param("now") long now);

  @Delete({
    "<script>",
    "delete from ymcx_merchant_car where deletetime is not null",
    "<if test='ids != null and ids.size() &gt; 0'>",
    "and id in <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>",
    "</if>",
    "</script>"
  })
  int destroyRecycledCars(@Param("ids") List<Long> ids);

  @Update({
    "<script>",
    "update ymcx_merchant_car set status=#{status},updatetime=#{now}",
    "where is_delete=1 and deletetime is null and id in",
    "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>",
    "</script>"
  })
  int updateCarStatuses(
      @Param("ids") List<Long> ids,
      @Param("status") int status,
      @Param("now") long now);

  @Select("select admin_id from ymcx_merchant where id=#{id} and is_delete=1 and deletetime is null")
  Long merchantAdminId(@Param("id") long id);

  @Select("select count(*) from ymcx_maintain where id=#{id} and deletetime is null")
  int maintainExists(@Param("id") long id);

  @Select("select count(*) from ymcx_merchant_maintain where merchant_id=#{merchantId} and maintain_id=#{maintainId} and deletetime is null")
  int duplicateMaintain(@Param("merchantId") long merchantId, @Param("maintainId") long maintainId);

  @Insert("insert into ymcx_merchant_maintain(merchant_id,maintain_id,admin_id,price,createtime,updatetime) values(#{merchantId},#{maintainId},#{ownerAdminId},#{price},#{now},#{now})")
  int insertMaintain(
      @Param("merchantId") long merchantId,
      @Param("maintainId") long maintainId,
      @Param("ownerAdminId") long ownerAdminId,
      @Param("price") java.math.BigDecimal price,
      @Param("now") long now);

  @Update("update ymcx_merchant_maintain set price=#{price},updatetime=#{now} where id=#{id} and deletetime is null")
  int updateMaintain(
      @Param("id") long id,
      @Param("price") java.math.BigDecimal price,
      @Param("now") long now);

  @Update("update ymcx_merchant_maintain set deletetime=#{now},updatetime=#{now} where id=#{id} and deletetime is null")
  int deleteMaintain(@Param("id") long id, @Param("now") long now);

  @Select("select count(*) from ymcx_merchant_maintain where deletetime is not null")
  int recycledMaintainCount();

  @Select("select * from ymcx_merchant_maintain where deletetime is not null order by id desc limit #{limit} offset #{offset}")
  List<Map<String, Object>> selectRecycledMaintains(
      @Param("limit") int limit, @Param("offset") int offset);

  @Update({
    "<script>",
    "update ymcx_merchant_maintain set deletetime=null,updatetime=#{now}",
    "where deletetime is not null",
    "<if test='ids != null and ids.size() &gt; 0'>",
    "and id in <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>",
    "</if>",
    "</script>"
  })
  int restoreRecycledMaintains(@Param("ids") List<Long> ids, @Param("now") long now);

  @Delete({
    "<script>",
    "delete from ymcx_merchant_maintain where deletetime is not null",
    "<if test='ids != null and ids.size() &gt; 0'>",
    "and id in <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>",
    "</if>",
    "</script>"
  })
  int destroyRecycledMaintains(@Param("ids") List<Long> ids);
}
