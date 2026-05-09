package com.example.data_demo_002.modules.article.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.data_demo_002.common.result.Result;
import com.example.data_demo_002.common.util.permissionUtil.HasPermission;
import com.example.data_demo_002.common.util.Jwt.UserContext;
import com.example.data_demo_002.modules.article.dao.ArticleAuditVO;
import com.example.data_demo_002.modules.article.dao.ArticleDTO;
import com.example.data_demo_002.modules.article.dao.ArticleListQuery;
import com.example.data_demo_002.modules.article.dao.ArticleStatsVO;
import com.example.data_demo_002.modules.article.dao.ArticleVO;
import com.example.data_demo_002.modules.article.service.ArticleBusinessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "📝 用户端 - 文章管理", description = "用户投稿、编辑、查看自己的文章")
@RestController
@RequestMapping("/article/user")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ArticleUserController {

    private final ArticleBusinessService articleService;

    @Operation(summary = "用户投稿", description = "创建新文章并提交审核")
    @PostMapping
    @HasPermission("article:list:publish")
    public Result<ArticleVO> submitArticle(@Valid @RequestBody ArticleDTO dto) {
        Long authorId = UserContext.getUserId();
        log.info("用户投稿：title={}, authorId={}", dto.getTitle(), authorId);
        ArticleVO vo = articleService.submitArticle(dto, authorId);
        return Result.success(vo);
    }

    @Operation(summary = "编辑文章", description = "修改自己的文章内容")
    @PutMapping
    @HasPermission("article:list:edit")
    public Result<ArticleVO> updateArticle(@Valid @RequestBody ArticleDTO dto) {
        Long authorId = UserContext.getUserId();
        log.info("用户编辑文章：id={}", dto.getId());
        ArticleVO vo = articleService.updateUserArticle(dto, authorId);
        return Result.success(vo);
    }

    @Operation(summary = "删除文章", description = "逻辑删除自己的文章")
    @DeleteMapping("/{id}")
    @HasPermission("article:list:delete")
    public Result<Void> deleteArticle(@PathVariable Long id) {
        Long authorId = UserContext.getUserId();
        log.info("用户删除文章：id={}, authorId={}", id, authorId);
        articleService.deleteUserArticle(id, authorId);
        return Result.success();
    }

    @Operation(summary = "撤回投稿", description = "撤回待审核的文章（仅限审核状态为 0 时）")
    @PostMapping("/withdraw/{id}")
    @HasPermission("article:list:edit")
    public Result<Void> withdrawArticle(@PathVariable Long id) {
        Long authorId = UserContext.getUserId();
        log.info("用户撤回投稿：id={}, authorId={}", id, authorId);
        articleService.withdrawArticle(id, authorId);
        return Result.success();
    }

    @Operation(summary = "查看文章详情", description = "查看自己的文章详情")
    @GetMapping("/{id}")
    @HasPermission("article:list:view")
    public Result<ArticleVO> getArticleDetail(@PathVariable Long id) {
        Long authorId = UserContext.getUserId();
        log.info("查看文章详情：id={}", id);
        ArticleVO vo = articleService.getUserArticleDetail(id, authorId);
        return Result.success(vo);
    }

    @Operation(summary = "查看文章列表", description = "分页查看自己的文章列表")
    @GetMapping("/list")
    @HasPermission("article:list:view")
    public Result<Page<ArticleVO>> getArticleList(ArticleListQuery query) {
        Long authorId = UserContext.getUserId();
        log.info("查看文章列表：authorId={}", authorId);
        Page<ArticleVO> page = articleService.getUserArticleList(authorId, query);
        return Result.success(page);
    }

    @Operation(summary = "查看文章统计", description = "查看自己的文章统计数据")
    @GetMapping("/stats")
    @HasPermission("article:list:view")
    public Result<ArticleStatsVO> getArticleStats() {
        Long authorId = UserContext.getUserId();
        log.info("查看文章统计：authorId={}", authorId);
        ArticleStatsVO stats = articleService.getUserArticleStats(authorId);
        return Result.success(stats);
    }

    @Operation(summary = "查看审核记录", description = "查看文章的审核记录")
    @GetMapping("/{id}/audit")
    @HasPermission("article:list:view")
    public Result<ArticleAuditVO> getAuditRecord(@PathVariable Long id) {
        log.info("查看审核记录：articleId={}", id);
        ArticleAuditVO audit = articleService.getAuditRecord(id);
        return Result.success(audit);
    }
}
