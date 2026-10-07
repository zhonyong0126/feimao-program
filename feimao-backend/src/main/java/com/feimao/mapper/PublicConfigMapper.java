package com.feimao.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PublicConfigMapper {
  @Select("select value from ymcx_config where name='attachmentcategory' order by id limit 1")
  String selectAttachmentCategoryConfig();

  @Select(
      "select oldversion,newversion,packagesize,content,downloadurl,enforce "
          + "from ymcx_version where status='normal' order by weigh desc,id desc")
  List<Map<String, Object>> selectUpgrades();

  @Select(
      "select city.id,city.pid,city.shortname,city.name,city.mergename,city.level,"
          + "city.pinyin,city.code,city.zip,city.first,city.lng,city.lat "
          + "from (select district.pid,"
          + "6371.0*2*asin(least(1,sqrt("
          + "pow(sin((cast(district.lat as decimal(12,6))*pi()/180-#{lat}*pi()/180)/2),2)+"
          + "cos(cast(district.lat as decimal(12,6))*pi()/180)*cos(#{lat}*pi()/180)*"
          + "pow(sin((cast(district.lng as decimal(12,6))*pi()/180-#{lng}*pi()/180)/2),2)"
          + "))) distance from ymcx_area district where district.level=3 "
          + "and district.lng regexp '^-?[0-9]+([.][0-9]+)?$' "
          + "and district.lat regexp '^-?[0-9]+([.][0-9]+)?$' "
          + "and cast(district.lng as decimal(12,6)) between -180 and 180 "
          + "and cast(district.lat as decimal(12,6)) between -90 and 90) nearby "
          + "join ymcx_area city on city.id=nearby.pid "
          + "where nearby.distance<=200 order by nearby.distance limit 1")
  Map<String, Object> selectNearestCityWithinRange(
      @org.apache.ibatis.annotations.Param("lng") java.math.BigDecimal lng,
      @org.apache.ibatis.annotations.Param("lat") java.math.BigDecimal lat);

  @Select(
      "select city.id,city.pid,city.shortname,city.name,city.mergename,city.level,"
          + "city.pinyin,city.code,city.zip,city.first,city.lng,city.lat "
          + "from ymcx_area district join ymcx_area city on city.id=district.pid "
          + "where district.id=#{districtId} limit 1")
  Map<String, Object> selectCityByDistrictId(
      @org.apache.ibatis.annotations.Param("districtId") long districtId);
}
