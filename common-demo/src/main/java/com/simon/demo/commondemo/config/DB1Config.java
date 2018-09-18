package com.simon.demo.commondemo.config;

import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
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
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(@Qualifier("dataSource1") final DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean lef = new LocalContainerEntityManagerFactoryBean();
        lef.setDataSource(dataSource);
        lef.setPersistenceProviderClass(HibernatePersistenceProvider.class);
        lef.setPackagesToScan("com.simon.demo.commondemo.entities.db1");
        return lef;
    }

	@Primary
    @Bean(name = "transactionManager1")
    public PlatformTransactionManager transactionManager(@Qualifier("entityManagerFactory1") final EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

}
