package com.example.data_demo_002.modules.organization.dao;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class OrganizationDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @NotBlank(message = "单位名称不能为空")
    @Size(max = 100, message = "单位名称长度不能超过100个字符")
    private String orgName;

    @NotBlank(message = "单位编码不能为空")
    @Size(max = 50, message = "单位编码长度不能超过50个字符")
    private String orgCode;

    private Long parentId;

    private Integer level;

    private Integer orgType;

    private Integer isSystem;

    private Integer sortOrder;

    private Integer status;

    private String description;
}