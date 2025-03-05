package com.nomad.library.repositories;

import lombok.extern.log4j.Log4j2;

import org.neo4j.driver.Value;
import org.neo4j.driver.types.MapAccessor;
import org.neo4j.driver.types.TypeSystem;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.neo4j.core.mapping.Neo4jMappingContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomad.library.domain.TransportType;
import com.nomad.library.domain.neo4j.Neo4jCity;
import com.nomad.library.domain.neo4j.Neo4jCountry;
import com.nomad.library.domain.neo4j.Neo4jRoute;

import java.util.*;
import java.util.function.BiFunction;

@Configuration
@Log4j2
public abstract class Neo4jCommonRepository {

    protected Neo4jClient neo4jClient;
    protected final ObjectMapper objectMapper;
    protected final BiFunction<TypeSystem, MapAccessor, Neo4jCity> cityMapper;
    protected final BiFunction<TypeSystem, MapAccessor, Neo4jCountry> countryMapper;

    public Neo4jCommonRepository(Neo4jClient neo4jClient, ObjectMapper objectMapper, Neo4jMappingContext schema) {
        this.neo4jClient = neo4jClient;
        this.objectMapper = objectMapper;
        this.cityMapper = schema.getRequiredMappingFunctionFor(Neo4jCity.class);
        this.countryMapper = schema.getRequiredMappingFunctionFor(Neo4jCountry.class);
    }

    // TODO think there will be ways of making this more efficient. Note that there are ids within node sets start = 5 for instace..
    private Map<String, Neo4jCity> mapTargetCitiesAndCountries(TypeSystem typeSystem, Value targetCitiesValue, Value targetCityCountryRelValue, Value targetCityCountryValue) {
        Map<String, Neo4jCountry> targetCountriesMap = new HashMap<>();
        Map<String, Neo4jCity> targetCitiesMap = new HashMap<>();

        targetCityCountryValue.asList(targetCountry -> {
            String nodeElementId = targetCountry.asNode().elementId();
            Neo4jCountry createdTargetCountry = countryMapper.apply(typeSystem, targetCountry.asNode());
            targetCountriesMap.put(nodeElementId, createdTargetCountry);
            return null;
        });

        targetCitiesValue.asList(targetCity -> {
            String targetCityNodeElementId = targetCity.asNode().elementId();
            String targetCountryNodeElementId = targetCityCountryRelValue
                    .asList(targetCityCountry -> targetCityCountry.asRelationship())
                    .stream()
                    .filter(targetCityCountryRel -> targetCityCountryRel.startNodeElementId().equals(targetCityNodeElementId))
                    .findFirst().get().endNodeElementId();

            Neo4jCountry targetCityCountry = targetCountriesMap.get(targetCountryNodeElementId);
            Neo4jCity createdTargetCity = cityMapper.apply(typeSystem, targetCity.asNode());
            Neo4jCity createdTargetCityWithCountry = new Neo4jCity(createdTargetCity.getId(), createdTargetCity.getName(), createdTargetCity.getCityMetrics(), createdTargetCity.getRoutes(), targetCityCountry);
            targetCitiesMap.put(targetCityNodeElementId, createdTargetCityWithCountry);
            return null;
        });

        return targetCitiesMap;
    }

    // Same `mapTargetCitiesAndCountries` except we dont map the target cities to the country as we already have that.
    private Map<String, Neo4jCity> mapTargetCities(TypeSystem typeSystem, Value targetCitiesValue, Neo4jCountry country) {
        Map<String, Neo4jCity> targetCitiesMap = new HashMap<>();

        targetCitiesValue.asList(targetCity -> {
            String targetCityNodeElementId = targetCity.asNode().elementId();
            Neo4jCity createdTargetCity = cityMapper.apply(typeSystem, targetCity.asNode());
            Neo4jCity createdTargetCityWithCountry = new Neo4jCity(createdTargetCity.getId(), createdTargetCity.getName(), createdTargetCity.getCityMetrics(), createdTargetCity.getRoutes(), country);
            targetCitiesMap.put(targetCityNodeElementId, createdTargetCityWithCountry);
            return null;
        });

        return targetCitiesMap;
    }

    private Set<Neo4jRoute> mapRoutes(TypeSystem typeSystem, Value routesValue, Map<String, Neo4jCity> targetCitiesMap) {

        Set<Neo4jRoute> routes = new HashSet<>(routesValue
                .asList(route -> {
                    String endNodeElementId = route.asRelationship().endNodeElementId().toString();
                    String elementId = route.asEntity().elementId().toString();
                    return new Neo4jRoute(
                            elementId,
                            targetCitiesMap.get(endNodeElementId),
                            route.get("popularity").asDouble(),
                            route.get("time").asDouble(),
                            route.get("cost").asDouble(),
                            TransportType.valueOf(route.get("transportType").asString())
                    );
                }));

        return routes;
    }

    // public Set<Neo4jCity> findAllCities() {
    //     Collection<Neo4jCity> allCities = neo4jClient
    //         .query("""
    //             MATCH (city:City)
    //             RETURN city
    //         """)
    //         .fetchAs(Neo4jCity.class)
    //         .mappedBy((typeSystem, record) -> {
    //             Neo4jCity fetchedCity = cityMapper.apply(typeSystem, record.get("city").asNode());
    //             return new Neo4jCity(fetchedCity.getId(), fetchedCity.getName(), null);
    //         })
    //         .all();
    //     return new HashSet<>(allCities);
    // }

    public Set<Neo4jCity> findAllCities() {
        Collection<Neo4jCity> allCities = neo4jClient
                .query("""
                    MATCH (city:City)
                    OPTIONAL MATCH (city)-[ofCountry:OF_COUNTRY]->(country)
                    OPTIONAL MATCH (city)-[route:ROUTE]->(t)
                    OPTIONAL MATCH (t) -[targetCityCountryRel:OF_COUNTRY]-> (targetCityCountry:Country)
                    RETURN city, ofCountry, country, collect(route) as routes, collect(t) as targetCity, collect(targetCityCountryRel) as targetCityCountryRel, collect(targetCityCountry) as targetCityCountry
                """)
                .fetchAs(Neo4jCity.class)
                .mappedBy((typeSystem, record) -> {
                    Neo4jCity fetchedCity = cityMapper.apply(typeSystem, record.get("city").asNode());
                    Neo4jCountry fetchedCitiesCountry = countryMapper.apply(typeSystem, record.get("country").asNode());

                    Set<Neo4jRoute> routes = Set.of();
                    if (!record.get("routes").asList().isEmpty()) {
                        Map<String, Neo4jCity> targetCitiesMap = mapTargetCitiesAndCountries(typeSystem, record.get("targetCity"), record.get("targetCityCountryRel"), record.get("targetCityCountry"));
                        routes = mapRoutes(typeSystem, record.get("routes"), targetCitiesMap);
                    }

                    return new Neo4jCity(fetchedCity.getId(), fetchedCity.getName(), fetchedCity.getCityMetrics(), routes, fetchedCitiesCountry);
                })
                .all();
        return new HashSet<>(allCities);
    }

    public Optional<Neo4jCity> findById(String id, boolean includeRoutes) {
        Optional<Neo4jCity> city = neo4jClient
                .query("""
                    MATCH (city:City {id: $id})
                    OPTIONAL MATCH (city) -[toCountry:OF_COUNTRY]-> (country:Country)
                    OPTIONAL MATCH (city) -[route:ROUTE]-> (t)
                    OPTIONAL MATCH (t) -[targetCityCountryRel:OF_COUNTRY]-> (targetCityCountry:Country)
                    RETURN city, toCountry, country, collect(route) as routes, collect(t) as targetCity, collect(targetCityCountryRel) as targetCityCountryRel, collect(targetCityCountry) as targetCityCountry
                """)
                .bind(id).to("id")
                .fetchAs(Neo4jCity.class)
                .mappedBy((typeSystem, record) -> {
                    Neo4jCity fetchedCity = cityMapper.apply(typeSystem, record.get("city").asNode());
                    Neo4jCountry fetchedCitiesCountry = countryMapper.apply(typeSystem, record.get("country").asNode());

                    Set<Neo4jRoute> routes = Set.of();
                    if (!record.get("routes").asList().isEmpty() && includeRoutes) {
                        Map<String, Neo4jCity> targetCitiesMap = mapTargetCitiesAndCountries(typeSystem, record.get("targetCity"), record.get("targetCityCountryRel"), record.get("targetCityCountry"));
                        routes = mapRoutes(typeSystem, record.get("routes"), targetCitiesMap);
                    }

                    return new Neo4jCity(fetchedCity.getId(), fetchedCity.getName(), fetchedCity.getCityMetrics(), routes, fetchedCitiesCountry);
                })
                .first();
        return city;
    }

    // public Set<Neo4jCountry> findAllCountries() {
    //     Collection<Neo4jCountry> allCountries = neo4jClient
    //             .query("""
    //                 MATCH(country:Country) RETURN country
    //             """)
    //             .fetchAs(Neo4jCountry.class)
    //             .mappedBy((typeSystem, record) -> {
    //                 Neo4jCountry fetchedCountry = countryMapper.apply(typeSystem, record.get("country").asNode());

    //                 return new Neo4jCountry(fetchedCountry.getId(), fetchedCountry.getName());
    //             })
    //             .all();
    //     return new HashSet<>(allCountries);
    // }

}
