package com.nomad.admin_api.create_city;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.test.annotation.Commit;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import com.nomad.admin_api.Neo4jCityRepository;
import com.nomad.admin_api.Neo4jCountryRepository;
import com.nomad.admin_api.domain.CityDTO;
import com.nomad.admin_api.functions.create_city.CreateCityHandler;
import com.nomad.data_library.GenericTestGenerator;
import com.nomad.data_library.Neo4jTestConfiguration;
import com.nomad.data_library.domain.neo4j.Neo4jCity;
import com.nomad.data_library.domain.neo4j.Neo4jCountry;
import com.nomad.data_library.domain.sql.SqlCity;
import com.nomad.data_library.domain.sql.SqlCountry;
import com.nomad.data_library.exceptions.Neo4jGenericException;
import com.nomad.data_library.repositories.SqlCityRepository;
import com.nomad.data_library.repositories.SqlCountryRepository;

import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootTest
@Import({com.nomad.data_library.Neo4jTestConfiguration.class})
public class CreateCityHandlerTest {

    @Autowired
    private SqlCountryRepository sqlCountryRepository;
    
    @Autowired
    private SqlCityRepository sqlCityRepository;

    @Autowired
    private Neo4jCityRepository neo4jCityRepository;

    @Autowired
    private Neo4jCountryRepository neo4jCountryRepository;

    @Autowired
    private CreateCityHandler createCityHandler;

    @AfterEach
    void clearDB(@Autowired Neo4jClient neo4jClient) {
        sqlCityRepository.deleteAll();
        sqlCountryRepository.deleteAll();
        neo4jClient.query("MATCH (n) DETACH DELETE n;").run();    }

    @Test
    void createCityHandler_shouldCreateCityInSqlAndSyncInNeo4j_whenNoExceptionsThrown() throws Neo4jGenericException {
        String countryAName = "CountryA";
        SqlCountry countryToBeCreated = SqlCountry.of(countryAName, "A description of countryA");
        SqlCountry savedCountry = sqlCountryRepository.save(countryToBeCreated);
        Neo4jCountry neo4jCountry  = neo4jCountryRepository.save(savedCountry);

        CityDTO cityDTO = new CityDTO("CityA", "CityA desc", GenericTestGenerator.cityMetrics(), countryAName);

        createCityHandler.accept(cityDTO);
        
        Set<SqlCity> sqlCities = sqlCityRepository.findAll();
        Set<Neo4jCity> neo4jCities = neo4jCityRepository.findAllCities();


        log.info(neo4jCities);
        
        SqlCity sqlCity = sqlCities.stream().findFirst().get();
        Neo4jCity neo4jCity = neo4jCities.stream().findFirst().get();

        assertThat(sqlCities.size()).isEqualTo(1);
        assertThat(neo4jCities.size()).isEqualTo(1);
        assertThat(sqlCity.getId().toString()).isEqualTo(neo4jCity.getId().toString());
        assertThat(sqlCity.getCountryId().toString()).isEqualTo(savedCountry.getId().toString());
        assertThat(neo4jCity.getCountry().getId()).isEqualTo(savedCountry.getId().toString());
    }
}
