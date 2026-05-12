package com.example.data_demo_002.modules.user.service;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.data_demo_002.common.base.domain.SysRole;
import com.example.data_demo_002.common.base.domain.SysUser;
import com.example.data_demo_002.common.base.domain.SysUserRole;
import com.example.data_demo_002.common.base.mapper.SysUserMapper;
import com.example.data_demo_002.common.base.service.SysRoleService;
import com.example.data_demo_002.common.base.service.SysUserRoleService;
import com.example.data_demo_002.common.base.service.SysUserService;
import com.example.data_demo_002.common.constant.RoleConstants;
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
import java.util.Map;
import java.util.stream.Collectors;

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

    // ==================== 【认证中心】 Authentication (AUTH) ====================

    /**
     * [AUTH-002] 用户登录
     * 功能：验证用户名密码，生成双Token（Access+Refresh）
     * 入参：username, password
     * 返回：UserLoginVO(用户信息+Token+角色列表)
     * 影响：Redis存储Refresh Token
     */
    public UserLoginVO login(String username, String password) {
        SysUser user = sysUserService.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("账号或密码错误，请重新输入");
        }

        List<SysUserRole> relations = sysUserRoleService.list(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, user.getId())
        );
        List<Long> roleIds = relations.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        
        List<String> roleNames = new ArrayList<>();
        if (!roleIds.isEmpty()) {
            List<SysRole> roles = sysRoleService.listByIds(roleIds);
            roleNames = roles.stream()
                    .map(SysRole::getRoleName)
                    .collect(Collectors.toList());
        }
        
        JwtUtil.TokenPair tokenPair = jwtUtil.generateTokenPair(
                user.getId(),
                username,
                user.getStatus(),
                user.getVersion().longValue()
        );
        log.info("Generated token pair for user {}: accessExpire={}s, refreshExpire={}s",
                username, tokenPair.getExpiresIn(), tokenPair.getRefreshExpiresIn());
        
        JwtUtil.RefreshTokenInfo refreshInfo = jwtUtil.validateRefreshToken(tokenPair.getRefreshToken());
        log.info("Parsed refresh token info: userId={}, username={}, jti={}", 
                refreshInfo.getUserId(), refreshInfo.getUsername(), refreshInfo.getJti());
        
        refreshTokenService.saveRefreshToken(
                refreshInfo.getJti(),
                tokenPair.getRefreshToken(),
                user.getId(),
                tokenPair.getRefreshExpiresIn() * 1000
        );

        UserLoginVO loginVO = new UserLoginVO();
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

    // ==================== 【个人中心】 Personal Center (PC) ====================

    /**
     * [PC-001/UM-002] 获取用户详情（含角色列表）
     * 功能：根据username查询详细信息，包含角色列表
     * 入参：username
     * 返回：UserVO(含角色列表，不含内部ID)
     * 影响：无
     */
    public UserVO getUserDetail(String username) {
        SysUser user = sysUserService.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        List<SysUserRole> relations = sysUserRoleService.list(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, user.getId())
        );

        List<Long> roleIds = relations.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        List<String> roleNames = new ArrayList<>();
        if (!roleIds.isEmpty()) {
            List<SysRole> roles = sysRoleService.listByIds(roleIds);
            roleNames = roles.stream()
                    .map(SysRole::getRoleName)
                    .collect(Collectors.toList());
        }

        UserVO vo = new UserVO();
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setStatus(user.getStatus());
        vo.setStatusDesc(user.getStatus() == 0 ? "正常" : "禁用");
        vo.setCreateTime(user.getCreateTime());
        vo.setUpdateTime(user.getUpdateTime());
        vo.setRoleIds(roleIds);
        vo.setRoleNames(roleNames);

        return vo;
    }

    /**
     * [UM-001] 分页获取用户列表（含角色信息，支持多条件筛选）- 已优化N+1查询
     * 功能：分页查询用户，支持多条件筛选
     * 入参：pageNum, pageSize, username(可选), email(可选), phone(可选), status(可选)
     * 返回：Page<UserVO>(含角色列表)
     * 影响：无
     */
    public com.baomidou.mybatisplus.extension.plugins.pagination.Page<UserVO> listUsers(
            int pageNum, int pageSize, String username, String email, String phone, Integer status) {
        
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<SysUser> userPage = 
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize);
        
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
        
        sysUserService.page(userPage, wrapper);
        
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<UserVO> resultPage = 
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize);
        resultPage.setTotal(userPage.getTotal());
        resultPage.setCurrent(userPage.getCurrent());
        resultPage.setSize(userPage.getSize());
        resultPage.setPages(userPage.getPages());
        
        List<SysUser> users = userPage.getRecords();
        if (users.isEmpty()) {
            resultPage.setRecords(new ArrayList<>());
            return resultPage;
        }
        
        List<Long> userIds = users.stream().map(SysUser::getId).collect(Collectors.toList());
        
        List<SysUserRole> allRelations = sysUserRoleService.list(
                new LambdaQueryWrapper<SysUserRole>().in(SysUserRole::getUserId, userIds)
        );
        
        Map<Long, List<Long>> userRoleMap = allRelations.stream()
                .collect(Collectors.groupingBy(
                        SysUserRole::getUserId,
                        Collectors.mapping(SysUserRole::getRoleId, Collectors.toList())
                ));
        
        List<Long> allRoleIds = allRelations.stream()
                .map(SysUserRole::getRoleId)
                .distinct()
                .collect(Collectors.toList());
        
        Map<Long, String> roleNameMap = new java.util.HashMap<>();
        if (!allRoleIds.isEmpty()) {
            List<SysRole> roles = sysRoleService.listByIds(allRoleIds);
            roleNameMap = roles.stream()
                    .collect(Collectors.toMap(SysRole::getId, SysRole::getRoleName));
        }
        
        Map<Long, String> finalRoleNameMap = roleNameMap;
        List<UserVO> userVOList = users.stream().map(user -> {
            UserVO vo = new UserVO();
            vo.setUsername(user.getUsername());
            vo.setEmail(user.getEmail());
            vo.setPhone(user.getPhone());
            vo.setStatus(user.getStatus());
            vo.setStatusDesc(user.getStatus() == 0 ? "正常" : "禁用");
            vo.setCreateTime(user.getCreateTime());
            vo.setUpdateTime(user.getUpdateTime());
            
            List<Long> roleIds = userRoleMap.getOrDefault(user.getId(), new ArrayList<>());
            List<String> roleNames = roleIds.stream()
                    .map(finalRoleNameMap::get)
                    .filter(name -> name != null)
                    .collect(Collectors.toList());
            
            vo.setRoleIds(roleIds);
            vo.setRoleNames(roleNames);
            
            return vo;
        }).collect(Collectors.toList());
        
        resultPage.setRecords(userVOList);
        return resultPage;
    }

    // ==================== 【用户管理】 User Management (UM) ====================

    /**
     * [AUTH-001] 用户注册
     * 功能：创建新用户并分配默认角色（普通用户）
     * 入参：UserDTO(username, password, email, phone, roleIds可选)
     * 返回：UserDTO(包含userId)
     * 影响：插入sys_user表，插入sys_user_role表（默认角色1001）
     */
    @Transactional(rollbackFor = Exception.class)
    public UserDTO createUser(UserDTO dto) {
        long count = sysUserService.count(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, dto.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setStatus(0);

        sysUserService.save(user);

        List<Long> roleIds = dto.getRoleIds();
        if (roleIds == null || roleIds.isEmpty()) {
            roleIds = List.of(RoleConstants.DEFAULT_USER_ROLE_ID);
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

        UserDTO result = new UserDTO();
        result.setUsername(user.getUsername());
        result.setEmail(user.getEmail());
        result.setPhone(user.getPhone());
        result.setStatus(user.getStatus());
        result.setRoleIds(roleIds);
        
        return result;
    }

    /**
     * [PC-002] 修改当前登录用户的基本信息
     * 功能：修改当前登录用户的基本信息（用户名、邮箱、手机）
     * 入参：username, UserDTO(username, email, phone)
     * 返回：UserVO(更新后的用户信息，不含内部ID)
     * 影响：更新sys_user表的username/email/phone/update_time
     */
    public UserVO updateUserMe(String username, UserDTO dto) {
        SysUser currentUser = sysUserService.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (currentUser == null) {
            throw new BusinessException("用户不存在");
        }
        
        validateUserUniqueness(currentUser.getId(), dto.getUsername(), dto.getEmail(), dto.getPhone());
        
        SysUser user = new SysUser();
        user.setId(currentUser.getId());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setUpdateTime(new Date());
        
        boolean success = sysUserService.updateById(user);
        if (!success) {
            throw new BusinessException("修改用户信息失败");
        }

        UserVO vo = new UserVO();
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setStatus(currentUser.getStatus());
        vo.setStatusDesc(currentUser.getStatus() == 0 ? "正常" : "禁用");
        vo.setUpdateTime(user.getUpdateTime());
        
        return vo;
    }

    /**
     * [UM-003] 管理员修改用户基本信息
     * 功能：管理员修改用户的基本信息和状态
     * 入参：username, UserDTO(username, email, phone, status)
     * 返回：UserVO(更新后的用户信息，不含内部ID)
     * 影响：更新sys_user表的username/email/phone/status/update_time
     */
    public UserVO updateUser(String username, UserDTO dto) {
        SysUser user = sysUserService.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        validateUserUniqueness(user.getId(), dto.getUsername(), dto.getEmail(), dto.getPhone());
        
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setStatus(dto.getStatus());
        System.out.println(dto.getStatus());
        user.setUpdateTime(new Date());
        
        boolean success = sysUserService.updateById(user);
        if (!success) {
            throw new BusinessException("数据库更新失败");
        }
        
        UserVO vo = new UserVO();
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setStatus(user.getStatus());
        vo.setStatusDesc(user.getStatus() == 0 ? "正常" : "禁用");
        vo.setUpdateTime(user.getUpdateTime());
        
        return vo;
    }

    /**
     * 校验用户字段唯一性（提取公共方法）
     */
    private void validateUserUniqueness(Long userId, String username, String email, String phone) {
        if (username != null) {
            SysUser old = sysUserService.getOne(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getUsername, username));
            if (old != null && !old.getId().equals(userId)) {
                throw new BusinessException("用户名已存在");
            }
        }
        
        if (email != null) {
            long emailCount = sysUserService.count(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getEmail, email)
                    .ne(SysUser::getId, userId));
            if (emailCount > 0) {
                throw new BusinessException("邮箱已存在");
            }
        }
        
        if (phone != null) {
            long phoneCount = sysUserService.count(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getPhone, phone)
                    .ne(SysUser::getId, userId));
            if (phoneCount > 0) {
                throw new BusinessException("手机已存在");
            }
        }
    }

    /**
     * [PC-003/UM-007] 修改密码（通用方法）
     * 功能：修改用户密码，删除所有Refresh Token
     * 入参：userId, newPassword
     * 返回：无
     * 影响：更新sys_user表的password，删除Redis中所有Refresh Token
     */
    private void changePassword(Long userId, String newPassword) {
        SysUser user = sysUserService.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        String newPasswordEncoded = passwordEncoder.encode(newPassword);
        user.setPassword(newPasswordEncoded);
        sysUserService.updateById(user);
        
        refreshTokenService.deleteAllUserRefreshTokens(userId);
        log.info("Password updated and all refresh tokens deleted for user {}", userId);
    }

    /**
     * [PC-003] 修改当前用户密码（需验证旧密码）
     * 功能：修改当前登录用户的密码，需验证旧密码
     * 入参：username, oldPassword, newPassword
     * 返回：无
     * 影响：更新sys_user表的password，删除所有Refresh Token
     */
    public void updatePassword(String username, String oldPassword, String newPassword) {
        SysUser user = sysUserService.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("旧密码错误");
        }
        
        changePassword(user.getId(), newPassword);
    }

    /**
     * [UM-007] 管理员重置密码
     * 功能：管理员直接重置用户密码，无需旧密码
     * 入参：username, newPassword
     * 返回：无
     * 影响：更新sys_user表的password，删除所有Refresh Token
     */
    public void resetPassword(String username, String newPassword) {
        SysUser user = sysUserService.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        changePassword(user.getId(), newPassword);
    }

    /**
     * [UM-004] 删除用户（物理删除）
     * 功能：物理删除用户，同时删除角色关联和Token
     * 入参：userId
     * 返回：无
     * 影响：删除sys_user表记录，删除sys_user_role表记录，删除Redis中的Refresh Token
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteUserByUserId(Long userId) {
        SysUser user = sysUserService.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        sysUserRoleService.remove(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId));
        log.info("Deleted role associations for user: {}", userId);

        refreshTokenService.deleteAllUserRefreshTokens(userId);
        log.info("Deleted all refresh tokens for user: {}", userId);

        boolean success = sysUserService.removeById(userId);
        if (!success) {
            throw new BusinessException("删除用户失败");
        }
        
        log.info("Successfully deleted user: {}", userId);
    }

    /**
     * [UM-004] 删除用户（根据username）
     * 功能：物理删除用户，同时删除角色关联和Token
     * 入参：username
     * 返回：无
     * 影响：删除sys_user表记录，删除sys_user_role表记录，删除Redis中的Refresh Token
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteUserByUsername(String username) {
        SysUser user = sysUserService.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        deleteUserByUserId(user.getId());
    }

    /**
     * [UM-005] 禁用/启用用户
     * 功能：修改用户状态（0-正常 1-禁用）
     * 入参：userId, status
     * 返回：无
     * 影响：更新sys_user表的status和update_time
     */
    public void updateUserStatus(Long userId, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("状态参数错误，只能为0或1");
        }
        
        SysUser user = sysUserService.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        user.setStatus(status);
        user.setUpdateTime(new Date());
        
        boolean success = sysUserService.updateById(user);
        if (!success) {
            throw new BusinessException("更新用户状态失败");
        }
        
        log.info("User {} status updated to {}", userId, status);
    }

    /**
     * [UM-005] 禁用/启用用户（根据username）
     * 功能：修改用户状态（0-正常 1-禁用）
     * 入参：username, status
     * 返回：无
     * 影响：更新sys_user表的status和update_time
     */
    public void updateUserStatusByUsername(String username, Integer status) {
        SysUser user = sysUserService.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        updateUserStatus(user.getId(), status);
    }

    /**
     * [UM-006] 强制下线
     * 功能：清除用户所有Refresh Token，强制重新登录
     * 入参：userId
     * 返回：无
     * 影响：删除Redis中该用户的所有Refresh Token
     */
    public void forceLogout(Long userId) {
        refreshTokenService.deleteAllUserRefreshTokens(userId);
        log.info("User {} forced logout, all refresh tokens deleted", userId);
    }

    /**
     * [UM-006] 强制下线（根据username）
     * 功能：清除用户所有Refresh Token，强制重新登录
     * 入参：username
     * 返回：无
     * 影响：删除Redis中该用户的所有Refresh Token
     */
    public void forceLogoutByUsername(String username) {
        SysUser user = sysUserService.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        forceLogout(user.getId());
    }

    /**
     * [UM-007] 管理员重置密码（根据username）
     * 功能：管理员直接重置用户密码，无需旧密码
     * 入参：username, newPassword
     * 返回：无
     * 影响：更新sys_user表的password，删除所有Refresh Token
     */
    public void resetPasswordByUsername(String username, String newPassword) {
        SysUser user = sysUserService.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        changePassword(user.getId(), newPassword);
    }

    /**
     * [UM-008] 分配用户角色
     * 功能：给用户分配角色（全量替换模式）
     * 入参：userId, List<Long> roleIds
     * 返回：无
     * 影响：删除sys_user_role表旧记录，插入新记录
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(Long userId, List<Long> newRoleIds) {
        sysUserRoleService.remove(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId));

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
        
        log.info("Roles assigned to user {}, count: {}", userId, newRoleIds != null ? newRoleIds.size() : 0);
    }

    /**
     * [UM-008] 分配用户角色（根据username）
     * 功能：给用户分配角色（全量替换模式）
     * 入参：username, List<Long> roleIds
     * 返回：无
     * 影响：删除sys_user_role表旧记录，插入新记录
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignRolesByUsername(String username, List<Long> roleIds) {
        SysUser user = sysUserService.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        assignRoles(user.getId(), roleIds);
    }

    // ==================== 【批量操作】 Batch Operations (BATCH) ====================

    /**
     * [BATCH-001] 批量删除用户 - 已优化批量操作
     * 功能：一次性删除多个用户
     * 入参：List<Long> userIds
     * 返回：无
     * 影响：批量删除sys_user表记录，批量删除sys_user_role表记录，批量删除Redis中的Refresh Token
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteUsers(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            throw new BusinessException("用户ID列表不能为空");
        }
        
        sysUserRoleService.remove(new LambdaQueryWrapper<SysUserRole>()
                .in(SysUserRole::getUserId, userIds));
        log.info("Deleted role associations for {} users", userIds.size());
        
        userIds.forEach(userId -> {
            refreshTokenService.deleteAllUserRefreshTokens(userId);
            log.info("Deleted refresh tokens for user {}", userId);
        });
        
        sysUserService.removeByIds(userIds);
        log.info("Batch deleted {} users", userIds.size());
    }

    /**
     * [BATCH-002] 批量分配角色
     * 功能：为多个用户分配相同的角色
     * 入参：List<Long> userIds, List<Long> roleIds
     * 返回：无
     * 影响：批量更新sys_user_role表
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchAssignRoles(List<Long> userIds, List<Long> roleIds) {
        if (userIds == null || userIds.isEmpty()) {
            throw new BusinessException("用户ID列表不能为空");
        }
        
        if (roleIds == null || roleIds.isEmpty()) {
            throw new BusinessException("角色ID列表不能为空");
        }
        
        sysUserRoleService.remove(new LambdaQueryWrapper<SysUserRole>()
                .in(SysUserRole::getUserId, userIds));
        
        List<SysUserRole> relations = new ArrayList<>();
        for (Long userId : userIds) {
            for (Long roleId : roleIds) {
                SysUserRole relation = new SysUserRole();
                relation.setUserId(userId);
                relation.setRoleId(roleId);
                relations.add(relation);
            }
        }
        
        sysUserRoleService.saveBatch(relations);
        
        log.info("Batch assigned roles to {} users, total relations: {}", userIds.size(), relations.size());
    }

    // ==================== 【删除操作】 Delete Operations ====================

}
