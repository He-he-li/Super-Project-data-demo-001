package com.example.data_demo_002.modules.article.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.data_demo_002.common.result.Result;
import com.example.data_demo_002.modules.article.dao.ArticleListQuery;
import com.example.data_demo_002.modules.article.dao.ArticleVO;
import com.example.data_demo_002.modules.article.dao.CategoryVO;
import com.example.data_demo_002.modules.article.dao.TagVO;
import com.example.data_demo_002.modules.article.service.ArticleBusinessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

        import java.util.List;

@Slf4j
@Tag(name = "🌐 前台端 - 文章浏览", description = "公开的文章浏览接口（无需登录）")
@RestController
@RequestMapping("/article/portal")
@RequiredArgsConstructor
public class ArticlePortalController {

    private final ArticleBusinessService articleService;

    @Operation(summary = "文章详情", description = "查询已发布的文章详情")
    @GetMapping("/{id}")
    public Result<ArticleVO> getArticleDetail(@PathVariable Long id) {
        log.info("前台查看文章详情：id={}", id);
        ArticleVO vo = articleService.getPortalArticleDetail(id);
        return Result.success(vo);
    }

    @Operation(summary = "文章列表", description = "分页查询已发布的文章列表")
    @GetMapping("/list")
    public Result<Page<ArticleVO>> getArticleList(ArticleListQuery query) {
        log.info("前台查询文章列表");
        Page<ArticleVO> page = articleService.getPortalArticleList(query);
        return Result.success(page);
    }

    @Operation(summary = "增加浏览量", description = "文章浏览量 +1")
    @PostMapping("/view/{id}")
    public Result<Void> incrementViewCount(@PathVariable Long id) {
        log.debug("文章浏览量 +1：id={}", id);
        articleService.incrementViewCount(id);
        return Result.success();
    }

    @Operation(summary = "热门文章", description = "获取浏览量最高的前 N 篇文章")
    @GetMapping("/hot")
    public Result<List<ArticleVO>> getHotArticles(
            @Parameter(description = "数量限制", example = "10")
            @RequestParam(defaultValue = "10") Integer limit) {
        log.info("获取热门文章：limit={}", limit);
        List<ArticleVO> list = articleService.getHotArticles(limit);
        return Result.success(list);
    }

    @Operation(summary = "分类列表", description = "获取所有启用的分类")
    @GetMapping("/categories")
    public Result<List<CategoryVO>> getCategoryList() {
        log.info("获取分类列表");
        List<CategoryVO> list = articleService.getCategoryList();
        return Result.success(list);
    }

    @Operation(summary = "标签列表", description = "获取所有标签")
    @GetMapping("/tags")
    public Result<List<TagVO>> getTagList() {
        log.info("获取标签列表");
        List<TagVO> list = articleService.getTagList();
        return Result.success(list);
    }
}
