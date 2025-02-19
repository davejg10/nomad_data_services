package com.nomad.producer;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ProducerApplication {

	public static void main(String[] args) {

		SpringApplication.run(ProducerApplication.class, args);
	}

	private static final String QUEUE_NAME = "nomad_12goasia";
	private static final String FQDN_NAMESPACE = "sbns-dev-uks-nomad-02.servicebus.windows.net";

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
