package com.nomad.data_library.repositories;

import lombok.extern.log4j.Log4j2;

import org.neo4j.driver.Record;
import org.neo4j.driver.types.TypeSystem;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.core.Neo4jClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomad.data_library.domain.neo4j.Neo4jCity;
import com.nomad.data_library.domain.neo4j.Neo4jRoute;
import com.nomad.data_library.exceptions.Neo4jGenericException;

import java.util.*;
import java.util.function.BiFunction;

@Log4j2
@Configuration
public class Neo4jCommonCityRepository {

    protected Neo4jClient neo4jClient;
    protected final ObjectMapper objectMapper;
    protected final BiFunction<TypeSystem, Record, Neo4jCity> cityWithAllRelationshipsMapper;
    protected final BiFunction<TypeSystem, Record, Neo4jCity> cityWithNoRoutesMapper;

    public Neo4jCommonCityRepository(Neo4jClient neo4jClient, ObjectMapper objectMapper, Neo4jCommonCityMappers neo4jCommonCityMappers) {
        this.neo4jClient = neo4jClient;
        this.objectMapper = objectMapper;
        this.cityWithAllRelationshipsMapper = neo4jCommonCityMappers.cityWithAllRelationshipsMapper();
        this.cityWithNoRoutesMapper = neo4jCommonCityMappers.cityWithNoRoutesMapper();
    }

    public Optional<Neo4jCity> findById(String id) {
        return findById(id, false);
    }

    public Optional<Neo4jCity> findByIdFetchRoutes(String id) {
        return findById(id, true);
    }

    public static String QUERY_SAVE_ROUTE = """
                MATCH (c:City {id: $id})            
                MATCH (t:City {id: $targetId})
                     
                MERGE (c)-[rel:ROUTE {
                    transportType: $transportType
                }]->(t)
                SET rel.id = $routeId,
                    rel.popularity = $popularity,
                    rel.averageDuration = $averageDuration,
                    rel.averageCost = $averageCost
            """;

    private static String QUERY_RETURN_ALL_RELATIONSHIPS = """
            OPTIONAL MATCH (city)-[:OF_COUNTRY]->(country)

            WITH city, country
            OPTIONAL MATCH (city)-[:HAS_METRIC]->(m:Metric)
            WITH city, country, collect(m) as cityMetrics
           
            OPTIONAL MATCH (city)-[route:ROUTE]->(t)
           
            OPTIONAL MATCH (t) -[targetCityCountryRel:OF_COUNTRY]-> (targetCityCountry:Country)
           
            WITH city, country, cityMetrics, route, t, targetCityCountryRel, targetCityCountry
            OPTIONAL MATCH (t)-[targetCityMetricRel:HAS_METRIC]->(targetCityMetric:Metric)
           
            WITH city, country, cityMetrics, route, t, targetCityCountryRel, targetCityCountry,
                 collect(targetCityMetricRel) as targetCityMetricRels,
                 collect(targetCityMetric) as targetCityMetrics
                
            RETURN city, country, cityMetrics,
                   collect(route) as routes,
                   collect(t) as targetCities,
                   collect(targetCityCountryRel) as targetCityCountryRels,
                   collect(targetCityCountry) as targetCityCountries,
                   collect(targetCityMetricRels) as targetCityMetricRels,
                   collect(targetCityMetrics) as targetCityMetrics
            """;

    public Optional<Neo4jCity> findById(String id, boolean includeRoutes) {
        Optional<Neo4jCity> city = neo4jClient
                .query("MATCH (city:City {id: $id})" + QUERY_RETURN_ALL_RELATIONSHIPS)
                .bind(id).to("id")
                .fetchAs(Neo4jCity.class)
                .mappedBy((typeSystem, record) -> {
                    if (includeRoutes) {
                        return cityWithAllRelationshipsMapper.apply(typeSystem, record);
                    }
                    return cityWithNoRoutesMapper.apply(typeSystem, record);

                })
                .first();
        return city;
    }

    public Set<Neo4jCity> findAllCities() {
        Collection<Neo4jCity> allCities = neo4jClient
                .query(" MATCH (city:City)" + QUERY_RETURN_ALL_RELATIONSHIPS)
                .fetchAs(Neo4jCity.class)
                .mappedBy(cityWithAllRelationshipsMapper)
                .all();
        return new HashSet<>(allCities);
    }

    public Optional<Neo4jCity> findByName(String name) {
        Optional<Neo4jCity> city = neo4jClient
                .query("MATCH (city:City {name: $name})" + QUERY_RETURN_ALL_RELATIONSHIPS)
                .bind(name).to("name")
                .fetchAs(Neo4jCity.class)
                .mappedBy(cityWithAllRelationshipsMapper)
                .first();
        return city;
    }

    public Neo4jCity createCity(Neo4jCity city) throws Neo4jGenericException {
        Map<String, Object> cityAsMap = mapifyCity(city);

        try {
            Neo4jCity neo4jCity = neo4jClient
            .query("""
                MERGE (city:City {id: $id})
                ON CREATE SET city.name = $name
                SET city.shortDescription = $shortDescription,
                    city.primaryBlobUrl = $primaryBlobUrl,
                    city.coordinate = point($coordinate)

                WITH city
                MATCH(country:Country {id: $countryId})
                MERGE (country)-[fromCountry:HAS_CITY]->(city)
                ON CREATE SET fromCountry.id = randomUUID()
                MERGE (city)-[ofCountry:OF_COUNTRY]->(country)
                ON CREATE SET ofCountry.id = randomUUID()
                
                WITH city, ofCountry, country
                UNWIND $cityMetrics AS cityMetric
                MERGE (city)-[:HAS_METRIC]->(m:Metric {criteria: cityMetric.criteria})
                SET m.metric = cityMetric.metric
                    
                RETURN city, ofCountry, country, collect(m) as cityMetrics
            """)
            .bind(city.getCountry().getId()).to("countryId")
            .bindAll(cityAsMap)
            .fetchAs(Neo4jCity.class)
            .mappedBy(cityWithNoRoutesMapper)
            .first()
            .get();
            return neo4jCity;
        } catch (Exception e) {
            log.info("Exception when trying to create City; {}", e.getMessage(), e);
            throw new Neo4jGenericException("Issue when trying to createCity.", e);
        }
    }

    public Neo4jCity saveRoute(Neo4jCity city) {
        for (Neo4jRoute route : city.getRoutes()) {
            try {
                neo4jClient.query(QUERY_SAVE_ROUTE)
                .bind(route.getId()).to("routeId")
                .bind(city.getId()).to("id")
                .bind(route.getTargetCity().getId()).to("targetId")
                .bind(route.getPopularity()).to("popularity")
                .bind(route.getAverageDuration().toString()).to("averageDuration")
                .bind(route.getAverageCost().toString()).to("averageCost")
                .bind(route.getTransportType().toString()).to("transportType")
                .run();
            } catch (Exception e) {
                log.error("Unexpected exception when trying to save route to Neo4j. Exception: {}", e.getMessage());
                throw new Neo4jGenericException("Unexpected exception when trying to save route to Neo4j.", e);
            }
        }
        return findByName(city.getName()).get();
    }

    public Map<String, Object> mapifyCity(Neo4jCity city) {
        Map<String, Object> cityAsMap = objectMapper.convertValue(city, Map.class);
        return cityAsMap;
    }

}
