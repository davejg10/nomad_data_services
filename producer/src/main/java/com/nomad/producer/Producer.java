package com.nomad.producer;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusMessageBatch;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.nomad.producer.config.Job;
import com.nomad.producer.config.JobConfig;
import com.nomad.producer.messages.DataCollectionJob;
import com.nomad.producer.nomad.PotentialRoute;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Consumer;

@Service
@Log4j2
public class Producer implements Consumer<Message<String>> {


    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final ServiceBusSenderClient sender;

    private final CityRepository cityRepository;

    public Producer(CityRepository cityRepository, ServiceBusSenderClient client) {
        this.cityRepository = cityRepository;
        this.sender = client;
    }

    @Override
    public void accept(Message<String> stringMessage) {
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

            // Creating a batch without options set.
            ServiceBusMessageBatch batch = sender.createMessageBatch();
            for (DataCollectionJob job : dataCollectionJobs) {
                log.info("Queuing job: {} for route {} -> {}", job.jobId(), job.sourceCity().getName(), job.destinationCity().getName());
                ServiceBusMessage message = new ServiceBusMessage(OBJECT_MAPPER.writeValueAsString(job));
                if (batch.tryAddMessage(message)) {
                    continue;
                }

                // The batch is full. Send the current batch and create a new one.
                sender.sendMessages(batch);

                batch = sender.createMessageBatch();

                // Add the message we couldn't before.
                if (!batch.tryAddMessage(message)) {
                    throw new IllegalArgumentException("Message is too large for an empty batch.");
                }
            }

            // Send the final batch if there are any messages in it.
            if (batch.getCount() > 0) {
                sender.sendMessages(batch);
            }


        } catch (Exception e) {
            log.error("Error during processing", e);
        }
    }
}
