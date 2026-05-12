package com.example.data_demo_002.common.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.data_demo_002.common.base.domain.SysUserRole;
import com.example.data_demo_002.common.base.mapper.SysUserRoleMapper;
import com.example.data_demo_002.common.constant.RoleConstants;
import com.example.data_demo_002.common.util.Jwt.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TenantInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final SysUserRoleMapper userRoleMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader("Authorization");

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);

            try {
                Long userId = jwtUtil.getUserIdFromToken(token);
                Long organizationId = jwtUtil.getOrganizationIdFromToken(token);

                if (userId != null && organizationId != null) {
                    // 检查是否是超级管理员
                    boolean isSuperAdmin = checkSuperAdmin(userId);

                    if (isSuperAdmin) {
                        // 超级管理员不设置租户ID，实现跨租户查询
                        log.debug("用户 {} 是超级管理员，跳过租户隔离", userId);
                    } else {
                        // 普通用户设置租户ID
                        TenantContext.setTenantId(organizationId);
                        log.debug("设置租户ID: {}", organizationId);
                    }
                }
            } catch (Exception e) {
                log.warn("解析租户信息失败: {}", e.getMessage());
            }
        }

        return true;
    }

    /**
     * 从Token中获取用户ID
     */
    private Long getUserIdFromToken(String token) {
        try {
            io.jsonwebtoken.Claims claims = io.jsonwebtoken.Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            
            Object userId = claims.get("userId");
            if (userId != null) {
                return Long.valueOf(userId.toString());
            }
            return null;
        } catch (Exception e) {
            log.error("解析Token中的用户ID失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取签名密钥（复用JwtUtil的逻辑）
     */
    private javax.crypto.SecretKey getSigningKey() {
        String secret = System.getProperty("jwt.secret", 
                System.getenv("JWT_SECRET"));
        byte[] keyBytes = secret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return io.jsonwebtoken.security.Keys.hmacShaKeyFor(keyBytes);
    }

    private boolean checkSuperAdmin(Long userId) {
        List<SysUserRole> roles = userRoleMapper.selectList(
            new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId)
        );

        return roles.stream()
            .anyMatch(role -> RoleConstants.SUPER_ADMIN_ROLE_ID.equals(role.getRoleId()));
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                                Object handler, Exception ex) {
        TenantContext.clear();
    }
}