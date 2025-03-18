package com.nomad.admin_api.delete_country;

import com.nomad.admin_api.functions.delete_country.DeleteCountryHandler;
import com.nomad.admin_api.repositories.Neo4jCountryRepository;
import com.nomad.data_library.domain.sql.SqlCountry;
import com.nomad.data_library.exceptions.Neo4jGenericException;
import com.nomad.data_library.repositories.SqlCountryRepository;
import lombok.extern.log4j.Log4j2;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
@SpringBootTest
@Import({com.nomad.data_library.Neo4jTestConfiguration.class})
public class DeleteCountryHandlerMockNeo4jTest {

    @Autowired
    private SqlCountryRepository sqlCountryRepository;

    @MockitoBean
    private Neo4jCountryRepository neo4jCountryRepository;

    @Autowired
    private DeleteCountryHandler deleteCountryHandler;

    @AfterEach
    void clearDB(@Autowired Neo4jClient neo4jClient) {
        sqlCountryRepository.deleteAll();
        neo4jClient.query("MATCH (n) DETACH DELETE n;").run();    }

    @Test
    void deleteCountryHandler_shouldRollBackSql_whenExceptionThrownByNeo4jRepository() throws Neo4jGenericException {

        SqlCountry countryToBeDeleted = SqlCountry.of("CountryA", "A description of countryA");
        sqlCountryRepository.save(countryToBeDeleted);

        Mockito.doThrow(new Neo4jGenericException("", new Throwable()))
            .when(neo4jCountryRepository)
            .delete(Mockito.any(SqlCountry.class));

        try {
            deleteCountryHandler.accept(countryToBeDeleted.getName());
        } catch(Exception e) {
            // we dont care
        }

        assertThat(sqlCountryRepository.findAll().size()).isEqualTo(1);
 
    }

}