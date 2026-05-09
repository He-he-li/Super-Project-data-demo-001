package com.example.data_demo_002.modules.user.service;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.data_demo_002.common.base.domain.SysRole;
import com.example.data_demo_002.common.base.domain.SysUser;
import com.example.data_demo_002.common.base.domain.SysUserRole;
import com.example.data_demo_002.common.base.mapper.SysUserMapper;
import com.example.data_demo_002.common.base.service.SysRoleService;
import com.example.data_demo_002.common.base.service.SysUserRoleService;
import com.example.data_demo_002.common.base.service.SysUserService;
import com.example.data_demo_002.common.exception.BusinessException;
import com.example.data_demo_002.common.util.Jwt.JwtUtil;
import com.example.data_demo_002.common.util.Jwt.RefreshTokenService;
import com.example.data_demo_002.modules.user.dao.UserDTO;
import com.example.data_demo_002.modules.user.dao.UserLoginVO;
import com.example.data_demo_002.modules.user.dao.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务类
 * 提供用户注册、登录、信息查询、修改、密码管理等业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final SysUserService sysUserService;
    private final SysUserMapper sysUserMapper;
    private final SysRoleService sysRoleService;
    private final SysUserRoleService sysUserRoleService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    // ==================== 查询操作 ====================

    /**
     * 用户登录
     * 
     * @param username 用户名
     * @param password 密码（明文）
     * @return 登录响应（含用户信息和Token）
     * @throws BusinessException 用户不存在或密码错误时抛出
     */
    public UserLoginVO login(String username, String password) {
        // 1. 查询用户
        SysUser user = sysUserService.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 2. 校验密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("账号或密码错误，请重新输入");
        }

        // 3. 获取用户角色列表
        List<SysUserRole> relations = sysUserRoleService.list(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, user.getId())
        );
        List<Long> roleIds = relations.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        
        // 4. 获取角色名称
        List<String> roleNames = new ArrayList<>();
        if (!roleIds.isEmpty()) {
            List<SysRole> roles = sysRoleService.listByIds(roleIds);
            roleNames = roles.stream()
                    .map(SysRole::getRoleName)
                    .collect(Collectors.toList());
        }
        
        // 5. 生成双Token（Access Token + Refresh Token）
        JwtUtil.TokenPair tokenPair = jwtUtil.generateTokenPair(
                user.getId(),
                username,
                user.getStatus(),
                user.getVersion().longValue()
        );
        log.info("Generated token pair for user {}: accessExpire={}s, refreshExpire={}s",
                username, tokenPair.getExpiresIn(), tokenPair.getRefreshExpiresIn());
        
        // 6. 保存Refresh Token到Redis
        JwtUtil.RefreshTokenInfo refreshInfo = jwtUtil.validateRefreshToken(tokenPair.getRefreshToken());
        log.info("Parsed refresh token info: userId={}, username={}, jti={}", 
                refreshInfo.getUserId(), refreshInfo.getUsername(), refreshInfo.getJti());
        
        refreshTokenService.saveRefreshToken(
                refreshInfo.getJti(),
                tokenPair.getRefreshToken(),
                user.getId(),
                tokenPair.getRefreshExpiresIn() * 1000
        );

        // 7. 组装登录响应
        UserLoginVO loginVO = new UserLoginVO();
        loginVO.setId(user.getId());
        loginVO.setUsername(user.getUsername());
        loginVO.setEmail(user.getEmail());
        loginVO.setPhone(user.getPhone());
        loginVO.setStatus(user.getStatus());
        loginVO.setRoleIds(roleIds);
        loginVO.setRoleNames(roleNames);
        loginVO.setToken(tokenPair.getToken());
        loginVO.setRefreshToken(tokenPair.getRefreshToken());
        loginVO.setExpiresIn(tokenPair.getExpiresIn());
        loginVO.setRefreshExpiresIn(tokenPair.getRefreshExpiresIn());

        log.info("Login successful for user: {}, returning UserLoginVO with tokens", username);
        return loginVO;
    }

    /**
     * 获取用户详情（含角色列表）
     * 
     * @param userId 用户ID
     * @return 用户详细信息
     * @throws BusinessException 用户不存在时抛出
     */
    public UserVO getUserDetail(Long userId) {
        // 1. 查询用户基本信息
        SysUser user = sysUserService.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 2. 查询用户角色关联
        List<SysUserRole> relations = sysUserRoleService.list(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId)
        );

        // 3. 查询角色详情
        List<Long> roleIds = relations.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        List<String> roleNames = new ArrayList<>();
        if (!roleIds.isEmpty()) {
            List<SysRole> roles = sysRoleService.listByIds(roleIds);
            roleNames = roles.stream()
                    .map(SysRole::getRoleName)
                    .collect(Collectors.toList());
        }

        // 4. 组装VO（不包含密码）
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setStatus(user.getStatus());
        vo.setCreateTime(user.getCreateTime());
        vo.setUpdateTime(user.getUpdateTime());
        vo.setRoleIds(roleIds);
        vo.setRoleNames(roleNames);

        return vo;
    }

    /**
     * 获取用户详情（含角色列表）
     *
     * @param  机构id数组
     * @return 用户详细信息
     * @throws BusinessException 用户不存在时抛出
     */

    /**
     * 分页获取用户列表（含角色信息，支持多条件筛选）
     * 
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页数量
     * @param username 用户名（模糊查询）
     * @param email 邮箱（模糊查询）
     * @param phone 手机号（模糊查询）
     * @param status 用户状态
     * @return 分页用户列表
     */
    public com.baomidou.mybatisplus.extension.plugins.pagination.Page<UserVO> listUsers(
            int pageNum, int pageSize, String username, String email, String phone, Integer status) {
        
        // 1. 创建分页对象
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<SysUser> userPage = 
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize);
        
        // 2. 构建查询条件
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (username != null && !username.trim().isEmpty()) {
            wrapper.like(SysUser::getUsername, username);
        }
        if (email != null && !email.trim().isEmpty()) {
            wrapper.like(SysUser::getEmail, email);
        }
        if (phone != null && !phone.trim().isEmpty()) {
            wrapper.like(SysUser::getPhone, phone);
        }
        if (status != null) {
            wrapper.eq(SysUser::getStatus, status);
        }
        wrapper.orderByDesc(SysUser::getCreateTime);
        
        // 3. 分页查询用户
        sysUserService.page(userPage, wrapper);
        
        // 4. 创建结果分页对象
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<UserVO> resultPage = 
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize);
        resultPage.setTotal(userPage.getTotal());
        resultPage.setCurrent(userPage.getCurrent());
        resultPage.setSize(userPage.getSize());
        resultPage.setPages(userPage.getPages());
        
        // 5. 转换为VO并填充角色信息
        List<UserVO> userVOList = userPage.getRecords().stream().map(user -> {
            UserVO vo = new UserVO();
            vo.setId(user.getId());
            vo.setUsername(user.getUsername());
            vo.setEmail(user.getEmail());
            vo.setPhone(user.getPhone());
            vo.setStatus(user.getStatus());
            vo.setCreateTime(user.getCreateTime());
            vo.setUpdateTime(user.getUpdateTime());
            
            // 6. 查询用户角色
            List<SysUserRole> relations = sysUserRoleService.list(
                    new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, user.getId())
            );
            
            List<Long> roleIds = relations.stream()
                    .map(SysUserRole::getRoleId)
                    .collect(Collectors.toList());
            
            // 7. 查询角色名称
            List<String> roleNames = new ArrayList<>();
            if (!roleIds.isEmpty()) {
                List<SysRole> roles = sysRoleService.listByIds(roleIds);
                roleNames = roles.stream()
                        .map(SysRole::getRoleName)
                        .collect(Collectors.toList());
            }
            
            vo.setRoleIds(roleIds);
            vo.setRoleNames(roleNames);
            
            return vo;
        }).collect(Collectors.toList());
        
        resultPage.setRecords(userVOList);
        return resultPage;
    }

    /**
     * 根据用户名查询用户（含角色信息）
     * TODO: 待优化 - 自动关联当前用户的机构ID
     * @param userName 用户名
     * @return 用户详细信息
     * @throws BusinessException 用户不存在时抛出
     */
    public UserVO getUserByUserName(String userName) {
        // 1. 查询用户基本信息
        SysUser user = sysUserService.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, userName));
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 2. 查询用户角色关联
        List<SysUserRole> relations = sysUserRoleService.list(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, user.getId())
        );

        // 3. 查询角色详情
        List<Long> roleIds = relations.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        List<String> roleNames = new ArrayList<>();
        if (!roleIds.isEmpty()) {
            List<SysRole> roles = sysRoleService.listByIds(roleIds);
            roleNames = roles.stream()
                    .map(SysRole::getRoleName)
                    .collect(Collectors.toList());
        }

        // 4. 组装VO（不包含密码）
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setStatus(user.getStatus());
        vo.setCreateTime(user.getCreateTime());
        vo.setUpdateTime(user.getUpdateTime());
        vo.setRoleIds(roleIds);
        vo.setRoleNames(roleNames);

        return vo;
    }

    /**
     * 根据邮箱查询用户（含角色信息）
     * TODO: 待优化 - 自动关联当前用户的机构ID
     * @param email 邮箱
     * @return 用户详细信息
     * @throws BusinessException 用户不存在时抛出
     */
    public UserVO getUserByEmail(String email) {
        // 1. 查询用户基本信息
        SysUser user = sysUserService.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getEmail, email));
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 2. 查询用户角色关联
        List<SysUserRole> relations = sysUserRoleService.list(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, user.getId())
        );

        // 3. 查询角色详情
        List<Long> roleIds = relations.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        List<String> roleNames = new ArrayList<>();
        if (!roleIds.isEmpty()) {
            List<SysRole> roles = sysRoleService.listByIds(roleIds);
            roleNames = roles.stream()
                    .map(SysRole::getRoleName)
                    .collect(Collectors.toList());
        }

        // 4. 组装VO（不包含密码）
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setStatus(user.getStatus());
        vo.setCreateTime(user.getCreateTime());
        vo.setUpdateTime(user.getUpdateTime());
        vo.setRoleIds(roleIds);
        vo.setRoleNames(roleNames);

        return vo;
    }

    /**
     * 根据手机号查询用户（含角色信息）
     * @param  phone 手机号
     * TODO: 待优化 - 自动关联当前用户的机构ID
     * @return 用户详细信息
     * @throws BusinessException 用户不存在时抛出
     */
    public UserVO getUserByPhone(String phone) {
        // 1. 查询用户基本信息
        SysUser user = sysUserService.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getPhone, phone));
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 2. 查询用户角色关联
        List<SysUserRole> relations = sysUserRoleService.list(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, user.getId())
        );
        // 3. 查询角色详情
        List<Long> roleIds = relations.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        List<String> roleNames = new ArrayList<>();
        if (!roleIds.isEmpty()) {
            List<SysRole> roles = sysRoleService.listByIds(roleIds);
            roleNames = roles.stream()
                    .map(SysRole::getRoleName)
                    .collect(Collectors.toList());
        }
        // 4. 组装VO（不包含密码）
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setStatus(user.getStatus());
        vo.setCreateTime(user.getCreateTime());
        vo.setUpdateTime(user.getUpdateTime());
        vo.setRoleIds(roleIds);
        vo.setRoleNames(roleNames);

        return vo;

    }



    // ==================== 新增操作 ====================

    /**
     * 用户注册
     * TODO: 待优化 - 自动关联当前用户的机构ID
     * @param dto 用户注册信息（username, password, email, phone, roleIds可选）
     * @return 注册成功的用户信息
     * @throws BusinessException 用户名已存在时抛出
     */
    @Transactional(rollbackFor = Exception.class)
    public UserDTO createUser(UserDTO dto) {
        // 1. 校验用户名唯一性
        long count = sysUserService.count(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, dto.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 2. 构建用户实体
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setStatus(0); // 默认正常状态

        // 3. 保存用户
        sysUserService.save(user);

        // 4. 分配角色（前端未传则默认分配普通用户角色1001）
        List<Long> roleIds = dto.getRoleIds();
        if (roleIds == null || roleIds.isEmpty()) {
            roleIds = List.of(1001L);
        }
        
        List<SysUserRole> relations = roleIds.stream()
                .map(roleId -> {
                    SysUserRole rel = new SysUserRole();
                    rel.setUserId(user.getId());
                    rel.setRoleId(roleId);
                    return rel;
                })
                .collect(Collectors.toList());
        
        sysUserRoleService.saveBatch(relations);

        // 5. 返回结果
        UserDTO result = new UserDTO();
        result.setId(user.getId());
        result.setUsername(user.getUsername());
        result.setEmail(user.getEmail());
        result.setPhone(user.getPhone());
        result.setStatus(user.getStatus());
        result.setRoleIds(roleIds);
        
        return result;
    }

    // ==================== 修改操作 ====================

    /**
     * 修改当前登录用户的基本信息
     * 
     * @param userId 用户ID（从Token中获取）
     * @param dto 要修改的信息（username, email, phone）
     * @return 更新后的用户信息
     * @throws BusinessException 用户名/邮箱/手机已存在时抛出
     */
    public UserVO updateUserMe(Long userId, UserDTO dto) {
        // 1. 校验用户名唯一性
        if (dto.getUsername() != null) {
            SysUser old = sysUserService.getOne(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getUsername, dto.getUsername()));
            if (old != null && !old.getId().equals(userId)) {
                throw new BusinessException("用户名已存在");
            }
        }
        
        // 2. 校验邮箱唯一性
        if (dto.getEmail() != null) {
            long emailCount = sysUserService.count(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getEmail, dto.getEmail())
                    .ne(SysUser::getId, userId));
            if (emailCount > 0) {
                throw new BusinessException("邮箱已存在");
            }
        }
        
        // 3. 校验手机唯一性
        if (dto.getPhone() != null) {
            long phoneCount = sysUserService.count(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getPhone, dto.getPhone())
                    .ne(SysUser::getId, userId));
            if (phoneCount > 0) {
                throw new BusinessException("手机已存在");
            }
        }
        
        // 4. 更新用户信息
        SysUser user = new SysUser();
        user.setId(userId);
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setUpdateTime(new Date());
        
        boolean success = sysUserService.updateById(user);
        if (!success) {
            throw new BusinessException("修改用户信息失败");
        }

        // 5. 返回结果
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setStatus(user.getStatus());
        vo.setUpdateTime(user.getUpdateTime());
        
        return vo;
    }



    /**
     * 管理员修改用户基本信息
     * 
     * @param userId 目标用户ID
     * @param dto 要修改的信息（username, email, phone, status）
     * @return 更新后的用户信息
     * @throws BusinessException 用户不存在或字段重复时抛出
     */
    public UserVO upDateUser(Long userId, UserDTO dto) {
        // 1. 查询用户是否存在
        SysUser user = sysUserService.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 2. 校验用户名唯一性
        if (dto.getUsername() != null) {
            SysUser old = sysUserService.getOne(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getUsername, dto.getUsername()));
            if (old != null && !old.getId().equals(userId)) {
                throw new BusinessException("用户名已存在");
            }
        }
        
        // 3. 校验邮箱唯一性
        if (dto.getEmail() != null) {
            long emailCount = sysUserService.count(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getEmail, dto.getEmail())
                    .ne(SysUser::getId, userId));
            if (emailCount > 0) {
                throw new BusinessException("邮箱已存在");
            }
        }
        
        // 4. 校验手机唯一性
        if (dto.getPhone() != null) {
            long phoneCount = sysUserService.count(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getPhone, dto.getPhone())
                    .ne(SysUser::getId, userId));
            if (phoneCount > 0) {
                throw new BusinessException("手机已存在");
            }
        }
        
        // 5. 更新用户信息
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setStatus(dto.getStatus());
        user.setUpdateTime(new Date());
        
        boolean success = sysUserService.updateById(user);
        if (!success) {
            throw new BusinessException("数据库更新失败");
        }
        
        // 6. 返回结果
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setUpdateTime(user.getUpdateTime());
        
        return vo;
    }

    /**
     * 管理员通过用户名修改用户基本信息
     * 
     * @param username 用户名
     * @param dto 要修改的信息（username, email, phone, status）
     * @return 更新后的用户信息
     * @throws BusinessException 用户不存在或字段重复时抛出
     */
    public UserVO upDateUserByUserName(String username, UserDTO dto) {
        // 1. 查询用户是否存在
        SysUser user = sysUserService.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 2. 校验用户名唯一性
        if (dto.getUsername() != null && !dto.getUsername().equals(username)) {
            SysUser old = sysUserService.getOne(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getUsername, dto.getUsername()));
            if (old != null && !old.getId().equals(user.getId())) {
                throw new BusinessException("用户名已存在");
            }
        }
        
        // 3. 校验邮箱唯一性
        if (dto.getEmail() != null) {
            long emailCount = sysUserService.count(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getEmail, dto.getEmail())
                    .ne(SysUser::getId, user.getId()));
            if (emailCount > 0) {
                throw new BusinessException("邮箱已存在");
            }
        }
        
        // 4. 校验手机唯一性
        if (dto.getPhone() != null) {
            long phoneCount = sysUserService.count(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getPhone, dto.getPhone())
                    .ne(SysUser::getId, user.getId()));
            if (phoneCount > 0) {
                throw new BusinessException("手机已存在");
            }
        }
        
        // 5. 更新用户信息
        if (dto.getUsername() != null) user.setUsername(dto.getUsername());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getPhone() != null) user.setPhone(dto.getPhone());
        if (dto.getStatus() != null) user.setStatus(dto.getStatus());
        user.setUpdateTime(new Date());
        
        boolean success = sysUserService.updateById(user);
        if (!success) {
            throw new BusinessException("数据库更新失败");
        }
        
        // 6. 返回结果
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setUpdateTime(user.getUpdateTime());
        
        return vo;
    }

    /**
     * 修改当前用户密码（需验证旧密码）
     * 
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @throws BusinessException 旧密码错误时抛出
     */
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        // 1. 查询用户
        SysUser user = sysUserService.getById(userId);
        
        // 2. 校验旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("账号或密码错误，请重新输入");
        }
        
        // 3. 加密并更新新密码
        String newPasswordEncoded = passwordEncoder.encode(newPassword);
        user.setPassword(newPasswordEncoded);
        sysUserService.updateById(user);
        
        // 4. 删除该用户的所有Refresh Token（强制下线）
        refreshTokenService.deleteAllUserRefreshTokens(userId);
        log.info("All refresh tokens deleted for user {} after password update", userId);
    }

    /**
     * 管理员通过用户名修改密码
     * 
     * @param username 用户名
     * @param newPassword 新密码
     * @throws BusinessException 用户不存在时抛出
     */
    public void updatePasswordByUserName(String username, String newPassword) {
        // 1. 查询用户
        SysUser user = sysUserService.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 2. 加密并更新密码
        String newPasswordEncoded = passwordEncoder.encode(newPassword);
        user.setPassword(newPasswordEncoded);
        sysUserService.updateById(user);
        
        // 3. 删除该用户的所有Refresh Token
        refreshTokenService.deleteAllUserRefreshTokens(user.getId());
        log.info("All refresh tokens deleted for user {} after password update by admin", user.getId());
    }

    /**
     * 管理员通过邮箱修改密码
     * 
     * @param email 邮箱
     * @param newPassword 新密码
     * @throws BusinessException 用户不存在时抛出
     */
    public void updatePasswordByEmail(String email, String newPassword) {
        // 1. 查询用户
        SysUser user = sysUserService.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getEmail, email));
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 2. 加密并更新密码
        String newPasswordEncoded = passwordEncoder.encode(newPassword);
        user.setPassword(newPasswordEncoded);
        sysUserService.updateById(user);
        
        // 3. 删除该用户的所有Refresh Token
        refreshTokenService.deleteAllUserRefreshTokens(user.getId());
        log.info("All refresh tokens deleted for user {} after password update by email", user.getId());
    }

    /**
     * 未登录状态下通过用户名修改密码（需验证旧密码）
     * 
     * @param username 用户名
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @throws BusinessException 用户不存在或旧密码错误时抛出
     */
    public void NotLoginUpdatePasswordByUserName(String username, String oldPassword, String newPassword) {
        // 1. 查询用户
        SysUser user = sysUserService.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 2. 校验旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("旧密码错误");
        }
        
        // 3. 加密并更新密码
        String newPasswordEncoded = passwordEncoder.encode(newPassword);
        user.setPassword(newPasswordEncoded);
        sysUserService.updateById(user);
        
        // 4. 删除该用户的所有Refresh Token
        refreshTokenService.deleteAllUserRefreshTokens(user.getId());
        log.info("All refresh tokens deleted for user {} after non-login password update", user.getId());
    }

    /**
     * 未登录状态下通过邮箱修改密码（无需旧密码，通常配合邮箱验证码使用）
     * 
     * @param email 邮箱
     * @param newPassword 新密码
     * @throws BusinessException 密码修改失败时抛出
     */
    public void NotLoginUpdatePasswordByEmail(String email, String newPassword) {
        // 1. 加密新密码
        String newPasswordEncoded = passwordEncoder.encode(newPassword);
        
        // 2. 更新密码
        boolean success = sysUserService.lambdaUpdate()
                .eq(SysUser::getEmail, email)
                .set(SysUser::getPassword, newPasswordEncoded)
                .update();
        
        if (!success) {
            throw new BusinessException("密码修改失败，请联系管理员");
        }
        
        // 3. 查询用户并删除所有Refresh Token
        SysUser user = sysUserService.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getEmail, email));
        if (user != null) {
            refreshTokenService.deleteAllUserRefreshTokens(user.getId());
            log.info("All refresh tokens deleted for user {} after email-based password update", user.getId());
        }
    }

    /**
     * 管理员给用户重新分配角色
     * 
     * @param userId 用户ID
     * @param newRoleIds 新的角色ID列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(Long userId, List<Long> newRoleIds) {
        // 1. 删除旧的角色关联
        sysUserRoleService.remove(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId));

        // 2. 添加新的角色关联
        if (newRoleIds != null && !newRoleIds.isEmpty()) {
            List<SysUserRole> relations = newRoleIds.stream()
                    .map(roleId -> {
                        SysUserRole rel = new SysUserRole();
                        rel.setUserId(userId);
                        rel.setRoleId(roleId);
                        return rel;
                    })
                    .collect(Collectors.toList());
            sysUserRoleService.saveBatch(relations);
        }
    }

    // ==================== 删除操作 ====================

    /**
     * 管理员通过用户名删除用户（物理删除）
     * 
     * @param username 用户名
     * @throws BusinessException 用户不存在时抛出
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteUserByUserName(String username) {
        // 1. 查询用户是否存在
        SysUser user = sysUserService.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 2. 删除用户的角色关联
        sysUserRoleService.remove(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, user.getId()));
        log.info("Deleted role associations for user: {}", username);

        // 3. 删除该用户的所有Refresh Token
        refreshTokenService.deleteAllUserRefreshTokens(user.getId());
        log.info("Deleted all refresh tokens for user: {}", username);

        // 4. 删除用户
        boolean success = sysUserService.removeById(user.getId());
        if (!success) {
            throw new BusinessException("删除用户失败");
        }
        
        log.info("Successfully deleted user: {}", username);
    }
}
