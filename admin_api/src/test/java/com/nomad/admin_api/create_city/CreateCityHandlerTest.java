package com.nomad.admin_api.create_city;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import com.nomad.admin_api.Neo4jRepository;
import com.nomad.admin_api.domain.CityDTO;
import com.nomad.admin_api.functions.create_city.CreateCityHandler;
import com.nomad.library.domain.Neo4jCity;
import com.nomad.library.domain.SqlCity;
import com.nomad.library.domain.SqlCountry;
import com.nomad.library.exceptions.Neo4jGenericException;
import com.nomad.library.repositories.SqlCityRepository;
import com.nomad.library.repositories.SqlCountryRepository;

import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootTest
@Import({com.nomad.library.Neo4jTestConfiguration.class})
@Transactional
public class CreateCityHandlerTest {

    @Autowired
    private SqlCountryRepository sqlCountryRepository;
    
    @Autowired
    private SqlCityRepository sqlCityRepository;

    @Autowired
    private Neo4jRepository neo4jRepository;

    @Autowired
    private CreateCityHandler createCityHandler;

    @Test
    void createCityHandler_shouldCreateCityInSqlAndNeo4j_whenNoExceptionsThrown() throws Neo4jGenericException {
        String countryAName = "CountryA";
        SqlCountry countryToBeCreated = SqlCountry.of(countryAName, "A description of countryA");
        SqlCountry savedCountry = sqlCountryRepository.save(countryToBeCreated);
        neo4jRepository.syncCountry(savedCountry);
        CityDTO cityDTO = new CityDTO("CityA", "CityA desc", countryAName);

        createCityHandler.accept(cityDTO);
        
        Set<SqlCity> sqlCities = sqlCityRepository.findAll();
        Set<Neo4jCity> neo4jCities = neo4jRepository.findAllCities();

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
