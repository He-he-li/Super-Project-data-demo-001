package com.example.data_demo_002.modules.user.dao;

import lombok.Data;

/**
 * 用户登录响应对象
 * 包含用户基本信息和 Token 信息
 */
@Data
public class UserLoginVO {

    /**
     * 用户 ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 用户状态
     */
    private Integer status;

    /**
     * 角色 ID 列表
     */
    private java.util.List<Long> roleIds;

    /**
     * 角色名称列表
     */
    private java.util.List<String> roleNames;

    /**
     * Access Token
     */
    private String token;

    /**
     * Refresh Token
     */
    private String refreshToken;

    /**
     * Access Token 有效期（秒）
     */
    private Long expiresIn;

    /**
     * Refresh Token 有效期（秒）
     */
    private Long refreshExpiresIn;
}
