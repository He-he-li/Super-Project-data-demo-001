package com.example.data_demo_002.modules.permission.dao;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class PermissionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long parentId;

    @NotBlank(message = "权限名称不能为空")
    @Size(max = 50, message = "权限名称不能超过50个字符")
    private String permissionName;

    @NotBlank(message = "权限编码不能为空")
    @Pattern(regexp = "^[a-z]+:[a-z]+(:[a-z]+)?$", message = "权限编码格式如：system:user:view")
    @Size(max = 100, message = "权限编码不能超过100个字符")
    private String permissionCode;

    @NotNull(message = "菜单类型不能为空")
    private Integer menuType;

    @Size(max = 200, message = "路径不能超过200个字符")
    private String path;

    @Size(max = 200, message = "组件路径不能超过200个字符")
    private String component;

    @Size(max = 50, message = "图标不能超过50个字符")
    private String icon;

    private Integer sortOrder;

    private Integer status;
}