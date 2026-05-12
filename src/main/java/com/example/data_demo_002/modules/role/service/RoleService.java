package com.example.data_demo_002.modules.role.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.data_demo_002.common.base.domain.SysRole;
import com.example.data_demo_002.modules.role.dao.RoleDTO;
import com.example.data_demo_002.modules.role.dao.RoleVO;

import java.util.List;

public interface RoleService {

    List<SysRole> listAllRoles();

    Page<RoleVO> listRoles(int pageNum, int pageSize, String roleName);

    RoleVO getRoleDetail(Long roleId);

    void createRole(RoleDTO dto);

    void updateRole(Long roleId, RoleDTO dto);

    void deleteRole(Long roleId);

    List<Long> getRolePermissions(Long roleId);

    void assignPermissions(Long roleId, List<Long> permissionIds);

    List<Long> getUserRoles(Long userId);

    void assignUserRoles(Long userId, List<Long> roleIds);

    List<Long> getUserRolesByUsername(String username);

    void assignUserRolesByUsername(String username, List<Long> roleIds);
}