package com.nomad.job_orchestrator.domain;

import com.nomad.library.domain.City;

public record PotentialRoute(City sourceCity, City destinationCity) {}
