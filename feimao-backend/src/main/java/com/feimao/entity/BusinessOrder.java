package com.feimao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;

/** 租车、购车和维修业务订单。 */
@Data
@TableName("ymcx_order")
public class BusinessOrder implements Serializable {
  private static final long serialVersionUID = 1L;

  @TableId(type = IdType.AUTO)
  private Long id;

  private Long userId;
  private Long merchantId;
  private String number;
  private Integer type;
  private String orderName;
  private String serviceName;
  private String serviceImage;
  private Long merchantCarId;
  private Long merchantServiceId;
  private BigDecimal price;
  private BigDecimal payPrice;
  private String transactionId;
  private Long paytime;
  private Integer state;
  private Integer cashState;
  private Integer pickState;
  private Long createtime;
  private Long updatetime;
  private Integer isDelete;
  private Long cancelTime;
  private Long cancelId;
  private BigDecimal cancelRefundPrice;
  private String cancelRefundNumber;
  private BigDecimal lastAllMoney;
}
