package com.feimao.mapper;

import java.util.Map;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface VerificationCodeMapper {
  @Insert("insert into ymcx_sms(event,mobile,code,times,ip,createtime) values(#{event},#{target},#{code},0,#{ip},#{now})")
  int insertSms(@Param("target") String target, @Param("event") String event,
      @Param("code") String code, @Param("ip") String ip, @Param("now") long now);

  @Insert("insert into ymcx_ems(event,email,code,times,ip,createtime) values(#{event},#{target},#{code},0,#{ip},#{now})")
  int insertEmail(@Param("target") String target, @Param("event") String event,
      @Param("code") String code, @Param("ip") String ip, @Param("now") long now);

  @Select("select id,code,times,createtime from ymcx_sms where mobile=#{target} and event=#{event} order by id desc limit 1")
  Map<String, Object> latestSms(@Param("target") String target, @Param("event") String event);

  @Select("select id,code,times,createtime from ymcx_ems where email=#{target} and event=#{event} order by id desc limit 1")
  Map<String, Object> latestEmail(@Param("target") String target, @Param("event") String event);

  @Update("update ymcx_sms set times=times+1 where id=#{id}")
  int incrementSms(@Param("id") long id);

  @Update("update ymcx_ems set times=times+1 where id=#{id}")
  int incrementEmail(@Param("id") long id);

  @Delete("delete from ymcx_sms where mobile=#{target} and event=#{event}")
  int deleteSms(@Param("target") String target, @Param("event") String event);

  @Delete("delete from ymcx_ems where email=#{target} and event=#{event}")
  int deleteEmail(@Param("target") String target, @Param("event") String event);

  @Select("select count(*) from ymcx_sms where mobile=#{target} and event=#{event} and createtime>#{after}")
  int recentSms(@Param("target") String target, @Param("event") String event,
      @Param("after") long after);

  @Select("select count(*) from ymcx_ems where email=#{target} and event=#{event} and createtime>#{after}")
  int recentEmail(@Param("target") String target, @Param("event") String event,
      @Param("after") long after);

  @Select("select count(*) from ymcx_sms where ip=#{ip} and createtime>#{after}")
  int recentSmsByIp(@Param("ip") String ip, @Param("after") long after);

  @Select("select count(*) from ymcx_user where mobile=#{target}")
  int mobileAccountCount(@Param("target") String target);

  @Select("select count(*) from ymcx_user where email=#{target}")
  int emailAccountCount(@Param("target") String target);
}
