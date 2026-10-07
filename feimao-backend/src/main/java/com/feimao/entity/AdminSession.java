package com.feimao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/** 后台管理员独立登录会话。 */
@Data
@TableName("feimao_admin_session")
public class AdminSession implements Serializable {
  private static final long serialVersionUID = 1L;

  @TableId(type = IdType.INPUT)
  private String token;
  private Long adminId;
  private Long createTime;
  private Long expireTime;
}
