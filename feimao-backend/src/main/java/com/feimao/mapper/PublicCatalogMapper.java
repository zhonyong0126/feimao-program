package com.feimao.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PublicCatalogMapper {
  @Select({"<script>", "select name,title,value,extend from ymcx_config where id&gt;18",
      "<if test='name != null and name != \"\"'>and name=#{name}</if>", "</script>"})
  List<Map<String, Object>> selectConfig(@Param("name") String name);

  @Select("select id,image from ymcx_banner where status=1 order by weigh desc")
  List<Map<String, Object>> selectBanners();

  @Select("select id as brand_id,name from ymcx_brand where status=1 and deletetime is null order by weigh asc")
  List<Map<String, Object>> selectBrands();

  @Select("select id as merchant_id,name,car_num,min_price,avatar,administrative,business,address,pick_type,lat,lng,(6378.138*2*asin(sqrt(pow(sin((cast(lat as decimal(10,6))*pi()/180-#{lat}*pi()/180)/2),2)+cos(cast(lat as decimal(10,6))*pi()/180)*cos(#{lat}*pi()/180)*pow(sin((cast(lng as decimal(10,6))*pi()/180-#{lng}*pi()/180)/2),2)))*1000) distance from ymcx_merchant where status=1 and is_delete=1 and examine_status=2 and deletetime is null order by distance limit #{limit} offset #{offset}")
  List<Map<String, Object>> selectNearbyMerchants(@Param("lat") double lat,
      @Param("lng") double lng, @Param("limit") long limit, @Param("offset") long offset);

  @Select("select id as merchant_id,name,car_num,min_price,avatar,administrative,business,address,pick_type,lat,lng,(6378.138*2*asin(sqrt(pow(sin((cast(lat as decimal(10,6))*pi()/180-#{lat}*pi()/180)/2),2)+cos(cast(lat as decimal(10,6))*pi()/180)*cos(#{lat}*pi()/180)*pow(sin((cast(lng as decimal(10,6))*pi()/180-#{lng}*pi()/180)/2),2)))*1000) distance from ymcx_merchant where status=1 and is_delete=1 and examine_status=2 and deletetime is null order by distance")
  List<Map<String, Object>> selectMapMerchants(@Param("lat") double lat, @Param("lng") double lng);

  @Select("select count(*) from ymcx_merchant where status=1 and is_delete=1 and examine_status=2 and deletetime is null")
  int merchantCount();

  @Select({"<script>", "select p.id,p.name,p.image index_image,p.image,p.brand_id,any_value(mc.day_price) min_price,(select sum(x.stock) from ymcx_merchant_car x where x.platform_car_id=p.id and x.status=1 and x.is_delete=1 and x.deletetime is null) car_num,any_value(mc.createtime) createtime from ymcx_merchant_car mc join ymcx_platform_car p on p.id=mc.platform_car_id join ymcx_merchant m on m.id=mc.merchant_id where mc.status=1 and mc.is_delete=1 and mc.deletetime is null and p.status=1 and p.is_delete=1 and p.deletetime is null and m.status=1 and m.is_delete=1 and m.examine_status=2 and m.deletetime is null",
      "<if test='brandId &gt; 0'>and p.brand_id=#{brandId}</if>", "group by p.id,p.name,p.image,p.brand_id",
      "<choose><when test='sort==1'>order by any_value(mc.hour_price) asc</when><when test='sort==2'>order by any_value(mc.hour_price) desc</when><otherwise>order by createtime desc</otherwise></choose>",
      "limit #{limit} offset #{offset}", "</script>"})
  List<Map<String, Object>> selectLeaseCars(@Param("brandId") long brandId, @Param("sort") int sort,
      @Param("limit") long limit, @Param("offset") long offset);

  @Select({"<script>", "select count(distinct p.id) from ymcx_merchant_car mc join ymcx_platform_car p on p.id=mc.platform_car_id join ymcx_merchant m on m.id=mc.merchant_id where mc.status=1 and mc.is_delete=1 and mc.deletetime is null and p.status=1 and p.is_delete=1 and p.deletetime is null and m.status=1 and m.is_delete=1 and m.examine_status=2 and m.deletetime is null",
      "<if test='brandId &gt; 0'>and p.brand_id=#{brandId}</if>", "</script>"})
  int leaseCarCount(@Param("brandId") long brandId);

  @Select({"<script>", "select p.id,p.image,p.image index_image,any_value(mc.price) min_price,p.name from ymcx_merchant_car mc join ymcx_platform_car p on p.id=mc.platform_car_id where mc.status=1 and mc.is_delete=1 and mc.deletetime is null and p.status=1 and p.is_delete=1 and p.deletetime is null",
      "<if test='brandId &gt; 0'>and p.brand_id=#{brandId}</if>", "group by p.id,p.image,p.name",
      "<choose><when test='sort==1'>order by min_price asc</when><when test='sort!=0'>order by min_price desc</when><otherwise>order by p.createtime desc</otherwise></choose>",
      "limit #{limit} offset #{offset}", "</script>"})
  List<Map<String, Object>> selectBuyCars(@Param("brandId") long brandId, @Param("sort") int sort,
      @Param("limit") long limit, @Param("offset") long offset);

  @Select({"<script>", "select count(distinct mc.platform_car_id) from ymcx_merchant_car mc join ymcx_platform_car p on p.id=mc.platform_car_id where mc.status=1 and mc.is_delete=1 and mc.deletetime is null and p.status=1 and p.is_delete=1 and p.deletetime is null",
      "<if test='brandId &gt; 0'>and p.brand_id=#{brandId}</if>", "</script>"})
  int buyCarCount(@Param("brandId") long brandId);

  @Select("select mc.id,mc.merchant_id,mc.platform_car_id,mc.day_price price,mc.stock,p.name,p.image,m.id mid,m.pick_type,m.image merchant_image,m.avatar,m.administrative,m.address,m.name merchant_name from ymcx_merchant_car mc left join ymcx_platform_car p on p.id=mc.platform_car_id and p.deletetime is null left join ymcx_merchant m on m.id=mc.merchant_id and m.deletetime is null where mc.platform_car_id=#{carId} and mc.status=1 and mc.deletetime is null order by mc.day_price")
  List<Map<String, Object>> selectLeaseMerchants(@Param("carId") long carId);

  @Select("select mc.id,mc.merchant_id,mc.price,m.id mid,m.pick_type,m.image merchant_image,m.address,m.name merchant_name from ymcx_merchant_car mc left join ymcx_merchant m on m.id=mc.merchant_id and m.is_delete=1 and m.deletetime is null where mc.platform_car_id=#{carId} and mc.status=1 and mc.is_delete=1 and mc.deletetime is null order by mc.price")
  List<Map<String, Object>> selectBuyMerchants(@Param("carId") long carId);

  @Select("select mm.maintain_id,min(mm.price) price,m.image,m.name,m.introductory from ymcx_merchant_maintain mm join ymcx_maintain m on m.id=mm.maintain_id where mm.deletetime is null and m.deletetime is null group by mm.maintain_id,m.image,m.name,m.introductory order by price limit #{limit} offset #{offset}")
  List<Map<String, Object>> selectMaintains(@Param("limit") long limit, @Param("offset") long offset);

  @Select("select count(distinct mm.maintain_id) from ymcx_merchant_maintain mm join ymcx_maintain m on m.id=mm.maintain_id where mm.deletetime is null and m.deletetime is null")
  int maintainCount();

  @Select("select mm.id,mm.merchant_id,mm.price,m.id mid,m.pick_type,m.image merchant_image,m.address,m.name merchant_name from ymcx_merchant_maintain mm left join ymcx_merchant m on m.id=mm.merchant_id and m.deletetime is null where mm.maintain_id=#{maintainId} and mm.deletetime is null order by mm.price")
  List<Map<String, Object>> selectMaintainMerchants(@Param("maintainId") long maintainId);
}
