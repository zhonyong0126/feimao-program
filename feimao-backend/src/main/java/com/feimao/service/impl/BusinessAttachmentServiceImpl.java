package com.feimao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.feimao.entity.BusinessAttachment;
import com.feimao.mapper.BusinessAttachmentMapper;
import com.feimao.service.BusinessAttachmentService;
import org.springframework.stereotype.Service;

@Service
public class BusinessAttachmentServiceImpl
    extends ServiceImpl<BusinessAttachmentMapper, BusinessAttachment>
    implements BusinessAttachmentService {}
