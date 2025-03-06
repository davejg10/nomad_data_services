package com.nomad.library.connectors;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.messaging.servicebus.ServiceBusException;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusMessageBatch;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomad.library.messages.ScraperMessage;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class ServiceBusBatchSender<T extends ScraperMessage> {

    private final ServiceBusSenderClient sender;
    private final ObjectMapper objectMapper;

    public ServiceBusBatchSender(ServiceBusSenderClient sender, ObjectMapper objectMapper) {
        this.sender = sender;
        this.objectMapper = objectMapper;
    }

    public ServiceBusSenderClient getSenderClient() {
        return sender;
    }

    public void sendBatch(List<T> scraperMessages) {
        
        // Creating a batch without options set.
        ServiceBusMessageBatch batch = sender.createMessageBatch();
        List<T> failedJobs = new ArrayList<>();

        for (T scraperMessage : scraperMessages) {
            try {
                log.info("Preparing message: {} for route {} -> {}", scraperMessage.getScraperRequestSource(), scraperMessage.getSourceCity().name(), scraperMessage.getTargetCity().name());
                ServiceBusMessage message = new ServiceBusMessage(objectMapper.writeValueAsString(scraperMessage));

                if (batch.tryAddMessage(message)) {
                    continue;
                }
    
                // The batch is full. Send the current batch and create a new one.
                sendBatchMessagesWithRetry(batch);
                
                log.info("Batch sent with {} messages", batch.getCount());

                batch = sender.createMessageBatch();
    
                // Add the message we couldn't before.
                if (!batch.tryAddMessage(message)) {
                    log.error("Message for scraperMessage {} is too large for an empty batch", scraperMessage.getScraperRequestSource());
                    failedJobs.add(scraperMessage);
                }

                
            } catch (JsonProcessingException  e) {
                log.error("Failed to serialize scraperMessage {}: {}", scraperMessage.getScraperRequestSource(), e.getMessage());
                failedJobs.add(scraperMessage);
            }
        }

        // Send the final batch if there are any messages in it.
        if (batch.getCount() > 0) {
            sendBatchMessagesWithRetry(batch);
        }

        if (!failedJobs.isEmpty()) {
            log.warn("{} jobs failed to be queued", failedJobs.size());
        }
       
    }

    private void sendBatchMessagesWithRetry(ServiceBusMessageBatch batch) {
        int totalAttempts = 3;
        int attempts = 0;
        boolean succeeded = false;

        while(!succeeded && attempts < totalAttempts) {

            try {
                if (attempts > 0) {
                    long backoffTime = 1000 * (long)Math.pow(2, attempts - 1);
                    log.info("Retry attempt {} for batch, backing off for {} ms", attempts, backoffTime);
                    TimeUnit.MILLISECONDS.sleep(backoffTime);
                }

                sender.sendMessages(batch);
                succeeded = true;

            } catch (AmqpException | ServiceBusException e) {
                attempts ++;
                log.warn("Transient error on attempt {}, when sending batch messages, Exception: {}", attempts, e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Batch sending was interupted during backoff {}", e);
                throw new RuntimeException(e.getMessage());
            } catch (Exception e) {
                log.error("Non transient error on attempt {}, aborting.", attempts);
                throw e;
            }
        }
    }
    
}
