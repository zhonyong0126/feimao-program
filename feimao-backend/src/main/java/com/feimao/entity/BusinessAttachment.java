package com.feimao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/** 租售业务上传附件。 */
@Data
@TableName("ymcx_attachment")
public class BusinessAttachment implements Serializable {
  private static final long serialVersionUID = 1L;

  @TableId(type = IdType.AUTO)
  private Long id;
  private String category;
  private Long adminId;
  private Long userId;
  private String url;
  private String imagewidth;
  private String imageheight;
  private String imagetype;
  private Integer imageframes;
  private String filename;
  private Long filesize;
  private String mimetype;
  private String extparam;
  private Long createtime;
  private Long updatetime;
  private Long uploadtime;
  private String storage;
  private String sha1;
}
