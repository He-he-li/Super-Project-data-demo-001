package com.example.data_demo_002.modules.article.dao;

import lombok.Data;

import java.util.List;

/**
 * 文章输入 DTO
 */
@Data
public class ArticleDTO {

    /**
     * 文章 ID（编辑时传递）
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
     * 分类 ID
     */
    private Long categoryId;

    /**
     * 标签 ID 列表
     */
    private List<Long> tagIds;

    /**
     * 发布状态：0-草稿，1-已发布，2-下架
     */
    private Integer status;
}
