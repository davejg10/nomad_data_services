package com.nomad.job_orchestrator.functions.cron_job_producer;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CronJobs(List<CronJob> jobs) {}
