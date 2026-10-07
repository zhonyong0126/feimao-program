package com.feimao.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 员工实体类（调度员）
 *
 * @author feimao
 */
@Data
@TableName("fm_staff")
public class Staff implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 工号
     */
    private String staffNo;

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
     * 角色 1-调度员 2-管理员
     */
    private Integer role;

    /**
     * 状态 0-禁用 1-启用
     */
    private Integer status;

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
