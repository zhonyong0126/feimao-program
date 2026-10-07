package com.feimao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.feimao.entity.Order;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单Mapper
 *
 * @author feimao
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

}
