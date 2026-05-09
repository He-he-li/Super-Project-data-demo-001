package com.example.data_demo_002.common.base.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.data_demo_002.common.base.domain.SysUserRole;
import org.apache.ibatis.annotations.Mapper;

/**
* @author lhh
* @description 针对表【sys_user_role(用户与角色关联表)】的数据库操作Mapper
* @createDate 2026-03-20 09:28:16
* @Entity generator.domain.SysUserRole
*/
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

}




