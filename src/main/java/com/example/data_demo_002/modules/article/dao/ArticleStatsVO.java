package com.example.data_demo_002.modules.article.dao;


import lombok.Data;

/**
 * 文章统计 VO
 */
@Data
public class ArticleStatsVO {

    /**
     * 总文章数
     */
    private Long totalCount;

    /**
     * 待审核数量
     */
    private Long pendingCount;

    /**
     * 已通过数量
     */
    private Long approvedCount;

    /**
     * 已拒绝数量
     */
    private Long rejectedCount;

    /**
     * 已发布数量
     */
    private Long publishedCount;

    /**
     * 总浏览量
     */
    private Long totalViews;
}
