package com.nomad.admin_api.create_country;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import com.nomad.admin_api.Neo4jCountryRepository;
import com.nomad.admin_api.functions.create_country.CreateCountryHandler;
import com.nomad.data_library.domain.neo4j.Neo4jCountry;
import com.nomad.data_library.domain.sql.SqlCountry;
import com.nomad.data_library.repositories.SqlCountryRepository;

import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootTest
@Import({com.nomad.data_library.Neo4jTestConfiguration.class})
@Transactional  // Ensures test changes do not persist
public class CreateCountryHandlerTest {

    @Autowired
    private SqlCountryRepository sqlCountryRepository;

    @Autowired
    private Neo4jCountryRepository neo4jCountryRepository;

    @Autowired
    private CreateCountryHandler createCountryHandler;
    
    @Test
    void createCountryHandler_shouldCreateCountryInSqlAndNeo4j_whenNoExceptionsThrown() {

        SqlCountry countryToBeCreated = SqlCountry.of("CountryA", "A description of countryA");

        createCountryHandler.accept(countryToBeCreated);
        
        Set<SqlCountry> sqlCountries = sqlCountryRepository.findAll();
        Set<Neo4jCountry> neo4jCountries = neo4jCountryRepository.findAllCountries();
        
        SqlCountry sqlCountry = sqlCountries.stream().findFirst().get();
        Neo4jCountry neo4jCountry = neo4jCountries.stream().findFirst().get();

        assertThat(sqlCountries.size()).isEqualTo(1);
        assertThat(neo4jCountries.size()).isEqualTo(1);
        assertThat(sqlCountry.getId().toString()).isEqualTo(neo4jCountry.getId().toString());
    }
}
