package com.nomad.admin_api.create_city;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.nomad.admin_api.Neo4jCityRepository;
import com.nomad.admin_api.domain.CityDTO;
import com.nomad.admin_api.functions.create_city.CreateCityHandler;
import com.nomad.data_library.GenericTestGenerator;
import com.nomad.data_library.domain.sql.SqlCity;
import com.nomad.data_library.domain.sql.SqlCountry;
import com.nomad.data_library.exceptions.Neo4jGenericException;
import com.nomad.data_library.repositories.SqlCityRepository;
import com.nomad.data_library.repositories.SqlCountryRepository;

import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootTest
@Import({com.nomad.data_library.Neo4jTestConfiguration.class})
@Transactional
public class CreateCityHandlerMockNeo4jTest {

    @Autowired
    private SqlCountryRepository sqlCountryRepository;
    
    @Autowired
    private SqlCityRepository sqlCityRepository;

    @MockitoBean
    private Neo4jCityRepository neo4jCityRepository;

    @Autowired
    private CreateCityHandler createCityHandler;

    @Test
    void createCityHandler_shouldRollbackSql_whenExceptionsThrownByNeo4jRepository() throws Neo4jGenericException {
        String countryAName = "CountryA";
        SqlCountry countryToBeCreated = SqlCountry.of(countryAName, "A description of countryA");
        sqlCountryRepository.save(countryToBeCreated);
        
        Mockito.when(neo4jCityRepository.syncCity(Mockito.any(SqlCity.class))).thenThrow(new Neo4jGenericException(""));

        CityDTO cityDTO = new CityDTO("CityA", "CityA desc", GenericTestGenerator.cityMetrics(), countryAName);

        try {
            createCityHandler.accept(cityDTO);
        } catch (Exception e) {
            // we dont care about the exeception
        }
        
        Set<SqlCity> sqlCities = sqlCityRepository.findAll();

        assertThat(sqlCities.size()).isEqualTo(0);
       
    }
}
