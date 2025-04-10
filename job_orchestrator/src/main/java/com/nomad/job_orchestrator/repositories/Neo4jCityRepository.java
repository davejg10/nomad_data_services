package com.nomad.job_orchestrator.repositories;

import com.nomad.job_orchestrator.domain.CityPair;
import com.nomad.job_orchestrator.domain.Neo4jRouteToSave;
import com.nomad.data_library.domain.neo4j.Neo4jCity;
import com.nomad.data_library.domain.neo4j.Neo4jCountry;
import com.nomad.data_library.exceptions.Neo4jGenericException;
import com.nomad.data_library.repositories.Neo4jCommonCityRepository;

import com.nomad.scraping_library.domain.CityDTO;


import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.log4j.Log4j2;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@Log4j2
public class Neo4jCityRepository extends Neo4jCommonCityRepository {

    private final Neo4jCityMappers neo4jCityMappers;

    public Neo4jCityRepository(Neo4jClient neo4jClient, ObjectMapper objectMapper, Neo4jCityMappers neo4jCityMappers) {
        super(neo4jClient, objectMapper, neo4jCityMappers);
        this.neo4jCityMappers = neo4jCityMappers;
    }

    public List<CityPair> routeDiscoveryGivenCountry(String countryName) {
        Collection<CityPair> cities = neo4jClient
                .query("""
                    MATCH (source:City)-[:OF_COUNTRY]->(country:Country {name: $countryName})
                    MATCH (dest:City)-[:OF_COUNTRY]->(country)
                    WHERE source <> dest
                    RETURN source, dest, country
                """)
                .bind(countryName).to("countryName")
                .fetchAs(CityPair.class)
                .mappedBy((typeSystem, record) -> {

                    Neo4jCity sourceCity = neo4jCityMappers.cityMapper.apply(typeSystem, record.get("source").asNode());
                    Neo4jCity targetCity = neo4jCityMappers.cityMapper.apply(typeSystem, record.get("dest").asNode());
                    Neo4jCountry country = neo4jCityMappers.countryMapper.apply(typeSystem, record.get("country").asNode());

                    CityDTO sourceCityDTO = new CityDTO(sourceCity.getId(), sourceCity.getName());
                    CityDTO targetCityDTO = new CityDTO(targetCity.getId(), targetCity.getName());
                    return new CityPair(sourceCityDTO, targetCityDTO);
                })
                .all();
        return cities.stream().toList();
    }
    
    public void saveRoute(Neo4jRouteToSave routeToSave) {
        try {
            neo4jClient.query(Neo4jCommonCityRepository.QUERY_SAVE_ROUTE)
            .bind(routeToSave.routeDefinitionId()).to("routeId")
            .bind(routeToSave.sourceCityId()).to("id")
            .bind(routeToSave.targetCityId()).to("targetId")
            .bind(routeToSave.popularity()).to("popularity")
            .bind(routeToSave.averageDuration().toString()).to("averageDuration")
            .bind(routeToSave.averageCost().toString()).to("averageCost")
            .bind(routeToSave.transportType().toString()).to("transportType")
            .run();
        } catch (Exception e) {
            log.error("Unexpected exception when trying to save route to Neo4j. Exception: {}", e.getMessage());
            throw new Neo4jGenericException("Unexpected exception when trying to save route to Neo4j.", e);
        }
    }
}
