package com.example.data_demo_002.modules.article.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.data_demo_002.common.result.Result;
import com.example.data_demo_002.common.util.permissionUtil.HasPermission;
import com.example.data_demo_002.common.util.Jwt.UserContext;
import com.example.data_demo_002.modules.article.dao.*;
import com.example.data_demo_002.modules.article.service.ArticleBusinessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "🔧 管理员端 - 文章审核", description = "管理员审核文章、管理所有文章")
@RestController
@RequestMapping("/article/admin")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ArticleAdminController {

    private final ArticleBusinessService articleService;

    @Operation(summary = "查看待审核文章详情", description = "管理员查看待审核文章的完整内容")
    @GetMapping("/audit/{id}")
    @HasPermission("article:audit:view")
    public Result<ArticleVO> getAuditArticleDetail(@PathVariable Long id) {
        log.info("管理员查看待审核文章：id={}", id);
        ArticleVO vo = articleService.getAuditArticleDetail(id);
        return Result.success(vo);
    }

    @Operation(summary = "审核文章", description = "对文章进行审核（通过/拒绝）")
    @PutMapping("/audit")
    @HasPermission("article:audit:approve")
    public Result<ArticleAuditVO> auditArticle(@Valid @RequestBody ArticleAuditDTO dto) {
        Long adminId = UserContext.getUserId();
        log.info("管理员审核文章：articleId={}, adminId={}, result={}",
                dto.getArticleId(), adminId, dto.getAuditStatus());
        ArticleAuditVO audit = articleService.auditArticle(dto, adminId);
        return Result.success(audit);
    }

    @Operation(summary = "批量审核文章", description = "一次性审核多篇文章")
    @PutMapping("/audit/batch")
    @HasPermission("article:audit:approve")
    public Result<List<ArticleAuditVO>> batchAuditArticle(@Valid @RequestBody List<BatchAuditItemDTO> dtos) {
        Long adminId = UserContext.getUserId();
        log.info("管理员批量审核文章：count={}, adminId={}", dtos.size(), adminId);
        List<ArticleAuditVO> results = articleService.batchAuditArticle(dtos, adminId);
        return Result.success(results);
    }

    @Operation(summary = "待审核文章列表", description = "分页查看所有待审核的文章")
    @GetMapping("/audit-list")
    @HasPermission("article:audit:view")
    public Result<Page<ArticleVO>> getAuditArticleList(ArticleListQuery query) {
        log.info("查看待审核文章列表");
        Page<ArticleVO> page = articleService.getAuditArticleList(query);
        return Result.success(page);
    }

    @Operation(summary = "所有文章列表", description = "分页查看所有文章（支持筛选）")
    @GetMapping("/all-list")
    @HasPermission("article:list:view")
    public Result<Page<ArticleVO>> getAllArticleList(ArticleListQuery query) {
        log.info("查看所有文章列表");
        Page<ArticleVO> page = articleService.getAllArticleList(query);
        return Result.success(page);
    }

    @Operation(summary = "强制发布/下架文章", description = "管理员强制改变文章状态")
    @PutMapping("/publish/{id}")
    @HasPermission("article:list:publish")
    public Result<Void> forcePublishArticle(
            @PathVariable Long id,
            @Parameter(description = "状态：1-发布，2-下架") @RequestParam Integer status) {
        Long adminId = UserContext.getUserId();
        log.info("管理员强制{}文章：id={}, adminId={}", status == 1 ? "发布" : "下架", id, adminId);
        articleService.forcePublishArticle(id, status, adminId);
        return Result.success();
    }

    @Operation(summary = "管理员删除文章", description = "删除任意文章（逻辑删除）")
    @DeleteMapping("/{id}")
    @HasPermission("article:list:delete")
    public Result<Void> adminDeleteArticle(@PathVariable Long id) {
        log.info("管理员删除文章：id={}", id);
        articleService.adminDeleteArticle(id);
        return Result.success();
    }

    @Operation(summary = "查看审核记录", description = "查看文章的审核历史记录")
    @GetMapping("/{id}/audit")
    @HasPermission("article:audit:view")
    public Result<ArticleAuditVO> getAuditRecord(@PathVariable Long id) {
        log.info("查看审核记录：articleId={}", id);
        ArticleAuditVO audit = articleService.getAuditRecord(id);
        return Result.success(audit);
    }
}
