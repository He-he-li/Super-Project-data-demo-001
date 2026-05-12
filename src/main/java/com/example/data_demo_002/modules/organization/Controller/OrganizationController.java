package com.example.data_demo_002.modules.organization.Controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.data_demo_002.common.result.Result;
import com.example.data_demo_002.common.util.permissionUtil.HasPermission;
import com.example.data_demo_002.modules.organization.Service.OrganizationService;
import com.example.data_demo_002.modules.organization.dao.OrganizationDTO;
import com.example.data_demo_002.modules.organization.dao.OrganizationVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/system/organizations")
@RequiredArgsConstructor
@Tag(name = "🏛️ 单位管理", description = "单位配置管理（支持系统级单位和多租户）")
public class OrganizationController {

    private final OrganizationService organizationService;

    @Operation(summary = "系统级单位列表", description = "获取所有系统级单位")
    @GetMapping("/system-level")
    @HasPermission("system:org:system:view")
    public Result<List<OrganizationVO>> listSystemOrganizations() {
        List<OrganizationVO> list = organizationService.listSystemOrganizations();
        return Result.success(list);
    }

    @Operation(summary = "分页查询单位", description = "支持按名称、类型筛选")
    @GetMapping("/list")
    @HasPermission("system:org:view")
    public Result<Page<OrganizationVO>> listOrganizations(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String orgName,
            @RequestParam(required = false) Integer isSystem) {

        Page<OrganizationVO> page = organizationService.listOrganizations(pageNum, pageSize, orgName, isSystem);
        return Result.success(page);
    }

    @Operation(summary = "获取单位详情")
    @GetMapping("/{orgId}")
    @HasPermission("system:org:view")
    public Result<OrganizationVO> getOrganizationDetail(@PathVariable Long orgId) {
        OrganizationVO vo = organizationService.getOrganizationDetail(orgId);
        return Result.success(vo);
    }

    @Operation(summary = "创建单位", description = "可配置为系统级单位")
    @PostMapping
    @HasPermission("system:org:create")
    public Result<Void> createOrganization(@Valid @RequestBody OrganizationDTO dto) {
        organizationService.createOrganization(dto);
        return Result.success(null, "创建成功");
    }

    @Operation(summary = "修改单位", description = "可修改系统级标识")
    @PutMapping("/{orgId}")
    @HasPermission("system:org:edit")
    public Result<Void> updateOrganization(
            @PathVariable Long orgId,
            @Valid @RequestBody OrganizationDTO dto) {
        organizationService.updateOrganization(orgId, dto);
        return Result.success(null, "修改成功");
    }

    @Operation(summary = "删除单位", description = "系统级单位不可删除")
    @DeleteMapping("/{orgId}")
    @HasPermission("system:org:delete")
    public Result<Void> deleteOrganization(@PathVariable Long orgId) {
        organizationService.deleteOrganization(orgId);
        return Result.success(null, "删除成功");
    }

    @Operation(summary = "单位树形结构", description = "获取完整的单位树")
    @GetMapping("/tree")
    @HasPermission("system:org:view")
    public Result<List<OrganizationVO>> getOrganizationTree() {
        List<OrganizationVO> tree = organizationService.getOrganizationTree();
        return Result.success(tree);
    }
}