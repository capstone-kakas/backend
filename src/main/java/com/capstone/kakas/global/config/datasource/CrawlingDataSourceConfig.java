package com.capstone.kakas.global.config.datasource;


import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.*;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.capstone.kakas.crawlingdb.repository",
        entityManagerFactoryRef = "crawlingEntityManagerFactory",
        transactionManagerRef = "crawlingTransactionManager"
)
public class CrawlingDataSourceConfig {

    @Bean(name = "crawlingDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.crawling")
    public DataSource crawlingDataSource() {
        return DataSourceBuilder.create()
                .type(com.zaxxer.hikari.HikariDataSource.class) // 명시적 타입 지정
                .build();
    }

    @Bean(name = "crawlingEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean crawlingEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("crawlingDataSource") DataSource dataSource) {

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        properties.put("hibernate.show_sql", true);
        properties.put("hibernate.format_sql", true);
        properties.put("hibernate.use_sql_comments", true);
        properties.put("hibernate.default_batch_fetch_size", 1000);

        return builder
                .dataSource(dataSource)
                .packages("com.capstone.kakas.crawlingdb.domain")
                .persistenceUnit("crawling")
                .properties(properties) // ✅ 여기!
                .build();
    }

    @Bean(name = "crawlingTransactionManager")
    public PlatformTransactionManager crawlingTransactionManager(
            @Qualifier("crawlingEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
