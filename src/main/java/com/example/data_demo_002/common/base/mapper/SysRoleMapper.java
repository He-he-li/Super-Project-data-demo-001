package com.example.data_demo_002.common.base.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.data_demo_002.common.base.domain.SysRole;
import org.apache.ibatis.annotations.Mapper;

/**
* @author lhh
* @description 针对表【sys_role(系统角色表)】的数据库操作Mapper
* @createDate 2026-03-19 13:30:03
* @Entity generator.domain.SysRole
*/
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

}




