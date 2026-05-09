package com.example.data_demo_002.common.base.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.example.data_demo_002.common.base.domain.SysUserRole;
import com.example.data_demo_002.common.base.mapper.SysUserRoleMapper;
import com.example.data_demo_002.common.base.service.SysUserRoleService;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Service;

/**
* @author lhh
* @description 针对表【sys_user_role(用户与角色关联表)】的数据库操作Service实现
* @createDate 2026-03-20 09:28:16
*/
@Service

public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole>
    implements SysUserRoleService {

}




