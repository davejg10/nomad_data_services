package com.nomad.job_orchestrator.functions.api_job_producer;

import java.time.LocalDate;

import com.nomad.scraping_library.domain.CityDTO;

public record HttpScraperRequest(CityDTO sourceCity, CityDTO targetCity, LocalDate searchDate) {}
