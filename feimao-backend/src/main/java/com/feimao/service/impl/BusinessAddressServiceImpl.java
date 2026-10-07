package com.feimao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.feimao.entity.BusinessAddress;
import com.feimao.mapper.BusinessAddressMapper;
import com.feimao.service.BusinessAddressService;
import org.springframework.stereotype.Service;

@Service
public class BusinessAddressServiceImpl extends ServiceImpl<BusinessAddressMapper, BusinessAddress>
    implements BusinessAddressService {}
