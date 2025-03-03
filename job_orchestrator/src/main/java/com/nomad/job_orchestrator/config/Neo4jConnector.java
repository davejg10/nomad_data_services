package com.nomad.job_orchestrator.config;

import com.nomad.library.connectors.Neo4jConfig;

import java.util.concurrent.TimeUnit;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.core.Neo4jClient;

@Log4j2
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

    @Bean
    public Neo4jClient neo4jDriver(String key_vault_uri, String neo4j_uri, String neo4j_user, String neo4j_password_key) {
        return new Neo4jConfig(key_vault_uri, neo4j_uri, neo4j_user, neo4j_password_key).neo4jClient();
    }
}