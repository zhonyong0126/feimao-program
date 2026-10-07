package com.feimao.mapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SignMapper {
  @Select("select nickname,avatar,score from ymcx_user where id=#{userId}")
  Map<String, Object> selectUserSummary(@Param("userId") long userId);
  @Select("select date(createtime) day from ymcx_sign_log where user_id=#{userId} and createtime>=#{start} and createtime<#{end}")
  List<Map<String, Object>> selectSignedDays(@Param("userId") long userId,
      @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
  @Select("select count(*) from ymcx_sign_log where user_id=#{userId}") int signCount(@Param("userId") long userId);
  @Select("select id coupon_id,score,price,reduceprice,endtime from ymcx_coupon where status='normal' and endtime>#{now} order by weigh desc limit #{limit} offset #{offset}")
  List<Map<String, Object>> selectCoupons(@Param("now") long now, @Param("limit") long limit, @Param("offset") long offset);
  @Select("select count(*) from ymcx_coupon where status='normal' and endtime>#{now}") int couponCount(@Param("now") long now);
  @Select("select id,score,memo,createtime from ymcx_user_score_log where user_id=#{userId} order by createtime desc limit #{limit} offset #{offset}")
  List<Map<String, Object>> selectScoreLogs(@Param("userId") long userId, @Param("limit") long limit, @Param("offset") long offset);
  @Select("select count(*) from ymcx_user_score_log where user_id=#{userId}") int scoreLogCount(@Param("userId") long userId);
  @Select("select * from ymcx_coupon where id=#{id}")
  Map<String, Object> selectCoupon(@Param("id") long id);
  @Select("select score from ymcx_user where id=#{userId} for update") Integer lockUserScore(@Param("userId") long userId);
  @Update("update ymcx_user set score=#{score},updatetime=#{now} where id=#{userId}")
  int updateUserScore(@Param("userId") long userId, @Param("score") int score, @Param("now") long now);
  @Insert("insert into ymcx_user_coupon(user_id,price,reduceprice,state,endtime,createtime,updatetime) values(#{userId},#{price},#{reducePrice},1,#{endTime},#{now},#{now})")
  int insertUserCoupon(@Param("userId") long userId, @Param("price") BigDecimal price,
      @Param("reducePrice") BigDecimal reducePrice, @Param("endTime") Object endTime, @Param("now") long now);
  @Insert("insert into ymcx_user_score_log(user_id,score,`before`,`after`,memo,createtime) values(#{userId},#{change},#{before},#{after},#{memo},#{now})")
  int insertScoreLog(@Param("userId") long userId, @Param("change") int change,
      @Param("before") int before, @Param("after") int after, @Param("memo") String memo,
      @Param("now") long now);
  @Select("select count(*) from ymcx_sign_log where user_id=#{userId} and date(createtime)=current_date") int todaySignCount(@Param("userId") long userId);
  @Select("select value from ymcx_config where name='sign_score' limit 1") String signReward();
  @Insert("insert into ymcx_sign_log(user_id,score,createtime) values(#{userId},#{score},now())")
  int insertSign(@Param("userId") long userId, @Param("score") int score);
}
