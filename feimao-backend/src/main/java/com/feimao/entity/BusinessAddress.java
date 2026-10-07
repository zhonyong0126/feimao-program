package com.feimao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;

@Data
@TableName("ymcx_address")
public class BusinessAddress implements Serializable {
  private static final long serialVersionUID = 1L;
  @TableId(type = IdType.AUTO)
  private Long id;
  private Long userId;
  private String name;
  private String administrative;
  private String address;
  private String mobile;
  private Long createtime;
  private Long updatetime;
  private BigDecimal lng;
  private BigDecimal lat;
}
