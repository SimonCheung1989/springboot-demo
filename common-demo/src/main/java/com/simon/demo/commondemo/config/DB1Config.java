package com.simon.demo.commondemo.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef = "entityManagerFactory1",
        transactionManagerRef = "transactionManager1",
        basePackages = { "com.simon.demo.commondemo.dao.db1" }
)
public class DB1Config {

    @Primary
    @Bean(name = "dataSource1")
    @ConfigurationProperties(prefix = "db1.spring.datasource")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();

    }

    @Primary
    @Bean(name = "entityManagerFactory1")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder builder, @Qualifier("dataSource1") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("com.simon.demo.commondemo.entities.db1")
                .persistenceUnit("UserEntity")
                .build();
    }

    @Primary
    @Bean(name = "transactionManager1")
    public PlatformTransactionManager transactionManager(@Qualifier("entityManagerFactory1") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

}
