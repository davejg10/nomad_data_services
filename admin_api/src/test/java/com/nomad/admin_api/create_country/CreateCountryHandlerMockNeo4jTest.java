package com.nomad.admin_api.create_country;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.nomad.admin_api.Neo4jRepository;
import com.nomad.admin_api.functions.create_country.CreateCountryHandler;
import com.nomad.library.domain.SqlCountry;
import com.nomad.library.exceptions.Neo4jGenericException;
import com.nomad.library.repositories.SqlCountryRepository;

import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootTest
@Import({com.nomad.library.Neo4jTestConfiguration.class})
@Transactional  // Ensures test changes do not persist
public class CreateCountryHandlerMockNeo4jTest {

    @Autowired
    private SqlCountryRepository sqlCountryRepository;

    @MockitoBean
    private Neo4jRepository neo4jRepository;

    @Autowired
    private CreateCountryHandler createCountryHandler;

    
    @Test
    void createCountryHandler_shouldRollBackSql_whenExceptionThrownByNeo4jRepository() throws Neo4jGenericException {

        SqlCountry countryToBeCreated = SqlCountry.of("CountryA", "A description of countryA");

        Mockito.when(neo4jRepository.syncCountry(Mockito.any(SqlCountry.class))).thenThrow(new Neo4jGenericException(""));
        
        try {
            createCountryHandler.accept(countryToBeCreated);
        } catch(Exception e) {
            // we dont care
        }

        assertThat(sqlCountryRepository.findAll().size()).isEqualTo(0);
 
    }

}