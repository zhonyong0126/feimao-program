package com.feimao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

/** 租售业务会员。 */
@Data
@TableName("ymcx_user")
public class BusinessUser implements Serializable {
  private static final long serialVersionUID = 1L;

  @TableId(type = IdType.AUTO)
  private Long id;

  private Long groupId;
  private String username;
  private String nickname;
  @JsonIgnore private String password;
  private String salt;
  private String email;
  private String mobile;
  private String avatar;
  private Integer level;
  private Integer gender;
  private LocalDate birthday;
  private String bio;
  private BigDecimal money;
  private Integer score;
  private Long jointime;
  private Long createtime;
  private Long updatetime;
  private String token;
  private String status;
  @JsonIgnore private String openid;
  private Integer merchantStatus;
  private Long merchantId;
  private String truename;
  private Integer shopStatus;
  private Long shopId;
}
