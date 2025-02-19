package com.nomad.producer;

import com.azure.spring.messaging.servicebus.core.ServiceBusTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.microsoft.azure.functions.OutputBinding;
import com.nomad.producer.config.Job;
import com.nomad.producer.config.JobConfig;
import com.nomad.producer.messages.DataCollectionJob;
import com.nomad.producer.messages.JobType;
import com.nomad.producer.nomad.PotentialRoute;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Service
@Log4j2
public class Producer implements Consumer<OutputBinding<DataCollectionJob>> {


    @Autowired
    ConfigurableApplicationContext ctx;

    private final CityRepository cityRepository;

    public Producer(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    @Override
    public void accept(OutputBinding<DataCollectionJob> result) {
        try {
            log.info("Waiting for auth");

            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            Resource resource = new ClassPathResource("jobs-config.yml");
            JobConfig jobConfig = mapper.readValue(resource.getInputStream(), JobConfig.class);

            List<DataCollectionJob> dataCollectionJobs = new java.util.ArrayList<>(List.of());
            for(Job configJob : jobConfig.jobs()) {
                if (!configJob.isActive()) continue;

                switch(configJob.type()) {
                    case ROUTE_DISCOVERY:
                        log.info("Job is of type ROUTE_DISCOVERY. Calling cityRepository.routeDiscovery()");
                        List<PotentialRoute> potentialRoutes = cityRepository.routeDiscoveryGivenCountry(configJob.countryName());
                        log.info("Potential routes: {}", potentialRoutes);
                        for (PotentialRoute route : potentialRoutes) {
                            dataCollectionJobs.add(new DataCollectionJob(configJob.id(), configJob.type(), route.sourceCity(), route.destinationCity(), configJob.dateToCollectResults()));
                        }
                }

            }

            for (DataCollectionJob job : dataCollectionJobs) {
                log.info("Queued job: {} for route {} -> {}", job.jobId(), job.sourceCity().getName(), job.destinationCity().getName());
                result.setValue(job);
            }

            SpringApplication.exit(ctx, () -> 0);

        } catch (Exception e) {
            log.error("Error during processing", e);
            SpringApplication.exit(ctx, () -> 1);
        }
    }
}
