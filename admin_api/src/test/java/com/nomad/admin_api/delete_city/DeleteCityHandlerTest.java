package com.nomad.admin_api.delete_city;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.neo4j.core.Neo4jClient;

import com.nomad.admin_api.domain.CityToDeleteDTO;
import com.nomad.admin_api.functions.delete_city.DeleteCityHandler;
import com.nomad.admin_api.repositories.Neo4jCityRepository;
import com.nomad.admin_api.repositories.Neo4jCountryRepository;
import com.nomad.data_library.GenericTestGenerator;
import com.nomad.data_library.Neo4jTestGenerator;
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
public class DeleteCityHandlerTest {

    @Autowired
    private SqlCountryRepository sqlCountryRepository;
    
    @Autowired
    private SqlCityRepository sqlCityRepository;

    @Autowired
    private Neo4jCityRepository neo4jCityRepository;

    @Autowired
    private Neo4jCountryRepository neo4jCountryRepository;

    @Autowired
    private DeleteCityHandler deleteCityHandler;

    @AfterEach

    void clearDB(@Autowired Neo4jClient neo4jClient) {
        sqlCityRepository.deleteAll();
        sqlCountryRepository.deleteAll();
        neo4jClient.query("MATCH (n) DETACH DELETE n;").run();    }

    @Test
    void deleteCityHandler_shouldDeleteCityInSqlAndSyncInNeo4j_whenNoExceptionsThrown() throws Neo4jGenericException {

        String countryAName = "CountryA";
        SqlCountry countryOfCity = SqlCountry.of(countryAName, "A description of countryA");
        countryOfCity = sqlCountryRepository.save(countryOfCity);

        Neo4jCountry neo4jCountry = Neo4jTestGenerator.neo4jCountryFromSql(countryOfCity);
        neo4jCountryRepository.save(neo4jCountry);

        SqlCity sqlCity = SqlCity.of("CityA", "CityA desc", GenericTestGenerator.cityMetrics(), countryOfCity.getId());
        sqlCity = sqlCityRepository.save(sqlCity);

        Neo4jCity neo4jCity = Neo4jTestGenerator.neo4jCityFromSql(sqlCity, countryOfCity);
        neo4jCityRepository.save(neo4jCity);

        CityToDeleteDTO cityToDeleteDTO = new CityToDeleteDTO(sqlCity.getName(), countryOfCity.getName());

        assertThat(sqlCityRepository.findAll().size()).isEqualTo(1);
        assertThat(neo4jCityRepository.findAllCities().size()).isEqualTo(1);

        deleteCityHandler.accept(cityToDeleteDTO);

        assertThat(sqlCityRepository.findAll().size()).isEqualTo(0);
        assertThat(neo4jCityRepository.findAllCities().size()).isEqualTo(0);
    }
}
