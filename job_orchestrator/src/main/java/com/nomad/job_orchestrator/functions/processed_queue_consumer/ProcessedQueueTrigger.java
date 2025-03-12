package com.nomad.job_orchestrator.functions.processed_queue_consumer;


import java.util.Map;
import java.util.logging.Level;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.BindingName;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.ServiceBusQueueTrigger;
import com.nomad.scraping_library.domain.ScraperRequest;
import com.nomad.scraping_library.domain.ScraperResponse;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class ProcessedQueueTrigger {

    private final String sb_processed_queue_name = "nomad_processed";

    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private ProcessedQueueHandler processedQueueHandler;

    /*
     * This Azure Function reads messages from the Queue that the scapers post to. See payload.json for an example message.
     */
    @FunctionName("processedQueueConsumer")
    public void execute(@ServiceBusQueueTrigger(name = "msg", queueName = sb_processed_queue_name, connection = "nomadservicebus") String message,
                        @BindingName("CorrelationId") String correlationId,
                        ExecutionContext context) throws JsonProcessingException {
        try {
            ThreadContext.put("correlationId", correlationId);

            TelemetryClient telemetryClient = new TelemetryClient();
            telemetryClient.getContext().getOperation().setId(correlationId);
            telemetryClient.trackEvent("MessageProcessed", Map.of("correlationId", correlationId), null);

            ScraperResponse scraperResponse = objectMapper.readValue(message, ScraperResponse.class);
            log.info("someThing weird");

            processedQueueHandler.accept(scraperResponse);

        } catch (Exception e) {
            context.getLogger().log(Level.SEVERE, "An exception was thrown when trying to either serialize/desirialize. Message: " + message + ", Exception: " + e.getMessage(), e);
        } finally {
            ThreadContext.clearAll();
        }
    }

}
