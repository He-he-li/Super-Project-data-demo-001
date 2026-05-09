package com.example.data_demo_002.modules.article.dao;


import lombok.Data;

/**
 * 文章审核请求 DTO
 */
@Data
public class ArticleAuditDTO {

    /**
     * 文章 ID
     */
    private Long articleId;

    /**
     * 审核结果：1-通过，2-拒绝
     */
    private Integer auditStatus;

    /**
     * 审核意见（拒绝时必填）
     */
    private String auditRemark;
}
