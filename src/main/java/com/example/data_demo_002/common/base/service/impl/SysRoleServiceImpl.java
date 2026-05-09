package com.example.data_demo_002.common.base.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.example.data_demo_002.common.base.domain.SysRole;
import com.example.data_demo_002.common.base.mapper.SysRoleMapper;
import com.example.data_demo_002.common.base.service.SysRoleService;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Service;

/**
* @author lhh
* @description 针对表【sys_role(系统角色表)】的数据库操作Service实现
* @createDate 2026-03-19 13:30:03
*/
@Service

public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole>
    implements SysRoleService {


}




