package com.nomad.job_orchestrator.functions.cron_job_producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.nomad.scraping_library.domain.ScraperRequest;
import com.nomad.scraping_library.domain.ScraperRequestType;
import com.nomad.job_orchestrator.domain.CityPair;
import com.nomad.job_orchestrator.repositories.Neo4jCityRepository;

import lombok.extern.log4j.Log4j2;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Log4j2
public class CronJobHandler {

    private Neo4jCityRepository cityRepository;

    public CronJobHandler(Neo4jCityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    public final CronJobs readCronJobs(String fileName) {

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        Resource resource = new ClassPathResource(fileName);
        CronJobs allCronJobs;
        try {
            allCronJobs = mapper.readValue(resource.getInputStream(), CronJobs.class);
        } catch (IOException e) {
            log.error("Error when trying to read " + fileName + " file. Error: {}" + e.getMessage());
            throw new RuntimeException(e);
        } 

        return allCronJobs;
    }

    public final List<CronJob> filterCronJobs(LocalDateTime now, String cronTriggerSchedule, CronJobs cronJobs) {
        List<CronJob> filteredJobs = new ArrayList<CronJob>();

        for (CronJob cronJob : cronJobs.jobs()) {
            if (!cronJob.isActive()) continue;
            
            LocalDateTime nextcronJobSchedule = CronExpression.parse(cronJob.cronSchedule()).next(now);
            LocalDateTime nextCronJobScheduleAdjusted = nextcronJobSchedule.minusSeconds(20); // handles divisibility/equality issues
            LocalDateTime nextFunctionSchedule = CronExpression.parse(cronTriggerSchedule).next(now);
            
            log.info("CronJob with id: {}, is next scheduled for: {}, nextFunctionSchedule is: {}", cronJob.id(), nextCronJobScheduleAdjusted, nextFunctionSchedule);
            if (nextCronJobScheduleAdjusted.isBefore(nextFunctionSchedule)) {
                log.info("Adding CronJob with id: {} to filtered CronJob list", cronJob.id());

                filteredJobs.add(cronJob);
            }
        }
        return filteredJobs;
    }

    public final List<ScraperRequest> createScraperRequests(CronJob filteredCronJob) {
        List<ScraperRequest> scraperRequests = new java.util.ArrayList<>(List.of());
        

        switch(filteredCronJob.type()) {
            case ROUTE_DISCOVERY:
                log.info("Job is of type ROUTE_DISCOVERY. Calling cityRepository.routeDiscovery()");
                List<CityPair> cityPairs = cityRepository.routeDiscoveryGivenCountry(filteredCronJob.countryName());

                for (CityPair cityPair : cityPairs) {
                    ScraperRequest scraperRequest = new ScraperRequest("cronTrigger-" + filteredCronJob.id(), ScraperRequestType.ROUTE_DISCOVERY, cityPair.sourceCity(), cityPair.targetCity(), filteredCronJob.searchDate());
                    log.info("Scraper request created. Request is: {}", scraperRequest);
                    scraperRequests.add(scraperRequest);
                }
        }

        log.info("{} scraperRequests genereted for CronJob {}", scraperRequests.size(), filteredCronJob.id());
        
        return scraperRequests;
    }

}
