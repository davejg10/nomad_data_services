package com.nomad.data_library;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomad.data_library.domain.neo4j.CityMetricsDeserializer;
import com.nomad.data_library.domain.sql.CityMetricsConverter;

@TestConfiguration
public class TestConfig {
    
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
