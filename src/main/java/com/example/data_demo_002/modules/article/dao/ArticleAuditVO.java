package com.example.data_demo_002.modules.article.dao;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

/**
 * 审核记录 VO
 */
@Data
public class ArticleAuditVO {

    /**
     * 审核记录 ID
     */
    private Long id;

    /**
     * 文章 ID
     */
    private Long articleId;

    /**
     * 审核状态：0-待审核，1-已通过，2-已拒绝
     */
    private Integer auditStatus;

    /**
     * 审核意见
     */
    private String auditRemark;

    /**
     * 审核人 ID
     */
    private Long auditUserId;

    /**
     * 审核人名称
     */
    private String auditUserName;

    /**
     * 审核时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date auditTime;

    /**
     * 投稿时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date submitTime;
}
