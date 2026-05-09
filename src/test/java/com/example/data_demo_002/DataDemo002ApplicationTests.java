package com.example.data_demo_002;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Spring Boot 应用测试类
 * 包含数据库连接测试
 */
@SpringBootTest
class DataDemo002ApplicationTests {

    @Autowired
    private DataSource dataSource;

    /**
     * 测试 Spring 应用上下文能否正确加载
     */
    @Test
    void contextLoads() {
        assertNotNull(dataSource, "数据源应该成功注入");
        System.out.println("数据源：" + dataSource.getClass().getName());
    }

    /**
     * 测试数据库连接是否成功
     * 验证能否从数据源获取到有效的数据库连接
     */
    @Test
    void testDatabaseConnection() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            assertNotNull(connection, "数据库连接不应为空");
            assertTrue(connection.isValid(5), "数据库连接应该在 5 秒内有效");
            
            DatabaseMetaData metaData = connection.getMetaData();
            System.out.println("=== 数据库连接信息 ===");
            System.out.println("数据库产品名称：" + metaData.getDatabaseProductName());
            System.out.println("数据库产品版本：" + metaData.getDatabaseProductVersion());
            System.out.println("数据库 URL: " + metaData.getURL());
            System.out.println("当前用户：" + metaData.getUserName());
            
            assertThat(metaData.getDatabaseProductName())
                .as("数据库应该是 PostgreSQL")
                .containsIgnoringCase("PostgreSQL");
        }
    }

    /**
     * 测试数据库连接池是否正常工作
     */
    @Test
    void testDataSourcePool() throws SQLException {
        for (int i = 0; i < 3; i++) {
            try (Connection connection = dataSource.getConnection()) {
                assertTrue(connection.isValid(3), 
                    "第 " + (i + 1) + " 次获取的连接应该有效");
            }
        }
        System.out.println("数据库连接池测试通过");
    }
}
