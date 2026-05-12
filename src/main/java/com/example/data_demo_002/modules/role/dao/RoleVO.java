package com.example.data_demo_002.modules.role.dao;


import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class RoleVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String roleName;

    private String roleCode;

    private String description;

    private Date createTime;

    private Date updateTime;

    private List<Long> permissionIds;
}