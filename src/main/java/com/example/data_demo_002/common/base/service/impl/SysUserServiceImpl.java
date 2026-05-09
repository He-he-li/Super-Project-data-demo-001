package com.example.data_demo_002.common.base.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.example.data_demo_002.common.base.domain.SysUser;
import com.example.data_demo_002.common.base.mapper.SysUserMapper;
import com.example.data_demo_002.common.base.service.SysUserService;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Service;

/**
* @author lhh
* @description 针对表【sys_user(系统用户表)】的数据库操作Service实现
* @createDate 2026-03-20 09:27:57
*/
@Service

public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser>
    implements SysUserService {

}




