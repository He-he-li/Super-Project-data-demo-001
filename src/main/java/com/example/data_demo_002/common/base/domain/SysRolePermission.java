package com.example.data_demo_002.common.base.domain;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 角色权限关联表
 */
@TableName("sys_role_permission")
@Data
public class SysRolePermission {
    /**
     * 角色 ID
     */
    private Long roleId;

    /**
     * 权限 ID
     */
    private Long permissionId;
}
