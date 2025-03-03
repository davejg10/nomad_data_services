package com.nomad.admin_api.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import lombok.extern.log4j.Log4j2;

@Configuration
@Log4j2
public class PostgresConnector {

    @Bean
    public Connection postgresConnection() throws Exception {

        Properties properties = new Properties();
        properties.load(PostgresConnector.class.getClassLoader().getResourceAsStream("application.properties"));

        Connection connection = DriverManager.getConnection(properties.getProperty("url"), properties);

        log.info("Database connection test: " + connection.getCatalog());

        return connection;
    }
}
