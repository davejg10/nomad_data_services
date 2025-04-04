package com.nomad.admin_api.repositories;

import java.util.Map;

import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomad.data_library.domain.neo4j.Neo4jCountry;
import com.nomad.data_library.domain.sql.SqlCountry;
import com.nomad.data_library.exceptions.Neo4jGenericException;
import com.nomad.data_library.repositories.Neo4jCommonCountryRepository;

import lombok.extern.log4j.Log4j2;

@Repository
@Log4j2
public class Neo4jCountryRepository extends Neo4jCommonCountryRepository {

    public Neo4jCountryRepository(Neo4jClient neo4jClient, ObjectMapper objectMapper, Neo4jCountryMappers neo4jCountryMappers) {
        super(neo4jClient, objectMapper, neo4jCountryMappers);
    }

    public Neo4jCountry save(Neo4jCountry country) throws Neo4jGenericException {
        return super.createCountry(country);
    }

    public void delete(SqlCountry country) throws Neo4jGenericException {
        try {
            neo4jClient
            .query("""
                MATCH (country:Country {id: $id}) DETACH DELETE country;
            """)
            .bind(country.getId().toString()).to("id")
            .run();
        } catch (Exception e) {
            throw new Neo4jGenericException("Exception when trying to delete Country.", e);
        }
    }

    public Neo4jCountry update(Neo4jCountry country) throws Neo4jGenericException {
        Map<String, Object> countryAsMap = mapifyCountry(country);

        try {
            Neo4jCountry neo4jCountry = neo4jClient
            .query("""
                MATCH (country:Country {id: $id}) 
                SET country.name = $name,
                    country.shortDescription = $shortDescription,
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
            log.info("Exception when trying to update Country; {}", e.getMessage(), e);
            throw new Neo4jGenericException("Issue when trying to updateCountry.", e);
        }
    }
    

}