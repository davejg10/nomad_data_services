package com.nomad.job_orchestrator.functions.api_job_producer;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.OutputBinding;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import com.microsoft.azure.functions.annotation.ServiceBusQueueOutput;
import com.nomad.library.messages.ScraperRequest;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class ApiJobTrigger {

    private final String sb_pre_processed_queue_name = "nomad_pre_processed";

    @Autowired
    private ApiJobHandler apiJobHandler;

    /*
     * This Azure Function acts as a HTTP endpoints to queue scraping jobs. The nomad_backend is the only client.
     */
    @FunctionName("apiJobProducer")
    public HttpResponseMessage execute(@HttpTrigger(name = "req", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS)
        HttpRequestMessage<Optional<String>> request,
        @ServiceBusQueueOutput(name = "message", queueName = sb_pre_processed_queue_name, connection = "nomadservicebus") OutputBinding<ScraperRequest> message) throws JsonMappingException, JsonProcessingException  {
        
        if (!request.getBody().isPresent()) {
            log.info("Unable to read request body. Is empty");
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Unable to read request body.").build();
        } else {
            String requestString = request.getBody().get();
            log.info("apiJobProducer function hit. Request body is {}", requestString);
            
            Optional<ScraperRequest> scraperRequest = apiJobHandler.apply(requestString);

            if (scraperRequest.isPresent()) {
                message.setValue(scraperRequest.get());
                return request.createResponseBuilder(HttpStatus.OK).body("Successfully added scraper request to queue.").build(); 
            }
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("There was an issue mapping your request to a CityDTO.").build();
        }
    }
}