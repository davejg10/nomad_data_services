package com.nomad.job_orchestrator.functions.api_job_producer;

import java.util.Optional;
import java.util.logging.Level;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.OutputBinding;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import com.microsoft.azure.functions.annotation.ServiceBusQueueOutput;
import com.nomad.scraping_library.domain.ScraperRequest;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class ApiJobTrigger {

    private final String sb_pre_processed_queue_name = "nomad_pre_processed";

    @Autowired
    private ApiJobHandler apiJobHandler;

    @Autowired
    private ObjectMapper objectMapper;

    /*
     * This Azure Function acts as a HTTP endpoints to queue scraping jobs. The nomad_backend is the only client.
     */
    @FunctionName("apiJobProducer")
    public HttpResponseMessage execute(@HttpTrigger(name = "req", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS)
        HttpRequestMessage<Optional<String>> request,
        @ServiceBusQueueOutput(name = "message", queueName = sb_pre_processed_queue_name, connection = "nomadservicebus") OutputBinding<String> message,
        ExecutionContext context) throws JsonMappingException, JsonProcessingException  {
        try {
            if (!request.getBody().isPresent()) {
                
                log.info("Unable to read request body. Is empty");
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Unable to read request body.").build();
            } else {

                String requestString = request.getBody().get();
                log.info("apiJobProducer function hit. Request body is {}", requestString);

                HttpScraperRequest routeRequest = objectMapper.readValue(requestString, HttpScraperRequest.class);

                ScraperRequest scraperRequest = apiJobHandler.apply(routeRequest);
                
                String serviceBusMessage = objectMapper.writeValueAsString(scraperRequest);
                message.setValue(serviceBusMessage);
                String route = routeRequest.sourceCity().name() + " -> " + routeRequest.targetCity().name();
                return request.createResponseBuilder(HttpStatus.OK).body("Successfully added scraper request for route " + route + ", to " + sb_pre_processed_queue_name + " queue.").build(); 
            }
        } catch (JsonProcessingException e) {
            context.getLogger().log(Level.SEVERE, "A JsonProcessingException exception was thrown when trying to either serialize/desirialize. Exception: " + e.getMessage(), e);
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("A JsonProcessingException exception was thrown when trying to either serialize/desirialize. Error: " + e.getMessage()).build();
        } catch (Exception e) {
            context.getLogger().log(Level.SEVERE, "There was an error in the apiJobProducer. Exception: {}" + e.getMessage(), e);
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("An exception was thrown when trying to queue the job. Error: " + e.getMessage()).build();
        }
        
    }
}