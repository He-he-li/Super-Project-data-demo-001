package com.example.data_demo_002.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

@Configuration
public class MybatisPlusConfig {

    /**
     * 配置 MybatisPlusInterceptor (包含分页和乐观锁)
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 1. 分页插件 (注意： DbType 要根据你实际数据库修改，你之前写的是 POSTGRE_SQL)
        // 如果是 MySQL 8+, 请用 DbType.MYSQL
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.POSTGRE_SQL));

        // 2. 【关键】乐观锁插件 (用于自动维护 @Version 版本号)
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        return interceptor;
    }

    /**
     * 手动创建 SqlSessionFactory
     * 【修正点】必须手动将拦截器设置进去，因为覆盖了自动配置
     */
    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource, MybatisPlusInterceptor mybatisPlusInterceptor) throws Exception {
        MybatisSqlSessionFactoryBean factoryBean = new MybatisSqlSessionFactoryBean();

        // 1. 注入数据源
        factoryBean.setDataSource(dataSource);

        // 2. 【核心修正】手动注入拦截器
        factoryBean.setPlugins(mybatisPlusInterceptor);

        // 3. 配置 XML 映射文件路径 (可选)
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            // 尝试加载 mapper/*.xml，如果没有也不会报错
            factoryBean.setMapperLocations(resolver.getResources("classpath:mapper/*.xml"));
        } catch (Exception e) {
            System.out.println("未检测到 XML 映射文件，将使用纯注解模式。");
        }

        return factoryBean.getObject();
    }
}