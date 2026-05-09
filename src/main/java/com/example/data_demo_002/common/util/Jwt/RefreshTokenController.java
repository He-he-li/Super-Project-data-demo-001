package com.example.data_demo_002.common.util.Jwt;


import com.example.data_demo_002.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * Refresh Token 控制器
 * 提供刷新 token 的接口
 */
@Slf4j
@Tag(name = "Token 刷新", description = "提供刷新 Token 的接口")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class RefreshTokenController {

    private final RefreshTokenService refreshTokenService;

    /**
     * 刷新 Access Token
     * 使用 Refresh Token 获取新的 Access Token 和 Refresh Token
     *
     * @param refreshTokenFromHeader Refresh Token (从请求头)
     * @param requestBody Refresh Token (从请求体)
     * @return 新的双 Token 对
     */
    @Operation(summary = "刷新 Token", description = "使用 Refresh Token 获取新的 Access Token 和 Refresh Token")
    @PostMapping("/refresh")
    public Result<JwtUtil.TokenPair> refreshToken(
            @Parameter(description = "Refresh Token (从请求头传递)", required = false)
            @RequestHeader(value = "Refresh-Token", required = false) String refreshTokenFromHeader,
            
            @RequestBody(required = false) RefreshTokenRequest requestBody) {


        // 优先从请求头获取，如果没有则从请求体获取
        String refreshToken = refreshTokenFromHeader;
        if (refreshToken == null && requestBody != null) {
            refreshToken = requestBody.getRefreshToken();
        }

        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            throw new IllegalArgumentException("缺少必需参数：Refresh-Token");
        }

        // 去除可能的 Bearer 前缀、空格和引号
        refreshToken = refreshToken.trim();
        if (refreshToken.startsWith("Bearer ")) {
            refreshToken = refreshToken.substring(7).trim();
        }
        // 去除可能存在的前后引号
        if (refreshToken.startsWith("\"") && refreshToken.endsWith("\"")) {
            refreshToken = refreshToken.substring(1, refreshToken.length() - 1);
        }

        log.info("Refreshing access token, token length: {}", refreshToken.length());
        log.debug("RefreshToken first 50 chars: {}", refreshToken.substring(0, Math.min(50, refreshToken.length())));
        
        try {
            JwtUtil.TokenPair tokenPair = refreshTokenService.refreshAccessToken(refreshToken);
            log.info("Token refresh successful for user: {}", tokenPair.getToken().substring(0, 20));
            return Result.success(tokenPair);
        } catch (Exception e) {
            log.error("Failed to refresh token: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 用于接收请求体的 DTO
     */
    @lombok.Data
    public static class RefreshTokenRequest {
        @Parameter(description = "Refresh Token", required = true)
        private String refreshToken;
    }
}
