package com.nomad.admin_api;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.test.context.bean.override.mockito.MockitoBean;


@TestConfiguration
public class Neo4jTestConfiguration {

    @Bean
    public Driver neo4jDriver(Neo4j embeddedDatabaseServer) {
        return GraphDatabase.driver(
            embeddedDatabaseServer.boltURI().toString(), 
            AuthTokens.basic("neo4j", "mypassword")
        );
    }

    @Bean
    public Neo4jClient neo4jClient(Driver driver) {
        return Neo4jClient.create(driver);
    }

    @Bean
    public static Neo4j embeddedNeo4jServer() {
        return Neo4jBuilders.newInProcessBuilder()
                .withDisabledServer()
                .build();
    }
}