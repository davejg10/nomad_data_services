package com.nomad.admin_api.repositories;

import org.springframework.data.neo4j.core.mapping.Neo4jMappingContext;
import org.springframework.stereotype.Component;

import com.nomad.data_library.repositories.Neo4jCommonCountryMappers;

@Component
public class Neo4jCountryMappers extends Neo4jCommonCountryMappers {
    
    public Neo4jCountryMappers(Neo4jMappingContext schema) {
        super(schema);
    }
}
