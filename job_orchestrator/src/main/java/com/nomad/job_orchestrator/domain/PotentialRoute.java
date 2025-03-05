package com.nomad.job_orchestrator.domain;

import com.nomad.library.domain.neo4j.Neo4jCity;

public record PotentialRoute(Neo4jCity sourceCity, Neo4jCity destinationCity) {}
