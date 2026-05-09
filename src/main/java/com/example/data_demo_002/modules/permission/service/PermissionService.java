package com.example.data_demo_002.modules.permission.service;



import com.example.data_demo_002.modules.permission.dao.MenuVO;

import java.util.List;

/**
 * 权限服务接口
 */
public interface PermissionService {

    /**
     * 获取用户的权限编码列表
     */
    List<String> getUserPermissions(Long userId);

    /**
     * 获取用户的菜单树
     */
    List<MenuVO> getUserMenus(Long userId);

    /**
     * 检查用户是否有某个权限
     */
    boolean hasPermission(Long userId, String permissionCode);

    /**
     * 给角色分配权限
     */
    void assignPermissions(Long roleId, List<Long> permissionIds);

    /**
     * 获取角色的权限 ID 列表
     */
    List<Long> getRolePermissions(Long roleId);
}
