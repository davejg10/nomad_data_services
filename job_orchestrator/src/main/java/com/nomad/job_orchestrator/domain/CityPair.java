package com.nomad.job_orchestrator.domain;

import com.nomad.scraping_library.domain.CityDTO;

public record CityPair(CityDTO sourceCity, CityDTO targetCity) {}
