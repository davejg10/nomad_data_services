package com.nomad.admin_api.create_city;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomad.data_library.domain.CityCriteria;
import com.nomad.data_library.domain.CityMetric;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.neo4j.types.GeographicPoint3d;

import com.nomad.admin_api.domain.CityDTO;
import com.nomad.admin_api.functions.create_city.CreateCityHandler;
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

    @Autowired
    private ObjectMapper objectMapper;

    @AfterEach
    void clearDB(@Autowired Neo4jClient neo4jClient) {
        sqlCityRepository.deleteAll();
        sqlCountryRepository.deleteAll();
        neo4jClient.query("MATCH (n) DETACH DELETE n;").run();    }

    @Test
    void deserialize() throws Exception {
        Set<CityMetric> newCityMetrics = Set.of(
                new CityMetric(CityCriteria.SAILING, 4.0),
                new CityMetric(CityCriteria.FOOD, 8.0),
                new CityMetric(CityCriteria.NIGHTLIFE, 4.0));
        CityDTO jsonAsObject = new CityDTO(
                "CityA",
                "Short description",
                "Example description",
                "url:xx",
                new GeographicPoint3d(13.7563, 100.5018, 0.0),
                newCityMetrics,
                "CountryA");

        String requestBody = Files.readString(Path.of("src/main/java/com/nomad/admin_api/functions/create_city/payload.json"));
        CityDTO deserialized = objectMapper.readValue(requestBody, CityDTO.class);

        assertThat(deserialized).isEqualTo(jsonAsObject);
    }

    @Test
    void createCityHandler_shouldCreateCityInSqlAndSyncInNeo4j_whenNoExceptionsThrown() throws Neo4jGenericException {
        String countryAName = "CountryA";
        SqlCountry countryToBeCreated = SqlCountry.of(countryAName, "A description of countryA");
        SqlCountry savedCountry = sqlCountryRepository.save(countryToBeCreated);
        Neo4jCountry neo4jCountry = Neo4jTestGenerator.neo4jCountryFromSql(savedCountry);
        neo4jCountry  = neo4jCountryRepository.save(neo4jCountry);

        CityDTO cityDTO = new CityDTO("CityA", "Short desc", "CityA desc", "url:blob", Neo4jTestGenerator.generateCoords(), GenericTestGenerator.cityMetrics(), countryAName);

        createCityHandler.accept(cityDTO);

        Set<SqlCity> sqlCities = sqlCityRepository.findAll();
        Set<Neo4jCity> neo4jCities = neo4jCityRepository.findAllCities();

        SqlCity sqlCity = sqlCities.stream().findFirst().get();
        Neo4jCity neo4jCity = neo4jCities.stream().findFirst().get();

        assertThat(sqlCities.size()).isEqualTo(1);
        assertThat(neo4jCities.size()).isEqualTo(1);
        assertThat(sqlCity.getId().toString()).isEqualTo(neo4jCity.getId().toString());
        assertThat(sqlCity.getCountryId().toString()).isEqualTo(savedCountry.getId().toString());
        assertThat(neo4jCity.getCountry().getId()).isEqualTo(savedCountry.getId().toString());
        assertThat(neo4jCity.getShortDescription()).isEqualTo(cityDTO.shortDescription());
        assertThat(neo4jCity.getCoordinate()).isEqualTo(cityDTO.coordinate());
        assertThat(sqlCity.getDescription()).isEqualTo(cityDTO.description());
    }
}
