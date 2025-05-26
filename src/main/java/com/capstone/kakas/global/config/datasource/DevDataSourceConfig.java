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
        basePackages = "com.capstone.kakas.devdb.repository",
        entityManagerFactoryRef = "devEntityManagerFactory",
        transactionManagerRef = "devTransactionManager"
)
public class DevDataSourceConfig {

    @Bean(name = "devDataSource")
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.dev")
    public DataSource devDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean(name = "devEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean devEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("devDataSource") DataSource dataSource) {

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "create");
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        properties.put("hibernate.show_sql", true);
        properties.put("hibernate.format_sql", true);
        properties.put("hibernate.use_sql_comments", true);
        properties.put("hibernate.default_batch_fetch_size", 1000);

        return builder
                .dataSource(dataSource)
                .packages("com.capstone.kakas.devdb.domain")
                .persistenceUnit("dev")
                .properties(properties)
                .build();
    }

    @Primary
    @Bean(name = "devTransactionManager")
    public PlatformTransactionManager devTransactionManager(
            @Qualifier("devEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
