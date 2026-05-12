package com.example.data_demo_002.modules.permission.controller;



import com.example.data_demo_002.common.base.domain.SysPermission;
import com.example.data_demo_002.common.result.Result;
import com.example.data_demo_002.common.util.Jwt.UserContext;
import com.example.data_demo_002.common.util.permissionUtil.HasPermission;
import com.example.data_demo_002.modules.permission.dao.MenuVO;
import com.example.data_demo_002.modules.permission.dao.PermissionDTO;
import com.example.data_demo_002.modules.permission.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Tag(name = "🔐 权限管理", description = "提供权限CR操作及当前用户权限查询接口")
@RestController
@RequestMapping("/system/permissions")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class PermissionController {

    private final PermissionService permissionService;

    @Operation(summary = "获取当前用户权限", description = "返回当前登录用户的所有权限编码")
    @GetMapping("/my-permissions")
    public Result<Map<String, Object>> getCurrentUserPermissions() {
        String username = UserContext.getUsername();

        List<String> permissions = permissionService.getUserPermissionsByUsername(username);

        Map<String, Object> result = new HashMap<>();
        result.put("username", username);
        result.put("permissions", permissions);

        return Result.success(result);
    }

    @Operation(summary = "获取当前用户菜单", description = "返回当前登录用户的动态菜单树")
    @GetMapping("/my-menus")
    public Result<List<MenuVO>> getCurrentUserMenus() {
        String username = UserContext.getUsername();
        List<MenuVO> menus = permissionService.getUserMenusByUsername(username);
        return Result.success(menus);
    }

    @Operation(summary = "获取所有权限", description = "获取所有未删除的权限列表")
    @GetMapping("/list")
    @HasPermission("system:permission:view")
    public Result<List<SysPermission>> listAllPermissions() {
        return Result.success(permissionService.listAllPermissions());
    }

    @Operation(summary = "获取权限详情", description = "根据ID获取权限详细信息")
    @GetMapping("/{permissionId}")
    @HasPermission("system:permission:view")
    public Result<SysPermission> getPermissionDetail(@PathVariable Long permissionId) {
        SysPermission permission = permissionService.getPermissionDetail(permissionId);
        return Result.success(permission);
    }

    @Operation(summary = "创建权限", description = "新增菜单或按钮权限")
    @PostMapping
    @HasPermission("system:permission:create")
    public Result<Void> createPermission(@RequestBody @Valid PermissionDTO dto) {
        permissionService.createPermission(dto);
        return Result.success(null, "创建成功");
    }

    @Operation(summary = "修改权限", description = "更新权限信息")
    @PutMapping("/{permissionId}")
    @HasPermission("system:permission:edit")
    public Result<Void> updatePermission(
            @PathVariable Long permissionId,
            @RequestBody @Valid PermissionDTO dto) {
        permissionService.updatePermission(permissionId, dto);
        return Result.success(null, "修改成功");
    }

    @Operation(summary = "删除权限", description = "逻辑删除权限，有子权限或被引用的权限不可删除")
    @DeleteMapping("/{permissionId}")
    @HasPermission("system:permission:delete")
    public Result<Void> deletePermission(@PathVariable Long permissionId) {
        permissionService.deletePermission(permissionId);
        return Result.success(null, "删除成功");
    }
}
