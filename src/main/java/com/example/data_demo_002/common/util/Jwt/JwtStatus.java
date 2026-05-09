package com.example.data_demo_002.common.util.Jwt;



/**
 * JWT 令牌状态枚举
 * 决定令牌是否可用
 */
public class JwtStatus {
    // 正常：令牌可用
    public static final int NORMAL = 0;

    // 账号被冻结/禁用：令牌不可用，提示联系管理员，0 正常 1 冻结
    public static final int FROZEN = 1;

    // 密码已修改：令牌不可用，强制重新登录
    public static final int PASSWORD_CHANGED = 2;

    // 被强制下线（黑名单）：令牌不可用，通常由管理员操作触发
    public static final int FORCE_LOGOUT = 3;

    // Token 已过期：需要使用 Refresh Token 刷新
    public static final int TOKEN_EXPIRED = 4;

    // 其他自定义状态...
    public static final int UNKNOWN_ERROR = 99;

    /**
     * 获取状态描述
     */
    public static String getStatusDesc(int status) {
        switch (status) {
            case NORMAL: return "正常";
            case FROZEN: return "账号冻结";
            case PASSWORD_CHANGED: return "密码已变更";
            case FORCE_LOGOUT: return "已被强制下线";
            case TOKEN_EXPIRED: return "Token 已过期";
            default: return "未知状态";
        }
    }
}