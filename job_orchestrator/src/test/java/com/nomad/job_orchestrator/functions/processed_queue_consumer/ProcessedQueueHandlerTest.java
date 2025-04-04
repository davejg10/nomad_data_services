package com.nomad.job_orchestrator.functions.processed_queue_consumer;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.nomad.common_utils.domain.TransportType;
import com.nomad.data_library.SqlTestGenerator;
import com.nomad.data_library.domain.neo4j.Neo4jCity;
import com.nomad.data_library.domain.neo4j.Neo4jRoute;
import com.nomad.data_library.domain.sql.RouteDefinition;
import com.nomad.data_library.domain.sql.RouteInstance;
import com.nomad.data_library.domain.sql.RoutePopularity;
import com.nomad.data_library.domain.sql.RoutePopularityId;
import com.nomad.data_library.domain.sql.SqlCity;
import com.nomad.data_library.domain.sql.SqlCountry;
import com.nomad.data_library.repositories.RoutePopularityRepository;
import com.nomad.data_library.repositories.SqlCityRepository;
import com.nomad.data_library.repositories.SqlCountryRepository;
import com.nomad.job_orchestrator.repositories.Neo4jCityRepository;
import com.nomad.job_orchestrator.repositories.Neo4jCountryRepository;
import com.nomad.job_orchestrator.repositories.RouteDefinitionRepository;
import com.nomad.job_orchestrator.repositories.RouteInstanceRepository;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomad.data_library.Neo4jTestGenerator;
import com.nomad.data_library.exceptions.Neo4jGenericException;

import com.nomad.scraping_library.connectors.ServiceBusBatchSender;
import com.nomad.scraping_library.domain.CityDTO;
import com.nomad.scraping_library.domain.RouteDTO;
import com.nomad.scraping_library.domain.ScraperIdentifier;
import com.nomad.scraping_library.domain.ScraperRequest;
import com.nomad.scraping_library.domain.ScraperRequestSource;
import com.nomad.scraping_library.domain.ScraperRequestType;
import com.nomad.scraping_library.domain.ScraperResponse;

import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootTest
@Import({com.nomad.data_library.Neo4jTestConfiguration.class})
@Transactional  // Ensures test changes do not persist
public class ProcessedQueueHandlerTest {

    @MockitoBean
    private ServiceBusBatchSender<ScraperRequest> serviceBusBatchSender;
    @MockitoBean
    private ServiceBusSenderClient serviceBusSenderClient;

    @Autowired
    private Neo4jCityRepository neo4jRepository;

    @Autowired
    private Neo4jCountryRepository neo4jCountryRepository;

    @Autowired
    private SqlCityRepository sqlCityRepository;

    @Autowired
    private SqlCountryRepository sqlCountryRepository;

    @Autowired
    private RouteInstanceRepository routeInstanceRepository;

    @Autowired
    private RouteDefinitionRepository routeDefinitionRepository;

    @Autowired
    private RoutePopularityRepository routePopularityRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProcessedQueueHandler processedQueueHandler;

    private static CityDTO cityDTOA;
    private static CityDTO cityDTOB;
    static LocalDate futureDate = LocalDate.now().plusDays(2);

    static LocalDateTime generateRandomDepartureTime() {
        long randomLong = ThreadLocalRandom.current().nextLong(0, 59);
        return LocalDateTime.of(futureDate, LocalTime.parse("10:00:01").plusMinutes(randomLong));
    }
    static LocalDateTime generateRandomArrivalTime() {
        long randomLong = ThreadLocalRandom.current().nextLong(0, 59);
        return LocalDateTime.of(futureDate, LocalTime.parse("12:00:01").plusMinutes(randomLong));
    }

    private static RouteDTO routeDTOFlight1 = new RouteDTO(TransportType.FLIGHT, RandomStringUtils.randomAlphanumeric(6), generateRandomDepartureTime(), generateRandomArrivalTime(), "London", "Bangkok", BigDecimal.valueOf(10.40), "url");
    private static RouteDTO routeDTOFlight2 = new RouteDTO(TransportType.FLIGHT, RandomStringUtils.randomAlphanumeric(6), generateRandomDepartureTime(), generateRandomArrivalTime(), "London", "Bangkok", BigDecimal.valueOf(25.60), "url");
    private static RouteDTO routeDTOFlight3 = new RouteDTO(TransportType.FLIGHT, RandomStringUtils.randomAlphanumeric(6), generateRandomDepartureTime(), generateRandomArrivalTime(), "London", "Bangkok", BigDecimal.valueOf(120.20), "url");

    @BeforeEach
    void setupDb() {
        SqlCountry sqlCountry = sqlCountryRepository.save(SqlTestGenerator.sqlCountry("CountryA"));
        SqlCity sqlCityA = sqlCityRepository.save(SqlTestGenerator.sqlCity("CityA", sqlCountry));
        SqlCity sqlCityB = sqlCityRepository.save(SqlTestGenerator.sqlCity("CityB", sqlCountry));
        neo4jCountryRepository.createCountry(Neo4jTestGenerator.neo4jCountryFromSql(sqlCountry));
        neo4jRepository.createCity(Neo4jTestGenerator.neo4jCityFromSql(sqlCityA, sqlCountry));
        neo4jRepository.createCity(Neo4jTestGenerator.neo4jCityFromSql(sqlCityB, sqlCountry));

        cityDTOA = new CityDTO(sqlCityA.getId().toString(), sqlCityA.getName());
        cityDTOB = new CityDTO(sqlCityB.getId().toString(), sqlCityB.getName());
    }

    @Test
    void shouldNotRecreateRouteDefinition_ifReouteDefinitionWithSameTransportTypeSourceCityIdTargetCityIdExists() throws JsonProcessingException, Neo4jGenericException {
        UUID cityAId = UUID.fromString(cityDTOA.id());
        UUID cityBId = UUID.fromString(cityDTOB.id());

        routePopularityRepository.save(new RoutePopularity(new RoutePopularityId(cityAId, cityBId)));
        RouteDefinition routeDefinition = routeDefinitionRepository.save(RouteDefinition.of(TransportType.FLIGHT, cityAId, cityBId));

        ScraperResponse scraperResponse = new ScraperResponse(ScraperRequestSource.CRON, ScraperRequestType.ROUTE_DISCOVERY, ScraperIdentifier.ONE2GOASIA, TransportType.FLIGHT, cityDTOA, cityDTOB, List.of(routeDTOFlight1), futureDate);
        processedQueueHandler.accept(scraperResponse);

        Optional<RouteDefinition> routeDefinitionAfterSave = routeDefinitionRepository.findById(routeDefinition.getId());
        List<RouteDefinition> allRouteDefinitions = (List<RouteDefinition>) routeDefinitionRepository.findAll();

        assertThat(routeDefinitionAfterSave).isPresent();
        assertThat(allRouteDefinitions.size()).isEqualTo(1);
    }

    @Test
    void shouldCreateRouteDefinition_ifRouteDefinitionWithSameTransportTypeAndSourceCityIdAndTargetCityIdDoesntExist() throws Neo4jGenericException {
        List<RouteDefinition> allRouteDefinitionsBeforeSave = (List<RouteDefinition>) routeDefinitionRepository.findAll();

        ScraperResponse scraperResponse = new ScraperResponse(ScraperRequestSource.CRON, ScraperRequestType.ROUTE_DISCOVERY, ScraperIdentifier.ONE2GOASIA, TransportType.FLIGHT, cityDTOA, cityDTOB, List.of(routeDTOFlight1), futureDate);
        processedQueueHandler.accept(scraperResponse);
        List<RouteDefinition> allRouteDefinitionsAfterSave = (List<RouteDefinition>) routeDefinitionRepository.findAll();
        RouteDefinition routeDefinition = allRouteDefinitionsAfterSave.get(0);

        assertThat(allRouteDefinitionsBeforeSave.size()).isEqualTo(0);
        assertThat(allRouteDefinitionsAfterSave.size()).isEqualTo(1);
        assertThat(routeDefinition.getTransportType()).isEqualTo(scraperResponse.getTransportType());
        assertThat(routeDefinition.getSourceCityId().toString()).isEqualTo(scraperResponse.getSourceCity().id());
        assertThat(routeDefinition.getTargetCityId().toString()).isEqualTo(scraperResponse.getTargetCity().id());
    }

    @Test
    void shouldCreateRouteInstances_forEachRouteDTOInScraperResponse() throws Neo4jGenericException {
        ScraperResponse scraperResponse = new ScraperResponse(ScraperRequestSource.CRON, ScraperRequestType.ROUTE_DISCOVERY, ScraperIdentifier.ONE2GOASIA, TransportType.FLIGHT, cityDTOA, cityDTOB, List.of(routeDTOFlight1, routeDTOFlight2, routeDTOFlight3), futureDate);
        processedQueueHandler.accept(scraperResponse);
        List<RouteInstance> allRouteInstances = (List<RouteInstance>) routeInstanceRepository.findAll();

        List<String> allRouteDefinitionIds = allRouteInstances.stream().map(routeInstance -> routeInstance.getRouteDefinition().getId().toString()).distinct().toList();
        assertThat(allRouteInstances.size()).isEqualTo(3);
        assertThat(allRouteDefinitionIds.size()).isEqualTo(1);
    }

    @Test
    void shouldReplaceRouteInstances_ifRouteInstancesExistWithSameSearchDateAndRouteDefinitionId() throws Neo4jGenericException {
        UUID cityAId = UUID.fromString(cityDTOA.id());
        UUID cityBId = UUID.fromString(cityDTOB.id());

        routePopularityRepository.save(new RoutePopularity(new RoutePopularityId(cityAId, cityBId)));
        RouteDefinition routeDefinition = routeDefinitionRepository.save(RouteDefinition.of(TransportType.FLIGHT, cityAId, cityBId));

        RouteInstance routeInstance1 = routeInstanceRepository.save(RouteInstance.of(BigDecimal.valueOf(10.99), generateRandomDepartureTime(), generateRandomArrivalTime(), "EasyJet", "London", "Bangkok", "url", futureDate, routeDefinition));
        RouteInstance routeInstance2 = routeInstanceRepository.save(RouteInstance.of(BigDecimal.valueOf(10.99), generateRandomDepartureTime(), generateRandomArrivalTime(), "EasyJet", "London", "Bangkok", "url", futureDate, routeDefinition));
        List<RouteInstance> allRouteInstanceBeforeSave = (List<RouteInstance>) routeInstanceRepository.findAll();

        ScraperResponse scraperResponse = new ScraperResponse(ScraperRequestSource.CRON, ScraperRequestType.ROUTE_DISCOVERY, ScraperIdentifier.ONE2GOASIA, TransportType.FLIGHT, cityDTOA, cityDTOB, List.of(routeDTOFlight1, routeDTOFlight2, routeDTOFlight3), futureDate);
        processedQueueHandler.accept(scraperResponse);

        List<RouteInstance> allRouteInstanceAfterSave = (List<RouteInstance>) routeInstanceRepository.findAll();
        List<String> allRouteDefinitionIds = allRouteInstanceAfterSave.stream().map(routeInstance -> routeInstance.getRouteDefinition().getId().toString()).distinct().toList();

        assertThat(allRouteInstanceAfterSave.size()).isEqualTo(3);
        assertThat(allRouteDefinitionIds.size()).isEqualTo(1);
        assertThat(allRouteDefinitionIds.get(0)).isEqualTo(routeDefinition.getId().toString());
        assertThat(allRouteInstanceAfterSave).doesNotContain(routeInstance1, routeInstance2);
    }

    @Test
    void shouldNotReplaceRouteInstances_ifRouteInstancesExistButNotWithSameSearchDateAndRouteDefinitionId() throws Neo4jGenericException {
        UUID cityAId = UUID.fromString(cityDTOA.id());
        UUID cityBId = UUID.fromString(cityDTOB.id());

        routePopularityRepository.save(new RoutePopularity(new RoutePopularityId(cityAId, cityBId)));
        RouteDefinition routeDefinition = routeDefinitionRepository.save(RouteDefinition.of(TransportType.FLIGHT, cityAId, cityBId));

        RouteInstance routeInstance1 = routeInstanceRepository.save(RouteInstance.of(BigDecimal.valueOf(10.99), generateRandomDepartureTime(), generateRandomArrivalTime(), "EasyJet", "London", "Bangkok", "url", futureDate, routeDefinition));
        RouteInstance routeInstance2 = routeInstanceRepository.save(RouteInstance.of(BigDecimal.valueOf(10.99), generateRandomDepartureTime(), generateRandomArrivalTime(), "EasyJet", "London", "Bangkok", "url", futureDate, routeDefinition));
        List<RouteInstance> allRouteInstanceBeforeSave = (List<RouteInstance>) routeInstanceRepository.findAll();

        LocalDate differentSearchDate = futureDate.plusDays(5);
        ScraperResponse scraperResponse = new ScraperResponse(ScraperRequestSource.CRON, ScraperRequestType.ROUTE_DISCOVERY, ScraperIdentifier.ONE2GOASIA, TransportType.FLIGHT, cityDTOA, cityDTOB, List.of(routeDTOFlight1, routeDTOFlight2, routeDTOFlight3), differentSearchDate);
        processedQueueHandler.accept(scraperResponse);

        List<RouteInstance> allRouteInstanceAfterSave = (List<RouteInstance>) routeInstanceRepository.findAll();
        List<String> allRouteDefinitionIds = allRouteInstanceAfterSave.stream().map(routeInstance -> routeInstance.getRouteDefinition().getId().toString()).distinct().toList();

        assertThat(allRouteInstanceAfterSave.size()).isEqualTo(5);
        assertThat(allRouteDefinitionIds.size()).isEqualTo(1);
        assertThat(allRouteInstanceAfterSave).contains(routeInstance1, routeInstance2);
    }

    @Test
    void shouldCreateRouteRelationshipInNeo4j_ifRouteDefinitionWithSameTransportTypeAndSourceCityIdAndTargetCityIdDoesntExist() throws Neo4jGenericException {
        List<RouteDefinition> allRouteDefinitionsBeforeSave = (List<RouteDefinition>) routeDefinitionRepository.findAll();

        ScraperResponse scraperResponse = new ScraperResponse(ScraperRequestSource.CRON, ScraperRequestType.ROUTE_DISCOVERY, ScraperIdentifier.ONE2GOASIA, TransportType.FLIGHT, cityDTOA, cityDTOB, List.of(routeDTOFlight1, routeDTOFlight2, routeDTOFlight3), futureDate);
        processedQueueHandler.accept(scraperResponse);
        List<RouteDefinition> allRouteDefinitionsAfterSave = (List<RouteDefinition>) routeDefinitionRepository.findAll();

        Neo4jCity neo4jCity = neo4jRepository.findByIdFetchRoutes(cityDTOA.id()).get();
        List<Neo4jRoute> allNeo4jRoutes = neo4jCity.getRoutes().stream().distinct().toList();
        String routeDefinitionId = allRouteDefinitionsAfterSave.stream().map(routeDefinition -> routeDefinition.getId().toString()).distinct().toList().get(0);

        assertThat(allRouteDefinitionsBeforeSave.size()).isEqualTo(0);
        assertThat(allRouteDefinitionsAfterSave.size()).isEqualTo(1);
        assertThat(allNeo4jRoutes.size()).isEqualTo(1);
        assertThat(allNeo4jRoutes.get(0).getId()).isEqualTo(routeDefinitionId);

    }

    @Test
    void shouldNotCreateRouteRelationshipInNeo4j_ifRouteDefinitionWithSameTransportTypeAndSourceCityIdAndTargetCityIdExists() throws Neo4jGenericException {
        UUID cityAId = UUID.fromString(cityDTOA.id());
        UUID cityBId = UUID.fromString(cityDTOB.id());
        
        routePopularityRepository.save(new RoutePopularity(new RoutePopularityId(cityAId, cityBId)));
        RouteDefinition routeDefinition = routeDefinitionRepository.save(RouteDefinition.of(TransportType.FLIGHT, cityAId, cityBId));

        ScraperResponse scraperResponse = new ScraperResponse(ScraperRequestSource.CRON, ScraperRequestType.ROUTE_DISCOVERY, ScraperIdentifier.ONE2GOASIA, TransportType.FLIGHT, cityDTOA, cityDTOB, List.of(routeDTOFlight1, routeDTOFlight2, routeDTOFlight3), futureDate);
        processedQueueHandler.accept(scraperResponse);

        Neo4jCity neo4jCity = neo4jRepository.findByIdFetchRoutes(cityDTOA.id()).get();

        assertThat(neo4jCity.getRoutes().size()).isEqualTo(0);
    }

    @Test
    void shouldCalculateAverageCostAndDuration_whenCreatingRouteRelationshipInNeo4j() throws Neo4jGenericException {
        List<RouteDTO> allRoutes = List.of(routeDTOFlight1, routeDTOFlight2, routeDTOFlight3);

        ScraperResponse scraperResponse = new ScraperResponse(ScraperRequestSource.CRON, ScraperRequestType.ROUTE_DISCOVERY, ScraperIdentifier.ONE2GOASIA, TransportType.FLIGHT, cityDTOA, cityDTOB, allRoutes, futureDate);
        processedQueueHandler.accept(scraperResponse);

        Neo4jCity neo4jCity = neo4jRepository.findByIdFetchRoutes(cityDTOA.id()).get();
        Neo4jRoute neo4jRoute = neo4jCity.getRoutes().stream().distinct().toList().get(0);
        Duration duration1 = Duration.between(routeDTOFlight1.depart(), routeDTOFlight1.arrival());
        Duration duration2 = Duration.between(routeDTOFlight2.depart(), routeDTOFlight2.arrival());
        Duration duration3 = Duration.between(routeDTOFlight3.depart(), routeDTOFlight3.arrival());
        Duration averageDuration = duration1.plus(duration2).plus(duration3).dividedBy(3);

        BigDecimal averageCost = (routeDTOFlight1.cost().add(routeDTOFlight2.cost()).add(routeDTOFlight3.cost())).divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP);
        assertThat(neo4jRoute.getAverageCost()).isEqualTo(averageCost);
        assertThat(neo4jRoute.getAverageDuration()).isEqualTo(averageDuration);
    }

    @Test
    void shouldCreateRoutePopularity_andSetPopularityTo0_ifRouteDefinitionDoesntExistAndRoutePopularityDoesntExist() {
       List<RoutePopularity> allRoutePopularityBeforeSave = (List<RoutePopularity>) routePopularityRepository.findAll();

        ScraperResponse scraperResponse = new ScraperResponse(ScraperRequestSource.CRON, ScraperRequestType.ROUTE_DISCOVERY, ScraperIdentifier.ONE2GOASIA, TransportType.FLIGHT, cityDTOA, cityDTOB, List.of(routeDTOFlight1), futureDate);
        processedQueueHandler.accept(scraperResponse);

        List<RoutePopularity> allRoutePopularityAfterSave = (List<RoutePopularity>) routePopularityRepository.findAll();

        RoutePopularity createdRoutePopularity = allRoutePopularityAfterSave.get(0);
        Neo4jRoute neo4jRoute = neo4jRepository.findByIdFetchRoutes(cityDTOA.id()).get().getRoutes().stream().findFirst().get();
        assertThat(allRoutePopularityBeforeSave.size()).isEqualTo(0);
        assertThat(allRoutePopularityAfterSave.size()).isEqualTo(1);

        assertThat(createdRoutePopularity.getPopularity()).isEqualTo(0.0);
        assertThat(neo4jRoute.getPopularity()).isEqualTo(0.0);
    }

    @Test
    void shouldNotCreateRoutePopularity_ifRoutePopularityExists() {
        UUID cityAId = UUID.fromString(cityDTOA.id());
        UUID cityBId = UUID.fromString(cityDTOB.id());

        RoutePopularity routePopularity = routePopularityRepository.save(new RoutePopularity(cityAId, cityBId));

        ScraperResponse scraperResponse = new ScraperResponse(ScraperRequestSource.CRON, ScraperRequestType.ROUTE_DISCOVERY, ScraperIdentifier.ONE2GOASIA, TransportType.FLIGHT, cityDTOA, cityDTOB, List.of(routeDTOFlight1), futureDate);
        processedQueueHandler.accept(scraperResponse);

        RoutePopularity routePopularityAfterSave = ((List<RoutePopularity>) routePopularityRepository.findAll()).get(0);

        assertThat(routePopularity).isEqualTo(routePopularityAfterSave);
    }

    @Test
    void shouldSetRoutePopularityPopularity_toNeo4jRouteRelationship_ifRoutePopularityExist() {
        UUID cityAId = UUID.fromString(cityDTOA.id());
        UUID cityBId = UUID.fromString(cityDTOB.id());

        RoutePopularity routePopularity = routePopularityRepository.save(new RoutePopularity(cityAId, cityBId, 4));

        ScraperResponse scraperResponse = new ScraperResponse(ScraperRequestSource.CRON, ScraperRequestType.ROUTE_DISCOVERY, ScraperIdentifier.ONE2GOASIA, TransportType.FLIGHT, cityDTOA, cityDTOB, List.of(routeDTOFlight1), futureDate);
        processedQueueHandler.accept(scraperResponse);

        Neo4jRoute neo4jRoute = neo4jRepository.findByIdFetchRoutes(cityDTOA.id()).get().getRoutes().stream().findFirst().get();
        assertThat(routePopularity.getPopularity()).isEqualTo(4);
        assertThat(neo4jRoute.getPopularity()).isEqualTo(4);
    }
}