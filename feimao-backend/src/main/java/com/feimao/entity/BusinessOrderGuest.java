package com.feimao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;

/** 租赁订单同行人及保单信息。 */
@Data
@TableName("ymcx_order_guest")
public class BusinessOrderGuest implements Serializable {
  private static final long serialVersionUID = 1L;

  @TableId(type = IdType.AUTO)
  private Long id;

  private Long userId;
  private Long orderId;
  private String name;
  private String mobile;
  private String idcard;
  private Long createtime;
  private Long updatetime;
  private Integer isDelete;
  private Long deletetime;
  private Integer isInsure;
  private String bizOrderSn;
  private Long sTime;
  private Long eTime;
  private String numId;
  private Long cancelTime;
  private String data;
  private BigDecimal insureMoney;
}
