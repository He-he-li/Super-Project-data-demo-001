package com.example.data_demo_002.modules.permission.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.example.data_demo_002.common.base.domain.SysPermission;
import com.example.data_demo_002.common.base.domain.SysRolePermission;
import com.example.data_demo_002.common.base.domain.SysUserRole;
import com.example.data_demo_002.common.base.mapper.SysPermissionMapper;
import com.example.data_demo_002.common.base.mapper.SysRolePermissionMapper;
import com.example.data_demo_002.common.base.mapper.SysUserRoleMapper;
import com.example.data_demo_002.modules.permission.dao.MenuVO;
import com.example.data_demo_002.modules.permission.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final SysPermissionMapper permissionMapper;
    private final SysRolePermissionMapper rolePermissionMapper;
    private final SysUserRoleMapper userRoleMapper;

    @Override
    public List<String> getUserPermissions(Long userId) {
        // 查询用户的所有角色
        List<SysUserRole> userRoles = userRoleMapper.selectList(
            new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId)
        );

        if (userRoles.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> roleIds = userRoles.stream()
            .map(SysUserRole::getRoleId)
            .collect(Collectors.toList());

        // 检查是否是超级管理员（角色 ID=1002）
        boolean isSuperAdmin = roleIds.contains(1002L);

        if (isSuperAdmin) {
            // 超级管理员拥有所有权限
            List<SysPermission> allPermissions = permissionMapper.selectList(
                new LambdaQueryWrapper<SysPermission>()
                    .eq(SysPermission::getStatus, 0)
                    .eq(SysPermission::getDeleted, 0)
            );
            return allPermissions.stream()
                .map(SysPermission::getPermissionCode)
                .collect(Collectors.toList());
        }

        // 查询角色的所有权限
        List<SysRolePermission> rolePermissions = rolePermissionMapper.selectList(
            new LambdaQueryWrapper<SysRolePermission>()
                .in(SysRolePermission::getRoleId, roleIds)
        );

        if (rolePermissions.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> permissionIds = rolePermissions.stream()
            .map(SysRolePermission::getPermissionId)
            .distinct()
            .collect(Collectors.toList());

        // 查询权限详情
        List<SysPermission> permissions = permissionMapper.selectBatchIds(permissionIds);

        return permissions.stream()
            .filter(p -> p.getStatus() == 0 && p.getDeleted() == 0)
            .map(SysPermission::getPermissionCode)
            .collect(Collectors.toList());
    }

    @Override
    public List<MenuVO> getUserMenus(Long userId) {
        List<String> permissions = getUserPermissions(userId);

        // 查询所有启用的目录和菜单
        List<SysPermission> allMenus = permissionMapper.selectList(
            new LambdaQueryWrapper<SysPermission>()
                .in(SysPermission::getMenuType, Arrays.asList(0, 1))
                .eq(SysPermission::getStatus, 0)
                .eq(SysPermission::getDeleted, 0)
                .orderByAsc(SysPermission::getSortOrder)
        );

        // 过滤出有权限的菜单
        List<SysPermission> userMenus = allMenus.stream()
            .filter(menu -> permissions.contains(menu.getPermissionCode()))
            .collect(Collectors.toList());

        // 构建树形结构
        return buildMenuTree(userMenus, 0L);
    }

    @Override
    public boolean hasPermission(Long userId, String permissionCode) {
        List<String> permissions = getUserPermissions(userId);
        return permissions.contains(permissionCode);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        // 1. 删除旧的权限
        rolePermissionMapper.delete(new LambdaQueryWrapper<SysRolePermission>()
            .eq(SysRolePermission::getRoleId, roleId));

        // 2. 添加新的权限
        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (Long permissionId : permissionIds) {
                SysRolePermission relation = new SysRolePermission();
                relation.setRoleId(roleId);
                relation.setPermissionId(permissionId);
                rolePermissionMapper.insert(relation);
            }
        }

        log.info("角色 {} 权限分配完成，共 {} 个权限", roleId, permissionIds != null ? permissionIds.size() : 0);
    }

    @Override
    public List<Long> getRolePermissions(Long roleId) {
        List<SysRolePermission> relations = rolePermissionMapper.selectList(
            new LambdaQueryWrapper<SysRolePermission>()
                .eq(SysRolePermission::getRoleId, roleId)
        );

        return relations.stream()
            .map(SysRolePermission::getPermissionId)
            .collect(Collectors.toList());
    }

    /**
     * 构建菜单树
     */
    private List<MenuVO> buildMenuTree(List<SysPermission> menus, Long parentId) {
        return menus.stream()
            .filter(menu -> menu.getParentId().equals(parentId))
            .map(menu -> {
                MenuVO vo = new MenuVO();
                vo.setId(menu.getId());
                vo.setParentId(menu.getParentId());
                vo.setName(menu.getPermissionName());
                vo.setPath(menu.getPath());
                vo.setComponent(menu.getComponent());
                vo.setIcon(menu.getIcon());
                vo.setMenuType(menu.getMenuType());

                // 递归构建子菜单
                List<MenuVO> children = buildMenuTree(menus, menu.getId());
                vo.setChildren(children);

                return vo;
            })
            .collect(Collectors.toList());
    }
}
