package com.example.data_demo_002.modules.user.service;


import com.example.data_demo_002.common.base.mapper.SysUserMapper;
import com.example.data_demo_002.common.base.service.SysRoleService;
import com.example.data_demo_002.common.base.service.SysUserRoleService;
import com.example.data_demo_002.common.base.service.SysUserService;
import com.example.data_demo_002.modules.user.dao.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    //注入基础模块，基础的曾删改查模块
    //用户服务
    private final SysUserService sysUserService;
    //角色服务
    private final SysRoleService sysRoleService;
    //用户角色服务
    private final SysUserRoleService sysUserRoleService;
    //用户Mapper
    private final SysUserMapper sysUserMapper;


    //查询所有用户，分页查询
    /**
     * 分页获取用户列表（含角色信息，优化N+1查询）
     *
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页数量
     * @return 分页用户列表
     */
    public com.baomidou.mybatisplus.extension.plugins.pagination.Page<UserVO> listUsers(int pageNum, int pageSize) {
        // 1. 创建分页对象
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<UserVO> page =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize);

        // 2. 使用Mapper层SQL一次性查询用户和角色信息（解决N+1问题）
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<UserVO> resultPage =
                sysUserMapper.selectUserPageWithRoles(page);

        // 3. 解析字符串形式的角色ID和角色名称
        resultPage.getRecords().forEach(vo -> {
            // 解析角色ID字符串 "1001,1002" -> [1001, 1002]
            if (vo.getRoleIdsStr() != null && !vo.getRoleIdsStr().isEmpty()) {
                List<Long> roleIds = java.util.Arrays.stream(vo.getRoleIdsStr().split(","))
                        .map(String::trim)
                        .map(Long::parseLong)
                        .collect(Collectors.toList());
                vo.setRoleIds(roleIds);
            } else {
                vo.setRoleIds(new ArrayList<>());
            }

            // 解析角色名称字符串 "普通用户,管理员" -> ["普通用户", "管理员"]
            if (vo.getRoleNamesStr() != null && !vo.getRoleNamesStr().isEmpty()) {
                List<String> roleNames = java.util.Arrays.stream(vo.getRoleNamesStr().split(","))
                        .map(String::trim)
                        .collect(Collectors.toList());
                vo.setRoleNames(roleNames);
            } else {
                vo.setRoleNames(new ArrayList<>());
            }
        });

        return resultPage;
    }



    //根据id查询用户
    //根据用户名查询用户
    //根据邮箱查询用户

}
