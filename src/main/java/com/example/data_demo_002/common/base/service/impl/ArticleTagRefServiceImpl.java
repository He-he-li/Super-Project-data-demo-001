package com.example.data_demo_002.common.base.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.example.data_demo_002.common.base.domain.ArticleTagRef;
import com.example.data_demo_002.common.base.mapper.ArticleTagRefMapper;
import com.example.data_demo_002.common.base.service.ArticleTagRefService;
import org.springframework.stereotype.Service;

/**
* @author lhh
* @description 针对表【article_tag_ref(文章与标签的关联表：实现文章的多标签功能)】的数据库操作Service实现
* @createDate 2026-04-02 13:06:32
*/
@Service
public class ArticleTagRefServiceImpl extends ServiceImpl<ArticleTagRefMapper, ArticleTagRef>
    implements ArticleTagRefService {

}




