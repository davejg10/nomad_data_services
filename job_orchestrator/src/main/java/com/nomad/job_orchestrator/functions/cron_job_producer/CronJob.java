package com.nomad.job_orchestrator.functions.cron_job_producer;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nomad.library.messages.ScraperRequestType;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.nomad.job_orchestrator.exceptions.InvalidJobConfig;

import lombok.extern.log4j.Log4j2;

@Log4j2
@JsonIgnoreProperties(ignoreUnknown = true)
public record CronJob(String id, ScraperRequestType type, String cronSchedule, String countryName, boolean isActive, LocalDate searchDate) {

    @JsonCreator
    public static CronJob create(
        @JsonProperty("id") String id,
        @JsonProperty("type") ScraperRequestType type,
        @JsonProperty("cronSchedule") String cronSchedule,
        @JsonProperty("countryName") String countryName,
        @JsonProperty("isActive") boolean isActive,
        @JsonProperty("timeToFetch") String timeToFetch // This will be +2D, +8H but we want yyyy/dd/mm.
    ) {

        timeToFetch = timeToFetch.trim().toLowerCase();
        int timeToFetchIntValue = Integer.parseInt(timeToFetch.replaceAll("[^0-9]", ""));
        LocalDateTime now = LocalDateTime.now();
        LocalDate searchDate;
        try {
            if (timeToFetch.contains("d")) {
                LocalDateTime dateToFetch = now.plusDays(timeToFetchIntValue);
                searchDate = dateToFetch.toLocalDate();
            } else if (timeToFetch.contains("h")) {
                LocalDateTime dateToFetch = now.plusHours(timeToFetchIntValue);
                
                searchDate = dateToFetch.toLocalDate();
            } else {
                throw new InvalidJobConfig("timeToFetch field didnt contain a H or a D, setting timeToFetch to 2days from now.");
            }
        } catch (Exception e) {
            log.error("Exception when trying to convert jobs-config.yml timeToFetch to searchDate. Failling back to default +2 days.  Exception: {}", e);
            LocalDateTime dateToFetch = now.plusDays(timeToFetchIntValue);
            searchDate = dateToFetch.toLocalDate();
        }

        return new CronJob(id, type, cronSchedule, countryName, isActive, searchDate);
    }

   
}
