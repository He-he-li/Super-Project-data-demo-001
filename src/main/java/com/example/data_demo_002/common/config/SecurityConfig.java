package com.example.data_demo_002.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

/**
 * Spring Security 配置类
 * 
 * 说明：本项目使用 JWT 进行认证，Spring Security 仅用于密码加密和基础配置
 * 实际的请求鉴权逻辑由 JwtInterceptor（MVC 拦截器）处理
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {



    /**
     * 【新增关键代码】
     * 定义一个空的 UserDetailsService，防止 Spring Boot 自动生成随机密码和内存用户
     * 因为我们的认证逻辑完全由 JwtInterceptor 处理，Spring Security 不需要加载用户
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            // 这里永远不会被调用，因为 formLogin 和 httpBasic 都禁用了
            // 但它的存在能欺骗 Spring Boot 自动配置，让它以为我们已经配置好了
            throw new UnsupportedOperationException("Authentication is handled by JwtInterceptor, not Spring Security UserDetailsService");
        };
    }

    /**
     * 配置密码编码器
     * 使用 BCrypt 算法加密密码，增强安全性
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 配置安全过滤器链
     * 由于使用 JWT 认证，这里仅做基础配置，具体鉴权交给 JwtInterceptor
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. 禁用 CSRF 防护
                // API 项目通常禁用 CSRF，因为 JWT 本身已提供安全防护
                .csrf(AbstractHttpConfigurer::disable)

                // 2. 禁用表单登录和 HTTP Basic 认证
                // 本项目使用 JWT Token 认证，不需要传统的登录方式
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                // 3. 设置 Session 策略为无状态（STATELESS）
                // Spring Security 不会创建 HttpSession，每次请求都必须携带 JWT Token
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 4. 配置未认证时的异常处理
                // 返回 401 状态码和 JSON 响应，而不是跳转到登录页面
                .exceptionHandling(ex ->
                        ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )

                // 5. 配置请求授权规则
                .authorizeHttpRequests(auth -> auth
                        // 放行公开接口（无需 JWT Token）
                        // 认证接口：/auth/**（登录、注册等）
                        .requestMatchers("/auth/**").permitAll()
                        // 放行公开接口（无需 JWT Token）
                        //.requestMatchers("/email/send", "/email/check-code").permitAll()
                        // API 文档：Swagger UI 和 OpenAPI 文档
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // 错误页面：系统错误处理
                        .requestMatchers("/error").permitAll()

                        // 【关键配置】其他所有请求都允许通过（permitAll）
                        // 原因：具体的 JWT 鉴权逻辑已经交给了 JwtInterceptor（MVC 拦截器）处理
                        // 如果在这里写 .anyRequest().authenticated()，请求会在进入 JwtInterceptor 之前
                        // 就被 Spring Security 拦截并返回 401，导致 JWT 拦截器无法生效
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}