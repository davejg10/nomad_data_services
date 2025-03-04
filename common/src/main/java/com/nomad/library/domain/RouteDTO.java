package com.nomad.library.domain;

public record RouteDTO(Neo4jCity targetCity, double popularity, double time, double cost, TransportType transportType) { }
