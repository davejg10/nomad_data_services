package com.nomad.library.repositories;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

import org.neo4j.driver.types.MapAccessor;
import org.neo4j.driver.types.TypeSystem;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.neo4j.core.mapping.Neo4jMappingContext;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomad.library.domain.neo4j.Neo4jCity;
import com.nomad.library.domain.neo4j.Neo4jCountry;
import com.nomad.library.domain.sql.SqlCountry;
import com.nomad.library.exceptions.Neo4jGenericException;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Configuration
public abstract class Neo4jCommonCountryRepository {

    protected Neo4jClient neo4jClient;
    protected final ObjectMapper objectMapper;
    protected final BiFunction<TypeSystem, MapAccessor, Neo4jCity> cityMapper;
    protected final BiFunction<TypeSystem, MapAccessor, Neo4jCountry> countryMapper;

    public Neo4jCommonCountryRepository(Neo4jClient neo4jClient, ObjectMapper objectMapper, Neo4jMappingContext schema) {
        this.neo4jClient = neo4jClient;
        this.objectMapper = objectMapper;
        this.cityMapper = schema.getRequiredMappingFunctionFor(Neo4jCity.class);
        this.countryMapper = schema.getRequiredMappingFunctionFor(Neo4jCountry.class);
    }

    public Optional<Neo4jCountry> findById(String countryId) {
        return findById(countryId, false);
    }

    public Optional<Neo4jCountry> findByIdFetchCities(String countryId) {
        return findById(countryId, true);
    }

    private Optional<Neo4jCountry> findById(String countryId, boolean returnAllCities) {
        Optional<Neo4jCountry> country = neo4jClient
                .query("""
                    MATCH (country:Country {id: $countryId})
                    OPTIONAL MATCH (country) -[hasCity:HAS_CITY]-> (cities:City)
                    RETURN country, collect(hasCity) as hasCity, collect(cities) as cities
                """)
                .bind(countryId).to("countryId")
                .fetchAs(Neo4jCountry.class)
                .mappedBy((typeSystem, record) -> {
                    Neo4jCountry fetchedCountry = countryMapper.apply(typeSystem, record.get("country").asNode());

                    Set<Neo4jCity> cities = new HashSet<>();
                    if (!record.get("hasCity").asList().isEmpty() && returnAllCities) {

                        record.get("cities").asList(city -> {
                            Neo4jCity createdTargetCity = cityMapper.apply(typeSystem, city.asNode());
                            createdTargetCity = new Neo4jCity(createdTargetCity.getId(), createdTargetCity.getName(), createdTargetCity.getCityMetrics(), createdTargetCity.getRoutes(), fetchedCountry);
                            cities.add(createdTargetCity);
                            return null;
                        });
                    }

                    return new Neo4jCountry(fetchedCountry.getId(), fetchedCountry.getName(), fetchedCountry.getDescription(), cities);
                })
                .first();
        return country;
    }

    public Set<Neo4jCountry> findAllCountries() {
        Collection<Neo4jCountry> allCountries = neo4jClient
                .query("""
                    MATCH(country:Country) RETURN country
                """)
                .fetchAs(Neo4jCountry.class)
                .mappedBy((typeSystem, record) -> {
                    Neo4jCountry fetchedCountry = countryMapper.apply(typeSystem, record.get("country").asNode());

                    return new Neo4jCountry(fetchedCountry.getId(), fetchedCountry.getName(), fetchedCountry.getDescription(), Set.of());
                })
                .all();
        return new HashSet<>(allCountries);
    }

    public Neo4jCountry createCountry(Neo4jCountry country) throws Neo4jGenericException {
        try {
            Neo4jCountry neo4jCountry = neo4jClient
            .query("""
                MERGE (country:Country {id: $id})
                ON CREATE SET country.name = $name
                RETURN country
            """)
            .bind(country.getId().toString()).to("id")
            .bind(country.getName()).to("name")
            .fetchAs(Neo4jCountry.class)
            .mappedBy((typeSystem, record) -> {
                return countryMapper.apply(typeSystem, record.get("country").asNode());
            })
            .first()
            .get();
            return neo4jCountry;
        } catch (Exception e) {
            log.info("Exception when trying to create Country; {}", e );
            throw new Neo4jGenericException("Issue when trying to createCountry: " + e.getMessage());
        }
    }

}