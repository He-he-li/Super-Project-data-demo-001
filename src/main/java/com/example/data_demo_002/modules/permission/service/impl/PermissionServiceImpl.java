package com.example.data_demo_002.modules.permission.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.example.data_demo_002.common.base.domain.SysPermission;
import com.example.data_demo_002.common.base.domain.SysRolePermission;
import com.example.data_demo_002.common.base.domain.SysUserRole;
import com.example.data_demo_002.common.base.mapper.SysPermissionMapper;
import com.example.data_demo_002.common.base.mapper.SysRolePermissionMapper;
import com.example.data_demo_002.common.base.mapper.SysUserRoleMapper;
import com.example.data_demo_002.common.exception.BusinessException;
import com.example.data_demo_002.modules.permission.dao.MenuVO;
import com.example.data_demo_002.modules.permission.dao.PermissionDTO;
import com.example.data_demo_002.modules.permission.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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

        boolean isSuperAdmin = roleIds.contains(1002L);

        if (isSuperAdmin) {
            List<SysPermission> allPermissions = permissionMapper.selectList(
                new LambdaQueryWrapper<SysPermission>()
                    .eq(SysPermission::getStatus, 0)
                    .eq(SysPermission::getDeleted, 0)
            );
            return allPermissions.stream()
                .map(SysPermission::getPermissionCode)
                .collect(Collectors.toList());
        }

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

        List<SysPermission> permissions = permissionMapper.selectBatchIds(permissionIds);

        return permissions.stream()
            .filter(p -> p.getStatus() == 0 && p.getDeleted() == 0)
            .map(SysPermission::getPermissionCode)
            .collect(Collectors.toList());
    }

    @Override
    public List<MenuVO> getUserMenus(Long userId) {
        List<String> permissions = getUserPermissions(userId);

        List<SysPermission> allMenus = permissionMapper.selectList(
            new LambdaQueryWrapper<SysPermission>()
                .in(SysPermission::getMenuType, Arrays.asList(0, 1))
                .eq(SysPermission::getStatus, 0)
                .eq(SysPermission::getDeleted, 0)
                .orderByAsc(SysPermission::getSortOrder)
        );

        List<SysPermission> userMenus = allMenus.stream()
            .filter(menu -> permissions.contains(menu.getPermissionCode()))
            .collect(Collectors.toList());

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
        rolePermissionMapper.delete(new LambdaQueryWrapper<SysRolePermission>()
            .eq(SysRolePermission::getRoleId, roleId));

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

    @Override
    public List<SysPermission> listAllPermissions() {
        return permissionMapper.selectList(
            new LambdaQueryWrapper<SysPermission>()
                .eq(SysPermission::getDeleted, 0)
                .orderByAsc(SysPermission::getSortOrder)
                .orderByAsc(SysPermission::getId)
        );
    }

    @Override
    public SysPermission getPermissionDetail(Long permissionId) {
        SysPermission permission = permissionMapper.selectById(permissionId);
        if (permission == null || permission.getDeleted() == 1) {
            throw new BusinessException("权限不存在");
        }
        return permission;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createPermission(PermissionDTO dto) {
        long count = permissionMapper.selectCount(
            new LambdaQueryWrapper<SysPermission>()
                .eq(SysPermission::getPermissionCode, dto.getPermissionCode())
                .eq(SysPermission::getDeleted, 0)
        );
        if (count > 0) {
            throw new BusinessException("权限编码已存在");
        }

        if (dto.getParentId() != null && dto.getParentId() > 0) {
            SysPermission parent = permissionMapper.selectById(dto.getParentId());
            if (parent == null || parent.getDeleted() == 1) {
                throw new BusinessException("父级权限不存在");
            }
        }

        SysPermission permission = new SysPermission();
        BeanUtils.copyProperties(dto, permission);
        permission.setDeleted(0);
        permission.setCreateTime(new Date());
        permission.setUpdateTime(new Date());

        if (permission.getSortOrder() == null) {
            permission.setSortOrder(0);
        }
        if (permission.getStatus() == null) {
            permission.setStatus(0);
        }

        boolean success = permissionMapper.insert(permission) > 0;
        if (!success) {
            throw new BusinessException("创建权限失败");
        }

        log.info("创建权限成功: {}", permission.getPermissionCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePermission(Long permissionId, PermissionDTO dto) {
        SysPermission permission = permissionMapper.selectById(permissionId);
        if (permission == null || permission.getDeleted() == 1) {
            throw new BusinessException("权限不存在");
        }

        if (!dto.getPermissionCode().equals(permission.getPermissionCode())) {
            long count = permissionMapper.selectCount(
                new LambdaQueryWrapper<SysPermission>()
                    .eq(SysPermission::getPermissionCode, dto.getPermissionCode())
                    .ne(SysPermission::getId, permissionId)
                    .eq(SysPermission::getDeleted, 0)
            );
            if (count > 0) {
                throw new BusinessException("权限编码已存在");
            }
        }

        if (dto.getParentId() != null && dto.getParentId() > 0) {
            if (dto.getParentId().equals(permissionId)) {
                throw new BusinessException("不能将自己设为父级");
            }
            SysPermission parent = permissionMapper.selectById(dto.getParentId());
            if (parent == null || parent.getDeleted() == 1) {
                throw new BusinessException("父级权限不存在");
            }
        }

        BeanUtils.copyProperties(dto, permission);
        permission.setId(permissionId);
        permission.setUpdateTime(new Date());

        boolean success = permissionMapper.updateById(permission) > 0;
        if (!success) {
            throw new BusinessException("更新权限失败");
        }

        log.info("更新权限成功: {}", permission.getPermissionCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePermission(Long permissionId) {
        SysPermission permission = permissionMapper.selectById(permissionId);
        if (permission == null || permission.getDeleted() == 1) {
            throw new BusinessException("权限不存在");
        }

        long childCount = permissionMapper.selectCount(
            new LambdaQueryWrapper<SysPermission>()
                .eq(SysPermission::getParentId, permissionId)
                .eq(SysPermission::getDeleted, 0)
        );
        if (childCount > 0) {
            throw new BusinessException("该权限下还有子权限，无法删除");
        }

        long roleCount = rolePermissionMapper.selectCount(
            new LambdaQueryWrapper<SysRolePermission>()
                .eq(SysRolePermission::getPermissionId, permissionId)
        );
        if (roleCount > 0) {
            throw new BusinessException("该权限已被角色引用，无法删除");
        }

        permission.setDeleted(1);
        permission.setUpdateTime(new Date());
        
        boolean success = permissionMapper.updateById(permission) > 0;
        if (!success) {
            throw new BusinessException("删除权限失败");
        }

        log.info("删除权限成功: {}", permission.getPermissionCode());
    }

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

                List<MenuVO> children = buildMenuTree(menus, menu.getId());
                vo.setChildren(children);

                return vo;
            })
            .collect(Collectors.toList());
    }
}
