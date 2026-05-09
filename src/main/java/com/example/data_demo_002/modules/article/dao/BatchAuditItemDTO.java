package com.example.data_demo_002.modules.article.dao;


import lombok.Data;
import jakarta.validation.constraints.NotNull;

/**
 * 批量审核请求项
 */
@Data
public class BatchAuditItemDTO {

    /**
     * 文章 ID
     */
    @NotNull(message = "文章 ID 不能为空")
    private Long articleId;

    /**
     * 审核结果：1-通过，2-拒绝
     */
    @NotNull(message = "审核结果不能为空")
    private Integer auditStatus;

    /**
     * 审核意见（拒绝时必填）
     */
    private String auditRemark;
}
