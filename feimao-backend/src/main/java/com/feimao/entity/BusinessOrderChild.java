package com.feimao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;

/** 租售订单分期账单。 */
@Data
@TableName("ymcx_order_child")
public class BusinessOrderChild implements Serializable {
  private static final long serialVersionUID = 1L;

  @TableId(type = IdType.AUTO)
  private Long id;

  private Long userId;
  private String number;
  private String childNumber;
  private Integer num;
  private BigDecimal price;
  private Integer state;
  private BigDecimal payPrice;
  private String transactionId;
  private Long starttime;
  private Long endtime;
  private Long paytime;
  private Long createtime;
  private Long updatetime;
}
