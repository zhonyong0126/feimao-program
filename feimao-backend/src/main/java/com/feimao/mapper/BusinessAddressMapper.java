package com.feimao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.feimao.entity.BusinessAddress;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface BusinessAddressMapper extends BaseMapper<BusinessAddress> {
  @Select({"<script>", "select count(*) from ymcx_address a where 1=1",
      "<if test='userId != null'>and a.user_id=#{userId}</if>",
      "<if test='name != null and name != \"\"'>and a.name like concat('%',#{name},'%')</if><if test='mobile != null and mobile != \"\"'>and a.mobile like concat('%',#{mobile},'%')</if><if test='administrative != null and administrative != \"\"'>and a.administrative like concat('%',#{administrative},'%')</if><if test='address != null and address != \"\"'>and a.address like concat('%',#{address},'%')</if>",
      "<if test='createdFrom != null'>and a.createtime&gt;=#{createdFrom}</if><if test='createdTo != null'>and a.createtime&lt;=#{createdTo}</if><if test='updatedFrom != null'>and a.updatetime&gt;=#{updatedFrom}</if><if test='updatedTo != null'>and a.updatetime&lt;=#{updatedTo}</if>",
      "</script>"})
  int countAdminRows(@Param("userId") Long userId,@Param("name") String name,@Param("administrative") String administrative,@Param("address") String address,@Param("mobile") String mobile,@Param("createdFrom") Long createdFrom,@Param("createdTo") Long createdTo,@Param("updatedFrom") Long updatedFrom,@Param("updatedTo") Long updatedTo);

  @Select({"<script>", "select a.* from ymcx_address a where 1=1",
      "<if test='userId != null'>and a.user_id=#{userId}</if>",
      "<if test='name != null and name != \"\"'>and a.name like concat('%',#{name},'%')</if><if test='mobile != null and mobile != \"\"'>and a.mobile like concat('%',#{mobile},'%')</if><if test='administrative != null and administrative != \"\"'>and a.administrative like concat('%',#{administrative},'%')</if><if test='address != null and address != \"\"'>and a.address like concat('%',#{address},'%')</if>",
      "<if test='createdFrom != null'>and a.createtime&gt;=#{createdFrom}</if><if test='createdTo != null'>and a.createtime&lt;=#{createdTo}</if><if test='updatedFrom != null'>and a.updatetime&gt;=#{updatedFrom}</if><if test='updatedTo != null'>and a.updatetime&lt;=#{updatedTo}</if>",
      "order by a.id desc limit #{limit} offset #{offset}", "</script>"})
  List<Map<String, Object>> selectAdminRows(@Param("userId") Long userId,@Param("name") String name,@Param("administrative") String administrative,@Param("address") String address,@Param("mobile") String mobile,@Param("createdFrom") Long createdFrom,@Param("createdTo") Long createdTo,@Param("updatedFrom") Long updatedFrom,@Param("updatedTo") Long updatedTo,@Param("limit") int limit,@Param("offset") int offset);

  @Delete({"<script>delete from ymcx_address where id in <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach></script>"})
  int deleteAdminRows(@Param("ids") List<Long> ids);

  @Select({"<script>", "select id value,id,pid,name,shortname,mergename,level,code,lng,lat from ymcx_area where pid=#{pid}",
      "<if test='level &gt; 0'>and level=#{level}</if>",
      "<if test='keyword != null and keyword != \"\"'>and (name like concat('%',#{keyword},'%') or shortname like concat('%',#{keyword},'%') or pinyin like concat('%',#{keyword},'%') or code like concat('%',#{keyword},'%'))</if>",
      "order by id", "</script>"})
  List<Map<String, Object>> selectAreas(@Param("pid") long pid, @Param("level") int level,
      @Param("keyword") String keyword);

  @Select("select id as value,name from ymcx_area where pid=#{pid} and level=#{level}")
  List<Map<String, Object>> selectLegacyAreas(
      @Param("pid") String pid, @Param("level") int level);
}
