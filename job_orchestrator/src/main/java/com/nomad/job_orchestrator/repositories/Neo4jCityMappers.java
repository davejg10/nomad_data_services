package com.nomad.job_orchestrator.repositories;

import org.springframework.data.neo4j.core.mapping.Neo4jMappingContext;
import org.springframework.stereotype.Component;

import com.nomad.data_library.repositories.Neo4jCommonCityMappers;

@Component
public class Neo4jCityMappers extends Neo4jCommonCityMappers {

    public Neo4jCityMappers(Neo4jMappingContext schema) {
        super(schema);
    }
}
