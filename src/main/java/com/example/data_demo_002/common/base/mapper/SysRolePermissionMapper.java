package com.example.data_demo_002.common.base.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.data_demo_002.common.base.domain.SysRolePermission;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色权限关联 Mapper
 */
@Mapper
public interface SysRolePermissionMapper extends BaseMapper<SysRolePermission> {

}
