package com.nomad.admin_api.create_city;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.nomad.admin_api.domain.CityDTO;
import com.nomad.admin_api.functions.create_city.CreateCityHandler;
import com.nomad.admin_api.repositories.Neo4jCityRepository;
import com.nomad.data_library.GenericTestGenerator;
import com.nomad.data_library.Neo4jTestGenerator;
import com.nomad.data_library.domain.neo4j.Neo4jCity;
import com.nomad.data_library.domain.sql.SqlCity;
import com.nomad.data_library.domain.sql.SqlCountry;
import com.nomad.data_library.exceptions.Neo4jGenericException;
import com.nomad.data_library.repositories.SqlCityRepository;
import com.nomad.data_library.repositories.SqlCountryRepository;

import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootTest
@Import({com.nomad.data_library.Neo4jTestConfiguration.class})
public class CreateCityHandlerMockNeo4jTest {

    @Autowired
    private SqlCountryRepository sqlCountryRepository;

    @Autowired
    private SqlCityRepository sqlCityRepository;

    @MockitoBean
    private Neo4jCityRepository neo4jCityRepository;

    @Autowired
    private CreateCityHandler createCityHandler;

    @AfterEach
    void clearDB(@Autowired Neo4jClient neo4jClient) {
        sqlCityRepository.deleteAll();
        sqlCountryRepository.deleteAll();
        neo4jClient.query("MATCH (n) DETACH DELETE n;").run();    }

    @Test
    void createCityHandler_shouldRollbackSql_whenExceptionsThrownByNeo4jRepository() throws Neo4jGenericException {
        String countryAName = "CountryA";
        SqlCountry countryToBeCreated = SqlCountry.of(countryAName, "A description of countryA");
        sqlCountryRepository.save(countryToBeCreated);

        Mockito.when(neo4jCityRepository.save(Mockito.any(Neo4jCity.class))).thenThrow(new Neo4jGenericException("", new Throwable()));

        CityDTO cityDTO = new CityDTO("CityA", "Short desc", "CityA desc", "url:blob", Neo4jTestGenerator.generateCoords(), GenericTestGenerator.cityMetrics(), countryAName);

        try {
            createCityHandler.accept(cityDTO);
        } catch (Exception e) {
            // we dont care about the exeception
        }

        Set<SqlCity> sqlCities = sqlCityRepository.findAll();

        assertThat(sqlCities.size()).isEqualTo(0);

    }
}
