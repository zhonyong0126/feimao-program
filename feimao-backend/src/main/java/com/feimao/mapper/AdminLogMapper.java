package com.feimao.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AdminLogMapper {
  @Insert("insert into ymcx_admin_log(admin_id,username,url,title,content,ip,useragent,createtime) select id,username,#{url},#{title},#{content},'','',#{now} from ymcx_admin where id=#{adminId}")
  int insertLog(@Param("adminId") long adminId, @Param("url") String url,
      @Param("title") String title, @Param("content") String content, @Param("now") long now);
}
