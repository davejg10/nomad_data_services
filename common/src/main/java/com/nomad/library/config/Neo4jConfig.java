package com.nomad.library.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomad.library.domain.neo4j.CityMetricsDeserializer;

import org.neo4j.cypherdsl.core.renderer.Dialect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Neo4jConfig {

    @Bean
    org.neo4j.cypherdsl.core.renderer.Configuration cypherDslConfiguration() {
        return org.neo4j.cypherdsl.core.renderer.Configuration.newConfig()
                .withDialect(Dialect.NEO4J_5).build();
    }

    // @Bean
    // public Neo4jTransactionManager transactionManager(org.neo4j.driver.Driver driver) {
    //     return new Neo4jTransactionManager(driver);
    // }

    @Bean
    public CityMetricsDeserializer cityMetricsDeserializer(ObjectMapper objectMapper) {
        return new CityMetricsDeserializer(objectMapper);
    }

}