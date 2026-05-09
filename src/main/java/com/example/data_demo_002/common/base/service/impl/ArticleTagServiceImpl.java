package com.example.data_demo_002.common.base.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.example.data_demo_002.common.base.domain.ArticleTag;
import com.example.data_demo_002.common.base.mapper.ArticleTagMapper;
import com.example.data_demo_002.common.base.service.ArticleTagService;
import org.springframework.stereotype.Service;

/**
* @author lhh
* @description 针对表【article_tag(文章标签表：用于文章的灵活标记)】的数据库操作Service实现
* @createDate 2026-04-02 13:06:32
*/
@Service
public class ArticleTagServiceImpl extends ServiceImpl<ArticleTagMapper, ArticleTag>
    implements ArticleTagService {

}




