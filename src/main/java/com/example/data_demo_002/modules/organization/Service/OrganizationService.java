package com.example.data_demo_002.modules.organization.Service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.data_demo_002.modules.organization.dao.OrganizationDTO;
import com.example.data_demo_002.modules.organization.dao.OrganizationVO;

import java.util.List;

public interface OrganizationService {

    /**
     * 检查单位是否是系统级单位
     */
    boolean isSystemOrganization(Long orgId);

    /**
     * 获取所有系统级单位
     */
    List<OrganizationVO> listSystemOrganizations();

    /**
     * 分页查询单位列表
     */
    Page<OrganizationVO> listOrganizations(int pageNum, int pageSize, String orgName, Integer isSystem);

    /**
     * 获取单位详情
     */
    OrganizationVO getOrganizationDetail(Long orgId);

    /**
     * 创建单位
     */
    void createOrganization(OrganizationDTO dto);

    /**
     * 修改单位
     */
    void updateOrganization(Long orgId, OrganizationDTO dto);

    /**
     * 删除单位
     */
    void deleteOrganization(Long orgId);

    /**
     * 获取单位树形结构
     */
    List<OrganizationVO> getOrganizationTree();
}