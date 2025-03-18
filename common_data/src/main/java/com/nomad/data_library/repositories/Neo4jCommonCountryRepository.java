package com.nomad.data_library.repositories;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

import org.neo4j.driver.Record;
import org.neo4j.driver.types.TypeSystem;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.core.Neo4jClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomad.data_library.domain.neo4j.Neo4jCountry;
import com.nomad.data_library.exceptions.Neo4jGenericException;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Configuration
public class Neo4jCommonCountryRepository {

    protected Neo4jClient neo4jClient;
    protected final ObjectMapper objectMapper;
    protected final BiFunction<TypeSystem, Record, Neo4jCountry> countryWithCitiesMapper;
    protected final BiFunction<TypeSystem, Record, Neo4jCountry> countryNoCitiesMapper;

    public Neo4jCommonCountryRepository(Neo4jClient neo4jClient, ObjectMapper objectMapper, Neo4jCountryMappers neo4jCountryMappers) {
        this.neo4jClient = neo4jClient;
        this.objectMapper = objectMapper;
        this.countryWithCitiesMapper = neo4jCountryMappers.countryWithCitiesMapper();
        this.countryNoCitiesMapper = neo4jCountryMappers.countryNoCitiesMapper();
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
                    OPTIONAL MATCH (country) -[:HAS_CITY]-> (cities:City)
                    RETURN country, collect(cities) as cities
                """)
                .bind(countryId).to("countryId")
                .fetchAs(Neo4jCountry.class)
                .mappedBy((typeSystem, record) -> {

                    if (returnAllCities) {
                        return countryWithCitiesMapper.apply(typeSystem, record);
                    }
                    return countryNoCitiesMapper.apply(typeSystem, record);

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
                .mappedBy(countryNoCitiesMapper)
                .all();
        return new HashSet<>(allCountries);
    }

    public Neo4jCountry createCountry(Neo4jCountry country) throws Neo4jGenericException {
        Map<String, Object> countryAsMap = mapifyCountry(country);
        try {
            Neo4jCountry neo4jCountry = neo4jClient
            .query("""
                MERGE (country:Country {id: $id})
                ON CREATE SET country.name = $name
                SET country.shortDescription = $shortDescription,
                    country.primaryBlobUrl = $primaryBlobUrl
                    
                RETURN country
            """)
            .bindAll(countryAsMap)
            .fetchAs(Neo4jCountry.class)
            .mappedBy(countryNoCitiesMapper)
            .first()
            .get();
            return neo4jCountry;
        } catch (Exception e) {
            log.info("Exception when trying to create Country; {}", e.getMessage(), e);
            throw new Neo4jGenericException("Issue when trying to createCountry", e);
        }
    }

    public Map<String, Object> mapifyCountry(Neo4jCountry country) {
        Map<String, Object> countryAsMap = objectMapper.convertValue(country, Map.class);
        return countryAsMap;
    }

}