package com.feimao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类
 *
 * @author feimao
 */
@Data
@TableName("fm_orders")
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 客户ID
     */
    private Long customerId;

    /**
     * 司机ID
     */
    private Long driverId;

    /**
     * 员工ID（派单员工）
     */
    private Long staffId;

    /**
     * 起始地址
     */
    private String startAddress;

    /**
     * 起始经度
     */
    private BigDecimal startLng;

    /**
     * 起始纬度
     */
    private BigDecimal startLat;

    /**
     * 目的地址
     */
    private String endAddress;

    /**
     * 目的经度
     */
    private BigDecimal endLng;

    /**
     * 目的纬度
     */
    private BigDecimal endLat;

    /**
     * 货物名称
     */
    private String goodsName;

    /**
     * 货物重量（吨）
     */
    private BigDecimal goodsWeight;

    /**
     * 货物体积（方）
     */
    private BigDecimal goodsVolume;

    /**
     * 运费
     */
    private BigDecimal freight;

    /**
     * 实际运费
     */
    private BigDecimal actualFreight;

    /**
     * 距离（公里）
     */
    private BigDecimal distance;

    /**
     * 订单状态 0-待派单 1-待接单 2-已接单 3-运输中 4-已完成 5-已取消
     */
    private Integer status;

    /**
     * 支付状态 0-未支付 1-已支付
     */
    private Integer payStatus;

    /**
     * 备注
     */
    private String remark;

    /**
     * 接单时间
     */
    private LocalDateTime acceptTime;

    /**
     * 开始运输时间
     */
    private LocalDateTime startTime;

    /**
     * 到达时间
     */
    private LocalDateTime arriveTime;

    /**
     * 完成时间
     */
    private LocalDateTime completeTime;

    /**
     * 逻辑删除
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
