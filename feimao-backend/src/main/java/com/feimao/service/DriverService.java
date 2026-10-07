package com.feimao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.feimao.entity.Driver;

/**
 * 司机服务接口
 *
 * @author feimao
 */
public interface DriverService extends IService<Driver> {

    /**
     * 根据手机号查询司机
     */
    Driver getByMobile(String mobile);

    /**
     * 根据openid查询司机
     */
    Driver getByOpenid(String openid);

}
