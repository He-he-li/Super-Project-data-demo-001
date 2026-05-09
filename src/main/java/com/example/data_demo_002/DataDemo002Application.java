package com.example.data_demo_002;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

/**
 * Spring Boot 应用主启动类
 * 
 * 该类是整个应用的入口点，使用 @SpringBootApplication 注解标识，
 * 自动配置 Spring 应用上下文并启用组件扫描。
 * 
 * @author Spring Boot
 * @version 1.0
 */
@PropertySource("file:.env")
@SpringBootApplication
@MapperScan("com.example.data_demo_002.common.base.mapper")
public class DataDemo002Application {

    /**
     * 应用主方法
     * 
     * 启动 Spring Boot 应用程序，初始化嵌入式服务器（如 Tomcat）
     * 并加载所有自动配置。
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(DataDemo002Application.class, args);
    }

}
