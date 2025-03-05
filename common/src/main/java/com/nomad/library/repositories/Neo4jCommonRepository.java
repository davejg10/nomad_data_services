package com.nomad.library.repositories;

import com.nomad.library.domain.Neo4jCity;
import com.nomad.library.domain.Neo4jCountry;

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
public abstract class Neo4jCommonRepository {

    protected Neo4jClient neo4jClient;
    protected final BiFunction<TypeSystem, MapAccessor, Neo4jCity> cityMapper;
    protected final BiFunction<TypeSystem, MapAccessor, Neo4jCountry> countryMapper;

    public Neo4jCommonRepository(Neo4jClient neo4jClient, Neo4jMappingContext schema) {
        this.neo4jClient = neo4jClient;
        this.cityMapper = schema.getRequiredMappingFunctionFor(Neo4jCity.class);
        this.countryMapper = schema.getRequiredMappingFunctionFor(Neo4jCountry.class);
    }

    public Set<Neo4jCity> findAllCities() {
        Collection<Neo4jCity> allCities = neo4jClient
            .query("""
                MATCH (city:City)
                RETURN city
            """)
            .fetchAs(Neo4jCity.class)
            .mappedBy((typeSystem, record) -> {
                Neo4jCity fetchedCity = cityMapper.apply(typeSystem, record.get("city").asNode());
                return new Neo4jCity(fetchedCity.getId(), fetchedCity.getName(), null);
            })
            .all();
        return new HashSet<>(allCities);
    }

    public Set<Neo4jCity> findAllCitiesMapCountry() {
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

                return new Neo4jCity(fetchedCity.getId(), fetchedCity.getName(), fetchedCitiesCountry);
            })
            .all();
        return new HashSet<>(allCities);
    }

    public Set<Neo4jCountry> findAllCountries() {
        Collection<Neo4jCountry> allCountries = neo4jClient
                .query("""
                    MATCH(country:Country) RETURN country
                """)
                .fetchAs(Neo4jCountry.class)
                .mappedBy((typeSystem, record) -> {
                    Neo4jCountry fetchedCountry = countryMapper.apply(typeSystem, record.get("country").asNode());

                    return new Neo4jCountry(fetchedCountry.getId(), fetchedCountry.getName());
                })
                .all();
        return new HashSet<>(allCountries);
    }

}
