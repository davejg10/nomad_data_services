package com.nomad.admin_api.services;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.nomad.admin_api.domain.CountryDTO;
import com.nomad.admin_api.repositories.Neo4jCountryRepository;
import com.nomad.data_library.domain.neo4j.Neo4jCountry;
import com.nomad.data_library.domain.sql.SqlCountry;
import com.nomad.data_library.exceptions.Neo4jGenericException;
import com.nomad.data_library.repositories.SqlCountryRepository;

import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootTest
@Import({com.nomad.data_library.Neo4jTestConfiguration.class})
public class CountryServiceMockNeo4jTest {
    
    @Autowired
    private SqlCountryRepository sqlCountryRepository;

    @MockitoBean
    private Neo4jCountryRepository neo4jCountryRepository;

    @Autowired
    private CountryService countryService;

    @AfterEach
    void clearDB(@Autowired Neo4jClient neo4jClient) {
        sqlCountryRepository.deleteAll();
        neo4jClient.query("MATCH (n) DETACH DELETE n;").run();    
    }
    
    @Test
    void createCountry_shouldRollBackSql_whenExceptionThrownByNeo4jRepository() throws Neo4jGenericException {
        CountryDTO countryToBeCreated = new CountryDTO("CountryA", "Short description", "long desription", "url:blob");

        Mockito.when(neo4jCountryRepository.save(Mockito.any(Neo4jCountry.class))).thenThrow(new Neo4jGenericException("", new Throwable()));
        
        try {
            countryService.createCountry(countryToBeCreated);
        } catch(Exception e) {
            // we dont care
        }

        assertThat(sqlCountryRepository.findAll().size()).isEqualTo(0);
    }

    @Test
    void updateCountry_shouldRollBackSql_whenExceptionThrownByNeo4jRepository() throws Neo4jGenericException {
        SqlCountry sqlCountry = SqlCountry.of("CountryA", "A description of countryA");
        sqlCountry = sqlCountryRepository.save(sqlCountry);

        Mockito.doThrow(new Neo4jGenericException("", new Throwable()))
                .when(neo4jCountryRepository)
                .update(Mockito.any());

        CountryDTO countryDTO = new CountryDTO("newName", "newShortDescription", "newDescription", "newBlobUrl");

        try {
            countryService.updateCountry(sqlCountry.getId().toString(), countryDTO);
        } catch(Exception e) {
            // we dont care
        }

        SqlCountry sqlCountryAfterUpdate = sqlCountryRepository.findById(sqlCountry.getId()).get();

        assertThat(sqlCountryRepository.findAll().size()).isEqualTo(1);
        assertThat(sqlCountryAfterUpdate).isEqualTo(sqlCountry);
    }

    @Test
    void deleteCountry_shouldRollBackSql_whenExceptionThrownByNeo4jRepository() throws Neo4jGenericException {
        SqlCountry sqlCountry = SqlCountry.of("CountryA", "A description of countryA");
        sqlCountryRepository.save(sqlCountry);

        Mockito.doThrow(new Neo4jGenericException("", new Throwable()))
            .when(neo4jCountryRepository)
            .delete(Mockito.any(SqlCountry.class));

        try {
            countryService.deleteCountry(sqlCountry.getName());
        } catch(Exception e) {
            // we dont care
        }

        assertThat(sqlCountryRepository.findAll().size()).isEqualTo(1);
 
    }
}
