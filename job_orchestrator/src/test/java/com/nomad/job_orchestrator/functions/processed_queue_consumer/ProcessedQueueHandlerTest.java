// package com.nomad.job_orchestrator.functions.processed_queue_consumer;

// import static org.assertj.core.api.Assertions.assertThat;

// import java.time.LocalDate;
// import java.time.LocalDateTime;
// import java.time.LocalTime;
// import java.util.Set;

// import org.junit.jupiter.api.BeforeAll;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.context.annotation.Import;
// import org.springframework.data.neo4j.core.Neo4jClient;
// import org.springframework.test.context.ActiveProfiles;
// import org.springframework.test.context.bean.override.mockito.MockitoBean;

// import com.fasterxml.jackson.core.JsonProcessingException;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.nomad.job_orchestrator.Neo4jCityRepository;
// import com.nomad.data_library.Neo4jTestGenerator;
// import com.nomad.data_library.domain.neo4j.Neo4jCity;
// import com.nomad.data_library.domain.neo4j.Neo4jCountry;
// import com.nomad.data_library.exceptions.Neo4jGenericException;

// import com.nomad.scraping_library.connectors.ServiceBusBatchSender;
// import com.nomad.scraping_library.domain.TransportType;
// import com.nomad.scraping_library.domain.CityDTO;
// import com.nomad.scraping_library.domain.RouteDTO;
// import com.nomad.scraping_library.domain.ScraperRequest;
// import com.nomad.scraping_library.domain.ScraperRequestType;
// import com.nomad.scraping_library.domain.ScraperResponse;

// import jakarta.transaction.Transactional;
// import lombok.extern.log4j.Log4j2;

// @Log4j2
// @SpringBootTest
// @ActiveProfiles("maven")
// @Import({com.nomad.data_library.Neo4jTestConfiguration.class})
// @Transactional  // Ensures test changes do not persist
// public class ProcessedQueueHandlerTest {

//     @MockitoBean
//     private ServiceBusBatchSender<ScraperRequest> serviceBusBatchSender;

//     @Autowired
//     private Neo4jCityRepository neo4jRepository;

//     @Autowired
//     private ObjectMapper objectMapper;
    
//     @Autowired
//     private ProcessedQueueHandler processedQueueHandler;
    
//     private static Neo4jCountry neo4jCountry =  Neo4jTestGenerator.neo4jCountryNoCities("CountryA");
//     private static Neo4jCity neo4jCityB = Neo4jTestGenerator.neo4jCityNoRoutes("CityB", neo4jCountry);
//     private static Neo4jCity neo4jCityA = Neo4jTestGenerator.neo4jCityNoRoutes("CityA", neo4jCountry);

//     static LocalDate futureDate = LocalDate.now().plusDays(2);
//     static LocalDateTime depart = LocalDateTime.of(futureDate, LocalTime.parse("10:00:01"));
//     static LocalDateTime arrival = LocalDateTime.of(futureDate, LocalTime.parse("14:00:01"));

//     private static CityDTO cityDTOB = new CityDTO(neo4jCityB.getId(), neo4jCityB.getName());
//     private static CityDTO cityDTOA = new CityDTO(neo4jCityA.getId(), neo4jCityA.getName());
//     private static RouteDTO routeDTO = new RouteDTO(TransportType.FLIGHT, "easyjet", depart, arrival, 19.99);
    
//     @BeforeAll
//     static void setupDb(@Autowired Neo4jClient neo4jClient) {
        
//         neo4jClient.query("""
//             CREATE (c:Country {id: $id, name: $name})
//         """)
//         .bind(neo4jCountry.getId().toString()).to("id")
//         .bind(neo4jCountry.getName()).to("name")
//         .run();
//     }

//     @Test
//     void processedQueueHandler_shouldSaveCityWithRoutes_whenValidCityDTOPassed() throws JsonProcessingException, Neo4jGenericException {
//         neo4jRepository.createCity(neo4jCityA);
//         neo4jRepository.createCity(neo4jCityB);

//         ScraperResponse scraperResponse = new ScraperResponse("cronTrigger", ScraperRequestType.ROUTE_DISCOVERY, cityDTOA, cityDTOB, routeDTO, futureDate);
//         String scraperResponseString = objectMapper.writeValueAsString(scraperResponse);
//         processedQueueHandler.accept(scraperResponseString);

//         Set<Neo4jCity> neo4jCities = neo4jRepository.findAllCities();
//         assertThat(neo4jCities.size()).isEqualTo(2);
//     }

// }