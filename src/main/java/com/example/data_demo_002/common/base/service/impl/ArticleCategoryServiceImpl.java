package com.example.data_demo_002.common.base.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;



import com.example.data_demo_002.common.base.domain.ArticleCategory;
import com.example.data_demo_002.common.base.mapper.ArticleCategoryMapper;
import com.example.data_demo_002.common.base.service.ArticleCategoryService;
import org.springframework.stereotype.Service;

/**
* @author lhh
* @description 针对表【article_category(文章分类表：用于对文章进行归类管理)】的数据库操作Service实现
* @createDate 2026-04-02 13:06:32
*/
@Service
public class ArticleCategoryServiceImpl extends ServiceImpl<ArticleCategoryMapper, ArticleCategory>
    implements ArticleCategoryService {

}




