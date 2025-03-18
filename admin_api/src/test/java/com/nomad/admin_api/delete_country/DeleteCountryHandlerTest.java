package com.nomad.admin_api.delete_country;

import com.nomad.admin_api.functions.delete_country.DeleteCountryHandler;
import com.nomad.admin_api.repositories.Neo4jCountryRepository;
import com.nomad.data_library.Neo4jTestGenerator;
import com.nomad.data_library.domain.sql.SqlCountry;
import com.nomad.data_library.repositories.SqlCountryRepository;
import lombok.extern.log4j.Log4j2;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.neo4j.core.Neo4jClient;

import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
@SpringBootTest
@Import({com.nomad.data_library.Neo4jTestConfiguration.class})
public class DeleteCountryHandlerTest {

    @Autowired
    private SqlCountryRepository sqlCountryRepository;

    @Autowired
    private Neo4jCountryRepository neo4jCountryRepository;

    @Autowired
    private DeleteCountryHandler deleteCountryHandler;

    @AfterEach
    void clearDB(@Autowired Neo4jClient neo4jClient) {
        sqlCountryRepository.deleteAll();
        neo4jClient.query("MATCH (n) DETACH DELETE n;").run();    }

    @Test
    void deleteCountryHandler_shouldDeleteCountryInSqlAndNeo4j_whenNoExceptionsThrown() {

        SqlCountry countryToBeDeleted = SqlCountry.of("CountryA", "A description of countryA");
        countryToBeDeleted = sqlCountryRepository.save(countryToBeDeleted);
        neo4jCountryRepository.save(Neo4jTestGenerator.neo4jCountryFromSql(countryToBeDeleted));

        assertThat(sqlCountryRepository.findAll().size()).isEqualTo(1);
        assertThat(neo4jCountryRepository.findAllCountries().size()).isEqualTo(1);

        deleteCountryHandler.accept(countryToBeDeleted.getName());
        
        assertThat(sqlCountryRepository.findAll().size()).isEqualTo(0);
        assertThat(neo4jCountryRepository.findAllCountries().size()).isEqualTo(0);
    }
}
