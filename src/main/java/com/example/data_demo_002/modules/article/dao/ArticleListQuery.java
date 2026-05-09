package com.example.data_demo_002.modules.article.dao;


import lombok.Data;

/**
 * 文章列表查询参数
 */
@Data
public class ArticleListQuery {

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;

    /**
     * 标题关键词（模糊查询）
     */
    private String title;

    /**
     * 状态筛选
     */
    private Integer status;

    /**
     * 分类 ID
     */
    private Long categoryId;

    /**
     * 标签 ID（前台使用）
     */
    private Long tagId;

    /**
     * 搜索关键词（前台使用）
     */
    private String keyword;
}
