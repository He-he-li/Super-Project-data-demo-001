package com.example.data_demo_002.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
// 移除全局 security 要求，改为在 Controller 上单独控制
@OpenAPIDefinition(
        info = @Info(title = "用户管理系统 API 文档", version = "1.0.0"),
        servers = { @Server(url = "http://localhost:8081") }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER,
        description = "请输入 JWT Token (无需加 'Bearer ' 前缀)"
)
public class OpenApiConfig {
}