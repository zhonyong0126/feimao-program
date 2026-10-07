package com.feimao.mapper;

import java.util.List;
import java.util.Map;
import com.feimao.mapper.provider.RbacAdminSqlProvider;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.UpdateProvider;

@Mapper
public interface RbacAdminMapper {
  @InsertProvider(type = RbacAdminSqlProvider.class, method = "insert")
  @Options(useGeneratedKeys = true, keyProperty = "v.id")
  int insert(
      @Param("kind") String kind, @Param("v") Map<String, Object> values);

  @UpdateProvider(type = RbacAdminSqlProvider.class, method = "update")
  int update(
      @Param("kind") String kind,
      @Param("id") long id,
      @Param("v") Map<String, Object> values);

  @Select({
    "<script>select count(*) from ymcx_admin a where 1=1",
    "<if test='id != null'>and a.id=#{id}</if>",
    "<if test='username != null and username != \"\"'>and a.username like concat('%',#{username},'%')</if>",
    "<if test='nickname != null and nickname != \"\"'>and a.nickname like concat('%',#{nickname},'%')</if>",
    "<if test='mobile != null and mobile != \"\"'>and a.mobile like concat('%',#{mobile},'%')</if>",
    "<if test='status != null and status != \"\"'>and a.status=#{status}</if>",
    "<if test='loginFrom != null'>and a.logintime&gt;=#{loginFrom}</if>",
    "<if test='loginTo != null'>and a.logintime&lt;=#{loginTo}</if>",
    "<if test='unrestricted == 0'>and a.id in <foreach collection='visibleIds' item='visibleId' open='(' separator=',' close=')'>#{visibleId}</foreach></if>",
    "</script>"
  })
  int adminCount(
      @Param("id") Long id,
      @Param("username") String username,
      @Param("nickname") String nickname,
      @Param("mobile") String mobile,
      @Param("status") String status,
      @Param("loginFrom") Long loginFrom,
      @Param("loginTo") Long loginTo,
      @Param("unrestricted") int unrestricted,
      @Param("visibleIds") List<Long> visibleIds);

  @Select({
    "<script>select a.id,a.username,a.nickname,a.avatar,a.email,a.mobile,a.loginfailure,a.logintime,a.loginip,a.createtime,a.updatetime,a.status,a.merchant_id from ymcx_admin a where 1=1",
    "<if test='id != null'>and a.id=#{id}</if>",
    "<if test='username != null and username != \"\"'>and a.username like concat('%',#{username},'%')</if>",
    "<if test='nickname != null and nickname != \"\"'>and a.nickname like concat('%',#{nickname},'%')</if>",
    "<if test='mobile != null and mobile != \"\"'>and a.mobile like concat('%',#{mobile},'%')</if>",
    "<if test='status != null and status != \"\"'>and a.status=#{status}</if>",
    "<if test='loginFrom != null'>and a.logintime&gt;=#{loginFrom}</if>",
    "<if test='loginTo != null'>and a.logintime&lt;=#{loginTo}</if>",
    "<if test='unrestricted == 0'>and a.id in <foreach collection='visibleIds' item='visibleId' open='(' separator=',' close=')'>#{visibleId}</foreach></if>",
    "order by a.id desc limit #{limit} offset #{offset}</script>"
  })
  List<Map<String, Object>> admins(
      @Param("id") Long id,
      @Param("username") String username,
      @Param("nickname") String nickname,
      @Param("mobile") String mobile,
      @Param("status") String status,
      @Param("loginFrom") Long loginFrom,
      @Param("loginTo") Long loginTo,
      @Param("unrestricted") int unrestricted,
      @Param("visibleIds") List<Long> visibleIds,
      @Param("limit") int limit,
      @Param("offset") int offset);

  @Select(
      "select g.id,g.name,g.status from ymcx_auth_group_access ga join ymcx_auth_group g on g.id=ga.group_id where ga.uid=#{adminId} order by g.id")
  List<Map<String, Object>> adminGroups(@Param("adminId") long adminId);

  @Select("select count(*) from ymcx_admin where id=#{id}")
  int adminExists(@Param("id") long id);

  @Select("select count(*) from ymcx_admin where username=#{username} and id<>#{excludeId}")
  int adminUsernameCount(
      @Param("username") String username, @Param("excludeId") long excludeId);

  @Select("select count(*) from ymcx_admin where mobile=#{mobile} and mobile<>'' and id<>#{excludeId}")
  int adminMobileCount(@Param("mobile") String mobile, @Param("excludeId") long excludeId);

  @Select("select group_id from ymcx_auth_group_access where uid=#{adminId} order by group_id")
  List<Long> currentAdminGroupIds(@Param("adminId") long adminId);

  @Select({
    "<script>select distinct uid from ymcx_auth_group_access where group_id in",
    "<foreach collection='groupIds' item='groupId' open='(' separator=',' close=')'>#{groupId}</foreach>",
    "</script>"
  })
  List<Long> adminIdsInGroups(@Param("groupIds") List<Long> groupIds);

  @Select({
    "<script>select distinct r.name from ymcx_auth_group_access ga",
    "join ymcx_auth_group g on g.id=ga.group_id and g.status='normal'",
    "join ymcx_auth_rule r on (g.rules='*' or find_in_set(r.id,g.rules)&gt;0) and r.status='normal'",
    "where ga.uid=#{adminId} and r.name in",
    "<foreach collection='names' item='name' open='(' separator=',' close=')'>#{name}</foreach>",
    "</script>"
  })
  List<String> allowedRuleNames(
      @Param("adminId") long adminId, @Param("names") List<String> names);

  @Select("select g.* from ymcx_auth_group g order by g.pid,g.id")
  List<Map<String, Object>> groups();

  @Select("select count(*) from ymcx_auth_group where id=#{id}")
  int groupExists(@Param("id") long id);

  @Select("select count(*) from ymcx_auth_group_access where group_id=#{id}")
  int groupMemberCount(@Param("id") long id);

  @Select("select count(*) from ymcx_auth_group where pid=#{id}")
  int childGroupCount(@Param("id") long id);

  @Select("select * from ymcx_auth_rule order by pid,weigh desc,id")
  List<Map<String, Object>> rules();

  @Select("select count(*) from ymcx_auth_rule where id=#{id}")
  int ruleExists(@Param("id") long id);

  @Select("select count(*) from ymcx_auth_rule where name=#{name} and id<>#{excludeId}")
  int ruleNameCount(@Param("name") String name, @Param("excludeId") long excludeId);

  @Select("select count(*) from ymcx_auth_rule where pid=#{id}")
  int childRuleCount(@Param("id") long id);

  @Delete("delete from ymcx_admin where id=#{id}") int deleteAdmin(@Param("id") long id);
  @Delete("delete from ymcx_auth_group_access where uid=#{id}") int deleteAdminGroups(@Param("id") long id);
  @Delete("delete from feimao_admin_session where admin_id=#{id}") int deleteAdminSessions(@Param("id") long id);
  @Insert("insert into ymcx_auth_group_access(uid,group_id) values(#{adminId},#{groupId})")
  int insertAdminGroup(@Param("adminId") long adminId,@Param("groupId") long groupId);
  @Delete("delete from ymcx_auth_group where id=#{id}") int deleteGroup(@Param("id") long id);
  @Delete("delete from ymcx_auth_rule where id=#{id}") int deleteRule(@Param("id") long id);
  @Select("select id,rules from ymcx_auth_group") List<Map<String,Object>> groupRules();
  @Update("update ymcx_auth_group set rules=#{rules},updatetime=#{now} where id=#{id}")
  int updateGroupRules(@Param("id") long id,@Param("rules") String rules,@Param("now") long now);
}
