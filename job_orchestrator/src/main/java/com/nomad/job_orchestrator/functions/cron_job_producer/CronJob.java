package com.nomad.job_orchestrator.functions.cron_job_producer;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nomad.library.messages.ScraperJobType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.nomad.job_orchestrator.exceptions.InvalidJobConfig;

import lombok.extern.log4j.Log4j2;

@JsonIgnoreProperties(ignoreUnknown = true)
record CronJobs(List<CronJob> jobs) {}

@Log4j2
record CronJob(String id, ScraperJobType type, String countryName, boolean isActive, String searchDate) {

    @JsonCreator
    public static CronJob create(
        @JsonProperty("id") String id,
        @JsonProperty("type") ScraperJobType type,
        @JsonProperty("countryName") String countryName,
        @JsonProperty("isActive") boolean isActive,
        @JsonProperty("timeToFetch") String timeToFetch // This will be +2D, +8H but we want yyyy/dd/mm.
    ) {

        timeToFetch = timeToFetch.trim().toLowerCase();
        int timeToFetchIntValue = Integer.parseInt(timeToFetch.replaceAll("[^0-9]", ""));
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        String searchDate;
        try {
            if (timeToFetch.contains("d")) {
                LocalDateTime dateToFetch = now.plusDays(timeToFetchIntValue);
                searchDate = dateToFetch.format(dateFormatter);
            } else if (timeToFetch.contains("h")) {
                LocalDateTime dateToFetch = now.plusHours(timeToFetchIntValue);
                searchDate = dateToFetch.format(dateFormatter);
            } else {
                throw new InvalidJobConfig("timeToFetch field didnt contain a H or a D, setting timeToFetch to 2days from now.");
            }
        } catch (Exception e) {
            log.error("Exception when trying to convert jobs-config.yml timeToFetch to searchDate. Failling back to default +2 days.  Exception: {}", e);
            LocalDateTime dateToFetch = now.plusDays(timeToFetchIntValue);
            searchDate = dateToFetch.format(dateFormatter);
        }

        return new CronJob(id, type, countryName, isActive, searchDate);
    }

   
}
