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
        basePackages = "com.capstone.kakas.devdb.repository",
        entityManagerFactoryRef = "devEntityManagerFactory",
        transactionManagerRef = "devTransactionManager"
)
public class DevDataSourceConfig {

    @Primary
    @Bean(name = "devDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.dev")
    public DataSource devDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean(name = "devEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean devEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("devDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("com.capstone.dev.domain") // Entity 경로
                .persistenceUnit("dev")
                .build();
    }

    @Primary
    @Bean(name = "devTransactionManager")
    public PlatformTransactionManager devTransactionManager(
            @Qualifier("devEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
