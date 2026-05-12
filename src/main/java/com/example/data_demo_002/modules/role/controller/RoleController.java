package com.example.data_demo_002.modules.role.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.data_demo_002.common.base.domain.SysRole;
import com.example.data_demo_002.common.result.Result;
import com.example.data_demo_002.common.util.permissionUtil.HasPermission;
import com.example.data_demo_002.modules.role.dao.RoleDTO;
import com.example.data_demo_002.modules.role.dao.RoleVO;
import com.example.data_demo_002.modules.role.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "🎭 角色管理", description = "提供角色的 CRUD 操作及权限分配接口")
@RestController
@RequestMapping("/system/roles")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "获取所有角色", description = "获取所有未删除的角色列表")
    @GetMapping("/list-all")
    @HasPermission("system:role:view")
    public Result<List<SysRole>> listAllRoles() {
        return Result.success(roleService.listAllRoles());
    }

    @Operation(summary = "分页获取角色列表", description = "支持按角色名称模糊查询")
    @GetMapping("/list")
    @HasPermission("system:role:view")
    public Result<Page<RoleVO>> listRoles(
            @Parameter(description = "页码", example = "1")
            @RequestParam(defaultValue = "1") @Min(1) int pageNum,
            @Parameter(description = "每页数量", example = "10")
            @RequestParam(defaultValue = "10") @Min(1) int pageSize,
            @Parameter(description = "角色名称（模糊查询）")
            @RequestParam(required = false) String roleName) {

        Page<RoleVO> page = roleService.listRoles(pageNum, pageSize, roleName);
        return Result.success(page);
    }

    @Operation(summary = "获取角色详情", description = "包含角色信息和权限ID列表")
    @GetMapping("/{roleId}")
    @HasPermission("system:role:view")
    public Result<RoleVO> getRoleDetail(@PathVariable Long roleId) {
        RoleVO vo = roleService.getRoleDetail(roleId);
        return Result.success(vo);
    }

    @Operation(summary = "创建角色", description = "新增角色，角色编码需唯一")
    @PostMapping
    @HasPermission("system:role:create")
    public Result<Void> createRole(@RequestBody @Valid RoleDTO dto) {
        roleService.createRole(dto);
        return Result.success(null, "创建成功");
    }

    @Operation(summary = "修改角色", description = "更新角色信息")
    @PutMapping("/{roleId}")
    @HasPermission("system:role:edit")
    public Result<Void> updateRole(
            @PathVariable Long roleId,
            @RequestBody @Valid RoleDTO dto) {
        roleService.updateRole(roleId, dto);
        return Result.success(null, "修改成功");
    }

    @Operation(summary = "删除角色", description = "逻辑删除角色，有用户的角色不可删除")
    @DeleteMapping("/{roleId}")
    @HasPermission("system:role:delete")
    public Result<Void> deleteRole(@PathVariable Long roleId) {
        roleService.deleteRole(roleId);
        return Result.success(null, "删除成功");
    }

    @Operation(summary = "获取角色权限", description = "获取指定角色的权限ID列表")
    @GetMapping("/{roleId}/permissions")
    @HasPermission("system:role:view")
    public Result<List<Long>> getRolePermissions(@PathVariable Long roleId) {
        List<Long> permissionIds = roleService.getRolePermissions(roleId);
        return Result.success(permissionIds);
    }

    @Operation(summary = "分配角色权限", description = "给角色分配权限（全量替换）")
    @PutMapping("/{roleId}/permissions")
    @HasPermission("system:role:assign")
    public Result<Void> assignPermissions(
            @PathVariable Long roleId,
            @RequestBody List<Long> permissionIds) {
        roleService.assignPermissions(roleId, permissionIds);
        return Result.success(null, "权限分配成功");
    }

    @Operation(summary = "获取用户角色", description = "获取指定用户的角色ID列表")
    @GetMapping("/user/{username}")
    @HasPermission("system:user:view")
    public Result<List<Long>> getUserRoles(@PathVariable String username) {
        List<Long> roleIds = roleService.getUserRolesByUsername(username);
        return Result.success(roleIds);
    }

    @Operation(summary = "分配用户角色", description = "给用户分配角色（全量替换）")
    @PutMapping("/user/{username}")
    @HasPermission("system:user:assign")
    public Result<Void> assignUserRoles(
            @PathVariable String username,
            @RequestBody List<Long> roleIds) {
        roleService.assignUserRolesByUsername(username, roleIds);
        return Result.success(null, "角色分配成功");
    }
}