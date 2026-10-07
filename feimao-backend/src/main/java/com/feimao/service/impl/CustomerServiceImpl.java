package com.feimao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.feimao.entity.Customer;
import com.feimao.mapper.CustomerMapper;
import com.feimao.service.CustomerService;
import org.springframework.stereotype.Service;

@Service
public class CustomerServiceImpl extends ServiceImpl<CustomerMapper, Customer> implements CustomerService {
}
