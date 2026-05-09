package com.example.data_demo_002.common.util.permissionUtil;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限检查注解（简化版）
 * 用于在方法级别声明所需的权限编码
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface HasPermission {
    /**
     * 权限编码，如：article:audit:approve
     * @return 权限编码字符串
     */
    String value();
}
