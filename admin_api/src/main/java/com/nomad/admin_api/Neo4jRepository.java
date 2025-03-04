package com.nomad.admin_api;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;

import org.neo4j.driver.types.MapAccessor;
import org.neo4j.driver.types.TypeSystem;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.neo4j.core.mapping.Neo4jMappingContext;
import org.springframework.stereotype.Repository;

import com.nomad.library.domain.Neo4jCity;
import com.nomad.library.domain.Neo4jCountry;
import com.nomad.library.domain.SqlCity;
import com.nomad.library.domain.SqlCountry;
import com.nomad.library.exceptions.Neo4jGenericException;

import lombok.extern.log4j.Log4j2;

@Repository
@Log4j2
public class Neo4jRepository {

    private Neo4jClient neo4jClient;
    private final BiFunction<TypeSystem, MapAccessor, Neo4jCity> cityMapper;
    private final BiFunction<TypeSystem, MapAccessor, Neo4jCountry> countryMapper;

    public Neo4jRepository(Neo4jClient neo4jClient, Neo4jMappingContext schema) {
        this.neo4jClient = neo4jClient;
        this.cityMapper = schema.getRequiredMappingFunctionFor(Neo4jCity.class);
        this.countryMapper = schema.getRequiredMappingFunctionFor(Neo4jCountry.class);
    }

    public Neo4jCountry syncCountry(SqlCountry country) throws Neo4jGenericException {
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
            log.info("Exception when trying to sync country; {}", e );
            throw new Neo4jGenericException("Issue when trying to syncCountry: " + e.getMessage());
        }
    }

    public Neo4jCity syncCity(SqlCity city) throws Neo4jGenericException {
        try {
            Neo4jCity neo4jCity = neo4jClient
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
            .fetchAs(Neo4jCity.class)
            .mappedBy((typeSystem, record) -> {
                return cityMapper.apply(typeSystem, record.get("c").asNode());
            })
            .first()
            .get();
            return neo4jCity;
        } catch (Exception e) {
            log.info("Exception when trying to sync city; {}", e.getMessage());
            throw new Neo4jGenericException("Issue when trying to syncCity: " + e.getMessage());
        }
    }

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
