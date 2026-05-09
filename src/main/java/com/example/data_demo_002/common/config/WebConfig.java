package com.example.data_demo_002.common.config;

import com.example.data_demo_002.common.util.Jwt.JwtInterceptor;
import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        // 认证相关
                        "/users/login",
                        "/users/loginByUserName",
                        "/users/register",
                        "/users/*/NotLoginPasswordByUserName",
                        "/users/*/NotLoginPasswordByEmail",

                        // 刷新 token
                        "/auth/refresh",
                        //邮箱验证
                        "/email/send-code",
                        "/email/check-code",
                        // 系统错误页
                        "/error",
                        "/favicon.ico",
                        // Swagger UI 相关路径
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/webjars/**",
                        // 监控端点
                        "/actuator/**",
                        // ==================== 文章模块公开接口 ====================
                        // 前台端接口（无需鉴权）
                        "/article/portal/**",
                        // 标签模糊搜索接口（用户创建文章时自动匹配）
                        "/article/tag/search"
                );
    }

    /**
     * 配置字符编码
     */
    @Override
    public void configureContentNegotiation(org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer configurer) {
        configurer.defaultContentType(org.springframework.http.MediaType.APPLICATION_JSON);
    }
}