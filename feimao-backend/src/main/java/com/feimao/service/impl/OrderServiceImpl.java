package com.feimao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.feimao.entity.Order;
import com.feimao.mapper.OrderMapper;
import com.feimao.service.OrderService;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {
}
