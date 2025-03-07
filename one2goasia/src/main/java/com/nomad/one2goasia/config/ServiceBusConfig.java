package com.nomad.one2goasia.config;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomad.library.connectors.ServiceBusBatchSender;
import com.nomad.library.messages.ScraperResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!maven")
public class ServiceBusConfig {

    @Value("${sb_processed_queue_name}")
    private String PROCESSED_QUEUE_NAME;
    @Value("${sb_pre_processed_queue_name}")
    private String PRE_PROCESSED_QUEUE_NAME;
    @Value("${sb_namespace_fqdn}")
    private String FQDN_NAMESPACE;

    // This is the client id of the User Assigned Identity assigned to the Azure Containerapp job
    @Value("${AZURE_CLIENT_ID:null}")
    private String AZURE_CLIENT_ID;

    @Bean
    public ServiceBusSenderClient clientSender() {
        // Must pass the client id if using a User Assigned Identity
        TokenCredential credential = null;
        if (AZURE_CLIENT_ID != "null") {
            credential = new ManagedIdentityCredentialBuilder().clientId(AZURE_CLIENT_ID).build();
        } else {
            credential = new DefaultAzureCredentialBuilder().build();
        }

        ServiceBusSenderClient sender = new ServiceBusClientBuilder()
                .credential(FQDN_NAMESPACE, credential)
                .sender()
                .queueName(PROCESSED_QUEUE_NAME)
                .buildClient();
        return sender;
    }

    @Bean
    public ServiceBusReceiverClient clientReciever() {
        TokenCredential credential = null;
        if (AZURE_CLIENT_ID != "null") {
            credential = new ManagedIdentityCredentialBuilder().clientId(AZURE_CLIENT_ID).build();
        } else {
            credential = new DefaultAzureCredentialBuilder().build();
        }
        
        ServiceBusReceiverClient receiver = new ServiceBusClientBuilder()
                .credential(FQDN_NAMESPACE, credential)
                .receiver()
                .queueName(PRE_PROCESSED_QUEUE_NAME)
                .buildClient();
        return receiver;
    }

    @Bean 
    public ServiceBusBatchSender<ScraperResponse> serviceBusBatchSender(ServiceBusSenderClient sender, ObjectMapper objectMapper) {
        return new ServiceBusBatchSender<ScraperResponse>(sender, objectMapper);
    }
}
