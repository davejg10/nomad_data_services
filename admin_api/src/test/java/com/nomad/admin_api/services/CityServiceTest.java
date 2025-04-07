package com.nomad.admin_api.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.neo4j.core.Neo4jClient;

import com.nomad.admin_api.domain.CityDTO;
import com.nomad.admin_api.domain.CityToDeleteDTO;
import com.nomad.admin_api.exceptions.DuplicateEntityException;
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
public class CityServiceTest {

    @Autowired
    private SqlCountryRepository sqlCountryRepository;
    
    @Autowired
    private SqlCityRepository sqlCityRepository;

    @Autowired
    private Neo4jCityRepository neo4jCityRepository;

    @Autowired
    private Neo4jCountryRepository neo4jCountryRepository;

    @Autowired
    private CityService cityService;


    private String countryAName = "CountryA";
    private SqlCountry sqlCountry;

    @AfterEach
    void clearDB(@Autowired Neo4jClient neo4jClient) {
        sqlCityRepository.deleteAll();
        sqlCountryRepository.deleteAll();
        neo4jClient.query("MATCH (n) DETACH DELETE n;").run();    
    }

    @BeforeEach
    void setupDB() {
        SqlCountry countryOfCity = SqlCountry.of(countryAName, "A description of countryA");
        sqlCountry = sqlCountryRepository.save(countryOfCity);

        Neo4jCountry neo4jCountry = Neo4jTestGenerator.neo4jCountryFromSql(countryOfCity);
        neo4jCountryRepository.save(neo4jCountry);
    }

    @Test
    void createCity_shouldCreateCityInSqlAndSyncInNeo4j_whenNoExceptionsThrown() throws Neo4jGenericException {
        CityDTO cityDTO = new CityDTO("CityA", "Short desc", "CityA desc", "url:blob", Neo4jTestGenerator.generateCoords(), GenericTestGenerator.cityMetrics(), countryAName);

        cityService.createCity(cityDTO);

        Set<SqlCity> sqlCities = sqlCityRepository.findAll();
        Set<Neo4jCity> neo4jCities = neo4jCityRepository.findAllCities();

        SqlCity sqlCity = sqlCities.stream().findFirst().get();
        Neo4jCity neo4jCity = neo4jCities.stream().findFirst().get();

        assertThat(sqlCities.size()).isEqualTo(1);
        assertThat(neo4jCities.size()).isEqualTo(1);
        assertThat(sqlCity.getId().toString()).isEqualTo(neo4jCity.getId().toString());
        assertThat(sqlCity.getCountryId().toString()).isEqualTo(sqlCountry.getId().toString());
        assertThat(neo4jCity.getCountry().getId()).isEqualTo(sqlCountry.getId().toString());
        assertThat(neo4jCity.getShortDescription()).isEqualTo(cityDTO.shortDescription());
        assertThat(neo4jCity.getCoordinate()).isEqualTo(cityDTO.coordinate());
        assertThat(sqlCity.getDescription()).isEqualTo(cityDTO.description());
    }

    @Test
    void createCity_shouldThrowDuplicateEntityException_whenCityExistsWithSameNameAndCountryId() throws Neo4jGenericException {

        CityDTO cityDTO = new CityDTO("CityA", "Short desc", "CityA desc", "url:blob", Neo4jTestGenerator.generateCoords(), GenericTestGenerator.cityMetrics(), countryAName);
        
        cityService.createCity(cityDTO);

        SqlCity sqlCity = sqlCityRepository.findAll().stream().findFirst().get();

        DuplicateEntityException ex = assertThrowsExactly(DuplicateEntityException.class, () -> {
            cityService.createCity(cityDTO);
        });

        assertThat(ex.getEntityId()).isEqualTo(sqlCity.getId().toString());
    }

    @Test
    void updateCity_souldUpdateCityInSqlAndNeo4j_whenCityWithIdExistsAndNoExceptionsThrown() {
        SqlCity sqlCity = SqlCity.of("CityA", "CityA desc", GenericTestGenerator.cityMetrics(), sqlCountry.getId());
        sqlCity = sqlCityRepository.save(sqlCity);

        Neo4jCity neo4jCity = Neo4jTestGenerator.neo4jCityFromSql(sqlCity, sqlCountry);
        neo4jCityRepository.save(neo4jCity);

        CityDTO cityToUpdate = new CityDTO("newname", "newShortDesc", "newDesc", "newblob", Neo4jTestGenerator.generateCoords(), GenericTestGenerator.cityMetrics(), countryAName);
        cityService.updateCity(sqlCity.getId().toString(), cityToUpdate);

        SqlCity updatedSqlCity = sqlCityRepository.findById(sqlCity.getId()).get();
        Neo4jCity updatedNeo4jCity = neo4jCityRepository.findById(sqlCity.getId().toString()).get();

        assertThat(sqlCity.getId()).isEqualTo(updatedSqlCity.getId());
        assertThat(sqlCity.getCountryId()).isEqualTo(updatedSqlCity.getCountryId());

        assertThat(sqlCity.getName()).isNotEqualTo(updatedSqlCity.getName());
        assertThat(sqlCity.getDescription()).isNotEqualTo(updatedSqlCity.getDescription());
        assertThat(sqlCity.getCityMetrics()).isNotEqualTo(updatedSqlCity.getCityMetrics());
        assertThat(updatedSqlCity.getName()).isEqualTo(cityToUpdate.name());
        assertThat(updatedSqlCity.getDescription()).isEqualTo(cityToUpdate.description());
        assertThat(updatedSqlCity.getCityMetrics()).isEqualTo(cityToUpdate.cityMetrics());

        assertThat(neo4jCity.getId()).isEqualTo(updatedNeo4jCity.getId());
        assertThat(neo4jCity.getCountry().getId()).isEqualTo(updatedNeo4jCity.getCountry().getId());

        assertThat(neo4jCity.getName()).isNotEqualTo(updatedNeo4jCity.getName());
        assertThat(neo4jCity.getShortDescription()).isNotEqualTo(updatedNeo4jCity.getShortDescription());
        assertThat(neo4jCity.getPrimaryBlobUrl()).isNotEqualTo(updatedNeo4jCity.getPrimaryBlobUrl());
        assertThat(neo4jCity.getCoordinate()).isNotEqualTo(updatedNeo4jCity.getCoordinate());
        assertThat(neo4jCity.getCityMetrics()).isNotEqualTo(updatedNeo4jCity.getCityMetrics());
        assertThat(updatedNeo4jCity.getName()).isEqualTo(cityToUpdate.name());
        assertThat(updatedNeo4jCity.getShortDescription()).isEqualTo(cityToUpdate.shortDescription());
        assertThat(updatedNeo4jCity.getPrimaryBlobUrl()).isEqualTo(cityToUpdate.primaryBlobUrl());
        assertThat(updatedNeo4jCity.getCoordinate()).isEqualTo(cityToUpdate.coordinate());
        assertThat(updatedNeo4jCity.getCityMetrics()).isEqualTo(cityToUpdate.cityMetrics());

        assertThat(sqlCityRepository.findAll().size()).isEqualTo(1);
        assertThat(neo4jCityRepository.findAllCities().size()).isEqualTo(1);
    }

    @Test
    void deleteCity_shouldDeleteCityInSqlAndSyncInNeo4j_whenNoExceptionsThrown() throws Neo4jGenericException {
        SqlCity sqlCity = SqlCity.of("CityA", "CityA desc", GenericTestGenerator.cityMetrics(), sqlCountry.getId());
        sqlCity = sqlCityRepository.save(sqlCity);

        Neo4jCity neo4jCity = Neo4jTestGenerator.neo4jCityFromSql(sqlCity, sqlCountry);
        neo4jCityRepository.save(neo4jCity);

        CityToDeleteDTO cityToDeleteDTO = new CityToDeleteDTO(sqlCity.getName(), sqlCountry.getName());

        assertThat(sqlCityRepository.findAll().size()).isEqualTo(1);
        assertThat(neo4jCityRepository.findAllCities().size()).isEqualTo(1);

        cityService.deleteCity(cityToDeleteDTO);

        assertThat(sqlCityRepository.findAll().size()).isEqualTo(0);
        assertThat(neo4jCityRepository.findAllCities().size()).isEqualTo(0);
    }

    
}
