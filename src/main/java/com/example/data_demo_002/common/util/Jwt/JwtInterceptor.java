package com.example.data_demo_002.common.util.Jwt;

import com.example.data_demo_002.common.base.domain.SysUser;
import com.example.data_demo_002.common.base.mapper.SysUserMapper;
import com.example.data_demo_002.common.result.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final SysUserMapper sysUserMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String bearerToken = request.getHeader("Authorization");
        String token = jwtUtil.resolveToken(bearerToken);

        if (token == null) {
            writeJsonResponse(response, 401, "TOKEN_MISSING", "缺少认证令牌");
            return false;
        }

        try {
            JwtUtil.TokenInfo info = jwtUtil.validateAccessToken(token);

            SysUser dbSysUser = sysUserMapper.selectById(info.getUserId());

            if (dbSysUser == null) {
                log.warn("User not found for ID: {}", info.getUserId());
                writeJsonResponse(response, 401, "USER_NOT_FOUND", "用户不存在");
                return false;
            }

            if (dbSysUser.getStatus() != JwtStatus.NORMAL) {
                String errorCode = "ACCOUNT_ABNORMAL";
                String message = "账户状态异常";
                int httpStatus = 403;

                if (dbSysUser.getStatus() == JwtStatus.FROZEN) {
                    errorCode = "ACCOUNT_FROZEN";
                    message = "账户已被冻结";
                    httpStatus = 403;
                } else if (dbSysUser.getStatus() == JwtStatus.PASSWORD_CHANGED) {
                    errorCode = "PASSWORD_CHANGED";
                    message = "密码已修改";
                    httpStatus = 401;
                } else if (dbSysUser.getStatus() == JwtStatus.FORCE_LOGOUT) {
                    errorCode = "FORCE_LOGOUT";
                    message = "已被强制下线";
                    httpStatus = 401;
                }

                log.warn("Access denied for user {}: status={}", dbSysUser.getUsername(), dbSysUser.getStatus());
                writeJsonResponse(response, httpStatus, errorCode, message);
                return false;
            }

            if (dbSysUser.getVersion() != null && dbSysUser.getVersion() > info.getDataVersion()) {
                log.info("Token version mismatch for user: {}. DB Version: {}, Token Version: {}",
                        dbSysUser.getUsername(), dbSysUser.getVersion(), info.getDataVersion());
                writeJsonResponse(response, 401, "VERSION_MISMATCH", "令牌版本不匹配");
                return false;
            }

            UserContext.setUserId(dbSysUser.getId());
            UserContext.setUsername(dbSysUser.getUsername());

            if (info.isShouldRenew()) {
                response.setHeader("Authorization-New", "Bearer " + info.getNewAccessToken());
                log.debug("Access token renewed for user: {}", dbSysUser.getUsername());
            }

            return true;

        } catch (JwtUtil.TokenStatusException e) {
            log.warn("Token status exception: {}", e.getMessage());
            
            if (e.getStatusCode() == JwtStatus.TOKEN_EXPIRED) {
                writeJsonResponse(response, 401, "TOKEN_EXPIRED", "Token已过期，请刷新");
            } else if (e.getStatusCode() == JwtStatus.PASSWORD_CHANGED) {
                writeJsonResponse(response, 8888, "PASSWORD_CHANGED", "密码已修改，请重新登录");
            } else if (e.getStatusCode() == JwtStatus.FORCE_LOGOUT) {
                writeJsonResponse(response, 8888, "FORCE_LOGOUT", "已被强制下线");
            } else if (e.getStatusCode() == JwtStatus.FROZEN) {
                writeJsonResponse(response, 7777, "ACCOUNT_FROZEN", "账户已被冻结");
            } else {
                writeJsonResponse(response, 403, "TOKEN_STATUS_INVALID", "Token状态无效");
            }
            return false;
        } catch (Exception e) {
            log.warn("JWT validation failed: {}", e.getMessage(), e);
            writeJsonResponse(response, 401, "TOKEN_INVALID", "Token无效或格式错误");
            return false;
        }
    }

    private void writeJsonResponse(HttpServletResponse response, int businessCode, String errorCode, String message) {
        response.setStatus(200);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        Result<Void> result = Result.error(businessCode, message);
        
        try {
            objectMapper.writeValue(response.getWriter(), result);
        } catch (IOException e) {
            log.error("Failed to write error response", e);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}