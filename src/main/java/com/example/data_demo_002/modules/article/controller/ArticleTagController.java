package com.example.data_demo_002.modules.article.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.data_demo_002.common.result.Result;
import com.example.data_demo_002.common.util.permissionUtil.HasPermission;
import com.example.data_demo_002.modules.article.dao.TagCreateDTO;
import com.example.data_demo_002.modules.article.dao.TagQuery;
import com.example.data_demo_002.modules.article.dao.TagVO;
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
@Tag(name = "🏷️ 标签管理", description = "文章标签的增删改查")
@RestController
@RequestMapping("/article/tag")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ArticleTagController {

    private final ArticleBusinessService articleService;

    @Operation(summary = "模糊搜索标签", description = "根据关键词模糊搜索标签")
    @GetMapping("/search")
    @HasPermission("article:tag:view")
    public Result<List<TagVO>> searchTags(
            @Parameter(description = "搜索关键词") @RequestParam String keyword,
            @Parameter(description = "返回数量限制", example = "10") 
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        log.info("模糊搜索标签：keyword={}, limit={}", keyword, limit);
        List<TagVO> tags = articleService.searchTags(keyword, limit);
        return Result.success(tags);
    }

    @Operation(summary = "分页查询标签", description = "分页查询所有标签")
    @GetMapping("/list")
    @HasPermission("article:tag:view")
    public Result<Page<TagVO>> getTagList(TagQuery query) {
        log.info("分页查询标签");
        Page<TagVO> page = articleService.getTagListByPage(query);
        return Result.success(page);
    }

    @Operation(summary = "创建标签", description = "创建新的文章标签")
    @PostMapping
    @HasPermission("article:tag:add")
    public Result<TagVO> createTag(@Valid @RequestBody TagCreateDTO dto) {
        log.info("创建标签：tagName={}", dto.getTagName());
        TagVO tag = articleService.createTag(dto);
        return Result.success(tag);
    }

    @Operation(summary = "更新标签状态", description = "禁用或启用标签")
    @PutMapping("/status/{id}")
    @HasPermission("article:tag:edit")
    public Result<Void> updateTagStatus(
            @PathVariable Long id,
            @Parameter(description = "标签状态：0-启用，1-禁用", required = true, example = "0")
            @RequestParam Integer status) {
        log.info("更新标签状态：id={}, status={}", id, status);
        articleService.updateTagStatus(id, status);
        return Result.success(null, "状态更新成功");
    }

    @Operation(summary = "删除标签", description = "删除标签（逻辑删除）")
    @DeleteMapping("/{id}")
    @HasPermission("article:tag:delete")
    public Result<Void> deleteTag(@PathVariable Long id) {
        log.info("删除标签：id={}", id);
        articleService.deleteTag(id);
        return Result.success(null, "标签删除成功");
    }
}


