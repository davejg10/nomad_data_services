package com.nomad.job_orchestrator.config;

import com.nomad.library.connectors.Neo4jConfig;

import lombok.extern.log4j.Log4j2;

import org.neo4j.driver.Driver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.neo4j.core.Neo4jClient;

@Log4j2
@Profile("!local")
@Configuration
public class Neo4jConnector {

    @Value("${key_vault_uri}")
    private String key_vault_uri;
    @Value("${neo4j_uri}")
    private String neo4j_uri;
    @Value("${neo4j_user}")
    private String neo4j_user;
    @Value("${neo4j_password_key}")
    private String neo4j_password_key;
    @Value("${AZURE_CLIENT_ID}")
    private String AZURE_CLIENT_ID;

    @Bean
    public Driver neo4jDriver() {
        return new Neo4jConfig(AZURE_CLIENT_ID, key_vault_uri, neo4j_uri, neo4j_user, neo4j_password_key).neo4jDriver();
    }

    @Bean
    public Neo4jClient neo4jClient(Driver driver) {
        return Neo4jClient.create(driver);
    }
}