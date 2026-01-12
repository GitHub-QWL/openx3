package com.openx3.framework.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@ConditionalOnClass(EntityManagerFactory.class)
@EnableJpaRepositories( basePackages = "com.openx3.**.repository", transactionManagerRef = "transactionManager")
@EntityScan(basePackages = {
        "com.openx3.**.entity",      // 扫描所有模块下的 entity 包 (包含 SysUser)
        "com.openx3.framework.jpa"   // 扫描 BaseEntity (如果它不在 entity 包下)
})
public class JpaConfig {

    // 配置 JPA 事务管理器为默认管理器
    // 因为 JPA 事务管理器是基于 JDBC 的，它也能管理 MyBatis 的事务，兼容性最好
    @Bean
    @Primary
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}