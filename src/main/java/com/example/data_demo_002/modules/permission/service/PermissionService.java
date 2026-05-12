package com.example.data_demo_002.modules.permission.service;


import com.example.data_demo_002.common.base.domain.SysPermission;
import com.example.data_demo_002.modules.permission.dao.MenuVO;
import com.example.data_demo_002.modules.permission.dao.PermissionDTO;

import java.util.List;

public interface PermissionService {

    List<String> getUserPermissions(Long userId);

    List<MenuVO> getUserMenus(Long userId);

    boolean hasPermission(Long userId, String permissionCode);

    void assignPermissions(Long roleId, List<Long> permissionIds);

    List<Long> getRolePermissions(Long roleId);

    List<SysPermission> listAllPermissions();

    SysPermission getPermissionDetail(Long permissionId);

    void createPermission(PermissionDTO dto);

    void updatePermission(Long permissionId, PermissionDTO dto);

    void deletePermission(Long permissionId);
}
