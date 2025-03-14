package com.nomad.admin_api.delete_city;

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

import com.nomad.admin_api.Neo4jCityRepository;
import com.nomad.admin_api.domain.CityToDeleteDTO;
import com.nomad.admin_api.functions.delete_city.DeleteCityHandler;
import com.nomad.data_library.GenericTestGenerator;
import com.nomad.data_library.Neo4jTestConfiguration;
import com.nomad.data_library.domain.sql.SqlCity;
import com.nomad.data_library.domain.sql.SqlCountry;
import com.nomad.data_library.exceptions.Neo4jGenericException;
import com.nomad.data_library.repositories.SqlCityRepository;
import com.nomad.data_library.repositories.SqlCountryRepository;

import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootTest
@Import({com.nomad.data_library.Neo4jTestConfiguration.class})
public class DeleteCityHandlerMockNeo4jTest {

    @Autowired
    private SqlCountryRepository sqlCountryRepository;
    
    @Autowired
    private SqlCityRepository sqlCityRepository;

    @MockitoBean
    private Neo4jCityRepository neo4jCityRepository;

    @Autowired
    private DeleteCityHandler deleteCityHandler;

    @AfterEach
    void clearDB(@Autowired Neo4jClient neo4jClient) {
        sqlCityRepository.deleteAll();
        sqlCountryRepository.deleteAll();
        neo4jClient.query("MATCH (n) DETACH DELETE n;").run();    }

    @Test
    void deleteCityHandler_shouldRollbackSql_whenExceptionsThrownByNeo4jRepository() throws Neo4jGenericException {
        String countryAName = "CountryA";
        SqlCountry countryOfCity = SqlCountry.of(countryAName, "A description of countryA");
        countryOfCity = sqlCountryRepository.save(countryOfCity);

        SqlCity sqlCity = SqlCity.of("CityA", "CityA desc", GenericTestGenerator.cityMetrics(), countryOfCity.getId());
        sqlCityRepository.save(sqlCity);

        CityToDeleteDTO cityToDeleteDTO = new CityToDeleteDTO(sqlCity.getName(), countryOfCity.getName());

        
        Mockito.doThrow(new Neo4jGenericException("", new Throwable()))
            .when(neo4jCityRepository)
            .delete(Mockito.any(SqlCity.class));

        assertThat(sqlCityRepository.findAll().size()).isEqualTo(1);

        try {
            deleteCityHandler.accept(cityToDeleteDTO);
        } catch (Exception e) {
            // we dont care about the exeception
        }
        
        assertThat(sqlCityRepository.findAll().size()).isEqualTo(1);
       
    }
}
