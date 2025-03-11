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
                
                Optional<ScraperRequest> scraperRequest = apiJobHandler.apply(requestString);
                
                if (scraperRequest.isPresent()) {
                    String serviceBusMessage = objectMapper.writeValueAsString(scraperRequest.get());
                    message.setValue(serviceBusMessage);
                    return request.createResponseBuilder(HttpStatus.OK).body("Successfully added scraper request to queue.").build(); 
                }
                context.getLogger().log(Level.SEVERE, "There was an error when trying to map the request to a CityDTO.");
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("There was an issue mapping your request to a CityDTO.").build();
            }
        } catch (Exception e) {
            context.getLogger().log(Level.SEVERE, "There was an error in the apiJobProducer. Probably a mapping issue. Exception: {}" + e.getMessage(), e);
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("An exception was thrown when trying to queue the job. Error: " + e.getMessage()).build();
        }
        
    }
}