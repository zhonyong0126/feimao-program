package com.feimao.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;

@Mapper
public interface SystemAdminMapper {
  @Select({
    "<script>select * from ymcx_config",
    "<if test='group != null and group != \"\"'>where `group`=#{group}</if>",
    "<choose><when test='group != null and group != \"\"'>order by id</when><otherwise>order by `group`,id</otherwise></choose>",
    "</script>"
  })
  List<Map<String, Object>> configs(@Param("group") String group);

  @Select("select * from ymcx_config where id=#{id} limit 1")
  Map<String, Object> configById(@Param("id") long id);

  @Select(
      "select count(*) from ymcx_config where name=#{name} and (#{excludeId} is null or id<>#{excludeId})")
  int configNameCount(@Param("name") String name, @Param("excludeId") Long excludeId);

  @Select({
    "<script>select id,name,title,`group`,type from ymcx_config",
    "<if test='keyword != null and keyword != \"\"'>where name like concat('%',#{keyword},'%') or title like concat('%',#{keyword},'%')</if>",
    "order by id limit #{limit}</script>"
  })
  List<Map<String, Object>> configOptions(
      @Param("keyword") String keyword, @Param("limit") int limit);

  @Select({
    "<script>select name from ymcx_config where name in",
    "<foreach collection='names' item='name' open='(' separator=',' close=')'>#{name}</foreach>",
    "</script>"
  })
  List<String> existingConfigNames(@Param("names") List<String> names);

  @Update("update ymcx_config set value=#{value} where name=#{name}")
  int updateConfig(@Param("name") String name, @Param("value") String value);

  @Select("select value from ymcx_config where name='attachmentcategory' order by id limit 1")
  String attachmentCategoryConfig();

  @Select({
    "<script>",
    "select count(*) from ymcx_attachment where 1=1",
    "<if test='keyword != null and keyword != \"\"'>and (cast(id as char)=#{keyword} or filename like concat('%',#{keyword},'%') or url like concat('%',#{keyword},'%'))</if>",
    "<if test='category != null'>and category=#{category}</if>",
    "<if test='adminId != null'>and admin_id=#{adminId}</if>",
    "<if test='userId != null'>and user_id=#{userId}</if>",
    "<if test='filename != null and filename != \"\"'>and filename like concat('%',#{filename},'%')</if>",
    "<if test='sizeMin != null'>and filesize&gt;=#{sizeMin}</if>",
    "<if test='sizeMax != null'>and filesize&lt;=#{sizeMax}</if>",
    "<if test='imagewidth != null and imagewidth != \"\"'>and imagewidth=#{imagewidth}</if>",
    "<if test='imageheight != null and imageheight != \"\"'>and imageheight=#{imageheight}</if>",
    "<if test='imagetype != null and imagetype != \"\"'>and imagetype like concat('%',#{imagetype},'%')</if>",
    "<if test='storage != null and storage != \"\"'>and storage like concat('%',#{storage},'%')</if>",
    "<if test='mimetypes != null and !mimetypes.isEmpty()'>",
    "and (<foreach collection='mimetypes' item='mimetype' separator=' or '>mimetype like concat('%',#{mimetype},'%')</foreach>)",
    "</if>",
    "<if test='createdFrom != null'>and createtime&gt;=#{createdFrom}</if>",
    "<if test='createdTo != null'>and createtime&lt;=#{createdTo}</if>",
    "</script>"
  })
  int attachmentCount(
      @Param("keyword") String keyword,
      @Param("category") String category,
      @Param("adminId") Long adminId,
      @Param("userId") Long userId,
      @Param("filename") String filename,
      @Param("sizeMin") Long sizeMin,
      @Param("sizeMax") Long sizeMax,
      @Param("imagewidth") String imagewidth,
      @Param("imageheight") String imageheight,
      @Param("imagetype") String imagetype,
      @Param("storage") String storage,
      @Param("mimetypes") List<String> mimetypes,
      @Param("createdFrom") Long createdFrom,
      @Param("createdTo") Long createdTo);

  @Select({
    "<script>",
    "select * from ymcx_attachment where 1=1",
    "<if test='keyword != null and keyword != \"\"'>and (cast(id as char)=#{keyword} or filename like concat('%',#{keyword},'%') or url like concat('%',#{keyword},'%'))</if>",
    "<if test='category != null'>and category=#{category}</if>",
    "<if test='adminId != null'>and admin_id=#{adminId}</if>",
    "<if test='userId != null'>and user_id=#{userId}</if>",
    "<if test='filename != null and filename != \"\"'>and filename like concat('%',#{filename},'%')</if>",
    "<if test='sizeMin != null'>and filesize&gt;=#{sizeMin}</if>",
    "<if test='sizeMax != null'>and filesize&lt;=#{sizeMax}</if>",
    "<if test='imagewidth != null and imagewidth != \"\"'>and imagewidth=#{imagewidth}</if>",
    "<if test='imageheight != null and imageheight != \"\"'>and imageheight=#{imageheight}</if>",
    "<if test='imagetype != null and imagetype != \"\"'>and imagetype like concat('%',#{imagetype},'%')</if>",
    "<if test='storage != null and storage != \"\"'>and storage like concat('%',#{storage},'%')</if>",
    "<if test='mimetypes != null and !mimetypes.isEmpty()'>",
    "and (<foreach collection='mimetypes' item='mimetype' separator=' or '>mimetype like concat('%',#{mimetype},'%')</foreach>)",
    "</if>",
    "<if test='createdFrom != null'>and createtime&gt;=#{createdFrom}</if>",
    "<if test='createdTo != null'>and createtime&lt;=#{createdTo}</if>",
    "order by id desc limit #{limit} offset #{offset}",
    "</script>"
  })
  List<Map<String, Object>> attachments(
      @Param("keyword") String keyword,
      @Param("category") String category,
      @Param("adminId") Long adminId,
      @Param("userId") Long userId,
      @Param("filename") String filename,
      @Param("sizeMin") Long sizeMin,
      @Param("sizeMax") Long sizeMax,
      @Param("imagewidth") String imagewidth,
      @Param("imageheight") String imageheight,
      @Param("imagetype") String imagetype,
      @Param("storage") String storage,
      @Param("mimetypes") List<String> mimetypes,
      @Param("createdFrom") Long createdFrom,
      @Param("createdTo") Long createdTo,
      @Param("limit") int limit,
      @Param("offset") int offset);

  @Update({
    "<script>update ymcx_attachment set category=#{category} where id in",
    "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>",
    "</script>"
  })
  int classifyAttachments(@Param("ids") List<Long> ids, @Param("category") String category);

  @Update({
    "<script>",
    "update ymcx_attachment",
    "<set>",
    "<if test='values.containsKey(\"category\")'>category=#{values.category},</if>",
    "<if test='values.containsKey(\"url\")'>url=#{values.url},</if>",
    "<if test='values.containsKey(\"imagewidth\")'>imagewidth=#{values.imagewidth},</if>",
    "<if test='values.containsKey(\"imageheight\")'>imageheight=#{values.imageheight},</if>",
    "<if test='values.containsKey(\"imagetype\")'>imagetype=#{values.imagetype},</if>",
    "<if test='values.containsKey(\"imageframes\")'>imageframes=#{values.imageframes},</if>",
    "<if test='values.containsKey(\"filename\")'>filename=#{values.filename},</if>",
    "<if test='values.containsKey(\"filesize\")'>filesize=#{values.filesize},</if>",
    "<if test='values.containsKey(\"mimetype\")'>mimetype=#{values.mimetype},</if>",
    "<if test='values.containsKey(\"extparam\")'>extparam=#{values.extparam},</if>",
    "<if test='values.containsKey(\"uploadtime\")'>uploadtime=#{values.uploadtime},</if>",
    "<if test='values.containsKey(\"storage\")'>storage=#{values.storage},</if>",
    "updatetime=#{now}",
    "</set>",
    "where id=#{id}",
    "</script>"
  })
  int updateAttachment(
      @Param("id") long id,
      @Param("values") Map<String, Object> values,
      @Param("now") long now);

  @Select("select * from ymcx_attachment where id=#{id}")
  Map<String, Object> attachment(@Param("id") long id);

  @Select({
    "<script>select id,url,storage from ymcx_attachment where id in",
    "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>",
    "</script>"
  })
  List<Map<String, Object>> attachmentsByIds(@Param("ids") List<Long> ids);

  @Delete("delete from ymcx_attachment where id=#{id}")
  int deleteAttachment(@Param("id") long id);

  @Delete({
    "<script>delete from ymcx_attachment where id in",
    "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>",
    "</script>"
  })
  int deleteAttachments(@Param("ids") List<Long> ids);

  @Select(
      "select id,username,nickname,avatar,email,mobile,logintime,loginip,status from ymcx_admin where id=#{id}")
  Map<String, Object> adminProfile(@Param("id") long id);

  @Select("select count(*) from ymcx_admin_log where admin_id=#{adminId}")
  int adminLogCount(@Param("adminId") long adminId);

  @Select(
      "select id,title,url,ip,createtime from ymcx_admin_log where admin_id=#{adminId} order by id desc limit #{limit} offset #{offset}")
  List<Map<String, Object>> adminLogs(
      @Param("adminId") long adminId,
      @Param("limit") int limit,
      @Param("offset") int offset);

  @Select("select count(*) from ymcx_admin where email=#{email} and id<>#{id}")
  int otherAdminEmailCount(@Param("email") String email, @Param("id") long id);

  @Update({
    "<script>update ymcx_admin set updatetime=#{now}",
    "<if test='v.containsKey(\"nickname\")'>,nickname=#{v.nickname}</if>",
    "<if test='v.containsKey(\"avatar\")'>,avatar=#{v.avatar}</if>",
    "<if test='v.containsKey(\"email\")'>,email=#{v.email}</if>",
    "<if test='v.containsKey(\"salt\")'>,salt=#{v.salt}</if>",
    "<if test='v.containsKey(\"password\")'>,password=#{v.password}</if>",
    "where id=#{id}</script>"
  })
  int updateAdminProfile(
      @Param("id") long id, @Param("v") Map<String, Object> values, @Param("now") long now);
}
