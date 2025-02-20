package com.nomad.consumer;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceBusConfig {

    @Value("${sb_processed_queue_name:nomad_processed}")
    private String PROCESSED_QUEUE_NAME;
    @Value("${sb_pre_processed_queue_name:nomad_pre_processed}")
    private String PRE_PROCESSED_QUEUE_NAME;
    @Value("${sb_namespace_fqdn:sbns-dev-uks-nomad-02.servicebus.windows.net}")
    private String FQDN_NAMESPACE;

    @Bean
    public ServiceBusSenderClient clientSender() {
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();

        ServiceBusSenderClient sender = new ServiceBusClientBuilder()
                .credential(FQDN_NAMESPACE, credential)
                .sender()
                .queueName(PROCESSED_QUEUE_NAME)
                .buildClient();
        return sender;
    }

    @Bean
    public ServiceBusReceiverClient clientReciever() {
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();

        ServiceBusReceiverClient receiver = new ServiceBusClientBuilder()
                .credential(FQDN_NAMESPACE, credential)
                .receiver()
                .queueName(PRE_PROCESSED_QUEUE_NAME)
                .buildClient();
        return receiver;
    }
}
