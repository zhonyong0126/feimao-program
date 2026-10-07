package com.feimao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.feimao.entity.BusinessUser;
import com.feimao.mapper.BusinessUserMapper;
import com.feimao.service.BusinessUserService;
import org.springframework.stereotype.Service;

@Service
public class BusinessUserServiceImpl extends ServiceImpl<BusinessUserMapper, BusinessUser>
    implements BusinessUserService {}
