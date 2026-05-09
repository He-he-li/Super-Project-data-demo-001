package com.example.data_demo_002.common.base.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.data_demo_002.common.base.domain.ArticleTagRef;

import java.util.List;

/**
* @author lhh
* @description 针对表【article_tag_ref(文章与标签的关联表：实现文章的多标签功能)】的数据库操作Mapper
* @createDate 2026-04-02 13:06:32
* @Entity base.domain.ArticleTagRef
*/
public interface ArticleTagRefMapper extends BaseMapper<ArticleTagRef> {
    
    /**
     * 批量插入标签关联
     */
    default void insertBatch(List<ArticleTagRef> list) {
        for (ArticleTagRef entity : list) {
            insert(entity);
        }
    }
}




