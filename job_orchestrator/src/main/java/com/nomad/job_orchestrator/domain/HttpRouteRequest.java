package com.nomad.job_orchestrator.domain;

import com.nomad.library.domain.Neo4jCity;

public record HttpRouteRequest(Neo4jCity sourceCity, Neo4jCity destinationCity, String searchDate) {}
