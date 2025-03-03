package com.nomad.admin_api;

import java.util.function.BiFunction;

import org.neo4j.driver.types.MapAccessor;
import org.neo4j.driver.types.TypeSystem;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.neo4j.core.mapping.Neo4jMappingContext;
import org.springframework.stereotype.Repository;

import com.nomad.admin_api.domain.SqlCity;
import com.nomad.admin_api.domain.SqlCountry;
import com.nomad.library.domain.City;
import com.nomad.library.domain.Country;

import lombok.extern.log4j.Log4j2;

@Repository
@Log4j2
public class Neo4jRepository {

    private Neo4jClient neo4jClient;
    private final BiFunction<TypeSystem, MapAccessor, City> cityMapper;
    private final BiFunction<TypeSystem, MapAccessor, Country> countryMapper;

    public Neo4jRepository(Neo4jClient neo4jClient, Neo4jMappingContext schema) {
        this.neo4jClient = neo4jClient;
        this.cityMapper = schema.getRequiredMappingFunctionFor(City.class);
        this.countryMapper = schema.getRequiredMappingFunctionFor(Country.class);
    }

    public Country syncCountry(SqlCountry country) {
        try {
            Country neo4jCountry = neo4jClient
            .query("""
                MERGE (country:Country {id: $id})
                ON CREATE SET country.name = $name
                RETURN country
            """)
            .bind(country.getId().toString()).to("id")
            .bind(country.getName()).to("name")
            .fetchAs(Country.class)
            .mappedBy((typeSystem, record) -> {
                return countryMapper.apply(typeSystem, record.get("country").asNode());
            })
            .first()
            .get();
            return neo4jCountry;
        } catch (Exception e) {
            log.info("Exception when trying to sync country; {}", e );
            throw e;
        }
    }

    public City syncCity(SqlCity city) {
        try {
            City neo4jCity = neo4jClient
            .query("""
                MERGE (c:City {id: $id})
                ON CREATE SET c.name = $name
                
                WITH c
                MATCH(country:Country {id: $countryId})
                MERGE (country)-[fromCountry:HAS_CITY]->(c)
                ON CREATE SET fromCountry.id = randomUUID()
                MERGE (c)-[toCountry:OF_COUNTRY]->(country)
                ON CREATE SET toCountry.id = randomUUID()
                RETURN c
            """)
            .bind(city.getId().toString()).to("id")
            .bind(city.getName()).to("name")
            .bind(city.getCountryId().toString()).to("countryId")
            .fetchAs(City.class)
            .mappedBy((typeSystem, record) -> {
                return cityMapper.apply(typeSystem, record.get("c").asNode());
            })
            .first()
            .get();
            return neo4jCity;
        } catch (Exception e) {
            log.info("Exception when trying to sync city; {}", e );
            throw e;
        }
    }
}
