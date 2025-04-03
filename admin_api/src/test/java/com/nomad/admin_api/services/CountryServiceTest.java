package com.nomad.admin_api.services;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.neo4j.core.Neo4jClient;

import com.nomad.admin_api.domain.CountryDTO;
import com.nomad.admin_api.repositories.Neo4jCountryRepository;
import com.nomad.data_library.Neo4jTestGenerator;
import com.nomad.data_library.domain.neo4j.Neo4jCountry;
import com.nomad.data_library.domain.sql.SqlCountry;
import com.nomad.data_library.repositories.SqlCountryRepository;

import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootTest
@Import({com.nomad.data_library.Neo4jTestConfiguration.class})
public class CountryServiceTest {

    @Autowired
    private SqlCountryRepository sqlCountryRepository;

    @Autowired
    private Neo4jCountryRepository neo4jCountryRepository;

    @Autowired
    private CountryService countryService;

    @AfterEach
    void clearDB(@Autowired Neo4jClient neo4jClient) {
        sqlCountryRepository.deleteAll();
        neo4jClient.query("MATCH (n) DETACH DELETE n;").run();    
    }

    @Test
    void createCountry_shouldCreateCountryInSqlAndNeo4j_whenNoExceptionsThrown() {

        CountryDTO countryToBeCreated = new CountryDTO("CountryA", "Short description", "A description of countryA", "url:blob");

        countryService.createCountry(countryToBeCreated);
        
        Set<SqlCountry> sqlCountries = sqlCountryRepository.findAll();
        Set<Neo4jCountry> neo4jCountries = neo4jCountryRepository.findAllCountries();
        
        SqlCountry sqlCountry = sqlCountries.stream().findFirst().get();
        Neo4jCountry neo4jCountry = neo4jCountries.stream().findFirst().get();

        assertThat(sqlCountries.size()).isEqualTo(1);
        assertThat(neo4jCountries.size()).isEqualTo(1);
        assertThat(sqlCountry.getId().toString()).isEqualTo(neo4jCountry.getId().toString());
    }

    @Test
    void updateCountry_shouldUpdateCountryInSqlAndNeo4j_whenCountryExistsAndNoErrorsThrown() {
        SqlCountry sqlCountry = SqlCountry.of("CountryA", "A description of countryA");
        sqlCountry = sqlCountryRepository.save(sqlCountry);
        Neo4jCountry neo4jCountry = neo4jCountryRepository.save(Neo4jTestGenerator.neo4jCountryFromSql(sqlCountry));

        CountryDTO countryDTO = new CountryDTO("newName", "newShortDescription", "newDescription", "newBlobUrl");

        countryService.updateCountry(sqlCountry.getId().toString(), countryDTO);

        SqlCountry updatedSqlCountry = sqlCountryRepository.findById(sqlCountry.getId()).get();
        Neo4jCountry updatedNeo4jCountry = neo4jCountryRepository.findById(sqlCountry.getId().toString()).get();

        assertThat(sqlCountry.getId()).isEqualTo(updatedSqlCountry.getId());

        assertThat(sqlCountry.getName()).isNotEqualTo(updatedSqlCountry.getName());
        assertThat(sqlCountry.getDescription()).isNotEqualTo(updatedSqlCountry.getDescription());
        assertThat(updatedSqlCountry.getName()).isEqualTo(countryDTO.name());
        assertThat(updatedSqlCountry.getDescription()).isEqualTo(countryDTO.description());

        assertThat(neo4jCountry.getId()).isEqualTo(updatedNeo4jCountry.getId());

        assertThat(neo4jCountry.getName()).isNotEqualTo(updatedNeo4jCountry.getName());
        assertThat(neo4jCountry.getShortDescription()).isNotEqualTo(updatedNeo4jCountry.getShortDescription());
        assertThat(neo4jCountry.getPrimaryBlobUrl()).isNotEqualTo(updatedNeo4jCountry.getPrimaryBlobUrl());
        assertThat(updatedNeo4jCountry.getName()).isEqualTo(countryDTO.name());
        assertThat(updatedNeo4jCountry.getShortDescription()).isEqualTo(countryDTO.shortDescription());
        assertThat(updatedNeo4jCountry.getPrimaryBlobUrl()).isEqualTo(countryDTO.primaryBlobUrl());

        assertThat(sqlCountryRepository.findAll().size()).isEqualTo(1);
        assertThat(neo4jCountryRepository.findAllCountries().size()).isEqualTo(1);

    }

    @Test
    void deleteCountry_shouldDeleteCountryInSqlAndNeo4j_whenNoExceptionsThrown() {

        SqlCountry countryToBeDeleted = SqlCountry.of("CountryA", "A description of countryA");
        countryToBeDeleted = sqlCountryRepository.save(countryToBeDeleted);
        neo4jCountryRepository.save(Neo4jTestGenerator.neo4jCountryFromSql(countryToBeDeleted));

        assertThat(sqlCountryRepository.findAll().size()).isEqualTo(1);
        assertThat(neo4jCountryRepository.findAllCountries().size()).isEqualTo(1);

        countryService.deleteCountry(countryToBeDeleted.getName());
        
        assertThat(sqlCountryRepository.findAll().size()).isEqualTo(0);
        assertThat(neo4jCountryRepository.findAllCountries().size()).isEqualTo(0);
    }
}