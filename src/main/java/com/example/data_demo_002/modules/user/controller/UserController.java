package com.example.data_demo_002.modules.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.data_demo_002.common.result.Result;
import com.example.data_demo_002.common.util.permissionUtil.HasPermission;
import com.example.data_demo_002.common.util.Jwt.UserContext;
import com.example.data_demo_002.modules.user.dao.*;
import com.example.data_demo_002.modules.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ==================== 【认证中心】 Authentication (AUTH) ====================
    
    /**
     * [AUTH-001] 用户注册
     * 功能：创建新用户并分配默认角色（普通用户）
     * 权限：无需认证
     * 入参：UserDTO(username, password, email, phone)
     * 返回：UserDTO(包含userId)
     * 影响：插入sys_user表，插入sys_user_role表（默认角色1001）
     */
    @Tag(name = "🔐 认证中心")
    @Operation(summary = "用户注册", description = "创建新用户并分配默认角色（普通用户）")
    @PostMapping("/register")
    public Result<UserDTO> register(@Valid @RequestBody UserDTO dto) {
        UserDTO result = userService.createUser(dto);
        return Result.success(result);
    }

    /**
     * [AUTH-002] 用户登录
     * 功能：验证用户名密码，生成双Token（Access+Refresh）
     * 权限：无需认证
     * 入参：UserDTO(username, password)
     * 返回：UserLoginVO(用户信息+Token+角色列表)
     * 影响：Redis存储Refresh Token
     */
    @Tag(name = "🔐 认证中心")
    @Operation(summary = "用户登录", description = "验证用户名密码，返回用户信息和双Token（Access+Refresh）")
    @PostMapping("/login")
    public Result<UserLoginVO> login(@Valid @RequestBody LoginDTO dto) {
        System.out.println("UserController.login:"+dto);
        UserLoginVO response = userService.login(dto.getUsername(), dto.getPassword());
        return Result.success(response);
    }

    // ==================== 【个人中心】 Personal Center (PC) ====================
    
    /**
     * [PC-001] 获取当前用户信息
     * 功能：从Token中获取username，查询当前登录用户的详细信息
     * 权限：system:user:view
     * 入参：无（从Token获取username）
     * 返回：UserVO(含角色列表，不含内部ID)
     * 影响：无
     */
    @Tag(name = "👤 个人中心")
    @Operation(summary = "获取当前用户信息", description = "从Token中获取username，查询当前登录用户的详细信息")
    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @HasPermission("system:user:view")
    public Result<UserVO> getMe() {
        UserVO vo = userService.getUserDetail(UserContext.getUsername());
        return Result.success(vo);
    }

    /**
     * [PC-002] 修改当前用户信息
     * 功能：修改当前登录用户的基本信息（用户名、邮箱、手机）
     * 权限：system:user:edit
     * 入参：UserDTO(username, email, phone)
     * 返回：UserVO(更新后的用户信息，不含内部ID)
     * 影响：更新sys_user表的username/email/phone/update_time
     */
    @Tag(name = "👤 个人中心")
    @Operation(summary = "修改当前用户信息", description = "修改当前登录用户的基本信息（用户名、邮箱、手机）")
    @PutMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @HasPermission("system:user:edit")
    public Result<UserVO> updateMe(@Valid @RequestBody UserDTO dto) {
        UserVO vo = userService.updateUserMe(UserContext.getUsername(), dto);
        return Result.success(vo, "修改成功");
    }

    /**
     * [PC-003] 修改当前用户密码
     * 功能：修改当前登录用户的密码，需验证旧密码
     * 权限：system:user:edit
     * 入参：oldPassword, newPassword
     * 返回：Result<Void>
     * 影响：更新sys_user表的password，删除所有Refresh Token
     */
    @Tag(name = "👤 个人中心")
    @Operation(summary = "修改当前用户密码", description = "修改当前登录用户的密码，需验证旧密码")
    @PutMapping("/me/password")
    @SecurityRequirement(name = "bearerAuth")
    @HasPermission("system:user:edit")
    public Result<Void> changeMyPassword(
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        userService.updatePassword(UserContext.getUsername(), oldPassword, newPassword);
        return Result.success(null, "密码修改成功，请重新登录");
    }

    // ==================== 【用户管理】 User Management (UM) ====================
    
    /**
     * [UM-001] 分页获取用户列表
     * 功能：分页查询用户，支持多条件筛选
     * 权限：system:user:list
     * 入参：pageNum, pageSize, username(可选), email(可选), phone(可选), status(可选)
     * 返回：Page<UserVO>(含角色列表)
     * 影响：无
     */
    @Tag(name = "👥 用户管理")
    @Operation(summary = "分页获取用户列表", description = "支持按用户名、邮箱、手机、状态多条件筛选")
    @GetMapping("/list")
    @SecurityRequirement(name = "bearerAuth")
    @HasPermission("system:user:list")
    public Result<Page<UserVO>> listUsers(
            @RequestParam(defaultValue = "1") @Min(1) int pageNum,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Integer status) {
        
        Page<UserVO> page = userService.listUsers(pageNum, pageSize, username, email, phone, status);
        return Result.success(page);
    }

    /**
     * [UM-002] 获取用户详情
     * 功能：根据用户名查询详细信息，包含角色列表
     * 权限：system:user:view
     * 入参：username(路径参数)
     * 返回：UserVO(含角色列表，不含内部ID)
     * 影响：无
     */
    @Tag(name = "👥 用户管理")
    @Operation(summary = "获取用户详情", description = "根据用户名查询详细信息，包含角色列表")
    @GetMapping("/{username}")
    @SecurityRequirement(name = "bearerAuth")
    @HasPermission("system:user:view")
    public Result<UserVO> getUserDetail(@PathVariable String username) {
        UserVO vo = userService.getUserDetail(username);
        return Result.success(vo);
    }

    /**
     * [UM-003] 修改用户信息
     * 功能：管理员修改用户的基本信息和状态
     * 权限：system:user:edit
     * 入参：username(路径参数), UserDTO(username, email, phone, status)
     * 返回：UserVO(更新后的用户信息，不含内部ID)
     * 影响：更新sys_user表的username/email/phone/status/update_time
     */
    @Tag(name = "👥 用户管理")
    @Operation(summary = "修改用户信息", description = "管理员修改用户的基本信息和状态")
    @PutMapping("/{username}")
    @SecurityRequirement(name = "bearerAuth")
    @HasPermission("system:user:edit")
    public Result<UserVO> updateUser(@PathVariable String username, @Valid @RequestBody UserDTO dto) {
        UserVO vo = userService.updateUser(username, dto);
        return Result.success(vo, "修改成功");
    }

    /**
     * [UM-004] 删除用户
     * 功能：物理删除用户，同时删除角色关联和Token
     * 权限：system:user:delete
     * 入参：username(路径参数)
     * 返回：Result<Void>
     * 影响：删除sys_user表记录，删除sys_user_role表记录，删除Redis中的Refresh Token
     */
    @Tag(name = "👥 用户管理")
    @Operation(summary = "删除用户", description = "物理删除用户，同时删除角色关联和Token")
    @DeleteMapping("/{username}")
    @SecurityRequirement(name = "bearerAuth")
    @HasPermission("system:user:delete")
    public Result<Void> deleteUser(@PathVariable String username) {
        userService.deleteUserByUsername(username);
        return Result.success(null, "删除成功");
    }

    /**
     * [UM-005] 禁用/启用用户
     * 功能：修改用户状态（0-正常 1-禁用）
     * 权限：system:user:edit
     * 入参：username(路径参数), status(0或1)
     * 返回：Result<Void>
     * 影响：更新sys_user表的status和update_time
     */
    @Tag(name = "👥 用户管理")
    @Operation(summary = "禁用/启用用户", description = "修改用户状态（0-正常 1-禁用）")
    @PutMapping("/{username}/status")
    @SecurityRequirement(name = "bearerAuth")
    @HasPermission("system:user:edit")
    public Result<Void> updateUserStatus(
            @PathVariable String username,
            @RequestParam Integer status) {
        userService.updateUserStatusByUsername(username, status);
        return Result.success(null, status == 0 ? "已启用" : "已禁用");
    }

    /**
     * [UM-006] 强制下线
     * 功能：清除用户所有Refresh Token，强制重新登录
     * 权限：system:user:edit
     * 入参：username(路径参数)
     * 返回：Result<Void>
     * 影响：删除Redis中该用户的所有Refresh Token
     */
    @Tag(name = "👥 用户管理")
    @Operation(summary = "强制下线", description = "清除用户所有Refresh Token，强制重新登录")
    @PostMapping("/{username}/force-logout")
    @SecurityRequirement(name = "bearerAuth")
    @HasPermission("system:user:edit")
    public Result<Void> forceLogout(@PathVariable String username) {
        userService.forceLogoutByUsername(username);
        return Result.success(null, "已强制下线");
    }

    /**
     * [UM-007] 管理员重置密码
     * 功能：管理员直接重置用户密码，无需旧密码
     * 权限：system:user:edit
     * 入参：username(路径参数), newPassword
     * 返回：Result<Void>
     * 影响：更新sys_user表的password，删除所有Refresh Token
     */
    @Tag(name = "👥 用户管理")
    @Operation(summary = "管理员重置密码", description = "管理员直接重置用户密码，无需旧密码")
    @PutMapping("/{username}/password")
    @SecurityRequirement(name = "bearerAuth")
    @HasPermission("system:user:edit")
    public Result<Void> resetPassword(
            @PathVariable String username,
            @RequestParam String newPassword) {
        userService.resetPasswordByUsername(username, newPassword);
        return Result.success(null, "密码重置成功");
    }

    /**
     * [UM-008] 分配用户角色
     * 功能：给用户分配角色（全量替换模式）
     * 权限：system:user:assign
     * 入参：username(路径参数), List<Long> roleIds
     * 返回：Result<Void>
     * 影响：删除sys_user_role表旧记录，插入新记录
     */
    @Tag(name = "👥 用户管理")
    @Operation(summary = "分配用户角色", description = "给用户分配角色（全量替换模式）")
    @PutMapping("/{username}/roles")
    @SecurityRequirement(name = "bearerAuth")
    @HasPermission("system:user:assign")
    public Result<Void> assignRoles(
            @PathVariable String username,
            @RequestBody List<Long> roleIds) {
        userService.assignRolesByUsername(username, roleIds);
        return Result.success(null, "角色分配成功");
    }

    // ==================== 【批量操作】 Batch Operations (BATCH) ====================
    
    /**
     * [BATCH-001] 批量删除用户
     * 功能：一次性删除多个用户
     * 权限：system:user:delete
     * 入参：List<Long> userIds
     * 返回：Result<Void>
     * 影响：批量删除sys_user表记录，批量删除sys_user_role表记录，批量删除Redis中的Refresh Token
     */
    @Tag(name = "⚡ 批量操作")
    @Operation(summary = "批量删除用户", description = "一次性删除多个用户")
    @PostMapping("/batch-delete")
    @SecurityRequirement(name = "bearerAuth")
    @HasPermission("system:user:delete")
    public Result<Void> batchDeleteUsers(@RequestBody List<Long> userIds) {
        userService.batchDeleteUsers(userIds);
        return Result.success(null, "批量删除成功");
    }

    /**
     * [BATCH-002] 批量分配角色
     * 功能：为多个用户分配相同的角色
     * 权限：system:user:assign
     * 入参：BatchAssignRolesRequest(userIds, roleIds)
     * 返回：Result<Void>
     * 影响：批量更新sys_user_role表
     */
    @Tag(name = "⚡ 批量操作")
    @Operation(summary = "批量分配角色", description = "为多个用户分配相同的角色")
    @PutMapping("/batch-assign-roles")
    @SecurityRequirement(name = "bearerAuth")
    @HasPermission("system:user:assign")
    public Result<Void> batchAssignRoles(@Valid @RequestBody BatchAssignRolesRequest request) {
        userService.batchAssignRoles(request.getUserIds(), request.getRoleIds());
        return Result.success(null, "批量分配成功");
    }
}
