package com.feimao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.feimao.entity.AdminSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AdminSessionMapper extends BaseMapper<AdminSession> {
  @Select(
      "select s.admin_id from feimao_admin_session s join ymcx_admin a on a.id=s.admin_id"
          + " where s.token=#{token} and s.expire_time>#{now} and a.status='normal'")
  Long selectActiveAdminId(@Param("token") String token, @Param("now") long now);
}
