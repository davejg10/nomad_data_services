package com.nomad.job_orchestrator.config;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceBusConfig {

    @Value("${sb_pre_processed_queue_name}")
    private String QUEUE_NAME;
    @Value("${nomadservicebus__fullyQualifiedNamespace}")
    private String FQDN_NAMESPACE;

    @Bean
    public ServiceBusSenderClient client() {
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();

        ServiceBusSenderClient sender = new ServiceBusClientBuilder()
                .credential(FQDN_NAMESPACE, credential)
                .sender()
                .queueName(QUEUE_NAME)
                .buildClient();
        return sender;
    }
}
