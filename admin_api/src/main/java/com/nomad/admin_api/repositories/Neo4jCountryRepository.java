package com.nomad.admin_api.repositories;

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
            throw new Neo4jGenericException("Exception when trying to delete Country. Exception: {}", e);
        }
        
    }
    

}