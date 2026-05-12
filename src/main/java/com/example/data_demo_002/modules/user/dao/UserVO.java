package com.example.data_demo_002.modules.user.dao;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 用户视图对象（View Object）
 * 
 * 用于向客户端展示用户信息，不包含敏感数据（如密码、内部ID）
 * 对外统一使用username作为用户标识
 * 
 * @author data_demo_002
 * @version 2.0
 */
@Data
public class UserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户名（唯一业务标识，对外暴露）
     */
    private String username;

    /**
     * 邮箱地址
     */
    private String email;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 用户状态
     * 0: 正常
     * 1: 禁用
     */
    private Integer status;

    /**
     * 状态描述（计算字段，数据库不存储）
     */
    private String statusDesc;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    /**
     * 关联的角色 ID 列表
     */
    private List<Long> roleIds;

    /**
     * 关联的角色名称列表（展示用）
     */
    private List<String> roleNames;

    // 用于接收SQL查询结果的临时字段
    private String roleNamesStr;
    private String roleIdsStr;


}
