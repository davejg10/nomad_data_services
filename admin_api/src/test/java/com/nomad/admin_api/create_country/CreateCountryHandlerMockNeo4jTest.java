package com.nomad.admin_api.create_country;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.test.annotation.Commit;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import com.nomad.admin_api.Neo4jCountryRepository;
import com.nomad.admin_api.functions.create_country.CreateCountryHandler;
import com.nomad.data_library.Neo4jTestConfiguration;
import com.nomad.data_library.domain.sql.SqlCountry;
import com.nomad.data_library.exceptions.Neo4jGenericException;
import com.nomad.data_library.repositories.SqlCountryRepository;

import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootTest
@Import({com.nomad.data_library.Neo4jTestConfiguration.class})
public class CreateCountryHandlerMockNeo4jTest {

    @Autowired
    private SqlCountryRepository sqlCountryRepository;

    @MockitoBean
    private Neo4jCountryRepository neo4jCountryRepository;

    @Autowired
    private CreateCountryHandler createCountryHandler;

    @AfterEach
    void clearDB(@Autowired Neo4jClient neo4jClient) {
        sqlCountryRepository.deleteAll();
        neo4jClient.query("MATCH (n) DETACH DELETE n;").run();    }
    
    @Test
    void createCountryHandler_shouldRollBackSql_whenExceptionThrownByNeo4jRepository() throws Neo4jGenericException {

        SqlCountry countryToBeCreated = SqlCountry.of("CountryA", "A description of countryA");

        Mockito.when(neo4jCountryRepository.save(Mockito.any(SqlCountry.class))).thenThrow(new Neo4jGenericException("", new Throwable()));
        
        try {
            createCountryHandler.accept(countryToBeCreated);
        } catch(Exception e) {
            // we dont care
        }

        assertThat(sqlCountryRepository.findAll().size()).isEqualTo(0);
 
    }

}