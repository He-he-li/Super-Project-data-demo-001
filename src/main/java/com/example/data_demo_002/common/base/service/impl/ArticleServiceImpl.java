package com.example.data_demo_002.common.base.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.example.data_demo_002.common.base.domain.Article;
import com.example.data_demo_002.common.base.mapper.ArticleMapper;
import com.example.data_demo_002.common.base.service.ArticleService;
import org.springframework.stereotype.Service;

/**
* @author lhh
* @description 针对表【article(文章主表：存储文章的核心数据)】的数据库操作Service实现
* @createDate 2026-04-02 13:06:32
*/
@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article>
    implements ArticleService {

}




