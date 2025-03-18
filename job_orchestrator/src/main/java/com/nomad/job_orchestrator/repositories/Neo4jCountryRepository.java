package com.nomad.job_orchestrator.repositories;

import com.nomad.data_library.repositories.Neo4jCommonCountryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.log4j.Log4j2;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Repository;

@Repository
@Log4j2
public class Neo4jCountryRepository extends Neo4jCommonCountryRepository {

    public Neo4jCountryRepository(Neo4jClient neo4jClient, ObjectMapper objectMapper, Neo4jCountryMappers neo4jCountryMappers) {
        super(neo4jClient, objectMapper, neo4jCountryMappers);
    }

}
