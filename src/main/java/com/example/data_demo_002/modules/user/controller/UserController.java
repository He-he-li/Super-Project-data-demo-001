package com.example.data_demo_002.modules.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.data_demo_002.common.result.Result;
import com.example.data_demo_002.common.util.permissionUtil.HasPermission;
import com.example.data_demo_002.common.util.Jwt.UserContext;
import com.example.data_demo_002.modules.user.dao.UserDTO;
import com.example.data_demo_002.modules.user.dao.UserLoginVO;
import com.example.data_demo_002.modules.user.dao.UserVO;
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
import java.util.Map;

/**
 * 用户管理控制器
 *
 * 提供用户注册、登录、信息查询和修改等接口
 */
@Slf4j
@Tag(name = "用户管理", description = "提供用户相关的 CRUD 操作接口")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    // 用户服务
    private final UserService userService;




    /**
     * 用户注册
     *
     * @param dto 用户注册信息（用户名、密码、邮箱、手机）
     * @return 注册成功后的用户信息（包含用户 ID）
     */
    @Operation(summary = "用户注册", description = "创建新用户并分配默认角色")
    @PostMapping("/register")
    public Result<UserDTO> register(
            @Parameter(description = "用户注册信息", required = true)
            @Valid @RequestBody UserDTO dto) {
        UserDTO result = userService.createUser(dto);
        return Result.success(result);
    }

    /**
     * 用户登录
     *
     * @param loginRequest 登录请求（用户名、密码）
     * @return 登录成功后的用户信息和 Token 信息
     */
    @Operation(summary = "用户登录"  , description = "验证用户名密码，返回用户信息和 Token 信息")
    @PostMapping("/loginByUserName")
    public Result<UserLoginVO> loginByUserName(
            @Parameter(description = "登录请求", required = true)
            @RequestBody Map<String, String> loginRequest){
        String userName = loginRequest.get("userName");
        String password = loginRequest.get("password");
        
        System.out.println("用户名：" + userName);
        System.out.println("密码：" + password);

        UserLoginVO response = userService.login(userName, password);
        return Result.success(response);
    }

    /**
     * 用户登录
     * @param dto 用户登录信息（用户名、密码）
     * @return 登录成功后的用户信息和 Token 信息
     */
    @Operation(summary = "用户登录", description = "验证用户名密码，返回用户信息和 Token 信息")
    @PostMapping("/login")
    public Result<UserLoginVO> login(
            @Parameter(description = "用户登录信息", required = true)
            @Valid @RequestBody UserDTO dto) {
        System.out.println("用户名：" + dto.getUsername());
        UserLoginVO response = userService.login(dto.getUsername(), dto.getPassword());
        System.out.println("登录成功：" + response);
        return Result.success(response);
    }

    /**
     * 用户接口：用户登录
     * @param userName password
     * @return token
     */
    @Operation(summary = "用户登录", description = "验证用户名密码，返回用户信息和角色列表")
    @PostMapping("/loginTest")
    public Result<UserLoginVO> loginTest(
            @Valid @RequestBody String userName, String password) {
        UserLoginVO vo = userService.login(userName, password);
        return Result.success(vo);
    }

    /**
     * 用户接口：获取当前登录用户的信息
     * 根据 token 中获取用户信息
     * token 中：userId,username
     * @return vo
     */
    @Operation(summary = "获取当前登录用户信息", description = "获取当前登录用户的信息")
    @GetMapping("/getMe")
    @SecurityRequirement(name = "bearerAuth")
    // 权限控制
    @HasPermission("system:user:view")
    public Result<UserVO> getUserInfo() {
        UserVO vo = userService.getUserDetail(UserContext.getUserId());
        return Result.success(vo);
    }

    /**
     * 用户接口：修改当前用户信息
     * @return vo
     */
    @Operation(summary = "修改当前登录用户信息", description = "修改当前登录用户的信息")
    @GetMapping("/upDateMe")
    @SecurityRequirement(name = "bearerAuth")
    @HasPermission("system:user:edit")
    public Result<UserVO> upDateUserInfo(
            @Parameter(description = "要修改的用户信息", required = true)
            @RequestBody UserDTO dto) {

        UserVO vo = userService.upDateUser(UserContext.getUserId(), dto);
        return Result.success(vo, "修改成功");
    }

    /**
     * 用户接口：登录状态下修改密码
     * 修改密码
     * @param userId 用户 ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 修改成功提示
     */
    @Operation(summary = "修改密码", description = "修改用户登录密码，需要验证旧密码")
    @PutMapping("/{userId}/LoginChangePasswordByMe")
    @SecurityRequirement(name = "bearerAuth")
    @HasPermission("system:user:edit")
    public Result<Void> LoginChangePasswordByMe(
            @Parameter(description = "用户 ID", required = true)
            @PathVariable Long userId,

            @Parameter(description = "旧密码", required = true)
            @RequestParam String oldPassword,

            @Parameter(description = "新密码", required = true)
            @RequestParam String newPassword) {

        userService.updatePassword(userId, oldPassword, newPassword);
        return Result.success(null, "密码修改成功，请重新登录");
    }

    /**
     * 用户接口：未登录状态通过用户名下修改密码
     * 修改密码
     * @param username 用户名
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 修改成功提示
     */
    @Operation(summary = "修改密码", description = "修改用户登录密码，需要验证旧密码")
    @PutMapping("/password/not-login/username/{username}")
    public Result<Void> NotLoginChangePasswordByUserName(
            @Parameter(description = "用户名", required = true)
            @PathVariable String username,

            @Parameter(description = "旧密码", required = true)
            @RequestParam String oldPassword,

            @Parameter(description = "新密码", required = true)
            @RequestParam String newPassword) {
        userService.NotLoginUpdatePasswordByUserName(username, oldPassword, newPassword);

        return Result.success(null, "密码修改成功，请重新登录");
    }
    /**
     * 用户接口：未登录状态通过邮箱修改密码
     * @param email 用户邮箱
     * @param newPassword 新密码
     * @return
     */
    @Operation(summary = "修改密码", description = "修改用户登录密码，需要验证旧密码")
    @PutMapping("/password/not-login/email/{email}")
    public Result<Void> NotLoginChangePasswordByEmail(
            @Parameter(description = "用户邮箱", required = true)
            @PathVariable String email,
            @Parameter(description = "新密码", required = true)
            @RequestParam String newPassword
    ){

        userService.NotLoginUpdatePasswordByEmail(email, newPassword);
        return Result.success(null, "密码修改成功，请重新登录");
    }


    /**
     * 管理员接口：修改用户信息
     * @param userId 用户 ID
     * @param dto 要修改的信息（用户名、邮箱、手机、状态）
     * @return 修改成功返回更新后的用户信息
     */
    @Operation(summary = "修改用户信息", description = "修改用户的基本信息（用户名、邮箱、手机、状态）")
    @PutMapping("/{userId}")
    @SecurityRequirement(name = "bearerAuth")
    @HasPermission("system:user:edit")
    public Result<UserVO> updateUser(
            @Parameter(description = "用户 ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "要修改的用户信息", required = true)
            @RequestBody UserDTO dto) {

        UserVO vo = userService.upDateUser(userId, dto);
        return Result.success(vo, "修改成功");
    }


    /**
     * 获取用户详情（含角色列表）
     *
     * @param userId 用户 ID
     * @return 用户详细信息和角色列表
     */
    @Operation(summary = "获取用户详情", description = "根据用户 ID 查询详细信息，包含角色列表")
    @GetMapping("/{userId}")
    @SecurityRequirement(name = "bearerAuth")
    @HasPermission("system:user:view")
    public Result<UserVO> getUserDetail(
            @Parameter(description = "用户 ID", required = true)
            @PathVariable Long userId) {

        UserVO vo = userService.getUserDetail(userId);
        return Result.success(vo);
    }





    /**
     * 管理员接口
     * 通过用户名修改密码
     * @param username 用户名
     * @param newPassword 新密码
     * @return 密码修改成功提示
     */
    @Operation(summary = "修改密码", description = "修改用户登录密码，需要验证旧密码")
    @PutMapping("/password/admin/username/{username}")
    @HasPermission("system:user:edit")
    public Result<Void> AdminChangePasswordByUserName(
            @Parameter(description = "用户名", required = true)
            @PathVariable String username,
            @Parameter(description = "新密码", required = true)
            @RequestParam String newPassword) {
        userService.updatePasswordByUserName(username, newPassword);

        return Result.success(null, "密码修改成功，请重新登录");
    }

    /**
     * 管理员接口
     * 通过邮箱修改密码
     * @param email 用户邮箱
     * 邮箱验证码
     * @param newPassword 新密码
     * @return 密码修改成功提示
     */
    @Operation(summary = "修改密码", description = "修改用户登录密码，需要验证旧密码")
    @PutMapping("/password/admin/email/{email}")
    @HasPermission("system:user:edit")
    public Result<Void> AdminChangePasswordByEmail(
            @Parameter(description = "用户邮箱", required = true)
            @PathVariable String email,
            @Parameter(description = "新密码", required = true)
            @RequestParam String newPassword
    ){
        userService.updatePasswordByEmail(email, newPassword);
        return Result.success(null, "密码修改成功，请重新登录");
    }

    /**
     * 管理员接口：通过用户名查询用户信息
     * @param username 用户名
     * @return 用户信息
     */
    @Operation(summary = "通过用户名查询用户信息", description = "根据用户名查询用户信息")
    @GetMapping("/username/{username}")
    @SecurityRequirement(name = "bearerAuth")
    @HasPermission("system:user:view")
    public Result<UserVO> getUserByUserName(
            @Parameter(description = "用户名", required = true)
            @PathVariable String username )
    {
        UserVO user = userService.getUserByUserName(username);
        return Result.success(user);
    }




    /**
     * 管理员接口：通过用户名修改用户信息、状态
     * 
     * @param username 用户名
     * @param dto 要修改的用户信息（用户名、邮箱、手机、状态）
     * @return 修改成功返回更新后的用户信息
     */
    @Operation(summary = "修改用户信息", description = "通过用户名修改用户的基本信息（用户名、邮箱、手机、状态）")
    @PutMapping("/username/{username}")
    @SecurityRequirement(name = "bearerAuth")
    @HasPermission("system:user:editByUsername")
    public Result<UserVO> updateUserByUserName(
            @Parameter(description = "用户名", required = true)
            @PathVariable String username,
            @Parameter(description = "要修改的用户信息", required = true)
            @RequestBody UserDTO dto) {
        
        UserVO updatedUser = userService.upDateUserByUserName(username, dto);
        return Result.success(updatedUser, "修改成功");
    }

    /**
     * 管理员接口：分页获取用户列表
     * 
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页数量（1-100）
     * @param username 用户名（模糊查询，可选）
     * @param email 邮箱（模糊查询，可选）
     * @param phone 手机号（模糊查询，可选）
     * @param status 用户状态（0-正常，1-禁用，可选）
     * @return 分页用户列表
     */
    @Operation(summary = "获取用户列表", description = "分页获取用户列表（支持多条件筛选）")
    @GetMapping("/list")
    @SecurityRequirement(name = "bearerAuth")
    @HasPermission("system:user:list")
    public Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<UserVO>> listUsers(
            @Parameter(description = "页码（从1开始）", example = "1")
            @RequestParam(defaultValue = "1") 
            @Min(value = 1, message = "页码必须大于0") int pageNum,
            
            @Parameter(description = "每页数量（1-100）", example = "10")
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "每页数量必须大于0")
            @Max(value = 100, message = "每页数量不能超过100") int pageSize,
            
            @Parameter(description = "用户名（模糊查询）", example = "admin")
            @RequestParam(required = false) String username,
            
            @Parameter(description = "邮箱（模糊查询）", example = "@qq.com")
            @RequestParam(required = false) String email,
            
            @Parameter(description = "手机号（模糊查询）", example = "138")
            @RequestParam(required = false) String phone,
            
            @Parameter(description = "用户状态（0-正常，1-禁用）", example = "0")
            @RequestParam(required = false) Integer status) {
        
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<UserVO> page = 
                userService.listUsers(pageNum, pageSize, username, email, phone, status);
        return Result.success(page);
    }

    /**
     * 管理员接口：通过用户名删除用户
     * @param username 用户名
     * @return 删除成功提示
     */
    @Operation(summary = "删除用户", description = "通过用户名删除用户")
    @DeleteMapping("/username/{username}")
    @SecurityRequirement(name = "bearerAuth")
    @HasPermission("system:user:delete")
    public Result<Void> deleteUserByUserName(
            @Parameter(description = "用户名", required = true)
            @PathVariable String username) {

        userService.deleteUserByUserName(username);
        return Result.success(null, "删除成功");
    }




    /**
     * 管理员接口
     * 给用户重新分配角色
     * @param userId 用户 ID
     * @param roleIds 角色 ID 列表
     * @return 分配成功提示
     */
    @Operation(summary = "分配角色", description = "给用户重新分配角色（会先删除旧角色，再添加新角色）")
    @PutMapping("/{userId}/roles")
    @SecurityRequirement(name = "bearerAuth")
    @HasPermission("system:user:assign")
    public Result<Void> assignRoles(
            @Parameter(description = "用户 ID", required = true)
            @PathVariable Long userId,

            @Parameter(description = "角色 ID 列表", required = true)
            @RequestBody List<Long> roleIds) {


        userService.assignRoles(userId, roleIds);

        return Result.success(null, "角色分配成功");
    }
}
