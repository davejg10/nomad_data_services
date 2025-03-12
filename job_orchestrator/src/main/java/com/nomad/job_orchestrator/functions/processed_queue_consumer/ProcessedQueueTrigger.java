package com.nomad.job_orchestrator.functions.processed_queue_consumer;


import java.util.Map;
import java.util.logging.Level;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.ServiceBusQueueTrigger;
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
                        ExecutionContext context) throws JsonProcessingException {

        try {
            ScraperResponse scraperResponse = objectMapper.readValue(message, ScraperResponse.class);
            Map<String, Object> cityAsMap = objectMapper.convertValue(scraperResponse,  Map.class);

            processedQueueHandler.accept(message);

        } catch (JsonProcessingException e) {
            context.getLogger().log(Level.SEVERE, "A JsonProcessingException exception was thrown when trying to either serialize/desirialize. Message: " + message + ", Exception: " + e.getMessage(), e);
        }
    }

}
