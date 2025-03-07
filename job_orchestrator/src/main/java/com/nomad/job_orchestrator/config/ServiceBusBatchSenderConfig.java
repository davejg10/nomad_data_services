package com.nomad.job_orchestrator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomad.library.connectors.ServiceBusBatchSender;
import com.nomad.library.messages.ScraperRequest;

@Configuration
@Profile("!maven")
public class ServiceBusBatchSenderConfig {
    
    @Bean 
    public ServiceBusBatchSender<ScraperRequest> serviceBusBatchSender(ServiceBusSenderClient sender, ObjectMapper objectMapper) {
        return new ServiceBusBatchSender<ScraperRequest>(sender, objectMapper);
    }
}
