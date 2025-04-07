package com.nomad.job_orchestrator.functions.batch_api_job_producer;

import java.time.LocalDate;

public record BatchHttpScraperRequest(String countryName, LocalDate searchDate) {}
