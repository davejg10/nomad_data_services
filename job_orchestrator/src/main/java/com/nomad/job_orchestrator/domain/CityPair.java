package com.nomad.job_orchestrator.domain;

import com.nomad.library.messages.CityDTO;

public record CityPair(CityDTO sourceCity, CityDTO targetCity) {}
