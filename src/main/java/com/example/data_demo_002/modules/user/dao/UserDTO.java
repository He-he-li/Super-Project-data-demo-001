package com.example.data_demo_002.modules.user.dao;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 用户数据传输对象（Data Transfer Object）
 * 
 * 用于接收前端传递的用户相关请求数据，包含校验规则
 * 
 * @author data_demo_002
 * @version 1.0
 */
@Data
public class UserDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户 ID（雪花算法生成）
     */
    @NotNull(message = "用户 ID 不能为空", groups = UpdateGroup.class)
    private Long id;

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空", groups = CreateGroup.class)
    @Size(min = 2, max = 20, message = "用户名长度必须在 2-20 个字符之间", groups = {CreateGroup.class, UpdateGroup.class})
    @Pattern(regexp = "^[a-zA-Z0-9_\\u4e00-\\u9fa5]+$", message = "用户名只能包含字母、数字、下划线或中文", groups = {CreateGroup.class, UpdateGroup.class})
    private String username;

    /**
     * 密码（加密后存储）
     */
    @NotBlank(message = "密码不能为空", groups = CreateGroup.class)
    @Size(min = 6, max = 32, message = "密码长度必须在 6-32 位之间", groups = CreateGroup.class)
    private String password;

    /**
     * 邮箱地址
     */
    @Email(message = "邮箱格式不正确", groups = {CreateGroup.class, UpdateGroup.class})
    @Size(max = 50, message = "邮箱长度不能超过 50 个字符", groups = {CreateGroup.class, UpdateGroup.class})
    private String email;

    /**
     * 手机号码
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号码格式不正确", groups = {CreateGroup.class, UpdateGroup.class})
    private String phone;

    /**
     * 用户状态
     * 0: 正常
     * 1: 禁用
     */
    @NotNull(message = "用户状态不能为空", groups = CreateGroup.class)
    @Pattern(regexp = "^[01]$", message = "用户状态只能是 0 或 1", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer status=0;

    /**
     * 关联的角色 ID 列表
     */
    private List<Long> roleIds;

    /**
     * 创建时使用的校验分组接口
     * 
     * 用于标识哪些校验规则仅在创建用户时生效
     */
    public interface CreateGroup {}

    /**
     * 更新时使用的校验分组接口
     * 
     * 用于标识哪些校验规则仅在更新用户时生效
     */
    public interface UpdateGroup {}

}
