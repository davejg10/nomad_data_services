package com.nomad.consumer;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomad.consumer.nomad.CityDTO;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.core.Neo4jClient;

import java.util.Map;

@Configuration
@Log4j2
public class CityRepository {

    private Neo4jClient client;
    private final static ObjectMapper objectMapper = new ObjectMapper();

    public CityRepository(Neo4jClient client) {
        this.client = client;
    }

    public void saveCityDTOWithDepth0(CityDTO city) {

        Map<String, Object> cityAsMap = objectMapper.convertValue(city, Map.class);

        client.query("""
            MERGE (c:City {id: $id})
          
            WITH c
            UNWIND $routes AS routeData
            
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

    }
}