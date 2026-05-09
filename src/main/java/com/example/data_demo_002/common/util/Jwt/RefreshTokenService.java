package com.example.data_demo_002.common.util.Jwt;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Refresh Token 服务
 * 负责管理 Refresh Token 的 Redis 存储和失效
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final StringRedisTemplate redisTemplate;
    private final JwtUtil jwtUtil;

    // Refresh Token 在 Redis 中的前缀
    private static final String REFRESH_TOKEN_PREFIX = "refresh:token:";

    /**
     * 保存 Refresh Token 到 Redis
     *
     * @param jti          Refresh Token 的唯一标识
     * @param refreshToken 实际的 refresh token
     * @param userId       用户 ID
     * @param expireTime   过期时间 (毫秒)
     */
    public void saveRefreshToken(String jti, String refreshToken, Long userId, long expireTime) {
        String key = REFRESH_TOKEN_PREFIX + jti;
        log.info("Saving refresh token to Redis: key={}, userId={}, expireTime={}ms", 
            key, userId, expireTime);
        
        try {
            redisTemplate.opsForValue().set(key, refreshToken, expireTime, TimeUnit.MILLISECONDS);
            
            // 验证是否保存成功
            Boolean exists = redisTemplate.hasKey(key);
            if (exists != null && exists) {
                log.info("Refresh token saved successfully to Redis: {}", key);
            } else {
                log.error("Failed to save refresh token to Redis: {} - Redis returned false for hasKey", key);
                throw new RuntimeException("Failed to save refresh token to Redis");
            }
        } catch (Exception e) {
            log.error("Error saving refresh token to Redis: key={}, error={}", key, e.getMessage(), e);
            throw new RuntimeException("Redis connection failed", e);
        }
    }

    /**
     * 验证 Refresh Token 是否在 Redis 中存在
     *
     * @param jti Refresh Token 的唯一标识
     * @return true-存在且有效，false-不存在或已失效
     */
    public boolean validateRefreshToken(String jti) {
        String key = REFRESH_TOKEN_PREFIX + jti;
        Boolean exists = redisTemplate.hasKey(key);
        return exists != null && exists;
    }

    /**
     * 删除 Refresh Token（用于登出或强制下线）
     *
     * @param jti Refresh Token 的唯一标识
     */
    public void deleteRefreshToken(String jti) {
        String key = REFRESH_TOKEN_PREFIX + jti;
        redisTemplate.delete(key);
        log.info("Deleted refresh token: {}", key);
    }

    /**
     * 根据用户 ID 删除所有 Refresh Token（用于封禁账号或改密）
     *
     * @param userId 用户 ID
     */
    public void deleteAllUserRefreshTokens(Long userId) {
        // 扫描所有以 refresh:token: 开头的 key
        String pattern = REFRESH_TOKEN_PREFIX + "*";
        
        try {
            // 获取所有匹配的 key
            java.util.Set<String> keys = redisTemplate.keys(pattern);
            
            if (keys != null && !keys.isEmpty()) {
                int deletedCount = 0;
                for (String key : keys) {
                    // 获取 token 并解析，检查是否属于该用户
                    String token = redisTemplate.opsForValue().get(key);
                    if (token != null) {
                        try {
                            JwtUtil.RefreshTokenInfo info = jwtUtil.validateRefreshToken(token);
                            if (info.getUserId().equals(userId)) {
                                redisTemplate.delete(key);
                                deletedCount++;
                                log.debug("Deleted refresh token for user {}: {}", userId, key);
                            }
                        } catch (Exception e) {
                            // 忽略无效的 token，直接删除
                            redisTemplate.delete(key);
                            deletedCount++;
                        }
                    }
                }
                log.info("Deleted {} refresh tokens for user {}", deletedCount, userId);
            } else {
                log.info("No refresh tokens found for user {}", userId);
            }
        } catch (Exception e) {
            log.error("Error deleting refresh tokens for user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to delete user refresh tokens", e);
        }
    }

    /**
     * 刷新 Access Token（使用 Refresh Token）
     *
     * @param refreshToken 原始的 refresh token
     * @return 新的双 Token 对
     */
    public JwtUtil.TokenPair refreshAccessToken(String refreshToken) {
        // 1. 解析 refresh token
        JwtUtil.RefreshTokenInfo refreshInfo = jwtUtil.validateRefreshToken(refreshToken);

        // 2. 检查 jti 是否在 Redis 中存在
        if (!validateRefreshToken(refreshInfo.getJti())) {
            throw new JwtUtil.TokenStatusException("Refresh token not found in Redis or has been invalidated", 401);
        }

        // 3. 查询数据库获取最新状态（可选，但推荐）
        // 这里可以注入 SysUserMapper 来检查用户状态
        // 为了解耦，建议在 Interceptor 或 Controller 层处理

        // 4. 生成新的双 token 对
        return jwtUtil.generateTokenPair(
                refreshInfo.getUserId(),
                refreshInfo.getUsername(),
                JwtStatus.NORMAL, // 假设状态正常，实际应从数据库读取
                System.currentTimeMillis() // 更新数据版本号
        );
    }
}
