package com.feimao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/** 租售业务会员登录令牌。 */
@Data
@TableName("ymcx_user_token")
public class BusinessUserToken implements Serializable {
  private static final long serialVersionUID = 1L;

  @TableId(type = IdType.INPUT)
  private String token;

  private Long userId;
  private Long createtime;
  private Long expiretime;
}
