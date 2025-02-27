package com.nomad.job_orchestrator.domain;

import com.nomad.library.domain.City;

public record HttpRouteRequest(City sourceCity, City destinationCity, String searchDate) {}
