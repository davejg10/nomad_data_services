package com.nomad.job_orchestrator.functions.cron_job_producer;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.nomad.library.messages.ScraperJob;
import com.nomad.library.messages.ScraperJobType;
import com.nomad.job_orchestrator.CityRepository;
import com.nomad.job_orchestrator.domain.PotentialRoute;

import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@Log4j2
public class CronJobProcessor {

    private final CityRepository cityRepository;

    public CronJobProcessor(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    public List<ScraperJob> generateScraperJobs() throws StreamReadException, DatabindException, IOException  {
        List<ScraperJob> scraperJobs = new java.util.ArrayList<>(List.of());

        
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            Resource resource = new ClassPathResource("jobs-config.yml");
            CronJobs allCronJobs = mapper.readValue(resource.getInputStream(), CronJobs.class);

            for(CronJob cronJob : allCronJobs.jobs()) {
                if (!cronJob.isActive()) continue;

                switch(cronJob.type()) {
                    case ROUTE_DISCOVERY:
                        log.info("Job is of type ROUTE_DISCOVERY. Calling cityRepository.routeDiscovery()");
                        List<PotentialRoute> potentialRoutes = cityRepository.routeDiscoveryGivenCountry(cronJob.countryName());

                        for (PotentialRoute route : potentialRoutes) {
                            scraperJobs.add(new ScraperJob("cronTrigger-" + cronJob.id(), ScraperJobType.ROUTE_DISCOVERY, route.sourceCity(), route.destinationCity(), cronJob.searchDate()));
                        }
                }
            }
        
        return scraperJobs;
    }
}
