package com.nomad.admin_api.services;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nomad.admin_api.exceptions.DatabaseSyncException;
import com.nomad.admin_api.functions.put_route_popularity.RoutePopularityDTO;
import com.nomad.admin_api.repositories.Neo4jCityRepository;
import com.nomad.data_library.domain.sql.RoutePopularity;
import com.nomad.data_library.exceptions.Neo4jGenericException;
import com.nomad.data_library.repositories.RoutePopularityRepository;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class RoutePopularityService {
    
    private final RoutePopularityRepository routePopularityRepository;
    private final Neo4jCityRepository neo4jCityRepository;

    public RoutePopularityService(
            RoutePopularityRepository routePopularityRepository,
            Neo4jCityRepository neo4jCityRepository) {
        this.routePopularityRepository = routePopularityRepository;
        this.neo4jCityRepository = neo4jCityRepository;
    }

    @Transactional
    public void updateRoutePopularity(RoutePopularityDTO routePopularityDTO) {
        UUID sourceCityId = routePopularityDTO.sourceCityId();
        UUID targetCityId = routePopularityDTO.targetCityId();
        double popularity = routePopularityDTO.popularity();
        try {
            log.info("Creating/Overwriting RoutePopularity for sourceCityId: {}, targetCityId: {}, with new popularity: {}", sourceCityId, targetCityId, popularity);
            // We dont need to lookup if it exists. This will either create new or overwrite existing.
            RoutePopularity routePopularity = new RoutePopularity(sourceCityId, targetCityId, popularity);
            routePopularityRepository.save(routePopularity);
            log.info("Successfully saved RoutePopularity in Postgres Flexible Server.");

            log.info("Attempting to update all routes with new popularity in Neo4j between cities if any exist..");
            neo4jCityRepository.updateAllRoutesPopularity(sourceCityId.toString(), targetCityId.toString(), popularity);
        } catch (Neo4jGenericException e) {
            log.error("Failed to update City with id: {} routes. Transaction will be rolled back. Error: {}", sourceCityId, e.getMessage());
            throw new DatabaseSyncException("Failed to update City with id: " + sourceCityId + " routes in Neo4j.", e);
        } catch (Exception e) {
            log.error("Unexpected error while trying to updating city with id: {} routes. Transaction will be rolled back. Error: {}", sourceCityId, e.getMessage());
            throw new DatabaseSyncException("Unexpected error while trying to update city with id: " + sourceCityId + " routes.", e);
        }
    }
}
