package com.example.data_demo_002.modules.article.dao;


import lombok.Data;

/**
 * 标签查询参数
 */
@Data
public class TagQuery {

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 20;

    /**
     * 标签名称关键词（模糊查询）
     */
    private String keyword;

    /**
     * 状态：0-启用，1-禁用
     */
    private Integer status;
}
