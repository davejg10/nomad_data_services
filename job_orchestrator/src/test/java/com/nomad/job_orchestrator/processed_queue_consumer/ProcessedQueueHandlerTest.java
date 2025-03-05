package com.nomad.job_orchestrator.processed_queue_consumer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomad.job_orchestrator.Neo4jRepository;
import com.nomad.job_orchestrator.config.ServiceBusBatchSender;
import com.nomad.job_orchestrator.functions.processed_queue_consumer.ProcessedQueueHandler;
import com.nomad.library.domain.CityDTO;
import com.nomad.library.domain.Neo4jCity;
import com.nomad.library.domain.Neo4jCountry;
import com.nomad.library.domain.RouteDTO;
import com.nomad.library.domain.TransportType;

import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootTest
@Import({com.nomad.library.Neo4jTestConfiguration.class})
public class ProcessedQueueHandlerTest {

    @MockitoBean
    private ServiceBusBatchSender serviceBusBatchSender;

    @Autowired
    private Neo4jRepository neo4jRepository;

    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private ProcessedQueueHandler processedQueueHandler;

    private static Neo4jCountry neo4jCountry = new Neo4jCountry("9e88733d-e0b1-4c18-ad4f-c26a40e5cbe3", "CountryA");
    private static Neo4jCity neo4jCity = new Neo4jCity("4bf9fda7-8b31-4c1d-b0a5-7ec8048206fa", "CityB", neo4jCountry);
    private static RouteDTO route = new RouteDTO(neo4jCity, 3, 4.0, 12.99, TransportType.FLIGHT);
    private static CityDTO cityDTO = new CityDTO("dee851e0-f98f-493d-9f56-9938734b061d", "CityA", route);

    @BeforeAll
    static void setupDb(@Autowired Neo4jClient neo4jClient) {
        
        neo4jClient.query("""
            CREATE (c:City {id: $id, name: $name}), (d:City {id: $id2, name: $name2})
        """)
        .bind(neo4jCountry.getId()).to("id")
        .bind(neo4jCountry.getName()).to("name")
        .bind(cityDTO.id()).to("id2")
        .bind(cityDTO.name()).to("name2")
        .run();
    }

    @Test
    void processedQueueHandler_shouldSaveCityWithRoutes_whenValidCityDTOPassed() throws JsonProcessingException {
        String cityDTOMessage = objectMapper.writeValueAsString(cityDTO);

        processedQueueHandler.accept(cityDTOMessage);

        Set<Neo4jCity> neo4jCities = neo4jRepository.findAllCities();

        assertThat(neo4jCities.size()).isEqualTo(2);
        


    }

}