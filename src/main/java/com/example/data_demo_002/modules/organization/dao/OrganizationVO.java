package com.example.data_demo_002.modules.organization.dao;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class OrganizationVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String orgName;

    private String orgCode;

    private Long parentId;

    private String parentName;

    private Integer level;

    private Integer orgType;

    private String orgTypeDesc;

    private Integer isSystem;

    private Integer sortOrder;

    private Integer status;

    private String statusDesc;

    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;


    private List<OrganizationVO> children;
}

