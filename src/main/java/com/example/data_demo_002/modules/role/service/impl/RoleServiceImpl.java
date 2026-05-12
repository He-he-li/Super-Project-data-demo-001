package com.example.data_demo_002.modules.role.service.impl;

import com.example.data_demo_002.common.base.domain.SysUser;
import com.example.data_demo_002.common.base.service.SysUserService;
import com.example.data_demo_002.common.util.Jwt.UserContext;



import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.data_demo_002.common.base.domain.SysRole;
import com.example.data_demo_002.common.base.domain.SysRolePermission;
import com.example.data_demo_002.common.base.domain.SysUserRole;
import com.example.data_demo_002.common.base.mapper.SysRoleMapper;
import com.example.data_demo_002.common.base.mapper.SysRolePermissionMapper;
import com.example.data_demo_002.common.base.mapper.SysUserRoleMapper;
import com.example.data_demo_002.common.base.service.SysRoleService;
import com.example.data_demo_002.common.constant.RoleConstants;
import com.example.data_demo_002.common.exception.BusinessException;
import com.example.data_demo_002.modules.role.dao.RoleDTO;
import com.example.data_demo_002.modules.role.dao.RoleVO;
import com.example.data_demo_002.modules.role.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final SysUserService sysUserService;
    private final SysRoleService sysRoleService;
    private final SysRoleMapper sysRoleMapper;
    private final SysRolePermissionMapper rolePermissionMapper;
    private final SysUserRoleMapper userRoleMapper;

    @Override
    public List<SysRole> listAllRoles() {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getDeleted, 0);
        
        // 按单位过滤（包含全局角色）
        Long currentOrgId = UserContext.getOrganizationId();
        if (currentOrgId != null) {
            wrapper.and(w -> w.eq(SysRole::getOrganizationId, currentOrgId)
                              .or()
                              .isNull(SysRole::getOrganizationId));
        }
        
        wrapper.orderByAsc(SysRole::getId);
        
        return sysRoleService.list(wrapper);
    }

    @Override
    public Page<RoleVO> listRoles(int pageNum, int pageSize, String roleName) {
        Page<SysRole> rolePage = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getDeleted, 0);

        if (roleName != null && !roleName.trim().isEmpty()) {
            wrapper.like(SysRole::getRoleName, roleName);
        }

        // 按单位过滤（包含全局角色）
        Long currentOrgId = UserContext.getOrganizationId();
        if (currentOrgId != null) {
            wrapper.and(w -> w.eq(SysRole::getOrganizationId, currentOrgId)
                              .or()
                              .isNull(SysRole::getOrganizationId));
        }

        wrapper.orderByAsc(SysRole::getId);

        sysRoleService.page(rolePage, wrapper);

        Page<RoleVO> resultPage = new Page<>(pageNum, pageSize);
        resultPage.setTotal(rolePage.getTotal());
        resultPage.setCurrent(rolePage.getCurrent());
        resultPage.setSize(rolePage.getSize());
        resultPage.setPages(rolePage.getPages());

        List<SysRole> roles = rolePage.getRecords();
        if (roles.isEmpty()) {
            resultPage.setRecords(new ArrayList<>());
            return resultPage;
        }

        List<Long> roleIds = roles.stream().map(SysRole::getId).collect(Collectors.toList());

        List<SysRolePermission> allRelations = rolePermissionMapper.selectList(
                new LambdaQueryWrapper<SysRolePermission>().in(SysRolePermission::getRoleId, roleIds)
        );

        java.util.Map<Long, List<Long>> rolePermissionMap = allRelations.stream()
                .collect(Collectors.groupingBy(
                        SysRolePermission::getRoleId,
                        Collectors.mapping(SysRolePermission::getPermissionId, Collectors.toList())
                ));

        List<RoleVO> voList = roles.stream().map(role -> {
            RoleVO vo = new RoleVO();
            vo.setId(role.getId());
            vo.setRoleName(role.getRoleName());
            vo.setRoleCode(role.getRoleCode());
            vo.setDescription(role.getDescription());
            vo.setCreateTime(role.getCreateTime());
            vo.setUpdateTime(role.getUpdateTime());

            List<Long> permissionIds = rolePermissionMap.getOrDefault(role.getId(), new ArrayList<>());
            vo.setPermissionIds(permissionIds);

            return vo;
        }).collect(Collectors.toList());

        resultPage.setRecords(voList);
        return resultPage;
    }

    @Override
    public RoleVO getRoleDetail(Long roleId) {
        SysRole role = sysRoleService.getById(roleId);
        if (role == null || role.getDeleted() == 1) {
            throw new BusinessException("角色不存在");
        }

        RoleVO vo = new RoleVO();
        vo.setId(role.getId());
        vo.setRoleName(role.getRoleName());
        vo.setRoleCode(role.getRoleCode());
        vo.setDescription(role.getDescription());
        vo.setCreateTime(role.getCreateTime());
        vo.setUpdateTime(role.getUpdateTime());

        List<Long> permissionIds = getRolePermissions(roleId);
        vo.setPermissionIds(permissionIds);

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createRole(RoleDTO dto) {
        long count = sysRoleService.count(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, dto.getRoleCode())
                .eq(SysRole::getDeleted, 0));
        if (count > 0) {
            throw new BusinessException("角色编码已存在");
        }

        SysRole role = new SysRole();
        role.setRoleName(dto.getRoleName());
        role.setRoleCode(dto.getRoleCode());
        role.setDescription(dto.getDescription());
        role.setDeleted(0);
        role.setCreateTime(new Date());
        role.setUpdateTime(new Date());

        boolean success = sysRoleService.save(role);
        if (!success) {
            throw new BusinessException("创建角色失败");
        }

        log.info("创建角色成功: {}", role.getRoleCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(Long roleId, RoleDTO dto) {
        SysRole role = sysRoleService.getById(roleId);
        if (role == null || role.getDeleted() == 1) {
            throw new BusinessException("角色不存在");
        }

        if (!dto.getRoleCode().equals(role.getRoleCode())) {
            long count = sysRoleService.count(new LambdaQueryWrapper<SysRole>()
                    .eq(SysRole::getRoleCode, dto.getRoleCode())
                    .ne(SysRole::getId, roleId)
                    .eq(SysRole::getDeleted, 0));
            if (count > 0) {
                throw new BusinessException("角色编码已存在");
            }
        }

        role.setRoleName(dto.getRoleName());
        role.setRoleCode(dto.getRoleCode());
        role.setDescription(dto.getDescription());
        role.setUpdateTime(new Date());

        boolean success = sysRoleService.updateById(role);
        if (!success) {
            throw new BusinessException("更新角色失败");
        }

        log.info("更新角色成功: {}", role.getRoleCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long roleId) {
        SysRole role = sysRoleService.getById(roleId);
        if (role == null || role.getDeleted() == 1) {
            throw new BusinessException("角色不存在");
        }

        if (roleId.equals(RoleConstants.SUPER_ADMIN_ROLE_ID)) {
            throw new BusinessException("超级管理员角色不允许删除");
        }

        long userCount = userRoleMapper.selectCount(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getRoleId, roleId));
        if (userCount > 0) {
            throw new BusinessException("该角色下还有用户，无法删除");
        }

        rolePermissionMapper.delete(new LambdaQueryWrapper<SysRolePermission>()
                .eq(SysRolePermission::getRoleId, roleId));

        role.setDeleted(1);
        role.setUpdateTime(new Date());
        sysRoleService.updateById(role);

        log.info("删除角色成功: {}", role.getRoleCode());
    }

    @Override
    public List<Long> getRolePermissions(Long roleId) {
        List<SysRolePermission> relations = rolePermissionMapper.selectList(
                new LambdaQueryWrapper<SysRolePermission>()
                        .eq(SysRolePermission::getRoleId, roleId));

        return relations.stream()
                .map(SysRolePermission::getPermissionId)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        SysRole role = sysRoleService.getById(roleId);
        if (role == null || role.getDeleted() == 1) {
            throw new BusinessException("角色不存在");
        }

        rolePermissionMapper.delete(new LambdaQueryWrapper<SysRolePermission>()
                .eq(SysRolePermission::getRoleId, roleId));

        if (permissionIds != null && !permissionIds.isEmpty()) {
            List<SysRolePermission> relations = permissionIds.stream()
                    .map(permissionId -> {
                        SysRolePermission relation = new SysRolePermission();
                        relation.setRoleId(roleId);
                        relation.setPermissionId(permissionId);
                        return relation;
                    })
                    .collect(Collectors.toList());
            
            relations.forEach(rolePermissionMapper::insert);
        }

        log.info("角色 {} 权限分配完成，共 {} 个权限", roleId,
                permissionIds != null ? permissionIds.size() : 0);
    }

    @Override
    public List<Long> getUserRoles(Long userId) {
        List<SysUserRole> relations = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userId));

        return relations.stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignUserRoles(Long userId, List<Long> roleIds) {
        if (roleIds != null && !roleIds.isEmpty()) {
            List<SysRole> roles = sysRoleService.listByIds(roleIds);
            
            if (roles.size() != roleIds.size()) {
                throw new BusinessException("部分角色不存在");
            }
            
            long deletedCount = roles.stream().filter(r -> r.getDeleted() == 1).count();
            if (deletedCount > 0) {
                throw new BusinessException("存在已删除的角色");
            }
        }

        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId));

        if (roleIds != null && !roleIds.isEmpty()) {
            List<SysUserRole> relations = roleIds.stream()
                    .map(roleId -> {
                        SysUserRole relation = new SysUserRole();
                        relation.setUserId(userId);
                        relation.setRoleId(roleId);
                        return relation;
                    })
                    .collect(Collectors.toList());
            
            relations.forEach(userRoleMapper::insert);
        }

        log.info("用户 {} 角色分配完成，共 {} 个角色", userId,
                roleIds != null ? roleIds.size() : 0);
    }

    @Override
    public List<Long> getUserRolesByUsername(String username) {
        SysUser user = sysUserService.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return getUserRoles(user.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignUserRolesByUsername(String username, List<Long> roleIds) {
        SysUser user = sysUserService.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        assignUserRoles(user.getId(), roleIds);
    }
}