package com.feimao.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserAdminMapper {
  @Select("select id,money,score from ymcx_user where id=#{id} for update")
  Map<String, Object> lockUser(@Param("id") long id);

  @Insert("insert into ymcx_user_money_log(user_id,money,`before`,`after`,memo,createtime) values(#{userId},#{change},#{before},#{after},#{memo},#{now})")
  int insertMoneyLog(@Param("userId") long userId, @Param("change") java.math.BigDecimal change,
      @Param("before") java.math.BigDecimal before, @Param("after") java.math.BigDecimal after,
      @Param("memo") String memo, @Param("now") long now);

  @Insert("insert into ymcx_user_score_log(user_id,score,`before`,`after`,memo,createtime) values(#{userId},#{change},#{before},#{after},#{memo},#{now})")
  int insertScoreLog(@Param("userId") long userId, @Param("change") int change,
      @Param("before") int before, @Param("after") int after,
      @Param("memo") String memo, @Param("now") long now);

  @Select({"<script>", "select count(*) from ymcx_user u where 1=1",
      "<if test='id != null'>and u.id=#{id}</if>",
      "<if test='nickname != null and nickname != \"\"'>and u.nickname like concat('%',#{nickname},'%')</if>",
      "<if test='mobile != null and mobile != \"\"'>and u.mobile like concat('%',#{mobile},'%')</if>",
      "<if test='jointimeFrom != null'>and u.jointime &gt;= #{jointimeFrom}</if>",
      "<if test='jointimeTo != null'>and u.jointime &lt;= #{jointimeTo}</if>",
      "<if test='status != null and status != \"\"'>and u.status=#{status}</if>", "</script>"})
  int countRows(@Param("id") Long id, @Param("nickname") String nickname,
      @Param("mobile") String mobile, @Param("jointimeFrom") Long jointimeFrom,
      @Param("jointimeTo") Long jointimeTo, @Param("status") String status);

  @Select({"<script>",
      "select u.id,u.avatar,u.nickname,u.mobile,u.jointime,u.status from ymcx_user u where 1=1",
      "<if test='id != null'>and u.id=#{id}</if>",
      "<if test='nickname != null and nickname != \"\"'>and u.nickname like concat('%',#{nickname},'%')</if>",
      "<if test='mobile != null and mobile != \"\"'>and u.mobile like concat('%',#{mobile},'%')</if>",
      "<if test='jointimeFrom != null'>and u.jointime &gt;= #{jointimeFrom}</if>",
      "<if test='jointimeTo != null'>and u.jointime &lt;= #{jointimeTo}</if>",
      "<if test='status != null and status != \"\"'>and u.status=#{status}</if>",
      "order by u.id desc limit #{limit} offset #{offset}", "</script>"})
  List<Map<String, Object>> selectRows(@Param("id") Long id,
      @Param("nickname") String nickname, @Param("mobile") String mobile,
      @Param("jointimeFrom") Long jointimeFrom, @Param("jointimeTo") Long jointimeTo,
      @Param("status") String status, @Param("limit") int limit,
      @Param("offset") int offset);

  @Delete({"<script>", "delete from ymcx_user where id in",
      "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>",
      "</script>"})
  int deleteRows(@Param("ids") List<Long> ids);

  @Update({"<script>", "update ymcx_user set status=#{status},updatetime=#{now} where id in",
      "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>",
      "</script>"})
  int updateStatuses(@Param("ids") List<Long> ids, @Param("status") String status,
      @Param("now") long now);
}
