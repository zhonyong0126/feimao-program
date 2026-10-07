package com.feimao.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 司机实体类
 *
 * @author feimao
 */
@Data
@TableName("fm_driver")
public class Driver implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 密码
     */
    @JsonIgnore
    private String password;

    /**
     * 姓名
     */
    private String name;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 驾驶证号
     */
    private String driverLicense;

    /**
     * 行驶证号
     */
    private String vehicleLicense;

    /**
     * 车牌号
     */
    private String plateNumber;

    /**
     * 车辆类型
     */
    private String vehicleType;

    /**
     * 认证状态 0-未认证 1-审核中 2-已认证 3-认证失败
     */
    private Integer authStatus;

    /**
     * 状态 0-禁用 1-启用
     */
    private Integer status;

    /**
     * 在线状态 0-离线 1-在线空闲 2-运输中
     */
    private Integer onlineStatus;

    /**
     * 余额
     */
    private BigDecimal balance;

    /**
     * 微信openid
     */
    @JsonIgnore
    private String openid;

    /**
     * 逻辑删除 0-未删除 1-已删除
     */
    @TableLogic
    private Integer deleted;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}
