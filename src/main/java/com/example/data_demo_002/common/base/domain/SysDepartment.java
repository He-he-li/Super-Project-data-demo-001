package com.example.data_demo_002.common.base.domain;


import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/**
 * 系统部门表
 */
@TableName(value = "sys_department")
@Data
public class SysDepartment {
    @TableId
    private Long id;

    private Long organizationId;

    private String deptName;

    private Long parentId;

    private Integer level;

    private Integer sortOrder;

    private Long leaderId;

    private String phone;

    private String email;

    private Integer status;

    private Integer deleted;

    private Date createTime;

    private Date updateTime;
}
