package com.nomad.admin_api;

import java.util.Map;
import java.util.Set;

import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.neo4j.core.mapping.Neo4jMappingContext;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomad.library.domain.neo4j.Neo4jCity;
import com.nomad.library.domain.neo4j.Neo4jCountry;
import com.nomad.library.domain.sql.SqlCity;
import com.nomad.library.exceptions.Neo4jGenericException;
import com.nomad.library.repositories.Neo4jCommonCityRepository;

import lombok.extern.log4j.Log4j2;

@Repository
@Log4j2
public class Neo4jCityRepository extends Neo4jCommonCityRepository {

    public Neo4jCityRepository(Neo4jClient neo4jClient, ObjectMapper objectMapper, Neo4jMappingContext schema) {
        super(neo4jClient, objectMapper, schema);
    }

    public Neo4jCity syncCity(SqlCity city) throws Neo4jGenericException {
        Neo4jCity neo4jCity = new Neo4jCity(city.getId().toString(), city.getName(), city.getCityMetrics(), Set.of(), new Neo4jCountry(city.getCountryId().toString(), "", "", Set.of()));
        return super.createCity(neo4jCity);
    }
}
