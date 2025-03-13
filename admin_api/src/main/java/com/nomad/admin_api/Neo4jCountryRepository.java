package com.nomad.admin_api;

import java.util.Set;

import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.neo4j.core.mapping.Neo4jMappingContext;
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

    public Neo4jCountryRepository(Neo4jClient neo4jClient, ObjectMapper objectMapper, Neo4jMappingContext schema) {
        super(neo4jClient, objectMapper, schema);
    }

    public Neo4jCountry syncCountry(SqlCountry country) throws Neo4jGenericException {
        Neo4jCountry neo4jCountry = new Neo4jCountry(country.getId().toString(), country.getName(), Set.of());
        return super.createCountry(neo4jCountry);
    }

}