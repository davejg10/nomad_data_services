package com.nomad.job_orchestrator.functions.processed_queue_consumer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import org.springframework.stereotype.Component;

import com.nomad.data_library.domain.sql.RouteDefinition;
import com.nomad.data_library.domain.sql.RouteInstance;
import com.nomad.data_library.domain.sql.RoutePopularity;
import com.nomad.data_library.domain.sql.RoutePopularityId;
import com.nomad.data_library.repositories.RoutePopularityRepository;
import com.nomad.job_orchestrator.domain.Neo4jRouteToSave;
import com.nomad.job_orchestrator.repositories.Neo4jCityRepository;
import com.nomad.job_orchestrator.repositories.RouteDefinitionRepository;
import com.nomad.job_orchestrator.repositories.RouteInstanceRepository;
import com.nomad.scraping_library.domain.RouteDTO;
import com.nomad.scraping_library.domain.ScraperResponse;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class ProcessedQueueHandler implements Consumer<ScraperResponse> {
    
    private final Neo4jCityRepository neo4jCityRepository;
    private final RouteDefinitionRepository routeDefinitionRepository;
    private final RouteInstanceRepository routeInstanceRepository;
    private final RoutePopularityRepository routePopularityRepository;

    public ProcessedQueueHandler(
            Neo4jCityRepository neo4jCityRepository, 
            RouteDefinitionRepository routeDefinitionRepository, 
            RouteInstanceRepository routeInstanceRepository,
            RoutePopularityRepository routePopularityRepository) {
        this.neo4jCityRepository = neo4jCityRepository;
        this.routeDefinitionRepository = routeDefinitionRepository;
        this.routeInstanceRepository = routeInstanceRepository;
        this.routePopularityRepository = routePopularityRepository;
    }

    public void accept(ScraperResponse scraperResponse) {
        log.info("In processedQueueHandler. Starting to process scraperResponse");

        UUID sourceCityId = UUID.fromString(scraperResponse.getSourceCity().id());
        UUID targetCityId = UUID.fromString(scraperResponse.getTargetCity().id());
        
        Optional<RouteDefinition> existingRouteDefinition = routeDefinitionRepository.findByTransportTypeAndSourceCityIdAndTargetCityId(scraperResponse.getTransportType(), sourceCityId, targetCityId);
        RouteDefinition routeDefinition;

        if (existingRouteDefinition.isPresent()) { // If RouteDefinition exists WE KNOW RoutePopularity exists.. so need to do nothing
            log.info("Route definition exists, deleting RouteInstance(s) first and then re-creating");
            routeDefinition = existingRouteDefinition.get();

            routeInstanceRepository.deleteAllByRouteDefinitionAndSearchDate(routeDefinition, scraperResponse.getSearchDate());

        } else {
            log.info("Route definition does not exist, ensuring RoutePopularity exists and then creating RouteDefinition.");

            RoutePopularity routePopularity = findOrCreateRoutePopularity(sourceCityId, targetCityId);

            log.info("Saving RouteDefinition..");
            routeDefinition = routeDefinitionRepository.save(RouteDefinition.of(scraperResponse.getTransportType(), sourceCityId, targetCityId));

            BigDecimal averageCost = scraperResponse.getRoutes().stream()
                                                .map(RouteDTO::cost)
                                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                                                .divide(BigDecimal.valueOf(scraperResponse.getRoutes().size()), 2, RoundingMode.HALF_UP);
            Duration averageTravelTime = scraperResponse.getRoutes().stream()
                                                .map(routeDTO -> Duration.between(routeDTO.depart(), routeDTO.arrival()))
                                                .reduce(Duration.ZERO, Duration::plus)
                                                .dividedBy(Math.max(1, scraperResponse.getRoutes().size()));

            Neo4jRouteToSave routeToSave = new Neo4jRouteToSave(routeDefinition.getId().toString(), sourceCityId.toString(), routePopularity.getPopularity(), averageTravelTime, averageCost, scraperResponse.getTransportType(), targetCityId.toString());
            log.info("Saving Neo4jRoute: {}", routeToSave);
            neo4jCityRepository.saveRoute(routeToSave);
        }

        Set<RouteInstance> routeInstances = new HashSet<>();
        for (RouteDTO route : scraperResponse.getRoutes()) {
            routeInstances.add(RouteInstance.of(route.cost(), route.depart(), route.arrival(), route.operator(), route.departureLocation(), route.arrivalLocation(), route.url(), scraperResponse.getSearchDate(), routeDefinition));
        }
        log.info("Number of RouteInstances to save: {}", routeInstances.size());
        log.info("Number of routes in ScraperResponse: {}", scraperResponse.getRoutes().size());
        routeInstanceRepository.saveAll(routeInstances);
    }

    private RoutePopularity findOrCreateRoutePopularity(UUID sourceCityId, UUID targetCityId) {
        RoutePopularityId popularityId = new RoutePopularityId(sourceCityId, targetCityId);

        Optional<RoutePopularity> routePopularity = routePopularityRepository.findById(popularityId);

        if (routePopularity.isPresent()) {
            log.info("RoutePopularity found with ID: {}, returning", popularityId);
            return routePopularity.get();
        } else {
            log.info("RoutePopularity not found for ID: {}. Creating and saving new one.", popularityId);

            RoutePopularity newPopularity = new RoutePopularity(popularityId);

            return routePopularityRepository.save(newPopularity);
        }
    }
}

