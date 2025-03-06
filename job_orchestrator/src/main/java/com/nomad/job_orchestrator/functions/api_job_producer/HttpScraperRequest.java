package com.nomad.job_orchestrator.functions.api_job_producer;

import java.time.LocalDate;

import com.nomad.library.messages.CityDTO;

public record HttpScraperRequest(CityDTO sourceCity, CityDTO targetCity, LocalDate searchDate) {}
