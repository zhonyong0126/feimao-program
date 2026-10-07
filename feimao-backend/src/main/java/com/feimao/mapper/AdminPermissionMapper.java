package com.feimao.mapper;

import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AdminPermissionMapper {
  @Select(
      "select g.rules from ymcx_auth_group_access ga"
          + " join ymcx_auth_group g on g.id=ga.group_id"
          + " where ga.uid=#{adminId} and g.status='normal'")
  List<String> selectEnabledGroupRules(@Param("adminId") long adminId);

  @Select({
    "<script>",
    "select count(*) from ymcx_auth_rule",
    "where status='normal' and name=#{ruleName} and id in",
    "<foreach collection='ruleIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>",
    "</script>"
  })
  int countAllowedRule(
      @Param("ruleName") String ruleName, @Param("ruleIds") Collection<Long> ruleIds);
}
