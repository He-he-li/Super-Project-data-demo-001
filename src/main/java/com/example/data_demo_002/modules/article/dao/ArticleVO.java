package com.example.data_demo_002.modules.article.dao;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 文章输出 VO
 */
@Data
public class ArticleVO {

    /**
     * 文章 ID
     */
    private Long id;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 文章副标题
     */
    private String subTitle;

    /**
     * 封面图片 URL
     */
    private String coverImage;

    /**
     * 文章摘要
     */
    private String summary;

    /**
     * 文章内容
     */
    private String content;

    /**
     * 作者 ID
     */
    private Long authorId;

    /**
     * 作者名称
     */
    private String authorName;

    /**
     * 分类 ID
     */
    private Long categoryId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 标签 ID 列表
     */
    private List<Long> tagIds;

    /**
     * 标签名称列表
     */
    private List<String> tagNames;

    /**
     * 发布状态：0-草稿，1-已发布，2-下架
     */
    private Integer status;

    /**
     * 审核状态：0-待审核，1-已通过，2-已拒绝
     */
    private Integer auditStatus;

    /**
     * 审核意见（拒绝原因）
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
     * 浏览量
     */
    private Long viewCount;

    /**
     * 点赞数
     */
    private Integer likeCount;

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
}
