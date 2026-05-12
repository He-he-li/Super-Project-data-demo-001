package com.example.data_demo_002.modules.organization.Service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.data_demo_002.common.base.domain.SysOrganization;
import com.example.data_demo_002.common.base.mapper.SysOrganizationMapper;
import com.example.data_demo_002.common.exception.BusinessException;
import com.example.data_demo_002.modules.organization.Service.OrganizationService;
import com.example.data_demo_002.modules.organization.dao.OrganizationDTO;
import com.example.data_demo_002.modules.organization.dao.OrganizationVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {

    private final SysOrganizationMapper organizationMapper;

    @Override
    public boolean isSystemOrganization(Long orgId) {
        if (orgId == null) {
            return false;
        }
        SysOrganization org = organizationMapper.selectById(orgId);
        return org != null && Integer.valueOf(1).equals(org.getIsSystem());
    }

    @Override
    public List<OrganizationVO> listSystemOrganizations() {
        List<SysOrganization> organizations = organizationMapper.selectList(
            new LambdaQueryWrapper<SysOrganization>()
                .eq(SysOrganization::getIsSystem, 1)
                .eq(SysOrganization::getStatus, 0)
                .eq(SysOrganization::getDeleted, 0)
                .orderByAsc(SysOrganization::getSortOrder)
        );

        return organizations.stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
    }

    @Override
    public Page<OrganizationVO> listOrganizations(int pageNum, int pageSize, String orgName, Integer isSystem) {
        Page<SysOrganization> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<SysOrganization> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysOrganization::getDeleted, 0);

        if (orgName != null && !orgName.trim().isEmpty()) {
            wrapper.like(SysOrganization::getOrgName, orgName);
        }

        if (isSystem != null) {
            wrapper.eq(SysOrganization::getIsSystem, isSystem);
        }

        wrapper.orderByAsc(SysOrganization::getSortOrder)
               .orderByDesc(SysOrganization::getCreateTime);

        organizationMapper.selectPage(page, wrapper);

        Page<OrganizationVO> resultPage = new Page<>(pageNum, pageSize);
        resultPage.setTotal(page.getTotal());
        resultPage.setCurrent(page.getCurrent());
        resultPage.setSize(page.getSize());
        resultPage.setPages(page.getPages());

        List<OrganizationVO> voList = page.getRecords().stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());

        resultPage.setRecords(voList);
        return resultPage;
    }

    @Override
    public OrganizationVO getOrganizationDetail(Long orgId) {
        SysOrganization org = organizationMapper.selectById(orgId);
        if (org == null || Integer.valueOf(1).equals(org.getDeleted())) {
            throw new BusinessException("单位不存在");
        }
        return convertToVO(org);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createOrganization(OrganizationDTO dto) {
        long count = organizationMapper.selectCount(
            new LambdaQueryWrapper<SysOrganization>()
                .eq(SysOrganization::getOrgCode, dto.getOrgCode())
                .eq(SysOrganization::getDeleted, 0)
        );
        if (count > 0) {
            throw new BusinessException("单位编码已存在");
        }

        if (dto.getParentId() != null && dto.getParentId() > 0) {
            SysOrganization parent = organizationMapper.selectById(dto.getParentId());
            if (parent == null || Integer.valueOf(1).equals(parent.getDeleted())) {
                throw new BusinessException("父级单位不存在");
            }
        }

        SysOrganization org = new SysOrganization();
        BeanUtils.copyProperties(dto, org);

        if (org.getOrgType() == null) {
            org.setOrgType(2);
        }
        if (org.getIsSystem() == null) {
            org.setIsSystem(0);
        }
        if (org.getParentId() == null) {
            org.setParentId(0L);
        }
        if (org.getStatus() == null) {
            org.setStatus(0);
        }
        if (org.getDeleted() == null) {
            org.setDeleted(0);
        }
        if (org.getLevel() == null) {
            org.setLevel(1);
        }

        org.setCreateTime(new Date());
        org.setUpdateTime(new Date());

        organizationMapper.insert(org);
        log.info("创建单位成功: {}", org.getOrgName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOrganization(Long orgId, OrganizationDTO dto) {
        SysOrganization existing = organizationMapper.selectById(orgId);
        if (existing == null || Integer.valueOf(1).equals(existing.getDeleted())) {
            throw new BusinessException("单位不存在");
        }

        if (!dto.getOrgCode().equals(existing.getOrgCode())) {
            long count = organizationMapper.selectCount(
                new LambdaQueryWrapper<SysOrganization>()
                    .eq(SysOrganization::getOrgCode, dto.getOrgCode())
                    .ne(SysOrganization::getId, orgId)
                    .eq(SysOrganization::getDeleted, 0)
            );
            if (count > 0) {
                throw new BusinessException("单位编码已存在");
            }
        }

        SysOrganization org = new SysOrganization();
        BeanUtils.copyProperties(dto, org);
        org.setId(orgId);
        org.setUpdateTime(new Date());

        organizationMapper.updateById(org);
        log.info("更新单位成功: {}", org.getOrgName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOrganization(Long orgId) {
        SysOrganization org = organizationMapper.selectById(orgId);
        if (org == null || Integer.valueOf(1).equals(org.getDeleted())) {
            throw new BusinessException("单位不存在");
        }

        if (Integer.valueOf(1).equals(org.getIsSystem())) {
            throw new BusinessException("系统级单位不允许删除");
        }

        long childCount = organizationMapper.selectCount(
            new LambdaQueryWrapper<SysOrganization>()
                .eq(SysOrganization::getParentId, orgId)
                .eq(SysOrganization::getDeleted, 0)
        );
        if (childCount > 0) {
            throw new BusinessException("该单位下还有子单位，无法删除");
        }

        org.setDeleted(1);
        org.setUpdateTime(new Date());
        organizationMapper.updateById(org);

        log.info("删除单位成功: {}", org.getOrgName());
    }

    @Override
    public List<OrganizationVO> getOrganizationTree() {
        List<SysOrganization> allOrgs = organizationMapper.selectList(
            new LambdaQueryWrapper<SysOrganization>()
                .eq(SysOrganization::getDeleted, 0)
                .eq(SysOrganization::getStatus, 0)
                .orderByAsc(SysOrganization::getSortOrder)
        );

        List<OrganizationVO> allVos = allOrgs.stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());

        return buildTree(allVos, 0L);
    }

    private List<OrganizationVO> buildTree(List<OrganizationVO> allOrgs, Long parentId) {
        return allOrgs.stream()
            .filter(org -> org.getParentId().equals(parentId))
            .peek(org -> {
                List<OrganizationVO> children = buildTree(allOrgs, org.getId());
                org.setChildren(children);
            })
            .collect(Collectors.toList());
    }

    private OrganizationVO convertToVO(SysOrganization org) {
        OrganizationVO vo = new OrganizationVO();
        BeanUtils.copyProperties(org, vo);

        if (Integer.valueOf(1).equals(org.getOrgType())) {
            vo.setOrgTypeDesc("系统级");
        } else {
            vo.setOrgTypeDesc("普通单位");
        }

        if (Integer.valueOf(0).equals(org.getStatus())) {
            vo.setStatusDesc("正常");
        } else {
            vo.setStatusDesc("禁用");
        }

        if (org.getParentId() != null && org.getParentId() > 0) {
            SysOrganization parent = organizationMapper.selectById(org.getParentId());
            if (parent != null) {
                vo.setParentName(parent.getOrgName());
            }
        }

        return vo;
    }
}