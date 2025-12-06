package com.kulturman.irembotest;

import com.github.springtestdbunit.bean.DatabaseConfigBean;
import com.github.springtestdbunit.bean.DatabaseDataSourceConnectionFactoryBean;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DbUnitConfig {

    @Bean
    public DatabaseConfigBean dbUnitDatabaseConfig() {
        DatabaseConfigBean config = new DatabaseConfigBean();
        config.setDatatypeFactory(new PostgresqlDataTypeFactory());
        config.setCaseSensitiveTableNames(false);
        return config;
    }

    @Bean
    public DatabaseDataSourceConnectionFactoryBean dbUnitDatabaseConnection(DataSource dataSource) {
        DatabaseDataSourceConnectionFactoryBean connectionFactory = new DatabaseDataSourceConnectionFactoryBean();
        connectionFactory.setDataSource(dataSource);
        connectionFactory.setDatabaseConfig(dbUnitDatabaseConfig());
        return connectionFactory;
    }
}
