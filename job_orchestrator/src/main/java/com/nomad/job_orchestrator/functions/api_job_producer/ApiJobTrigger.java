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
import com.microsoft.azure.functions.annotation.HttpOutput;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import com.microsoft.azure.functions.annotation.ServiceBusQueueOutput;
import com.nomad.job_orchestrator.domain.HttpRouteRequest;
import com.nomad.library.messages.ScraperJob;
import com.nomad.library.messages.ScraperJobType;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class ApiJobTrigger {

    private final String sb_pre_processed_queue_name = "nomad_pre_processed";

    @Autowired
    private ObjectMapper objectMapper;

    @FunctionName("apiJobProducer")
    public HttpResponseMessage execute(@HttpTrigger(name = "req", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS)
        HttpRequestMessage<Optional<String>> request,
        @ServiceBusQueueOutput(name = "message", queueName = sb_pre_processed_queue_name, connection = "nomadservicebus") OutputBinding<ScraperJob> message) throws JsonMappingException, JsonProcessingException  {
        
        if (!request.getBody().isPresent()) {
            log.info("Unable to read request body. Is empty");
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Unable to read request body.").build();
        } else {
            HttpRouteRequest routeRequest = objectMapper.readValue(request.getBody().get(), HttpRouteRequest.class);
            log.info("apiJobProducer function hit. Request body is {}", routeRequest);
            ScraperJob scraperJob = new ScraperJob("httpTrigger", ScraperJobType.ROUTE_DISCOVERY, routeRequest.sourceCity(), routeRequest.destinationCity(), routeRequest.searchDate());
            message.setValue(scraperJob);
            return request.createResponseBuilder(HttpStatus.OK).body("Successfully added job to queue.").build();
        }
    }
}