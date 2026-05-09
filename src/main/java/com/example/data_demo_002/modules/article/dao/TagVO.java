package com.example.data_demo_002.modules.article.dao;

import lombok.Data;

/**
 * 标签输出 VO
 */
@Data
public class TagVO {

    /**
     * 标签 ID
     */
    private Long id;

    /**
     * 标签名称
     */
    private String tagName;

    /**
     * 状态：0-启用，1-禁用
     */
    private Integer status;

    /**
     * 使用次数（关联的文章数量）
     */
    private Long usageCount;

    /**
     * 创建时间
     */
    private java.util.Date createTime;
}
