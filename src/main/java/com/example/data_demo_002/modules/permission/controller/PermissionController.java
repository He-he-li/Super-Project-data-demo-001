package com.example.data_demo_002.modules.permission.controller;



import com.example.data_demo_002.common.result.Result;
import com.example.data_demo_002.common.util.Jwt.UserContext;
import com.example.data_demo_002.modules.permission.dao.MenuVO;
import com.example.data_demo_002.modules.permission.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Tag(name = "🔐 权限管理", description = "提供用户权限、菜单查询等接口")
@RestController
@RequestMapping("/system")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class PermissionController {

    private final PermissionService permissionService;

    @Operation(summary = "获取当前用户权限", description = "返回当前登录用户的所有权限编码")
    @GetMapping("/permissions")
    public Result<Map<String, Object>> getCurrentUserPermissions() {
        Long userId = UserContext.getUserId();

        List<String> permissions = permissionService.getUserPermissions(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("permissions", permissions);

        return Result.success(result);
    }

    @Operation(summary = "获取当前用户菜单", description = "返回当前登录用户的动态菜单树")
    @GetMapping("/menus")
    public Result<List<MenuVO>> getCurrentUserMenus() {
        Long userId = UserContext.getUserId();
        List<MenuVO> menus = permissionService.getUserMenus(userId);
        return Result.success(menus);
    }

    @Operation(summary = "获取角色权限", description = "获取指定角色的权限 ID 列表")
    @GetMapping("/role/{roleId}/permissions")
    public Result<List<Long>> getRolePermissions(@PathVariable Long roleId) {
        List<Long> permissionIds = permissionService.getRolePermissions(roleId);
        return Result.success(permissionIds);
    }

    @Operation(summary = "分配角色权限", description = "给角色分配权限")
    @PutMapping("/role/{roleId}/permissions")
    public Result<Void> assignRolePermissions(
            @PathVariable Long roleId,
            @RequestBody List<Long> permissionIds) {
        permissionService.assignPermissions(roleId, permissionIds);
        return Result.success(null, "权限分配成功");
    }
}
