package com.example.data_demo_002.common.util.Jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    // Access Token 有效期 (毫秒)，例如 30 分钟 = 1800000
    @Value("${jwt.access-expiration:1800000}")
    private Long accessExpiration;

    // Refresh Token 有效期 (毫秒)，例如 7 天 = 604800000
    @Value("${jwt.refresh-expiration:604800000}")
    private Long refreshExpiration;

    @Value("${jwt.prefix}")
    private String prefix;

    /**
     * 生成密钥
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成 Access Token
     *
     * @param userId      用户 ID
     * @param username    用户名
     * @param status      状态标识 (使用 JwtStatus 常量)
     * @param dataVersion 数据版本号 (用于检测密码/权限变更，建议传 updateTime 的时间戳)
     * @param extraClaims 其他自定义字段 (可选)
     */
    public String generateAccessToken(Long userId, String username, int status, Long dataVersion, Map<String, Object> extraClaims) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("status", status);
        claims.put("dataVersion", dataVersion);
        claims.put("tokenType", "ACCESS"); // 标记 token 类型

        if (extraClaims != null) {
            claims.putAll(extraClaims);
        }

        Date now = new Date();
        Date expireDate = new Date(now.getTime() + accessExpiration);

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(now)
                .expiration(expireDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 生成 Refresh Token
     *
     * @param userId   用户 ID
     * @param username 用户名
     * @param jti      唯一标识 (建议使用 UUID)
     */
    public String generateRefreshToken(Long userId, String username, String jti) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("jti", jti); // 唯一标识，用于 Redis 存储和失效
        claims.put("tokenType", "REFRESH");

        Date now = new Date();
        Date expireDate = new Date(now.getTime() + refreshExpiration);

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(now)
                .expiration(expireDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 生成双 Token（同时返回 access token 和 refresh token）
     */
    public TokenPair generateTokenPair(Long userId, String username, int status, Long dataVersion) {
        String jti = java.util.UUID.randomUUID().toString();
        String accessToken = generateAccessToken(userId, username, status, dataVersion, null);
        String refreshToken = generateRefreshToken(userId, username, jti);

        TokenPair pair = new TokenPair();
        pair.setToken(accessToken);
        pair.setRefreshToken(refreshToken);
        pair.setExpiresIn(accessExpiration / 1000); // 秒
        pair.setRefreshExpiresIn(refreshExpiration / 1000); // 秒
        return pair;
    }

    /**
     * 校验 Access Token 并尝试滑动续期
     *
     * @param token 原始 Token
     * @return TokenInfo 包含解析信息和新生成的 Token (如果需要续期)
     * @throws RuntimeException 如果 Token 无效、过期或状态异常
     */
    public TokenInfo validateAccessToken(String token) {
        Claims claims = parseToken(token);

        // 校验 token 类型
        String tokenType = claims.get("tokenType", String.class);
        if (!"ACCESS".equals(tokenType)) {
            throw new JwtException("Invalid token type: expected ACCESS, got " + tokenType);
        }

        // 校验状态字段
        Integer status = claims.get("status", Integer.class);
        if (status == null) {
            throw new JwtException("Token missing status field");
        }

        if (status != JwtStatus.NORMAL) {
            String msg = JwtStatus.getStatusDesc(status);
            throw new TokenStatusException("Token status invalid: " + msg, status);
        }

        // 提取基础信息
        TokenInfo info = new TokenInfo();
        info.setUserId(Long.valueOf(claims.get("userId").toString()));
        info.setUsername(claims.getSubject());
        info.setStatus(status);
        info.setDataVersion(Long.valueOf(claims.get("dataVersion").toString()));
        info.setValid(true);

        // 滑动续期逻辑
        Date expiration = claims.getExpiration();
        long now = System.currentTimeMillis();
        long remainingTime = expiration.getTime() - now;

        if (remainingTime < accessExpiration / 2) { // 剩余时间不足一半时续期
            log.debug("Access token renew triggered for user: {}, remaining: {} ms", info.getUsername(), remainingTime);
            info.setShouldRenew(true);
            info.setNewAccessToken(generateAccessToken(
                    info.getUserId(),
                    info.getUsername(),
                    info.getStatus(),
                    info.getDataVersion(),
                    null
            ));
        } else {
            info.setShouldRenew(false);
        }

        return info;
    }

    /**
     * 校验 Refresh Token
     *
     * @param token Refresh Token
     * @return RefreshTokenInfo 包含解析信息
     * @throws RuntimeException 如果 Token 无效或过期
     */
    public RefreshTokenInfo validateRefreshToken(String token) {
        Claims claims = parseToken(token);

        // 校验 token 类型
        String tokenType = claims.get("tokenType", String.class);
        if (!"REFRESH".equals(tokenType)) {
            throw new JwtException("Invalid token type: expected REFRESH, got " + tokenType);
        }

        // 提取基础信息
        RefreshTokenInfo info = new RefreshTokenInfo();
        info.setUserId(Long.valueOf(claims.get("userId").toString()));
        info.setUsername(claims.getSubject());
        info.setJti(claims.get("jti", String.class));
        info.setValid(true);

        return info;
    }

    /**
     * 解析 Token (仅解析，不做业务状态校验，供内部使用)
     */
    private Claims parseToken(String token) {
        try {
            log.debug("Parsing token, length: {}, first 50 chars: {}", 
                token.length(), 
                token.substring(0, Math.min(50, token.length())));
            
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.error("Token has expired: {}", e.getMessage());
            throw new TokenStatusException("Token has expired", JwtStatus.TOKEN_EXPIRED);
        } catch (UnsupportedJwtException | MalformedJwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}, message: {}", e.getClass().getName(), e.getMessage());
            throw new RuntimeException("Invalid JWT token", e);
        } catch (Exception e) {
            log.error("JWT parsing failed: {}", e.getMessage(), e);
            throw new RuntimeException("JWT parsing failed", e);
        }
    }

    /**
     * 从 Header 中解析出纯 Token 字符串
     */
    public String resolveToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith(prefix)) {
            return bearerToken.substring(prefix.length()).trim();
        }
        return bearerToken;
    }

    // ================= 内部类 =================

    /**
     * 封装 Access Token 校验结果
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    public static class TokenInfo {
        private Long userId;
        private String username;
        private Integer status;
        private Long dataVersion;
        private boolean isValid;
        private boolean shouldRenew;
        private String newAccessToken;
    }

    /**
     * 封装 Refresh Token 校验结果
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    public static class RefreshTokenInfo {
        private Long userId;
        private String username;
        private String jti;
        private boolean isValid;
    }

    /**
     * 封装双 Token 对
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    public static class TokenPair {
        private String token;
        private String refreshToken;
        private Long expiresIn; // Access Token 有效期 (秒)
        private Long refreshExpiresIn; // Refresh Token 有效期 (秒)
    }

    /**
     * 自定义异常：用于区分是"过期"还是"状态被封禁"
     */
    public static class TokenStatusException extends RuntimeException {
        private final int statusCode;

        public TokenStatusException(String message, int statusCode) {
            super(message);
            this.statusCode = statusCode;
        }

        public int getStatusCode() {
            return statusCode;
        }
    }
}