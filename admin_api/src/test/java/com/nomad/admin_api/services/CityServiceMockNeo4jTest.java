package com.nomad.admin_api.services;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.nomad.admin_api.domain.CityDTO;
import com.nomad.admin_api.domain.CityToDeleteDTO;
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
public class CityServiceMockNeo4jTest {

    @Autowired
    private SqlCountryRepository sqlCountryRepository;
    
    @Autowired
    private SqlCityRepository sqlCityRepository;

    @MockitoBean
    private Neo4jCityRepository neo4jCityRepository;

    @Autowired
    private CityService cityService;

    private static final String countryAName = "CountryA";
    private static SqlCountry sqlCountry;

    @AfterEach
    void clearDB(@Autowired Neo4jClient neo4jClient) {
        sqlCityRepository.deleteAll();
        sqlCountryRepository.deleteAll();
        neo4jClient.query("MATCH (n) DETACH DELETE n;").run();    
    }

    @BeforeEach
    void setupDB() {
        SqlCountry countryToBeCreated = SqlCountry.of(countryAName, "A description of countryA");
        sqlCountry = sqlCountryRepository.save(countryToBeCreated);
    }

    @Test
    void createCity_shouldRollbackSql_whenExceptionsThrownByNeo4jRepository() throws Neo4jGenericException {
        Mockito.when(neo4jCityRepository.save(Mockito.any(Neo4jCity.class))).thenThrow(new Neo4jGenericException("", new Throwable()));

        CityDTO cityDTO = new CityDTO("CityA", "Short desc", "CityA desc", "url:blob", Neo4jTestGenerator.generateCoords(), GenericTestGenerator.cityMetrics(), countryAName);

        try {
            cityService.createCity(cityDTO);
        } catch (Exception e) {
            // we dont care about the exeception
        }

        Set<SqlCity> sqlCities = sqlCityRepository.findAll();

        assertThat(sqlCities.size()).isEqualTo(0);

    }

    @Test
    void updateCity_shouldRollbackSql_whenExceptionsThrownByNeo4jRepository() throws Neo4jGenericException {

        SqlCity sqlCity = SqlCity.of("CityA", "CityA desc", GenericTestGenerator.cityMetrics(), sqlCountry.getId());
        sqlCity = sqlCityRepository.save(sqlCity);

        CityDTO cityToUpdate = new CityDTO("newname", "newShortDesc", "newDesc", "newblob", Neo4jTestGenerator.generateCoords(), GenericTestGenerator.cityMetrics(), countryAName);

        Mockito.doThrow(new Neo4jGenericException("", new Throwable()))
                .when(neo4jCityRepository)
                .update(Mockito.any());

        try {
            cityService.updateCity(sqlCity.getId().toString(), cityToUpdate);
        } catch (Exception e) {
            // we dont care about the exeception
        }

        Set<SqlCity> sqlCities = sqlCityRepository.findAll();
        SqlCity sqlCityAfterUpdate = sqlCityRepository.findById(sqlCity.getId()).get();

        assertThat(sqlCities.size()).isEqualTo(1);
        assertThat(sqlCityAfterUpdate).usingRecursiveAssertion().isEqualTo(sqlCity);

    }

    @Test
    void deleteCity_shouldRollbackSql_whenExceptionsThrownByNeo4jRepository() throws Neo4jGenericException {


        SqlCity sqlCity = SqlCity.of("CityA", "CityA desc", GenericTestGenerator.cityMetrics(), sqlCountry.getId());
        sqlCityRepository.save(sqlCity);

        CityToDeleteDTO cityToDeleteDTO = new CityToDeleteDTO(sqlCity.getName(), sqlCountry.getName());

        
        Mockito.doThrow(new Neo4jGenericException("", new Throwable()))
            .when(neo4jCityRepository)
            .delete(Mockito.any(SqlCity.class));

        assertThat(sqlCityRepository.findAll().size()).isEqualTo(1);

        try {
            cityService.deleteCity(cityToDeleteDTO);
        } catch (Exception e) {
            // we dont care about the exeception
        }
        
        assertThat(sqlCityRepository.findAll().size()).isEqualTo(1);
       
    }
}
