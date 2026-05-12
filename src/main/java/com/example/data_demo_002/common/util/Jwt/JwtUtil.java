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
import java.util.UUID;

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-expiration:1800000}")
    private Long accessExpiration;

    @Value("${jwt.refresh-expiration:604800000}")
    private Long refreshExpiration;

    @Value("${jwt.prefix}")
    private String prefix;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成 Access Token
     */
    public String generateAccessToken(Long userId, String username, int status, Long dataVersion, Map<String, Object> extraClaims) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("status", status);
        claims.put("dataVersion", dataVersion);
        claims.put("tokenType", "ACCESS");

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
     */
    public String generateRefreshToken(Long userId, String username, String jti) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("jti", jti);
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
     * 生成双Token（Access + Refresh）- 不带单位ID
     */
    public TokenPair generateTokenPair(Long userId, String username, int status, Long dataVersion) {
        String jti = UUID.randomUUID().toString();
        String accessToken = generateAccessToken(userId, username, status, dataVersion, null);
        String refreshToken = generateRefreshToken(userId, username, jti);

        TokenPair pair = new TokenPair();
        pair.setToken(accessToken);
        pair.setRefreshToken(refreshToken);
        pair.setExpiresIn(accessExpiration / 1000);
        pair.setRefreshExpiresIn(refreshExpiration / 1000);
        return pair;
    }

    /**
     * 生成双Token（带单位ID）
     */
    public TokenPair generateTokenPair(Long userId, String username, int status, Long dataVersion, Long organizationId) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("organizationId", organizationId);
        
        String jti = UUID.randomUUID().toString();
        String accessToken = generateAccessToken(userId, username, status, dataVersion, extraClaims);
        
        Map<String, Object> refreshClaims = new HashMap<>();
        refreshClaims.put("organizationId", organizationId);
        String refreshToken = generateRefreshTokenWithClaims(userId, username, jti, refreshClaims);

        TokenPair pair = new TokenPair();
        pair.setToken(accessToken);
        pair.setRefreshToken(refreshToken);
        pair.setExpiresIn(accessExpiration / 1000);
        pair.setRefreshExpiresIn(refreshExpiration / 1000);
        return pair;
    }

    /**
     * 生成带自定义Claims的Refresh Token
     */
    private String generateRefreshTokenWithClaims(Long userId, String username, String jti, Map<String, Object> extraClaims) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("jti", jti);
        claims.put("tokenType", "REFRESH");
        
        if (extraClaims != null) {
            claims.putAll(extraClaims);
        }

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
     * 从Token中获取单位ID
     */
    public Long getOrganizationIdFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            
            Object orgId = claims.get("organizationId");
            if (orgId != null) {
                return Long.valueOf(orgId.toString());
            }
            return null;
        } catch (Exception e) {
            log.error("解析Token中的单位ID失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从Token中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
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
     * 校验 Access Token 并尝试滑动续期
     */
    public TokenInfo validateAccessToken(String token) {
        Claims claims = parseToken(token);

        String tokenType = claims.get("tokenType", String.class);
        if (!"ACCESS".equals(tokenType)) {
            throw new JwtException("Invalid token type: expected ACCESS, got " + tokenType);
        }

        Integer status = claims.get("status", Integer.class);
        if (status == null) {
            throw new JwtException("Token missing status field");
        }

        if (status != JwtStatus.NORMAL) {
            String msg = JwtStatus.getStatusDesc(status);
            throw new TokenStatusException("Token status invalid: " + msg, status);
        }

        TokenInfo info = new TokenInfo();
        info.setUserId(Long.valueOf(claims.get("userId").toString()));
        info.setUsername(claims.getSubject());
        info.setStatus(status);
        info.setDataVersion(Long.valueOf(claims.get("dataVersion").toString()));
        info.setValid(true);

        Date expiration = claims.getExpiration();
        long now = System.currentTimeMillis();
        long remainingTime = expiration.getTime() - now;

        if (remainingTime < accessExpiration / 2) {
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
     */
    public RefreshTokenInfo validateRefreshToken(String token) {
        Claims claims = parseToken(token);

        String tokenType = claims.get("tokenType", String.class);
        if (!"REFRESH".equals(tokenType)) {
            throw new JwtException("Invalid token type: expected REFRESH, got " + tokenType);
        }

        RefreshTokenInfo info = new RefreshTokenInfo();
        info.setUserId(Long.valueOf(claims.get("userId").toString()));
        info.setUsername(claims.getSubject());
        info.setJti(claims.get("jti", String.class));
        info.setValid(true);

        return info;
    }

    /**
     * 解析 Token
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

    @lombok.Data
    @lombok.NoArgsConstructor
    public static class RefreshTokenInfo {
        private Long userId;
        private String username;
        private String jti;
        private boolean isValid;
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    public static class TokenPair {
        private String token;
        private String refreshToken;
        private Long expiresIn;
        private Long refreshExpiresIn;
    }

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