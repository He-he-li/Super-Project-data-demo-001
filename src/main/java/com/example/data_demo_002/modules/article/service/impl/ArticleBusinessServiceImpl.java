package com.example.data_demo_002.modules.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.example.data_demo_002.common.base.domain.Article;
import com.example.data_demo_002.common.base.domain.ArticleAudit;
import com.example.data_demo_002.common.base.domain.ArticleCategory;
import com.example.data_demo_002.common.base.domain.ArticleTag;
import com.example.data_demo_002.common.base.domain.ArticleTagRef;
import com.example.data_demo_002.common.base.domain.SysUser;

import com.example.data_demo_002.common.base.mapper.*;
import com.example.data_demo_002.common.exception.BusinessException;
import com.example.data_demo_002.modules.article.dao.*;

import com.example.data_demo_002.modules.article.service.ArticleBusinessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleBusinessServiceImpl implements ArticleBusinessService {

    private final ArticleMapper articleMapper;
    private final ArticleAuditMapper auditMapper;
    private final ArticleCategoryMapper categoryMapper;
    private final ArticleTagMapper tagMapper;
    private final ArticleTagRefMapper tagRefMapper;
    private final SysUserMapper userMapper;

    // ==================== 用户端接口 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArticleVO submitArticle(ArticleDTO dto, Long authorId) {
        // 1. 校验分类是否存在
        ArticleCategory category = categoryMapper.selectById(dto.getCategoryId());
        if (category == null) {
            throw new BusinessException("分类不存在");
        }

        // 2. 构建文章实体
        Article article = new Article();
        article.setTitle(dto.getTitle());
        article.setSubTitle(dto.getSubTitle());
        article.setCoverImage(dto.getCoverImage());
        article.setSummary(dto.getSummary());
        article.setContent(dto.getContent());
        article.setAuthorId(authorId);
        article.setCategoryId(dto.getCategoryId());
        article.setStatus(1); // 默认已发布（待审核）
        article.setViewCount(0L);
        article.setLikeCount(0);
        article.setDeleted(0);
        article.setCreateTime(new Date());
        article.setUpdateTime(new Date());
        article.setVersion(0);

        // 3. 保存文章
        articleMapper.insert(article);

        // 4. 保存审核记录（自动进入待审核）
        ArticleAudit audit = new ArticleAudit();
        audit.setArticleId(article.getId());
        audit.setAuditStatus(0); // 0-待审核
        audit.setSubmitTime(new Date());
        audit.setCreateTime(new Date());
        audit.setUpdateTime(new Date());
        auditMapper.insert(audit);

        // 5. 保存标签关联
        if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()) {
            saveTagRefs(article.getId(), dto.getTagIds());
        }

        log.info("用户投稿成功：articleId={}, authorId={}, title={}", article.getId(), authorId, article.getTitle());
        return convertToVO(article);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArticleVO updateUserArticle(ArticleDTO dto, Long authorId) {
        // 1. 查询文章（校验归属）
        Article article = articleMapper.selectById(dto.getId());
        if (article == null || article.getDeleted() == 1) {
            throw new BusinessException("文章不存在");
        }
        if (!article.getAuthorId().equals(authorId)) {
            throw new BusinessException("无权操作该文章");
        }

        // 2. 更新文章
        if (dto.getTitle() != null) article.setTitle(dto.getTitle());
        if (dto.getSubTitle() != null) article.setSubTitle(dto.getSubTitle());
        if (dto.getCoverImage() != null) article.setCoverImage(dto.getCoverImage());
        if (dto.getSummary() != null) article.setSummary(dto.getSummary());
        if (dto.getContent() != null) article.setContent(dto.getContent());
        if (dto.getCategoryId() != null) {
            ArticleCategory category = categoryMapper.selectById(dto.getCategoryId());
            if (category == null) {
                throw new BusinessException("分类不存在");
            }
            article.setCategoryId(dto.getCategoryId());
        }
        article.setUpdateTime(new Date());
        articleMapper.updateById(article);

        // 3. 更新标签关联（先删后增）
        if (dto.getTagIds() != null) {
            tagRefMapper.delete(new LambdaQueryWrapper<ArticleTagRef>()
                    .eq(ArticleTagRef::getArticleId, article.getId()));
            if (!dto.getTagIds().isEmpty()) {
                saveTagRefs(article.getId(), dto.getTagIds());
            }
        }

        // 4. 如果文章已通过审核，修改内容后重置为待审核状态
        ArticleAudit audit = auditMapper.selectOne(
                new LambdaQueryWrapper<ArticleAudit>()
                        .eq(ArticleAudit::getArticleId, article.getId())
                        .orderByDesc(ArticleAudit::getCreateTime)
                        .last("LIMIT 1"));
        
        if (audit != null && audit.getAuditStatus() == 1) {
            // 重置审核状态
            audit.setAuditStatus(0);
            audit.setSubmitTime(new Date());
            audit.setUpdateTime(new Date());
            auditMapper.updateById(audit);
            log.info("文章修改后重置为待审核状态：articleId={}", article.getId());
        }

        log.info("用户编辑文章成功：id={}", article.getId());
        return convertToVO(article);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUserArticle(Long articleId, Long authorId) {
        Article article = articleMapper.selectById(articleId);
        if (article == null || article.getDeleted() == 1) {
            throw new BusinessException("文章不存在");
        }
        if (!article.getAuthorId().equals(authorId)) {
            throw new BusinessException("无权操作该文章");
        }

        // 逻辑删除
        article.setDeleted(1);
        article.setUpdateTime(new Date());
        articleMapper.updateById(article);

        log.info("用户删除文章成功：id={}", articleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void withdrawArticle(Long articleId, Long authorId) {
        // 1. 查询文章
        Article article = articleMapper.selectById(articleId);
        if (article == null || article.getDeleted() == 1) {
            throw new BusinessException("文章不存在");
        }
        if (!article.getAuthorId().equals(authorId)) {
            throw new BusinessException("无权操作该文章");
        }

        // 2. 查询最新审核记录
        ArticleAudit audit = auditMapper.selectOne(
                new LambdaQueryWrapper<ArticleAudit>()
                        .eq(ArticleAudit::getArticleId, articleId)
                        .orderByDesc(ArticleAudit::getCreateTime)
                        .last("LIMIT 1"));
        
        if (audit == null || audit.getAuditStatus() != 0) {
            throw new BusinessException("只能撤回待审核的文章");
        }

        // 3. 将文章状态改为草稿
        article.setStatus(0);
        article.setUpdateTime(new Date());
        articleMapper.updateById(article);

        // 4. 标记审核记录为已撤回
        audit.setAuditStatus(2);
        audit.setAuditRemark("用户主动撤回");
        audit.setUpdateTime(new Date());
        auditMapper.updateById(audit);

        log.info("用户撤回投稿：articleId={}, authorId={}", articleId, authorId);
    }

    @Override
    public ArticleVO getUserArticleDetail(Long articleId, Long authorId) {
        Article article = articleMapper.selectById(articleId);
        if (article == null || article.getDeleted() == 1) {
            throw new BusinessException("文章不存在");
        }
        if (!article.getAuthorId().equals(authorId)) {
            throw new BusinessException("无权查看该文章");
        }
        return convertToVO(article);
    }

    @Override
    public Page<ArticleVO> getUserArticleList(Long authorId, ArticleListQuery query) {
        Page<Article> page = new Page<>(query.getPageNum(), query.getPageSize());
        
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getDeleted, 0)
               .eq(Article::getAuthorId, authorId);
        
        // 可按状态筛选
        if (query.getStatus() != null) {
            wrapper.eq(Article::getStatus, query.getStatus());
        }
        
        wrapper.orderByDesc(Article::getCreateTime);
        
        Page<Article> resultPage = articleMapper.selectPage(page, wrapper);
        
        List<ArticleVO> voList = resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        Page<ArticleVO> voPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public ArticleStatsVO getUserArticleStats(Long authorId) {
        // 统计总数
        long totalCount = articleMapper.selectCount(
                new LambdaQueryWrapper<Article>()
                        .eq(Article::getDeleted, 0)
                        .eq(Article::getAuthorId, authorId));
        
        // 统计各状态数量
        long pendingCount = auditMapper.selectCount(
                new LambdaQueryWrapper<ArticleAudit>()
                        .eq(ArticleAudit::getAuditStatus, 0));
        
        long approvedCount = auditMapper.selectCount(
                new LambdaQueryWrapper<ArticleAudit>()
                        .eq(ArticleAudit::getAuditStatus, 1));
        
        long rejectedCount = auditMapper.selectCount(
                new LambdaQueryWrapper<ArticleAudit>()
                        .eq(ArticleAudit::getAuditStatus, 2));
        
        long publishedCount = articleMapper.selectCount(
                new LambdaQueryWrapper<Article>()
                        .eq(Article::getDeleted, 0)
                        .eq(Article::getAuthorId, authorId)
                        .eq(Article::getStatus, 1));
        
        // 统计总浏览量
        List<Article> articles = articleMapper.selectList(
                new LambdaQueryWrapper<Article>()
                        .eq(Article::getDeleted, 0)
                        .eq(Article::getAuthorId, authorId)
                        .select(Article::getViewCount));
        
        long totalViews = articles.stream()
                .mapToLong(a -> a.getViewCount() != null ? a.getViewCount() : 0)
                .sum();
        
        ArticleStatsVO stats = new ArticleStatsVO();
        stats.setTotalCount(totalCount);
        stats.setPendingCount(pendingCount);
        stats.setApprovedCount(approvedCount);
        stats.setRejectedCount(rejectedCount);
        stats.setPublishedCount(publishedCount);
        stats.setTotalViews(totalViews);
        
        return stats;
    }

    // ==================== 管理员端接口 ====================

    @Override
    public ArticleVO getAuditArticleDetail(Long articleId) {
        Article article = articleMapper.selectById(articleId);
        if (article == null || article.getDeleted() == 1) {
            throw new BusinessException("文章不存在");
        }
        return convertToVO(article);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArticleAuditVO auditArticle(ArticleAuditDTO dto, Long adminId) {
        // 1. 查询审核记录
        ArticleAudit audit = auditMapper.selectOne(
                new LambdaQueryWrapper<ArticleAudit>()
                        .eq(ArticleAudit::getArticleId, dto.getArticleId())
                        .orderByDesc(ArticleAudit::getCreateTime)
                        .last("LIMIT 1"));
        
        if (audit == null) {
            throw new BusinessException("该文章没有审核记录");
        }

        // 2. 更新审核记录
        audit.setAuditStatus(dto.getAuditStatus());
        audit.setAuditUserId(adminId);
        audit.setAuditRemark(dto.getAuditRemark());
        audit.setAuditTime(new Date());
        audit.setUpdateTime(new Date());
        auditMapper.updateById(audit);

        // 3. 根据审核结果更新文章状态
        Article article = articleMapper.selectById(dto.getArticleId());
        if (dto.getAuditStatus() == 1) {
            // 审核通过：设置为已发布
            article.setStatus(1);
            log.info("文章审核通过：articleId={}, adminId={}", dto.getArticleId(), adminId);
        } else if (dto.getAuditStatus() == 2) {
            // 审核拒绝：设置为草稿（仅自己可见）
            article.setStatus(0);
            log.info("文章审核拒绝：articleId={}, adminId={}, remark={}", 
                    dto.getArticleId(), adminId, dto.getAuditRemark());
        }
        article.setUpdateTime(new Date());
        articleMapper.updateById(article);

        return convertAuditToVO(audit);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ArticleAuditVO> batchAuditArticle(List<BatchAuditItemDTO> dtos, Long adminId) {
        if (dtos == null || dtos.isEmpty()) {
            throw new BusinessException("审核列表不能为空");
        }

        List<ArticleAuditVO> results = new ArrayList<>();
        for (BatchAuditItemDTO dto : dtos) {
            try {
                ArticleAuditDTO singleDto = new ArticleAuditDTO();
                singleDto.setArticleId(dto.getArticleId());
                singleDto.setAuditStatus(dto.getAuditStatus());
                singleDto.setAuditRemark(dto.getAuditRemark());
                
                ArticleAuditVO result = auditArticle(singleDto, adminId);
                results.add(result);
            } catch (Exception e) {
                log.error("批量审核失败：articleId={}, error={}", dto.getArticleId(), e.getMessage());
                // 继续处理下一个，不中断整个流程
            }
        }
        
        return results;
    }

    @Override
    public Page<ArticleVO> getAuditArticleList(ArticleListQuery query) {
        // 查询待审核的文章 ID 列表
        Page<ArticleAudit> auditPage = new Page<>(query.getPageNum(), query.getPageSize());
        
        LambdaQueryWrapper<ArticleAudit> auditWrapper = new LambdaQueryWrapper<>();
        auditWrapper.eq(ArticleAudit::getAuditStatus, 0)
                    .orderByAsc(ArticleAudit::getSubmitTime);
        
        Page<ArticleAudit> auditResult = auditMapper.selectPage(auditPage, auditWrapper);
        
        // 提取文章 ID 列表
        List<Long> articleIds = auditResult.getRecords().stream()
                .map(ArticleAudit::getArticleId)
                .collect(Collectors.toList());
        
        if (articleIds.isEmpty()) {
            Page<ArticleVO> emptyPage = new Page<>(auditResult.getCurrent(), auditResult.getSize(), 0);
            emptyPage.setRecords(new ArrayList<>());
            return emptyPage;
        }
        
        // 查询文章详情
        List<Article> articles = articleMapper.selectList(
                new LambdaQueryWrapper<Article>()
                        .in(Article::getId, articleIds)
                        .eq(Article::getDeleted, 0));
        
        List<ArticleVO> voList = articles.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        Page<ArticleVO> voPage = new Page<>(auditResult.getCurrent(), auditResult.getSize(), auditResult.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public Page<ArticleVO> getAllArticleList(ArticleListQuery query) {
        Page<Article> page = new Page<>(query.getPageNum(), query.getPageSize());
        
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getDeleted, 0);
        
        // 筛选条件
        if (query.getTitle() != null && !query.getTitle().isBlank()) {
            wrapper.like(Article::getTitle, query.getTitle());
        }
        if (query.getStatus() != null) {
            wrapper.eq(Article::getStatus, query.getStatus());
        }
        if (query.getCategoryId() != null) {
            wrapper.eq(Article::getCategoryId, query.getCategoryId());
        }
        
        wrapper.orderByDesc(Article::getCreateTime);
        
        Page<Article> resultPage = articleMapper.selectPage(page, wrapper);
        
        List<ArticleVO> voList = resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        Page<ArticleVO> voPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void forcePublishArticle(Long articleId, Integer status, Long adminId) {
        Article article = articleMapper.selectById(articleId);
        if (article == null || article.getDeleted() == 1) {
            throw new BusinessException("文章不存在");
        }

        article.setStatus(status);
        article.setUpdateTime(new Date());
        articleMapper.updateById(article);

        log.info("管理员强制{}文章：articleId={}, adminId={}", 
                status == 1 ? "发布" : "下架", articleId, adminId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminDeleteArticle(Long articleId) {
        Article article = articleMapper.selectById(articleId);
        if (article == null || article.getDeleted() == 1) {
            throw new BusinessException("文章不存在");
        }

        article.setDeleted(1);
        article.setUpdateTime(new Date());
        articleMapper.updateById(article);

        log.info("管理员删除文章：articleId={}", articleId);
    }

    @Override
    public ArticleAuditVO getAuditRecord(Long articleId) {
        ArticleAudit audit = auditMapper.selectOne(
                new LambdaQueryWrapper<ArticleAudit>()
                        .eq(ArticleAudit::getArticleId, articleId)
                        .orderByDesc(ArticleAudit::getCreateTime)
                        .last("LIMIT 1"));
        
        if (audit == null) {
            throw new BusinessException("该文章没有审核记录");
        }
        
        return convertAuditToVO(audit);
    }

    // ==================== 前台端接口 ====================

    @Override
    public ArticleVO getPortalArticleDetail(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null || article.getDeleted() == 1 || article.getStatus() != 1) {
            throw new BusinessException("文章不存在或未发布");
        }
        return convertToVO(article);
    }

    @Override
    public Page<ArticleVO> getPortalArticleList(ArticleListQuery query) {
        Page<Article> page = new Page<>(query.getPageNum(), query.getPageSize());
        
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getDeleted, 0)
               .eq(Article::getStatus, 1); // 只查询已发布
        
        // 筛选条件
        if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
            wrapper.and(w -> w.like(Article::getTitle, query.getKeyword())
                           .or().like(Article::getSummary, query.getKeyword()));
        }
        if (query.getCategoryId() != null) {
            wrapper.eq(Article::getCategoryId, query.getCategoryId());
        }
        
        wrapper.orderByDesc(Article::getCreateTime);
        
        Page<Article> resultPage = articleMapper.selectPage(page, wrapper);
        
        List<ArticleVO> voList = resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        Page<ArticleVO> voPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementViewCount(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null || article.getDeleted() == 1 || article.getStatus() != 1) {
            throw new BusinessException("文章不存在或未发布");
        }

        article.setViewCount(article.getViewCount() + 1);
        articleMapper.updateById(article);
        
        log.debug("文章浏览量 +1：id={}, viewCount={}", id, article.getViewCount());
    }

    @Override
    public List<ArticleVO> getHotArticles(Integer limit) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getDeleted, 0)
               .eq(Article::getStatus, 1)
               .orderByDesc(Article::getViewCount)
               .last("LIMIT " + limit);
        
        List<Article> articles = articleMapper.selectList(wrapper);
        return articles.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public List<CategoryVO> getCategoryList() {
        LambdaQueryWrapper<ArticleCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleCategory::getStatus, 0)
               .eq(ArticleCategory::getDeleted, 0)
               .orderByAsc(ArticleCategory::getSortOrder);
        
        List<ArticleCategory> categories = categoryMapper.selectList(wrapper);
        return categories.stream().map(this::convertCategoryToVO).collect(Collectors.toList());
    }

    @Override
    public List<TagVO> getTagList() {
        LambdaQueryWrapper<ArticleTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleTag::getStatus, 0)
               .eq(ArticleTag::getDeleted, 0)
               .orderByDesc(ArticleTag::getCreateTime);
        
        List<ArticleTag> tags = tagMapper.selectList(wrapper);
        return tags.stream().map(this::convertTagToVO).collect(Collectors.toList());
    }

    @Override
    public Page<TagVO> getTagListByPage(TagQuery query) {
        Page<ArticleTag> page = new Page<>(query.getPageNum(), query.getPageSize());
        
        LambdaQueryWrapper<ArticleTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleTag::getDeleted, 0);
        
        // 关键词模糊搜索
        if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
            wrapper.like(ArticleTag::getTagName, query.getKeyword());
        }
        
        // 状态筛选
        if (query.getStatus() != null) {
            wrapper.eq(ArticleTag::getStatus, query.getStatus());
        }
        
        wrapper.orderByDesc(ArticleTag::getCreateTime);
        
        Page<ArticleTag> resultPage = tagMapper.selectPage(page, wrapper);
        
        // 转换为 VO 并统计使用次数
        List<TagVO> voList = resultPage.getRecords().stream()
                .map(tag -> {
                    TagVO vo = convertTagToVO(tag);
                    // 统计使用该标签的文章数量
                    long count = tagRefMapper.selectCount(
                            new LambdaQueryWrapper<ArticleTagRef>()
                                    .eq(ArticleTagRef::getTagId, tag.getId()));
                    vo.setUsageCount(count);
                    return vo;
                })
                .collect(Collectors.toList());
        
        Page<TagVO> voPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public List<TagVO> searchTags(String keyword, Integer limit) {
        if (keyword == null || keyword.isBlank()) {
            return new ArrayList<>();
        }
        
        LambdaQueryWrapper<ArticleTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleTag::getDeleted, 0)
               .eq(ArticleTag::getStatus, 0)
               .like(ArticleTag::getTagName, keyword)
               .orderByDesc(ArticleTag::getCreateTime)
               .last("LIMIT " + (limit != null ? limit : 10));
        
        List<ArticleTag> tags = tagMapper.selectList(wrapper);
        return tags.stream().map(this::convertTagToVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TagVO createTag(TagCreateDTO dto) {
        // 1. 检查标签是否已存在
        ArticleTag existing = tagMapper.selectOne(
                new LambdaQueryWrapper<ArticleTag>()
                        .eq(ArticleTag::getTagName, dto.getTagName())
                        .eq(ArticleTag::getDeleted, 0));
        
        if (existing != null) {
            throw new BusinessException("标签已存在");
        }
        
        // 2. 创建标签
        ArticleTag tag = new ArticleTag();
        tag.setTagName(dto.getTagName());
        tag.setStatus(dto.getStatus());
        tag.setDeleted(0);
        tag.setCreateTime(new Date());
        tag.setUpdateTime(new Date());
        tagMapper.insert(tag);
        
        log.info("创建标签成功：id={}, tagName={}", tag.getId(), tag.getTagName());
        return convertTagToVO(tag);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTagStatus(Long tagId, Integer status) {
        // 1. 查询标签是否存在
        ArticleTag tag = tagMapper.selectById(tagId);
        if (tag == null) {
            throw new BusinessException("标签不存在");
        }
        
        // 2. 更新状态
        tag.setStatus(status);
        tagMapper.updateById(tag);
        
        log.info("标签状态已更新：tagId={}, status={}", tagId, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTag(Long tagId) {
        ArticleTag tag = tagMapper.selectById(tagId);
        if (tag == null || tag.getDeleted() == 1) {
            throw new BusinessException("标签不存在");
        }
        
        // 检查是否有文章使用该标签
        long count = tagRefMapper.selectCount(
                new LambdaQueryWrapper<ArticleTagRef>()
                        .eq(ArticleTagRef::getTagId, tagId));
        
        if (count > 0) {
            throw new BusinessException("标签已被使用，无法删除");
        }
        
        // 逻辑删除
        tag.setDeleted(1);
        tag.setUpdateTime(new Date());
        tagMapper.updateById(tag);
        log.info("删除标签成功：tagId={}", tagId);
    }

    // ==================== 辅助方法 ====================

    private void saveTagRefs(Long articleId, List<Long> tagIds) {
        List<ArticleTagRef> refs = new ArrayList<>();
        for (Long tagId : tagIds) {
            ArticleTag tag = tagMapper.selectById(tagId);
            if (tag != null && tag.getDeleted() == 0) {
                ArticleTagRef ref = new ArticleTagRef();
                ref.setArticleId(articleId);
                ref.setTagId(tagId);
                refs.add(ref);
            }
        }
        if (!refs.isEmpty()) {
            tagRefMapper.insertBatch(refs);
        }
    }

    private ArticleVO convertToVO(Article article) {
        ArticleVO vo = new ArticleVO();
        vo.setId(article.getId());
        vo.setTitle(article.getTitle());
        vo.setSubTitle(article.getSubTitle());
        vo.setCoverImage(article.getCoverImage());
        vo.setSummary(article.getSummary());
        vo.setContent(article.getContent());
        vo.setAuthorId(article.getAuthorId());
        vo.setCategoryId(article.getCategoryId());
        vo.setStatus(article.getStatus());
        vo.setViewCount(article.getViewCount());
        vo.setLikeCount(article.getLikeCount());
        vo.setCreateTime(article.getCreateTime());
        vo.setUpdateTime(article.getUpdateTime());

        // 查询作者名称
        if (article.getAuthorId() != null) {
            SysUser author = userMapper.selectById(article.getAuthorId());
            if (author != null) {
                vo.setAuthorName(author.getUsername());
            }
        }

        // 查询分类名称
        if (article.getCategoryId() != null) {
            ArticleCategory category = categoryMapper.selectById(article.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getCategoryName());
            }
        }

        // 查询标签
        List<ArticleTagRef> refs = tagRefMapper.selectList(
                new LambdaQueryWrapper<ArticleTagRef>().eq(ArticleTagRef::getArticleId, article.getId()));
        if (!refs.isEmpty()) {
            List<Long> tagIds = refs.stream().map(ArticleTagRef::getTagId).collect(Collectors.toList());
            vo.setTagIds(tagIds);
            
            List<ArticleTag> tags = tagMapper.selectBatchIds(tagIds);
            vo.setTagNames(tags.stream().map(ArticleTag::getTagName).collect(Collectors.toList()));
        }

        // 查询审核信息
        ArticleAudit audit = auditMapper.selectOne(
                new LambdaQueryWrapper<ArticleAudit>()
                        .eq(ArticleAudit::getArticleId, article.getId())
                        .orderByDesc(ArticleAudit::getCreateTime)
                        .last("LIMIT 1"));
        
        if (audit != null) {
            vo.setAuditStatus(audit.getAuditStatus());
            vo.setAuditRemark(audit.getAuditRemark());
            vo.setAuditUserId(audit.getAuditUserId());
            vo.setAuditTime(audit.getAuditTime());
            
            // 查询审核人名称
            if (audit.getAuditUserId() != null) {
                SysUser auditor = userMapper.selectById(audit.getAuditUserId());
                if (auditor != null) {
                    vo.setAuditUserName(auditor.getUsername());
                }
            }
        }

        return vo;
    }

    private ArticleAuditVO convertAuditToVO(ArticleAudit audit) {
        ArticleAuditVO vo = new ArticleAuditVO();
        vo.setId(audit.getId());
        vo.setArticleId(audit.getArticleId());
        vo.setAuditStatus(audit.getAuditStatus());
        vo.setAuditRemark(audit.getAuditRemark());
        vo.setAuditUserId(audit.getAuditUserId());
        vo.setAuditTime(audit.getAuditTime());
        vo.setSubmitTime(audit.getSubmitTime());

        // 查询审核人名称
        if (audit.getAuditUserId() != null) {
            SysUser auditor = userMapper.selectById(audit.getAuditUserId());
            if (auditor != null) {
                vo.setAuditUserName(auditor.getUsername());
            }
        }

        return vo;
    }

    private CategoryVO convertCategoryToVO(ArticleCategory category) {
        CategoryVO vo = new CategoryVO();
        vo.setId(category.getId());
        vo.setCategoryName(category.getCategoryName());
        vo.setDescription(category.getDescription());
        vo.setSortOrder(category.getSortOrder());
        return vo;
    }

    private TagVO convertTagToVO(ArticleTag tag) {
        TagVO vo = new TagVO();
        vo.setId(tag.getId());
        vo.setTagName(tag.getTagName());
        vo.setStatus(tag.getStatus());
        vo.setCreateTime(tag.getCreateTime());
        // usageCount 在调用时单独统计
        return vo;
    }
}
