package com.feimao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/** 商户、门店等业务角色的独立登录会话。 */
@Data
@TableName("business_role_session")
public class BusinessRoleSession implements Serializable {
  private static final long serialVersionUID = 1L;

  @TableId(type = IdType.INPUT)
  private String token;
  private Long subjectId;
  private String subjectType;
  private Long createTime;
  private Long expireTime;
}
