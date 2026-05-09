package com.example.data_demo_002.common.exception;



import lombok.Getter;

/**
 * 自定义业务异常
 * 例如：用户不存在、密码错误、权限不足
 */
@Getter
public class BusinessException extends RuntimeException {
    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = 500; // 默认业务错误码
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}