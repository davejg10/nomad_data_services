package com.nomad.job_orchestrator;

import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomad.data_library.Neo4jTestConfiguration;
import com.nomad.data_library.Neo4jTestGenerator;
import com.nomad.data_library.domain.neo4j.Neo4jCountry;
import com.nomad.data_library.exceptions.Neo4jGenericException;
import com.nomad.data_library.repositories.Neo4jCommonCountryRepository;
import com.nomad.job_orchestrator.domain.CityPair;
import com.nomad.job_orchestrator.repositories.Neo4jCityMappers;
import com.nomad.job_orchestrator.repositories.Neo4jCityRepository;
import com.nomad.job_orchestrator.repositories.Neo4jCountryMappers;
import com.nomad.scraping_library.connectors.ServiceBusBatchSender;
import com.nomad.scraping_library.domain.ScraperRequest;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.neo4j.core.mapping.Neo4jMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
@DataNeo4jTest
@Import({Neo4jTestConfiguration.class})
@Transactional
public class Neo4jCityRepositoryTest {

    @MockitoBean
    private ServiceBusBatchSender<ScraperRequest> serviceBusBatchSender;
    @MockitoBean
    private ServiceBusSenderClient serviceBusSenderClient;

    private Neo4jCityRepository cityRepository;
    private Neo4jCommonCountryRepository countryRepository;

    Neo4jCountry countryA =  Neo4jTestGenerator.neo4jCountryNoCities("CountryA");
    Neo4jCountry countryB =  Neo4jTestGenerator.neo4jCountryNoCities("CountryB");

    Neo4jCountry savedCountryA;
    Neo4jCountry savedCountryB;

    @BeforeEach
    void setup(@Autowired Neo4jClient neo4jClient, @Autowired Neo4jMappingContext schema, @Autowired ObjectMapper objectMapper) throws Neo4jGenericException {
        Neo4jCityMappers neo4jCityMappers = new Neo4jCityMappers(schema);
        Neo4jCountryMappers neo4jCountryMappers = new Neo4jCountryMappers(schema);

        cityRepository = new Neo4jCityRepository(neo4jClient, objectMapper, neo4jCityMappers);
        countryRepository = new Neo4jCommonCountryRepository(neo4jClient, objectMapper, neo4jCountryMappers);

        savedCountryA = countryRepository.createCountry(countryA);
        savedCountryB = countryRepository.createCountry(countryB);
    }

    @Test
    void findById_shouldReturnEmptyOptionalOfCity_WhenCityDoesntExist() {
        cityRepository.createCity(Neo4jTestGenerator.neo4jCityNoRoutes("CityA", savedCountryA));
        cityRepository.createCity(Neo4jTestGenerator.neo4jCityNoRoutes("CityB", savedCountryA));
        cityRepository.createCity(Neo4jTestGenerator.neo4jCityNoRoutes("CityC", savedCountryA));
        cityRepository.createCity(Neo4jTestGenerator.neo4jCityNoRoutes("CityD", savedCountryB));

        List<CityPair> allCityPairs = cityRepository.routeDiscoveryGivenCountry(countryA.getName());

        List<List<String>> allCityPairsNames = allCityPairs.stream().map(cityPair -> List.of(cityPair.sourceCity().name(), cityPair.targetCity().name())).toList();
        List<String> allCityNames = allCityPairsNames.stream().flatMap(list -> list.stream()).toList();
        assertThat(allCityPairsNames.size()).isEqualTo(6);
        assertThat(allCityNames).containsOnly("CityA", "CityB", "CityC");
    }
}
