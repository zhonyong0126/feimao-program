package com.feimao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.feimao.entity.Staff;
import com.feimao.mapper.StaffMapper;
import com.feimao.service.StaffService;
import org.springframework.stereotype.Service;

@Service
public class StaffServiceImpl extends ServiceImpl<StaffMapper, Staff> implements StaffService {
}
