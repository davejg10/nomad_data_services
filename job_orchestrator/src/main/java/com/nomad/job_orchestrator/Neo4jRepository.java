package com.nomad.job_orchestrator;

import com.nomad.library.domain.Neo4jCity;
import com.nomad.library.domain.Neo4jCountry;
import com.nomad.library.domain.SqlCity;
import com.nomad.library.domain.SqlCountry;
import com.nomad.library.exceptions.Neo4jGenericException;
import com.nomad.library.repositories.Neo4jCommonRepository;
import com.nomad.job_orchestrator.domain.PotentialRoute;

import lombok.extern.log4j.Log4j2;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.neo4j.core.mapping.Neo4jMappingContext;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@Log4j2
public class Neo4jRepository extends Neo4jCommonRepository {

    public Neo4jRepository(Neo4jClient neo4jClient, Neo4jMappingContext schema) {
        super(neo4jClient, schema);
    }

    public List<PotentialRoute> routeDiscoveryGivenCountry(String countryName) {
        Collection<PotentialRoute> cities = neo4jClient
                .query("""
                    MATCH (source:City)-[:OF_COUNTRY]->(country:Country {name: $countryName})
                    MATCH (dest:City)-[:OF_COUNTRY]->(country)
                    WHERE source <> dest
                    RETURN source, dest, country
                """)
                .bind(countryName).to("countryName")
                .fetchAs(PotentialRoute.class)
                .mappedBy((typeSystem, record) -> {
                    Neo4jCity sourceCity = cityMapper.apply(typeSystem, record.get("source").asNode());
                    Neo4jCity destinationCity = cityMapper.apply(typeSystem, record.get("dest").asNode());
                    Neo4jCountry country = countryMapper.apply(typeSystem, record.get("country").asNode());
                    return new PotentialRoute(sourceCity.withCountry(country), destinationCity.withCountry(country));
                })
                .all();
        return cities.stream().toList();
    }

    public void saveCityDTOWithDepth0(Map<String, Object> cityAsMap) throws Neo4jGenericException {
        try {
            neo4jClient.query("""
                MERGE (c:City {id: $id})
              
                WITH c
                UNWIND $route AS routeData
                
                MERGE (t:City {id: routeData.targetCity.id})
                
                WITH c, t, routeData   
                OPTIONAL MATCH (c)-[r:ROUTE {
                       transportType: routeData.transportType
                }]->(t)
                WHERE r.popularity <> routeData.popularity OR r.time <> routeData.time OR r.cost <> routeData.cost
                DELETE r
                
                MERGE (c)-[rel:ROUTE {
                    popularity: routeData.popularity,
                    time: routeData.time,
                    cost: routeData.cost,
                    transportType: routeData.transportType
                }]->(t)
                ON CREATE SET rel.id = randomUUID()
            """)
                    .bindAll(cityAsMap)
                    .run();
        } catch (Exception e) {
            log.error("Error when trying to saveCityDTOWithDepth0. City: {}. Error: {}", cityAsMap.get("name"), e.getMessage());
            throw new Neo4jGenericException("Error in saveCityDTOWithDepth0. Error: " + e.getMessage());
        }
    }
}
