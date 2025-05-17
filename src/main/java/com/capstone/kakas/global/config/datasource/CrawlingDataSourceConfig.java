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
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "crawlingEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean crawlingEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("crawlingDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("com.capstone.crawling.domain") // Entity 경로
                .persistenceUnit("crawling")
                .build();
    }

    @Bean(name = "crawlingTransactionManager")
    public PlatformTransactionManager crawlingTransactionManager(
            @Qualifier("crawlingEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
