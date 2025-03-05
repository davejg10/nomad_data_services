package com.nomad.library.domain;

import com.nomad.library.domain.neo4j.Neo4jCity;

public record RouteDTO(Neo4jCity targetCity, double popularity, double time, double cost, TransportType transportType) { }
