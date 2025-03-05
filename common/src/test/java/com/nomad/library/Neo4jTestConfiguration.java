package com.nomad.library;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import lombok.extern.log4j.Log4j2;

@Log4j2
@TestConfiguration
public class Neo4jTestConfiguration {

    private static final String neo4jUser = "neo4j";
    private static final String neo4jPass = "mypassword";

    @Bean
    public Driver neo4jDriver(Neo4j embeddedDatabaseServer) {
        return GraphDatabase.driver(
            embeddedDatabaseServer.boltURI().toString(), 
            AuthTokens.basic(neo4jUser, neo4jPass)
        );
    }

    @Bean
    public Neo4jClient neo4jClient(Driver driver) {
        return Neo4jClient.create(driver);
    }

    @Bean
    public Neo4j embeddedNeo4jServer() {
        return Neo4jBuilders.newInProcessBuilder()
                .withDisabledServer()
                .build();
    }

    @DynamicPropertySource
    static void neo4jProperties(DynamicPropertyRegistry registry, Neo4j embeddedDatabaseServer) {
        registry.add("spring.neo4j.uri", embeddedDatabaseServer::boltURI);
        registry.add("spring.neo4j.authentication.username", () -> neo4jUser);
        registry.add("spring.neo4j.authentication.password", () -> neo4jPass);
    }
}