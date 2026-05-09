package com.example.data_demo_002.modules.article.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.data_demo_002.modules.article.dao.*;

import java.util.List;

/**
 * 文章业务服务接口
 */
public interface ArticleBusinessService {



    // ==================== 用户端接口 ====================

    /**
     * 用户投稿
     */
    ArticleVO submitArticle(ArticleDTO dto, Long authorId);

    /**
     * 用户编辑自己的文章
     */
    ArticleVO updateUserArticle(ArticleDTO dto, Long authorId);

    /**
     * 用户删除自己的文章
     */
    void deleteUserArticle(Long articleId, Long authorId);

    /**
     * 撤回投稿（仅限待审核状态）
     */
    void withdrawArticle(Long articleId, Long authorId);

    /**
     * 用户查看自己的文章详情
     */
    ArticleVO getUserArticleDetail(Long articleId, Long authorId);

    /**
     * 用户查看自己的文章列表
     */
    Page<ArticleVO> getUserArticleList(Long authorId, ArticleListQuery query);

    /**
     * 用户查看文章统计
     */
    ArticleStatsVO getUserArticleStats(Long authorId);

    // ==================== 管理员端接口 ====================

    /**
     * 查看待审核文章详情
     */
    ArticleVO getAuditArticleDetail(Long articleId);

    /**
     * 审核文章
     */
    ArticleAuditVO auditArticle(ArticleAuditDTO dto, Long adminId);

    /**
     * 批量审核文章
     */
    List<ArticleAuditVO> batchAuditArticle(List<BatchAuditItemDTO> dtos, Long adminId);

    /**
     * 待审核文章列表
     */
    Page<ArticleVO> getAuditArticleList(ArticleListQuery query);

    /**
     * 所有文章列表（管理员）
     */
    Page<ArticleVO> getAllArticleList(ArticleListQuery query);

    /**
     * 强制发布/下架文章
     */
    void forcePublishArticle(Long articleId, Integer status, Long adminId);

    /**
     * 管理员删除文章
     */
    void adminDeleteArticle(Long articleId);

    /**
     * 获取审核记录
     */
    ArticleAuditVO getAuditRecord(Long articleId);

    // ==================== 前台端接口 ====================

    /**
     * 获取文章详情（前台）
     */
    ArticleVO getPortalArticleDetail(Long id);

    /**
     * 分页查询文章列表（前台）
     */
    Page<ArticleVO> getPortalArticleList(ArticleListQuery query);

    /**
     * 增加浏览量
     */
    void incrementViewCount(Long id);

    /**
     * 获取热门文章
     */
    List<ArticleVO> getHotArticles(Integer limit);

    // ==================== 分类与标签接口 ====================

    /**
     * 获取分类列表
     */
    List<CategoryVO> getCategoryList();

    /**
     * 获取标签列表（所有启用的标签）
     */
    List<TagVO> getTagList();

    /**
     * 分页查询标签（支持模糊搜索）
     */
    com.baomidou.mybatisplus.extension.plugins.pagination.Page<TagVO> getTagListByPage(TagQuery query);

    /**
     * 模糊搜索标签（返回前 N 条结果）
     */
    List<TagVO> searchTags(String keyword, Integer limit);

    /**
     * 创建标签
     */
    TagVO createTag(TagCreateDTO dto);

    /**
     * 更新标签状态
     */
    void updateTagStatus(Long tagId, Integer status);

    /**
     * 删除标签
     */
    void deleteTag(Long tagId);
}
