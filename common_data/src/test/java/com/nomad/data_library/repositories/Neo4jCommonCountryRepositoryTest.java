package com.nomad.data_library.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomad.data_library.Neo4jTestGenerator;
import com.nomad.data_library.config.Neo4jConfig;
import com.nomad.data_library.domain.neo4j.Neo4jCity;
import com.nomad.data_library.domain.neo4j.Neo4jCountry;
import com.nomad.data_library.domain.neo4j.Neo4jRoute;
import com.nomad.data_library.exceptions.Neo4jGenericException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.nomad.data_library.Neo4jTestConfiguration;
import com.nomad.data_library.TestConfig;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.neo4j.core.mapping.Neo4jMappingContext;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Log4j2
@DataNeo4jTest
@Import({TestConfig.class, Neo4jTestConfiguration.class, Neo4jConfig.class})
@Transactional
public class Neo4jCommonCountryRepositoryTest {

    private Neo4jCommonCountryRepository countryRepository;
    private Neo4jCommonCityRepository cityRepository;

    String countryAName = "CountryA";
    String countryBName = "CountryB";
    Neo4jCountry countryA = Neo4jTestGenerator.neo4jCountryNoCities(countryAName);
    Neo4jCountry countryB = Neo4jTestGenerator.neo4jCountryNoCities(countryBName);

    String cityAName = "CityA";
    String cityBName = "CityB";
    Neo4jCity cityA = Neo4jTestGenerator.neo4jCityNoRoutes(cityAName, countryA);
    Neo4jCity cityB = Neo4jTestGenerator.neo4jCityNoRoutes(cityBName, countryA);

    Neo4jRoute routeAToB = Neo4jTestGenerator.neo4jRoute(cityB);

    @BeforeEach
    void setup(@Autowired Neo4jClient neo4jClient, @Autowired Neo4jMappingContext schema, @Autowired ObjectMapper objectMapper) {
        Neo4jCityMappers neo4jCityMappers = new Neo4jCityMappers(schema);
        Neo4jCountryMappers neo4jCountryMappers = new Neo4jCountryMappers(schema);

        cityRepository = new Neo4jCommonCityRepository(neo4jClient, objectMapper, neo4jCityMappers);
        countryRepository = new Neo4jCommonCountryRepository(neo4jClient, objectMapper, neo4jCountryMappers);
    }

    @Test
    void findAllCountries_shouldReturnEmptySet_whenNoCountriesExist() {
        Set<Neo4jCountry> allCountries = countryRepository.findAllCountries();

        assertThat(allCountries.size()).isEqualTo(0);
    }

    @Test
    void findAllCountries_shouldReturnSetContainingAllCountries_ifCountriesExist() throws Neo4jGenericException {
        countryRepository.createCountry(countryA);
        countryRepository.createCountry(countryB);
        Set<Neo4jCountry> allCountries = countryRepository.findAllCountries();

        List<String> allCountryNames = allCountries.stream().map(Neo4jCountry::getName).toList();

        assertThat(allCountries.size()).isEqualTo(2);
        assertThat(allCountryNames.size()).isEqualTo(2);
        assertThat(allCountryNames).containsAll(List.of(countryAName, countryBName));
    }

    @Test
    void findAllCountries_shouldNotPopulateCitiesRelationship_ifCountryHasCities() throws Neo4jGenericException {
        countryRepository.createCountry(countryA);
        cityRepository.createCity(cityA);
        cityRepository.createCity(cityB);

        Set<Neo4jCountry> allCountries = countryRepository.findAllCountries();

        assertThat(allCountries.size()).isEqualTo(1);
        assertThat(allCountries.stream().findFirst().get().getCities()).isEmpty();
    }

    @Test
    void findByIdFetchCities_shouldReturnEmptyOptional_ifCountryDoesntExist() {
        Optional<Neo4jCountry> createdCountry = countryRepository.findByIdFetchCities("invalid");

        assertThat(createdCountry).isEmpty();
    }

    @Test
    void findByIdFetchCities_shouldReturnCountry_ifCountryExists() throws Neo4jGenericException {
        Neo4jCountry createdCountry = countryRepository.createCountry(countryA);

        createdCountry = countryRepository.findByIdFetchCities(createdCountry.getId()).get();

        assertThat(createdCountry).isEqualTo(countryA);
    }

    @Test
    void findByIdFetchCities_shouldPopulateCitiesRelationship_ifCountryHasCities() throws Neo4jGenericException {
        Neo4jCountry createdCountry = countryRepository.createCountry(countryA);
        cityRepository.createCity(cityA);

        createdCountry = countryRepository.findByIdFetchCities(createdCountry.getId()).get();

        assertThat(createdCountry.getCities()).isNotEmpty();
        assertThat(createdCountry.getCities().stream().findFirst().get())
                .usingRecursiveComparison()
                .ignoringFields("routes", "cityMetrics")
                .isEqualTo(cityA);
    }

    @Test
    void findByIdFetchCities_shouldNotPopulateCitiesRelationship_ifCountryDoesntHaveCities() throws Neo4jGenericException {
        Neo4jCountry createdCountry = countryRepository.createCountry(countryA);

        createdCountry = countryRepository.findByIdFetchCities(createdCountry.getId()).get();

        assertThat(createdCountry).isEqualTo(countryA);
        assertThat(createdCountry.getCities()).isEmpty();
    }

    @Test
    void createCountry_createsCountryNode_ifNotExist() throws Neo4jGenericException {
        Set<Neo4jCountry> allCountries = countryRepository.findAllCountries();
        Neo4jCountry createdCountry = countryRepository.createCountry(countryA);

        assertThat(createdCountry).isEqualTo(countryA);
        assertThat(allCountries).isEmpty();
    }

    @Test
    void createCountry_doesntRecreateCountry_ifExist() throws Neo4jGenericException {
        Neo4jCountry countryAFirstSave = countryRepository.createCountry(countryA);

        Neo4jCountry countryASecondSave = countryRepository.createCountry(countryA);

        Set<Neo4jCountry> allCountries = countryRepository.findAllCountries();

        assertThat(countryAFirstSave.getId()).isEqualTo(countryASecondSave.getId());
        assertThat(allCountries).isEqualTo(Set.of(countryAFirstSave));
    }

    @Test
    void createCountry_overwritesAllPropertiesExceptName_ifExist() {

        Neo4jCountry countryAFirstSave = countryRepository.createCountry(countryA);

        countryA = new Neo4jCountry(countryA.getId(), "new name", "newdescription", "new blob url", Set.of());

        Neo4jCountry countryASecondSave = countryRepository.createCountry(countryA);

        assertThat(countryASecondSave.getShortDescription()).isNotEqualTo(countryAFirstSave.getShortDescription());
        assertThat(countryASecondSave.getPrimaryBlobUrl()).isNotEqualTo(countryAFirstSave.getPrimaryBlobUrl());
        assertThat(countryASecondSave.getName()).isEqualTo(countryAFirstSave.getName());
        assertThat(countryASecondSave.getId()).isEqualTo(countryAFirstSave.getId());
    }

    @Test
    void createCountry_doesntTouchCityNodes_ever() throws Neo4jGenericException {
        countryRepository.createCountry(countryA);

        cityA = cityA.addRoute(routeAToB);
        cityRepository.createCity(cityA);
        cityRepository.createCity(cityB);
        cityRepository.saveRoute(cityA);

        Set<Neo4jCity> allCitiesFirstSearch = cityRepository.findAllCities();

        Neo4jCity cityADifferentProperties = Neo4jTestGenerator.neo4jCityNoRoutes(cityAName, countryA);
        Neo4jCity cityBDifferentProperties = Neo4jTestGenerator.neo4jCityNoRoutes(cityBName, countryA);

        Neo4jCountry countryAWithCities = new Neo4jCountry(countryA.getId(), countryA.getName(), countryA.getShortDescription(), countryA.getPrimaryBlobUrl(), Set.of(cityADifferentProperties, cityBDifferentProperties));
        countryRepository.createCountry(countryAWithCities);

        Set<Neo4jCity> allCitiesSecondSearch = cityRepository.findAllCities();

        assertThat(allCitiesSecondSearch.size()).isEqualTo(2);
        assertThat(allCitiesFirstSearch).isEqualTo(allCitiesSecondSearch);
    }

}
