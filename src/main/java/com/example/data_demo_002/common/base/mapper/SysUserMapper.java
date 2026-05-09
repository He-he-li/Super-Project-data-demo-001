package com.example.data_demo_002.common.base.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.data_demo_002.common.base.domain.SysUser;
import com.example.data_demo_002.modules.user.dao.UserVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author lhh
* @description 针对表【sys_user(系统用户表)】的数据库操作Mapper
* @createDate 2026-03-20 09:27:57
* @Entity generator.domain.SysUser
*/
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 分页查询用户列表（含角色信息，使用GROUP_CONCAT避免N+1问题）
     * 适用于 PostgreSQL
     * 
     * @param page 分页对象
     * @return 分页用户列表
     */
    @Select("SELECT " +
            "u.id, u.username, u.email, u.phone, u.status, " +
            "u.create_time as createTime, u.update_time as updateTime, " +
            "STRING_AGG(r.role_name, ',') as roleNamesStr, " +
            "STRING_AGG(CAST(r.id AS TEXT), ',') as roleIdsStr " +
            "FROM sys_user u " +
            "LEFT JOIN sys_user_role ur ON u.id = ur.user_id " +
            "LEFT JOIN sys_role r ON ur.role_id = r.id " +
            "GROUP BY u.id " +
            "ORDER BY u.create_time DESC")
    @Results(id = "UserVOMap", value = {
            @Result(property = "id", column = "id", id = true),
            @Result(property = "username", column = "username"),
            @Result(property = "email", column = "email"),
            @Result(property = "phone", column = "phone"),
            @Result(property = "status", column = "status"),
            @Result(property = "createTime", column = "createTime"),
            @Result(property = "updateTime", column = "updateTime"),
            @Result(property = "roleNamesStr", column = "roleNamesStr"),
            @Result(property = "roleIdsStr", column = "roleIdsStr")
    })
    Page<UserVO> selectUserPageWithRoles(Page<UserVO> page);

}




