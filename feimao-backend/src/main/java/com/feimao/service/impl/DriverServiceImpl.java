package com.feimao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.feimao.entity.Driver;
import com.feimao.mapper.DriverMapper;
import com.feimao.service.DriverService;
import org.springframework.stereotype.Service;

/**
 * 司机服务实现类
 *
 * @author feimao
 */
@Service
public class DriverServiceImpl extends ServiceImpl<DriverMapper, Driver> implements DriverService {

    @Override
    public Driver getByMobile(String mobile) {
        return getOne(new LambdaQueryWrapper<Driver>()
                .eq(Driver::getMobile, mobile)
                .eq(Driver::getDeleted, 0));
    }

    @Override
    public Driver getByOpenid(String openid) {
        return getOne(new LambdaQueryWrapper<Driver>()
                .eq(Driver::getOpenid, openid)
                .eq(Driver::getDeleted, 0));
    }

}
