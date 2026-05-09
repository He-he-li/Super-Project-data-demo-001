package com.example.data_demo_002.common.base.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.example.data_demo_002.common.base.domain.ArticleAudit;
import com.example.data_demo_002.common.base.mapper.ArticleAuditMapper;
import com.example.data_demo_002.common.base.service.ArticleAuditService;
import org.springframework.stereotype.Service;

/**
* @author lhh
* @description 针对表【article_audit(文章审核记录表)】的数据库操作Service实现
* @createDate 2026-04-02 15:26:40
*/
@Service
public class ArticleAuditServiceImpl extends ServiceImpl<ArticleAuditMapper, ArticleAudit>
    implements ArticleAuditService {

}




