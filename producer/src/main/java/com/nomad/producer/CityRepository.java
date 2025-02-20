package com.nomad.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomad.producer.nomad.City;
import com.nomad.producer.nomad.Country;
import com.nomad.producer.nomad.PotentialRoute;
import lombok.extern.log4j.Log4j2;
import org.neo4j.driver.types.MapAccessor;
import org.neo4j.driver.types.TypeSystem;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.neo4j.core.mapping.Neo4jMappingContext;

import java.util.*;
import java.util.function.BiFunction;

@Configuration
@Log4j2
public class CityRepository {
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private Neo4jClient neo4jClient;
    private final BiFunction<TypeSystem, MapAccessor, City> cityMapper;
    private final BiFunction<TypeSystem, MapAccessor, Country> countryMapper;

    public CityRepository(Neo4jClient neo4jClient, Neo4jMappingContext schema) {
        this.neo4jClient = neo4jClient;
        this.cityMapper = schema.getRequiredMappingFunctionFor(City.class);
        this.countryMapper = schema.getRequiredMappingFunctionFor(Country.class);
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
                    City sourceCity = cityMapper.apply(typeSystem, record.get("source").asNode());
                    City destinationCity = cityMapper.apply(typeSystem, record.get("dest").asNode());
                    Country country = countryMapper.apply(typeSystem, record.get("country").asNode());
                    return new PotentialRoute(sourceCity.withCountry(country), destinationCity.withCountry(country));
                })
                .all();
        return cities.stream().toList();
    }

    // public void saveCityDTOWithDepth0(CityDTO city) {

    //     Map<String, Object> cityAsMap = objectMapper.convertValue(city, Map.class);

    //     neo4jClient.query("""
    //         MERGE (c:City {id: $id})
          
    //         WITH c
    //         UNWIND $routes AS routeData
            
    //         MERGE (t:City {id: routeData.targetCity.id})
            
    //         WITH c, t, routeData   
    //         OPTIONAL MATCH (c)-[r:ROUTE {
    //                transportType: routeData.transportType
    //         }]->(t)
    //         WHERE r.popularity <> routeData.popularity OR r.time <> routeData.time OR r.cost <> routeData.cost
    //         DELETE r
            
    //         MERGE (c)-[rel:ROUTE {
    //             popularity: routeData.popularity,
    //             time: routeData.time,
    //             cost: routeData.cost,
    //             transportType: routeData.transportType
    //         }]->(t)
    //         ON CREATE SET rel.id = randomUUID()
    //     """)
    //             .bindAll(cityAsMap)
    //             .run();

    // }
}
