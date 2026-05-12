package com.example.data_demo_002.common.config;

import com.example.data_demo_002.common.util.Jwt.JwtInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;
    
    private final TenantInterceptor tenantInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 1. JWT认证拦截器
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                    "/users/login",
                    "/users/register",
                    "/swagger-ui/**",
                    "/v3/api-docs/**"
                );
        
        // 2. 租户隔离拦截器
        registry.addInterceptor(tenantInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                    "/users/login",
                    "/users/register",
                    "/swagger-ui/**",
                    "/v3/api-docs/**"
                );
    }
}