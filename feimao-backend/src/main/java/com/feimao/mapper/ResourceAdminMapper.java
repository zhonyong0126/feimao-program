package com.feimao.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.UpdateProvider;
import com.feimao.mapper.provider.ResourceAdminSqlProvider;

@Mapper
public interface ResourceAdminMapper {
  @InsertProvider(type = ResourceAdminSqlProvider.class, method = "insert")
  @Options(useGeneratedKeys = true, keyProperty = "v.id")
  int insertResource(@Param("resource") String resource, @Param("v") Map<String, Object> values);

  @UpdateProvider(type = ResourceAdminSqlProvider.class, method = "update")
  int updateResource(
      @Param("resource") String resource,
      @Param("id") long id,
      @Param("v") Map<String, Object> values);

  @Select({
    "<script>",
    "select count(*) from",
    "<choose>",
    "<when test='resource == \"banner\"'>ymcx_banner where 1=1</when>",
    "<when test='resource == \"brand\"'>ymcx_brand where is_delete=1 and deletetime is null</when>",
    "<when test='resource == \"platform-car\"'>ymcx_platform_car where is_delete=1 and deletetime is null</when>",
    "<when test='resource == \"maintain\"'>ymcx_maintain where deletetime is null</when>",
    "<when test='resource == \"coupon\"'>ymcx_coupon where 1=1</when>",
    "<when test='resource == \"category\"'>ymcx_category where 1=1</when>",
    "<otherwise>ymcx_malfunction where 1=1</otherwise>",
    "</choose>",
    "<if test='resource == \"category\" and type != null and type != \"\"'>and type=#{type}</if>",
    "<if test='keyword != null and keyword != \"\"'>",
    "<choose>",
    "<when test='resource == \"banner\"'>and image like concat('%',#{keyword},'%')</when>",
    "<when test='resource == \"brand\"'>and name like concat('%',#{keyword},'%')</when>",
    "<when test='resource == \"platform-car\"'>and (name like concat('%',#{keyword},'%') or specification like concat('%',#{keyword},'%') or (select b.name from ymcx_brand b where b.id=ymcx_platform_car.brand_id and b.is_delete=1 and b.deletetime is null limit 1) like concat('%',#{keyword},'%'))</when>",
    "<when test='resource == \"maintain\"'>and (name like concat('%',#{keyword},'%') or introductory like concat('%',#{keyword},'%'))</when>",
    "<when test='resource == \"malfunction\"'>and (`number` like concat('%',#{keyword},'%') or content like concat('%',#{keyword},'%'))</when>",
    "<when test='resource == \"category\"'>and (name like concat('%',#{keyword},'%') or nickname like concat('%',#{keyword},'%') or diyname like concat('%',#{keyword},'%'))</when>",
    "</choose>",
    "</if>",
    "<if test='status != null and (resource == \"banner\" or resource == \"brand\" or resource == \"platform-car\")'>and status=#{status}</if>",
    "<if test='resource == \"platform-car\" and brandId != null and brandId &gt; 0'>and brand_id=#{brandId}</if>",
    "<if test='createdFrom != null and createdFrom &gt; 0'>and createtime &gt;= #{createdFrom}</if>",
    "<if test='createdTo != null and createdTo &gt; 0'>and createtime &lt;= #{createdTo}</if>",
    "</script>"
  })
  int count(@Param("resource") String resource, @Param("keyword") String keyword,
      @Param("type") String type, @Param("status") Integer status,
      @Param("brandId") Long brandId, @Param("createdFrom") Long createdFrom,
      @Param("createdTo") Long createdTo);

  @Select({
    "<script>",
    "select <choose><when test='resource == \"platform-car\"'>ymcx_platform_car.*,(select b.name from ymcx_brand b where b.id=ymcx_platform_car.brand_id and b.is_delete=1 and b.deletetime is null limit 1) brand_name</when><otherwise>*</otherwise></choose> from",
    "<choose>",
    "<when test='resource == \"banner\"'>ymcx_banner where 1=1</when>",
    "<when test='resource == \"brand\"'>ymcx_brand where is_delete=1 and deletetime is null</when>",
    "<when test='resource == \"platform-car\"'>ymcx_platform_car where is_delete=1 and deletetime is null</when>",
    "<when test='resource == \"maintain\"'>ymcx_maintain where deletetime is null</when>",
    "<when test='resource == \"coupon\"'>ymcx_coupon where 1=1</when>",
    "<when test='resource == \"category\"'>ymcx_category where 1=1</when>",
    "<otherwise>ymcx_malfunction where 1=1</otherwise>",
    "</choose>",
    "<if test='resource == \"category\" and type != null and type != \"\"'>and type=#{type}</if>",
    "<if test='keyword != null and keyword != \"\"'>",
    "<choose>",
    "<when test='resource == \"banner\"'>and image like concat('%',#{keyword},'%')</when>",
    "<when test='resource == \"brand\"'>and name like concat('%',#{keyword},'%')</when>",
    "<when test='resource == \"platform-car\"'>and (name like concat('%',#{keyword},'%') or specification like concat('%',#{keyword},'%') or (select b.name from ymcx_brand b where b.id=ymcx_platform_car.brand_id and b.is_delete=1 and b.deletetime is null limit 1) like concat('%',#{keyword},'%'))</when>",
    "<when test='resource == \"maintain\"'>and (name like concat('%',#{keyword},'%') or introductory like concat('%',#{keyword},'%'))</when>",
    "<when test='resource == \"malfunction\"'>and (`number` like concat('%',#{keyword},'%') or content like concat('%',#{keyword},'%'))</when>",
    "<when test='resource == \"category\"'>and (name like concat('%',#{keyword},'%') or nickname like concat('%',#{keyword},'%') or diyname like concat('%',#{keyword},'%'))</when>",
    "</choose>",
    "</if>",
    "<if test='status != null and (resource == \"banner\" or resource == \"brand\" or resource == \"platform-car\")'>and status=#{status}</if>",
    "<if test='resource == \"platform-car\" and brandId != null and brandId &gt; 0'>and brand_id=#{brandId}</if>",
    "<if test='createdFrom != null and createdFrom &gt; 0'>and createtime &gt;= #{createdFrom}</if>",
    "<if test='createdTo != null and createdTo &gt; 0'>and createtime &lt;= #{createdTo}</if>",
    "<choose><when test='resource == \"brand\" or resource == \"category\"'>order by weigh asc,id desc</when><when test='resource == \"banner\" or resource == \"platform-car\"'>order by weigh desc,id desc</when><otherwise>order by id desc</otherwise></choose>",
    "limit #{limit} offset #{offset}",
    "</script>"
  })
  List<Map<String, Object>> selectPage(
      @Param("resource") String resource,
      @Param("keyword") String keyword,
      @Param("type") String type,
      @Param("status") Integer status,
      @Param("brandId") Long brandId,
      @Param("createdFrom") Long createdFrom,
      @Param("createdTo") Long createdTo,
      @Param("limit") int limit,
      @Param("offset") int offset);

  @Select("select id,pid from ymcx_category")
  List<Map<String, Object>> categoryParents();

  @Update(
      "update ymcx_brand set weigh=#{weigh},updatetime=#{now} where id=#{id} and is_delete=1 and deletetime is null")
  int sortBrand(@Param("id") long id, @Param("weigh") int weigh, @Param("now") long now);

  @Update({
    "<script><choose>",
    "<when test='resource == \"brand\"'>update ymcx_brand set is_delete=2,deletetime=#{now},updatetime=#{now} where id=#{id} and is_delete=1 and deletetime is null</when>",
    "<when test='resource == \"platform-car\"'>update ymcx_platform_car set deletetime=#{now},updatetime=#{now} where id=#{id} and is_delete=1 and deletetime is null</when>",
    "<when test='resource == \"banner\"'>delete from ymcx_banner where id=#{id}</when>",
    "<when test='resource == \"maintain\"'>update ymcx_maintain set deletetime=#{now},updatetime=#{now} where id=#{id} and deletetime is null</when>",
    "<when test='resource == \"category\"'>delete from ymcx_category where id=#{id}</when>",
    "<otherwise>delete from ymcx_coupon where id=#{id}</otherwise>",
    "</choose></script>"
  })
  int deleteResource(
      @Param("resource") String resource, @Param("id") long id, @Param("now") long now);

  @Update({
    "<script><choose>",
    "<when test='resource == \"banner\"'>update ymcx_banner set status=#{status},updatetime=#{now} where id in <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach></when>",
    "<when test='resource == \"brand\"'>update ymcx_brand set status=#{status},updatetime=#{now} where id in <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach> and is_delete=1 and deletetime is null</when>",
    "<when test='resource == \"platform-car\"'>update ymcx_platform_car set status=#{status},updatetime=#{now} where id in <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach> and is_delete=1 and deletetime is null</when>",
    "<when test='resource == \"category\"'>update ymcx_category set status=#{status},updatetime=#{now} where id in <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach></when>",
    "<otherwise>update ymcx_coupon set status=#{status},updatetime=#{now} where id in <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach></otherwise>",
    "</choose></script>"
  })
  int updateStatuses(
      @Param("resource") String resource,
      @Param("ids") List<Long> ids,
      @Param("status") Object status,
      @Param("now") long now);

  @Select({
    "<script>select count(*) from <choose>",
    "<when test='resource == \"banner\"'>ymcx_banner where id in</when>",
    "<when test='resource == \"brand\"'>ymcx_brand where is_delete=1 and deletetime is null and id in</when>",
    "<when test='resource == \"platform-car\"'>ymcx_platform_car where is_delete=1 and deletetime is null and id in</when>",
    "<when test='resource == \"category\"'>ymcx_category where id in</when>",
    "<otherwise>ymcx_coupon where id in</otherwise>",
    "</choose><foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach></script>"
  })
  int reorderableCount(@Param("resource") String resource, @Param("ids") List<Long> ids);

  @Select({
    "<script>select id,weigh from <choose>",
    "<when test='resource == \"banner\"'>ymcx_banner where</when>",
    "<when test='resource == \"brand\"'>ymcx_brand where is_delete=1 and deletetime is null and</when>",
    "<when test='resource == \"platform-car\"'>ymcx_platform_car where is_delete=1 and deletetime is null and</when>",
    "<when test='resource == \"category\"'>ymcx_category where</when>",
    "<when test='resource == \"coupon\"'>ymcx_coupon where</when>",
    "<otherwise>ymcx_coupon where 1=0 and</otherwise>",
    "</choose> id in <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>",
    "<if test='resource == \"category\" and pids != null and pids.size() &gt; 0'>",
    "and pid in <foreach collection='pids' item='pid' open='(' separator=',' close=')'>#{pid}</foreach>",
    "</if> order by weigh <choose><when test='ascending'>asc</when><otherwise>desc</otherwise></choose>",
    "</script>"
  })
  List<Map<String, Object>> selectOrderWeights(
      @Param("resource") String resource,
      @Param("ids") List<Long> ids,
      @Param("pids") List<Long> pids,
      @Param("ascending") boolean ascending);

  @Update({
    "<script><choose>",
    "<when test='resource == \"banner\"'>update ymcx_banner set weigh=#{weigh} where id=#{id}</when>",
    "<when test='resource == \"brand\"'>update ymcx_brand set weigh=#{weigh} where id=#{id} and is_delete=1 and deletetime is null</when>",
    "<when test='resource == \"platform-car\"'>update ymcx_platform_car set weigh=#{weigh} where id=#{id} and is_delete=1 and deletetime is null</when>",
    "<when test='resource == \"category\"'>update ymcx_category set weigh=#{weigh} where id=#{id}</when>",
    "<when test='resource == \"coupon\"'>update ymcx_coupon set weigh=#{weigh} where id=#{id}</when>",
    "<otherwise>update ymcx_coupon set weigh=#{weigh} where 1=0 and id=#{id}</otherwise>",
    "</choose></script>"
  })
  int updateOrderWeight(
      @Param("resource") String resource,
      @Param("id") long id,
      @Param("weigh") int weigh);

  @Update({
    "<script><choose>",
    "<when test='resource == \"banner\"'>update ymcx_banner set weigh=#{weigh},updatetime=#{now} where id=#{id}</when>",
    "<when test='resource == \"brand\"'>update ymcx_brand set weigh=#{weigh},updatetime=#{now} where id=#{id} and is_delete=1 and deletetime is null</when>",
    "<when test='resource == \"platform-car\"'>update ymcx_platform_car set weigh=#{weigh},updatetime=#{now} where id=#{id} and is_delete=1 and deletetime is null</when>",
    "<when test='resource == \"category\"'>update ymcx_category set weigh=#{weigh},updatetime=#{now} where id=#{id}</when>",
    "<otherwise>update ymcx_coupon set weigh=#{weigh},updatetime=#{now} where id=#{id}</otherwise>",
    "</choose></script>"
  })
  int updateWeight(
      @Param("resource") String resource,
      @Param("id") long id,
      @Param("weigh") int weigh,
      @Param("now") long now);

  @Select({
    "<script>",
    "select count(*) from",
    "<choose>",
    "<when test='resource == \"brand\"'>ymcx_brand</when>",
    "<when test='resource == \"platform-car\"'>ymcx_platform_car</when>",
    "<otherwise>ymcx_brand</otherwise>",
    "</choose>",
    "where deletetime is not null",
    "</script>"
  })
  int recycledResourceCount(@Param("resource") String resource);

  @Select({
    "<script>",
    "select id,name,deletetime from",
    "<choose>",
    "<when test='resource == \"brand\"'>ymcx_brand</when>",
    "<when test='resource == \"platform-car\"'>ymcx_platform_car</when>",
    "<otherwise>ymcx_brand</otherwise>",
    "</choose>",
    "where deletetime is not null order by id desc limit #{limit} offset #{offset}",
    "</script>"
  })
  List<Map<String, Object>> recycledResources(
      @Param("resource") String resource,
      @Param("limit") int limit,
      @Param("offset") int offset);

  @Update({
    "<script>",
    "update",
    "<choose>",
    "<when test='resource == \"brand\"'>ymcx_brand</when>",
    "<when test='resource == \"platform-car\"'>ymcx_platform_car</when>",
    "<otherwise>ymcx_brand</otherwise>",
    "</choose>",
    "set is_delete=1,deletetime=null,updatetime=#{now} where deletetime is not null",
    "<if test='ids != null and ids.size() &gt; 0'>",
    "and id in <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>",
    "</if>",
    "</script>"
  })
  int restoreRecycledResources(
      @Param("resource") String resource,
      @Param("ids") List<Long> ids,
      @Param("now") long now);

  @Delete({
    "<script>",
    "delete from",
    "<choose>",
    "<when test='resource == \"brand\"'>ymcx_brand</when>",
    "<when test='resource == \"platform-car\"'>ymcx_platform_car</when>",
    "<otherwise>ymcx_brand</otherwise>",
    "</choose>",
    "where deletetime is not null",
    "<if test='ids != null and ids.size() &gt; 0'>",
    "and id in <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>",
    "</if>",
    "</script>"
  })
  int destroyRecycledResources(
      @Param("resource") String resource, @Param("ids") List<Long> ids);

  @Select("select count(*) from ymcx_maintain where deletetime is not null")
  int recycledMaintainCount();
  @Select("select * from ymcx_maintain where deletetime is not null order by id desc limit #{limit} offset #{offset}")
  List<Map<String,Object>> recycledMaintains(@Param("limit") int limit,@Param("offset") int offset);
  @Update("update ymcx_maintain set deletetime=null,updatetime=#{now} where id=#{id} and deletetime is not null")
  int restoreMaintain(@Param("id") long id,@Param("now") long now);
  @Delete("delete from ymcx_maintain where id=#{id} and deletetime is not null")
  int destroyMaintain(@Param("id") long id);
}
