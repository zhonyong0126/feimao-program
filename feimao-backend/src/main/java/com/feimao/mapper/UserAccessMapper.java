package com.feimao.mapper;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserAccessMapper {
  @Select("select id,name,rules,status,createtime,updatetime from ymcx_user_group order by id") List<Map<String,Object>> selectGroups();
  @Select("select id,rules from ymcx_user_group") List<Map<String,Object>> selectGroupRules();
  @Insert("insert into ymcx_user_group(name,rules,createtime,updatetime,status) values(#{name},#{rules},#{now},#{now},#{status})") int insertGroup(@Param("name") String name,@Param("rules") String rules,@Param("status") String status,@Param("now") long now);
  @Select("select last_insert_id()") Long lastInsertId();
  @Select("select count(*) from ymcx_user_group where id=#{id}") int groupCount(@Param("id") long id);
  @Update("update ymcx_user_group set name=#{name},rules=#{rules},status=#{status},updatetime=#{now} where id=#{id}") int updateGroup(@Param("id") long id,@Param("name") String name,@Param("rules") String rules,@Param("status") String status,@Param("now") long now);
  @Select("select count(*) from ymcx_user where group_id=#{id}") int groupUserCount(@Param("id") long id);
  @Delete("delete from ymcx_user_group where id=#{id}") int deleteGroup(@Param("id") long id);
  @Select("select id,pid,name,title,remark,ismenu,weigh,status,createtime,updatetime from ymcx_user_rule order by weigh desc,id") List<Map<String,Object>> selectRules();
  @Insert("insert into ymcx_user_rule(pid,name,title,remark,ismenu,createtime,updatetime,weigh,status) values(#{pid},#{name},#{title},#{remark},#{ismenu},#{now},#{now},#{weigh},#{status})") int insertRule(@Param("pid") long pid,@Param("name") String name,@Param("title") String title,@Param("remark") String remark,@Param("ismenu") long ismenu,@Param("weigh") long weigh,@Param("status") String status,@Param("now") long now);
  @Select("select count(*) from ymcx_user_rule where id=#{id}") int ruleCount(@Param("id") long id);
  @Select("select count(*) from ymcx_user_rule where name=#{name} and id<>#{id}") int ruleNameCount(@Param("name") String name,@Param("id") long id);
  @Update("update ymcx_user_rule set pid=#{pid},name=#{name},title=#{title},remark=#{remark},ismenu=#{ismenu},weigh=#{weigh},status=#{status},updatetime=#{now} where id=#{id}") int updateRule(@Param("id") long id,@Param("pid") long pid,@Param("name") String name,@Param("title") String title,@Param("remark") String remark,@Param("ismenu") long ismenu,@Param("weigh") long weigh,@Param("status") String status,@Param("now") long now);
  @Select({"<script>","select id from ymcx_user_rule where pid in","<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>","</script>"}) List<Long> selectChildren(@Param("ids") Collection<Long> ids);
  @Delete({"<script>","delete from ymcx_user_rule where id in","<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>","</script>"}) int deleteRules(@Param("ids") Collection<Long> ids);
  @Update("update ymcx_user_group set rules=#{rules},updatetime=#{now} where id=#{id}") int updateGroupRules(@Param("id") long id,@Param("rules") String rules,@Param("now") long now);
}
