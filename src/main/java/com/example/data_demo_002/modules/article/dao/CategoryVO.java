package com.example.data_demo_002.modules.article.dao;


import lombok.Data;

/**
 * 分类输出 VO
 */
@Data
public class CategoryVO {

    /**
     * 分类 ID
     */
    private Long id;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 分类描述
     */
    private String description;

    /**
     * 排序序号
     */
    private Integer sortOrder;
}
