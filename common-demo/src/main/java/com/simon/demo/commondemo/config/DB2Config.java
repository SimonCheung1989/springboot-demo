package com.simon.demo.commondemo.config;

import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
        entityManagerFactoryRef = "entityManagerFactory2",
        transactionManagerRef = "transactionManager2",
        basePackages = { "com.simon.demo.commondemo.dao.db2" }
)
public class DB2Config {

    @Bean(name = "dataSource2")
    @ConfigurationProperties(prefix = "db2.spring.datasource")
    public DataSource dataSource() {
    	return DataSourceBuilder.create().build();

    }

    @Bean(name = "entityManagerFactory2")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(@Qualifier("dataSource2") final DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean lef = new LocalContainerEntityManagerFactoryBean();
        lef.setDataSource(dataSource);
        lef.setPersistenceProviderClass(HibernatePersistenceProvider.class);
        lef.setPackagesToScan("com.simon.demo.commondemo.entities.db2");
        return lef;
    }

    @Bean(name = "transactionManager2")
    public PlatformTransactionManager transactionManager(@Qualifier("entityManagerFactory2") final EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

}
