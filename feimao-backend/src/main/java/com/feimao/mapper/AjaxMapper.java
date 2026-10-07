package com.feimao.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AjaxMapper {
  @Select({
    "<script>",
    "select id as value,name from ymcx_category where status='normal'",
    "<if test='pid != null'>and pid=#{pid}</if>",
    "<if test='type != null'>and type=#{type}</if>",
    "order by weigh desc,id desc",
    "</script>"
  })
  List<Map<String, Object>> selectCategories(
      @Param("type") String type, @Param("pid") String pid);
}
