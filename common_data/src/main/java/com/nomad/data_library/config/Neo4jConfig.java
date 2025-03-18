package com.nomad.data_library.config;

import com.nomad.data_library.repositories.Neo4jCityMappers;
import com.nomad.data_library.repositories.Neo4jCountryMappers;
import org.neo4j.cypherdsl.core.renderer.Dialect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.core.mapping.Neo4jMappingContext;

@Configuration
public class Neo4jConfig {

    @Bean
    org.neo4j.cypherdsl.core.renderer.Configuration cypherDslConfiguration() {
        return org.neo4j.cypherdsl.core.renderer.Configuration.newConfig()
                .withDialect(Dialect.NEO4J_5).build();
    }

    @Bean
    public Neo4jCityMappers neo4jMappers(Neo4jMappingContext schema){
        return new Neo4jCityMappers(schema);
    }

    @Bean
    public Neo4jCountryMappers neo4jCountryMappers(Neo4jMappingContext schema) { return new Neo4jCountryMappers(schema); }
}