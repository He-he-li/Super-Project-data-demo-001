package com.example.data_demo_002.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

@Configuration
public class MybatisPlusConfig {

    /**
     * 配置 MybatisPlusInterceptor (包含分页、乐观锁、租户隔离)
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 1. 租户插件（多租户数据隔离）
        TenantLineInnerInterceptor tenantInterceptor = new TenantLineInnerInterceptor();
        tenantInterceptor.setTenantLineHandler(new com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler() {
            @Override
            public Expression getTenantId() {
                Long tenantId = TenantContext.getTenantId();
                if (tenantId != null) {
                    return new LongValue(tenantId);
                }
                return null;
            }

            @Override
            public String getTenantIdColumn() {
                return "organization_id";
            }

            @Override
            public boolean ignoreTable(String tableName) {
                // 不需要租户隔离的系统表
                return "sys_organization".equals(tableName) ||
                        "sys_permission".equals(tableName) ||
                        "sys_user".equals(tableName) ||
                        "sys_role".equals(tableName) ||
                        "sys_user_role".equals(tableName) ||
                        "sys_role_permission".equals(tableName);
            }
        });
        interceptor.addInnerInterceptor(tenantInterceptor);

        // 2. 分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.POSTGRE_SQL));

        // 3. 乐观锁插件（自动维护 @Version 版本号）
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        return interceptor;
    }

    /**
     * 手动创建 SqlSessionFactory
     * 必须手动将拦截器设置进去，因为覆盖了自动配置
     */
    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource, MybatisPlusInterceptor mybatisPlusInterceptor) throws Exception {
        MybatisSqlSessionFactoryBean factoryBean = new MybatisSqlSessionFactoryBean();

        // 1. 注入数据源
        factoryBean.setDataSource(dataSource);

        // 2. 手动注入拦截器
        factoryBean.setPlugins(mybatisPlusInterceptor);

        // 3. 配置 XML 映射文件路径（可选）
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            factoryBean.setMapperLocations(resolver.getResources("classpath:mapper/*.xml"));
        } catch (Exception e) {
            System.out.println("未检测到 XML 映射文件，将使用纯注解模式。");
        }

        return factoryBean.getObject();
    }
}