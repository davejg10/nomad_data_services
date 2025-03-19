package com.nomad.job_orchestrator.functions.processed_queue_consumer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.DoubleStream;

import org.springframework.stereotype.Component;

import com.nomad.data_library.domain.sql.RouteDefinition;
import com.nomad.data_library.domain.sql.RouteInstance;
import com.nomad.job_orchestrator.domain.Neo4jRouteToSave;
import com.nomad.job_orchestrator.repositories.Neo4jCityRepository;
import com.nomad.job_orchestrator.repositories.SqlRouteDefinitionRepository;
import com.nomad.job_orchestrator.repositories.SqlRouteInstanceRepository;
import com.nomad.scraping_library.domain.RouteDTO;
import com.nomad.scraping_library.domain.ScraperResponse;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class ProcessedQueueHandler implements Consumer<ScraperResponse> {
    
    private final Neo4jCityRepository neo4jCityRepository;
    private final SqlRouteDefinitionRepository sqlRouteDefinitionRepository;
    private final SqlRouteInstanceRepository sqlRouteInstanceRepository;

    public ProcessedQueueHandler(Neo4jCityRepository neo4jCityRepository, SqlRouteDefinitionRepository sqlRouteDefinitionRepository, SqlRouteInstanceRepository sqlRouteInstanceRepository) {
        this.neo4jCityRepository = neo4jCityRepository;
        this.sqlRouteDefinitionRepository = sqlRouteDefinitionRepository;
        this.sqlRouteInstanceRepository = sqlRouteInstanceRepository;
    }

    public void accept(ScraperResponse scraperResponse) {
        log.info("In here with scraperResponse: {}", scraperResponse);

        UUID sourceCityId = UUID.fromString(scraperResponse.getSourceCity().id());
        UUID targetCityId = UUID.fromString(scraperResponse.getTargetCity().id());
        
        Optional<RouteDefinition> existingRouteDefinition = sqlRouteDefinitionRepository.findByTransportTypeAndSourceCityIdAndTargetCityId(scraperResponse.getTransportType(), sourceCityId, targetCityId);
        RouteDefinition routeDefinition;

        if (existingRouteDefinition.isPresent()) {
            log.info("Route definition exists, deleting first and then re-adding");
            routeDefinition = existingRouteDefinition.get();
            sqlRouteInstanceRepository.deleteAllByRouteDefinitionAndSearchDate(routeDefinition, scraperResponse.getSearchDate());

        } else {
            log.info("Route definition does not exist, creating first and adding route to Neo4j");
            double popularity = 1;
            routeDefinition = sqlRouteDefinitionRepository.save(RouteDefinition.of(popularity, scraperResponse.getTransportType(), sourceCityId, targetCityId));

            BigDecimal averageCost = scraperResponse.getRoutes().stream()
                                                .map(RouteDTO::cost)
                                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                                                .divide(BigDecimal.valueOf(scraperResponse.getRoutes().size()), 2, RoundingMode.HALF_UP);
            Duration averageTravelTime = scraperResponse.getRoutes().stream()
                                                .map(routeDTO -> Duration.between(routeDTO.depart(), routeDTO.arrival()))
                                                .reduce(Duration.ZERO, Duration::plus)
                                                .dividedBy(Math.max(1, scraperResponse.getRoutes().size()));

            Neo4jRouteToSave routeToSave = new Neo4jRouteToSave(routeDefinition.getId().toString(), sourceCityId.toString(), popularity, averageTravelTime, averageCost, scraperResponse.getTransportType(), targetCityId.toString());
            neo4jCityRepository.saveRoute(routeToSave);
        }

        Set<RouteInstance> routeInstances = new HashSet<>();
        for (RouteDTO route : scraperResponse.getRoutes()) {
            routeInstances.add(RouteInstance.of(route.cost(), route.depart(), route.arrival(), scraperResponse.getSearchDate(), routeDefinition));
        }

        sqlRouteInstanceRepository.saveAll(routeInstances);
        
        
    }
}

