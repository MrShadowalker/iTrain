package org.itron.itrain.utils.database.sqlite;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.Resource;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Map;

/**
 * 基于 JPA 来操作 SQLite
 *
 * @author Shadowalker
 */
@Configuration
@EnableJpaRepositories(basePackages = "org.itron.itrain.core.repository", transactionManagerRef = "jpaTransactionManager", entityManagerFactoryRef = "localContainerEntityManagerFactoryBean")
@EnableTransactionManagement
public class JpaConfiguration {

    @Resource(name = "EmbeddeddataSource")
    private DataSource sqliteDataSource;

    @Resource
    private JpaProperties jpaProperties;

    @Resource
    private HibernateProperties hibernateProperties;

    @Autowired
    @Bean
    public JpaTransactionManager jpaTransactionManager(
            @Qualifier(value = "EmbeddeddataSource") DataSource dataSource,
            EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager jpaTransactionManager = new JpaTransactionManager();
        jpaTransactionManager.setEntityManagerFactory(entityManagerFactory);
        jpaTransactionManager.setDataSource(dataSource);
        return jpaTransactionManager;
    }

    /**
     * 配置 EntityManagerFactory 实体
     *
     * @param builder
     * @return 实体管理工厂 packages 扫描 @Entity 注释的软件包名称 persistenceUnit 持久性单元的名称。
     * 如果只建立一个 EntityManagerFactory，可以省略这个，但是如果在同一个应用程序中有多个，应该给它们不同的名字
     * properties 标准 JPA 或供应商特定配置的通用属性。这些属性覆盖构造函数中提供的任何值。
     */
    @Autowired
    @Bean
    LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean(
            @Qualifier(value = "EmbeddeddataSource") DataSource dataSource,
            EntityManagerFactoryBuilder builder) {
        return builder
//                .dataSource(sqliteDataSource)
                .dataSource(dataSource)
                .packages("org.itron.itrain.core.model")
                .properties(getVendorProperties())
                .build();
    }

    // Springboot 1.5.x 版本可使用此方法
    //    private Map<String, String> getVendorProperties(DataSource dataSource) {
    //        return jpaProperties.getHibernateProperties(new HibernateSettings());
    //    }

    // 2.1.x 之后的解决方案
    private Map<String, Object> getVendorProperties() {
        return hibernateProperties.determineHibernateProperties(
                jpaProperties.getProperties(), new HibernateSettings());
    }
}
