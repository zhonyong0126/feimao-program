package com.feimao.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface PartnerCodeMapper {
  @Select("select count(*) from ymcx_merchant where id=#{id} and is_delete=1 and deletetime is null")
  int merchantCount(@Param("id") long id);

  @Select("select count(*) from ymcx_shop where id=#{id} and is_delete=1 and deletetime is null")
  int shopCount(@Param("id") long id);

  @Update("update ymcx_merchant set qrimage=#{path},updatetime=#{now} where id=#{id} and is_delete=1 and deletetime is null")
  int updateMerchantCode(@Param("id") long id, @Param("path") String path, @Param("now") long now);

  @Update("update ymcx_shop set qrimage=#{path},updatetime=#{now} where id=#{id} and is_delete=1 and deletetime is null")
  int updateShopCode(@Param("id") long id, @Param("path") String path, @Param("now") long now);

  @Select("select id from ymcx_merchant where is_delete=1 and deletetime is null and examine_status in(1,2) and (qrimage is null or qrimage='') order by id limit 10")
  List<Long> missingMerchantCodes();

  @Select("select id from ymcx_shop where is_delete=1 and deletetime is null and examine_status in(1,2) and (qrimage is null or qrimage='') order by id limit 10")
  List<Long> missingShopCodes();
}
