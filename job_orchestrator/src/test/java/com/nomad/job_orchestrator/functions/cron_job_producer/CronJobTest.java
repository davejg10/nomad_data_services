package com.nomad.job_orchestrator.functions.cron_job_producer;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.nomad.scraping_library.domain.ScraperRequestType;

public class CronJobTest {

    @Test
    void createFactoryConstructor_shouldAddCorrectNumberOfDays_toSearchDate() {

        CronJob cronJob = CronJob.create("someid", ScraperRequestType.ROUTE_DISCOVERY, "0 */1 * * * *", "CountryA", false, "+2d");
        LocalDate dateIn2Days = LocalDate.now().plusDays(2);
        assertThat(cronJob.searchDate()).isEqualTo(dateIn2Days);
    }

    @Test
    void createFactoryConstructor_shouldAddCorrectNumberOfHours_toSearchDate() {

        CronJob cronJob = CronJob.create("someid", ScraperRequestType.ROUTE_DISCOVERY, "0 */1 * * * *", "CountryA", false, "+2h");
        LocalDateTime dateIn2Hours = LocalDateTime.now().plusHours(2);
        assertThat(cronJob.searchDate()).isEqualTo(dateIn2Hours.toLocalDate());
    }

    
}
