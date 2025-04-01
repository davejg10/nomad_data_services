package com.nomad.one2goasia.config;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomad.scraping_library.connectors.ServiceBusBatchSender;
import com.nomad.scraping_library.domain.ScraperResponse;

import lombok.extern.log4j.Log4j2;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Log4j2
@Configuration
@Profile("!maven")
public class ServiceBusConfig {

    @Value("${sb_processed_queue_name}")
    private String PROCESSED_QUEUE_NAME;
    @Value("${sb_pre_processed_queue_name}")
    private String PRE_PROCESSED_QUEUE_NAME;
    @Value("${sb_namespace_fqdn}")
    private String FQDN_NAMESPACE;

    @Value("${spring.profiles.active}")
    private String ACTIVE_PROFILE;

    // This is the client id of the;
    // - User Assigned Identity if executed on Azure Container App Job
    @Value("${AZURE_CLIENT_ID:local}")
    private String AZURE_CLIENT_ID;

    @Bean
    public ServiceBusSenderClient clientSender() {
        TokenCredential credential = createTokenCredential();

        ServiceBusSenderClient sender = new ServiceBusClientBuilder()
                .credential(FQDN_NAMESPACE, credential)
                .sender()
                .queueName(PROCESSED_QUEUE_NAME)
                .buildClient();
        return sender;
    }

    @Bean
    public ServiceBusReceiverClient clientReciever() {
        TokenCredential credential = createTokenCredential();
        
        ServiceBusReceiverClient receiver = new ServiceBusClientBuilder()
                .credential(FQDN_NAMESPACE, credential)
                .receiver()
                .queueName(PRE_PROCESSED_QUEUE_NAME)
                .buildClient();
        return receiver;
    }

    private TokenCredential createTokenCredential() {
        TokenCredential credential;

        if ("cloud".equalsIgnoreCase(ACTIVE_PROFILE)) {
            log.info("Using ManagedIdentityCredentialBuilder as ACTIVE_PROFILE is cloud.");
            ManagedIdentityCredentialBuilder builder = new ManagedIdentityCredentialBuilder();
            if (AZURE_CLIENT_ID != null && !AZURE_CLIENT_ID.isBlank()) {
                 log.info("Applying specific Client ID for Managed Identity: {}", AZURE_CLIENT_ID);
                 builder.clientId(AZURE_CLIENT_ID);
            } else {
                 log.info("Using System-Assigned Managed Identity (no specific client ID provided).");
            }
            credential = builder.build();

        } else { // Default case (e.g., "local or hetzner" profile)
            log.info("Using DefaultAzureCredentialBuilder as ACTIVE_PROFILE is '{}'.", ACTIVE_PROFILE);
            credential = new DefaultAzureCredentialBuilder()
                .build();
        }
        return credential;
    }

    @Bean 
    public ServiceBusBatchSender<ScraperResponse> serviceBusBatchSender(ServiceBusSenderClient sender, ObjectMapper objectMapper) {
        return new ServiceBusBatchSender<ScraperResponse>(sender, objectMapper);
    }
}
