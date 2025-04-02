package com.nomad.job_orchestrator.functions.processed_api_consumer;

import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import com.nomad.job_orchestrator.functions.api_job_producer.HttpScraperRequest;
import com.nomad.job_orchestrator.functions.processed_queue_consumer.ProcessedQueueHandler;
import com.nomad.scraping_library.domain.ScraperRequest;
import com.nomad.scraping_library.domain.ScraperResponse;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class ProcessedApiTrigger {

    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private ProcessedQueueHandler processedQueueHandler;

    @FunctionName("processedApiConsumer")
    public HttpResponseMessage execute(@HttpTrigger(name = "req", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS)
        HttpRequestMessage<Optional<String>> request,
        ExecutionContext context) throws JsonMappingException, JsonProcessingException  {
        
        String correlationId = UUID.randomUUID().toString();
        ThreadContext.put("correlationId", correlationId);

        try {
            if (!request.getBody().isPresent()) {
                
                log.info("Unable to read request body. Is empty");
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Unable to read request body.").build();
            } else {

                String requestString = request.getBody().get();
                log.info("apiJobProducer function hit. Request body is {}", requestString);

                ScraperResponse scraperResponse = objectMapper.readValue(requestString, ScraperResponse.class);

                processedQueueHandler.accept(scraperResponse);

                return request.createResponseBuilder(HttpStatus.OK).body("Successfully processed message...").build(); 
            }
        } catch (Exception e) {
            log.error("There was an error in the processedApiConsumer.", e);
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("An exception was thrown when trying to queue the job. Error: " + e.getMessage()).build();
        } finally {
            ThreadContext.clearAll();
        }
        
    }
}
