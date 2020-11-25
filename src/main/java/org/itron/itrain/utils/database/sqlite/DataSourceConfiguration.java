package org.itron.itrain.utils.database.sqlite;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.sqlite.SQLiteDataSource;

import javax.sql.DataSource;

/**
 * 配置 SQLite 数据库的 DataSource
 *
 * @author Shadowalker
 */
@Slf4j
@Configuration
public class DataSourceConfiguration {

    @Value("${sqlite.dbName}")
    private String dbName;

    @Bean(destroyMethod = "", name = "EmbeddeddataSource")
    public DataSource dataSource() {
        // Spring Boot 1.5.4  2.0.4
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName("org.sqlite.JDBC");
        dataSourceBuilder.url("jdbc:sqlite:" + dbName);
        dataSourceBuilder.type(SQLiteDataSource.class);
        return dataSourceBuilder.build();
    }
}
