package com.feimao.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MerchantCatalogMapper {
  @Select("select id merchant_id,name,image,mobile,business,administrative,address,pick_type,lat,lng from ymcx_merchant where id=#{id} and status=1 and deletetime is null")
  Map<String, Object> selectMerchant(@Param("id") long id);
  @Select({"<script>", "select b.id brand_id,b.name from ymcx_brand b join ymcx_platform_car p on p.brand_id=b.id join ymcx_merchant_car mc on mc.platform_car_id=p.id where mc.merchant_id=#{merchantId} and mc.status=1 and mc.is_delete=1 and mc.deletetime is null and p.status=1 and p.is_delete=1 and p.deletetime is null and b.status=1 and b.deletetime is null",
      "<if test='brandId &gt; 0'>and b.id=#{brandId}</if>", "group by b.id,b.name,b.weigh order by b.weigh desc", "</script>"})
  List<Map<String, Object>> selectBrands(@Param("merchantId") long merchantId, @Param("brandId") long brandId);
  @Select({"<script>", "select mc.id merchant_car_id,mc.day_price min_price,mc.stock,p.image,p.name,p.specification from ymcx_merchant_car mc join ymcx_platform_car p on p.id=mc.platform_car_id where mc.merchant_id=#{merchantId} and mc.status=1 and mc.is_delete=1 and mc.deletetime is null and p.status=1 and p.is_delete=1 and p.deletetime is null",
      "<if test='brandId &gt; 0'>and p.brand_id=#{brandId}</if>", "order by mc.createtime desc",
      "limit #{limit} offset #{offset}", "</script>"})
  List<Map<String, Object>> selectLeaseCars(@Param("merchantId") long merchantId,
      @Param("brandId") long brandId, @Param("limit") long limit, @Param("offset") long offset);
  @Select({"<script>", "select count(*) from ymcx_merchant_car mc join ymcx_platform_car p on p.id=mc.platform_car_id where mc.merchant_id=#{merchantId} and mc.status=1 and mc.is_delete=1 and mc.deletetime is null and p.status=1 and p.is_delete=1 and p.deletetime is null",
      "<if test='brandId &gt; 0'>and p.brand_id=#{brandId}</if>", "</script>"})
  int leaseCarCount(@Param("merchantId") long merchantId, @Param("brandId") long brandId);
  @Select({"<script>", "select mc.id merchat_car_id,p.image,p.name,mc.price from ymcx_merchant_car mc join ymcx_platform_car p on p.id=mc.platform_car_id where mc.merchant_id=#{merchantId} and mc.status=1 and mc.is_delete=1 and mc.deletetime is null and p.is_delete=1 and p.deletetime is null",
      "<if test='brandId &gt; 0'>and p.brand_id=#{brandId}</if>", "order by mc.price asc",
      "limit #{limit} offset #{offset}", "</script>"})
  List<Map<String, Object>> selectBuyCars(@Param("merchantId") long merchantId,
      @Param("brandId") long brandId, @Param("limit") long limit, @Param("offset") long offset);
  @Select({"<script>", "select count(*) from ymcx_merchant_car mc join ymcx_platform_car p on p.id=mc.platform_car_id where mc.merchant_id=#{merchantId} and mc.status=1 and mc.is_delete=1 and mc.deletetime is null and p.is_delete=1 and p.deletetime is null",
      "<if test='brandId &gt; 0'>and p.brand_id=#{brandId}</if>", "</script>"})
  int buyCarCount(@Param("merchantId") long merchantId, @Param("brandId") long brandId);
  @Select("select mc.id,mc.platform_car_id,mc.merchant_id,mc.intro,mc.images,mc.price,mc.hour_price,mc.day_price,mc.month_price,mc.stock,mc.status,p.brand_id,p.name,p.image,p.specification,b.name brand_name,m.name merchant_name,m.business,m.administrative,m.address,m.pick_type from ymcx_merchant_car mc left join ymcx_platform_car p on p.id=mc.platform_car_id and p.deletetime is null left join ymcx_brand b on b.id=p.brand_id and b.deletetime is null left join ymcx_merchant m on m.id=mc.merchant_id and m.deletetime is null where mc.id=#{id} and mc.is_delete=1 and mc.deletetime is null")
  Map<String, Object> selectCar(@Param("id") long id);
  @Select("select mm.id merchant_maintain_id,mm.price,m.name,m.image,m.introductory from ymcx_merchant_maintain mm join ymcx_maintain m on m.id=mm.maintain_id where mm.deletetime is null order by mm.price limit #{limit} offset #{offset}")
  List<Map<String, Object>> selectMaintains(@Param("merchantId") long merchantId,
      @Param("limit") long limit, @Param("offset") long offset);
  @Select("select count(*) from ymcx_merchant_maintain mm join ymcx_maintain m on m.id=mm.maintain_id where mm.deletetime is null")
  int maintainCount(@Param("merchantId") long merchantId);
  @Select("select mm.id merchant_maintain_id,mm.price,m.name,m.content,m.image,m.introductory,me.name merchant_name,me.business,me.address from ymcx_merchant_maintain mm join ymcx_maintain m on m.id=mm.maintain_id join ymcx_merchant me on me.id=mm.merchant_id where mm.id=#{id} and mm.deletetime is null")
  Map<String, Object> selectMaintain(@Param("id") long id);
  @Select("select id merchant_car_id,cash_price,month_price,day_price,hour_price,installment from ymcx_merchant_car where id=#{id} and deletetime is null")
  Map<String, Object> selectLeasePlan(@Param("id") long id);
  @Select("select staff_authentication,authentication from ymcx_user where id=#{userId}")
  Map<String, Object> selectUserAuthentication(@Param("userId") long userId);
}
