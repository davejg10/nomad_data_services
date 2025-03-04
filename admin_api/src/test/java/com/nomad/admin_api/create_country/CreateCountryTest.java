package com.nomad.admin_api.create_country;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomad.admin_api.Neo4jRepository;
import com.nomad.admin_api.SqlCountryRepository;
import com.nomad.admin_api.Neo4jTestConfiguration;


import com.nomad.admin_api.domain.SqlCountry;
import com.nomad.library.domain.Country;

import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootTest
@Import({Neo4jTestConfiguration.class})
public class CreateCountryTest {

    private static Neo4j embeddedDatabaseServer;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Neo4jRepository neo4jRepository;

    @Autowired
    private SqlCountryRepository sqlCountryRepository;

    @BeforeAll
    static void initializeNeo4j() {
        embeddedDatabaseServer = Neo4jBuilders.newInProcessBuilder()
                .withDisabledServer()
                .build();
    }

    @DynamicPropertySource
    static void neo4jProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.neo4j.uri", embeddedDatabaseServer::boltURI);
        registry.add("spring.neo4j.authentication.username", () -> "neo4j");
        registry.add("spring.neo4j.authentication.password", () -> "mypassword");
    }
    
    @AfterAll
    static void stopNeo4j() {
        embeddedDatabaseServer.close();
    }


    @Test
    void letsSeeWhatHappens() {
        SqlCountry countryToBeCreated = SqlCountry.of("CountryA", "A description of countryA");

        SqlCountry savedCountry = sqlCountryRepository.save(countryToBeCreated);

        log.info("SqlCountry: {}, id: {}", savedCountry.getName(), savedCountry.getId());

        Country neo4jCountry = neo4jRepository.syncCountry(savedCountry);

        log.info("Neo4jCountry: {}, id: {}", neo4jCountry.getName(), neo4jCountry.getId());


    }

    
}
