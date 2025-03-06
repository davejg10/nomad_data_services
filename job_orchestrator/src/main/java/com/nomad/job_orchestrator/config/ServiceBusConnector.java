package com.nomad.job_orchestrator.config;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomad.library.connectors.ServiceBusBatchSender;
import com.nomad.library.messages.ScraperRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!local")
public class ServiceBusConnector {

    @Value("${sb_pre_processed_queue_name}")
    private String QUEUE_NAME;
    @Value("${nomadservicebus__fullyQualifiedNamespace}")
    private String FQDN_NAMESPACE;

    // This is the client id of the User Assigned Identity assigned to the Azure Containerapp job
    @Value("${AZURE_CLIENT_ID}")
    private String AZURE_CLIENT_ID;

    @Bean
    public ServiceBusSenderClient client() {
        TokenCredential credential = new ManagedIdentityCredentialBuilder().clientId(AZURE_CLIENT_ID).build();

        ServiceBusSenderClient sender = new ServiceBusClientBuilder()
                .credential(FQDN_NAMESPACE, credential)
                .sender()
                .queueName(QUEUE_NAME)
                .buildClient();
        return sender;
    }

    @Bean 
    public ServiceBusBatchSender<ScraperRequest> serviceBusBatchSender(ServiceBusSenderClient sender, ObjectMapper objectMapper) {
        return new ServiceBusBatchSender<ScraperRequest>(sender, objectMapper);
    }
}
