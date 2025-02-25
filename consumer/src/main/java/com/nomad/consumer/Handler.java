package com.nomad.consumer;

import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.azure.core.util.IterableStream;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomad.consumer.messages.DataCollectionJob;
import com.nomad.consumer.nomad.CityDTO;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class Handler implements CommandLineRunner {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final ServiceBusSenderClient sender;
    private final ServiceBusReceiverClient receiver;
    private Function<DataCollectionJob, List<CityDTO>> processor;

    @Autowired
    private ApplicationContext applicationContext;
    
    public Handler(ServiceBusSenderClient sender, ServiceBusReceiverClient receiver, Function<DataCollectionJob, List<CityDTO>> processor) {
        this.sender = sender;
        this.receiver = receiver;
        this.processor = processor;
    }

    @Override
    public void run(String... args) throws JsonMappingException, JsonProcessingException {

        try {
            Duration timeout = Duration.ofSeconds(30);
            long endTime = System.currentTimeMillis() + timeout.toMillis();
            
            while (System.currentTimeMillis() < endTime) {

                IterableStream<ServiceBusReceivedMessage> messages = 
                receiver.receiveMessages(1, Duration.ofSeconds(5));
            
                Iterator<ServiceBusReceivedMessage> iterator = messages.iterator();

                if (iterator.hasNext()) {
                    ServiceBusReceivedMessage message = iterator.next();
                    log.info("Started processing message: {}", message.getMessageId());

                    try {
                        DataCollectionJob job = message.getBody().toObject(DataCollectionJob.class);
                        log.info("Started scraping with job: {}", message);
        
                        List<CityDTO> allRoutes = processor.apply(job);
        
                        for(CityDTO route : allRoutes) {
                            ServiceBusMessage messageOut = new ServiceBusMessage(OBJECT_MAPPER.writeValueAsString(route));
                            sender.sendMessage(messageOut);
                        }
    
                        receiver.complete(message);
                        
                        // Reset timeout
                        endTime = System.currentTimeMillis() + timeout.toMillis();
    
                    } catch (Exception e) {
                        log.info("Abandoning the following message with messageId {}, reason: {}", message.getMessageId(), e.getMessage());
                        receiver.abandon(message);
                    }
                }
            }
            log.info("Timeout exeeceded, no messages in 5 mins");
            
        } finally {
            log.info("Closing client connections...");
            receiver.close();
            sender.close();

            log.info("Shutting down application...");
            SpringApplication.exit(applicationContext, () -> 0);
            System.exit(0);
        }
    }

}