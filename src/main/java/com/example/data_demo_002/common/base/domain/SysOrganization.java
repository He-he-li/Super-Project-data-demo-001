package com.example.data_demo_002.common.base.domain;


import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/**
 * 系统单位表
 */
@TableName(value = "sys_organization")
@Data
public class SysOrganization {
    @TableId
    private Long id;

    private String orgName;

    private String orgCode;

    private Long parentId;

    private Integer level;

    /**
     * 单位类型（1=系统级 2=普通单位）
     */
    private Integer orgType;

    /**
     * 是否系统级单位（0=否 1=是）
     */
    private Integer isSystem;

    private Integer sortOrder;

    private Integer status;

    private String description;

    private Integer deleted;

    private Date createTime;

    private Date updateTime;
}
