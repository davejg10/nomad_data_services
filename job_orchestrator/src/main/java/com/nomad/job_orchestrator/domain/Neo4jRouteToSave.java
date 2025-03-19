package com.nomad.job_orchestrator.domain;

import java.math.BigDecimal;
import java.time.Duration;

import com.nomad.common_utils.domain.TransportType;

public record Neo4jRouteToSave(String routeDefinitionId, String sourceCityId, double popularity, Duration averageDuration, BigDecimal averageCost, TransportType transportType, String targetCityId) {}
