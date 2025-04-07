package com.nomad.job_orchestrator.functions.batch_api_job_producer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

import org.apache.logging.log4j.ThreadContext;
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
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import com.nomad.scraping_library.connectors.ServiceBusBatchSender;
import com.nomad.scraping_library.domain.ScraperRequest;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class BatchApiJobTrigger {

    private final String sb_pre_processed_queue_name = "nomad_pre_processed";

    @Autowired
    private BatchApiJobHandler batchApiJobHandler;

    @Autowired
    private ServiceBusBatchSender<ScraperRequest> serviceBusBatchSender;

    @Autowired
    private ObjectMapper objectMapper;

    /*
     * This Azure Function acts as a HTTP endpoints to queue scraping jobs. The nomad_backend is the only client.
     */
    @FunctionName("batchApiJobProducer")
    public HttpResponseMessage execute(
        @HttpTrigger(
            name = "req",
            methods = {HttpMethod.POST}, 
            authLevel = AuthorizationLevel.ANONYMOUS)
        HttpRequestMessage<Optional<String>> request,
        ExecutionContext context) throws JsonMappingException, JsonProcessingException  {
        
        String correlationId = UUID.randomUUID().toString();
        ThreadContext.put("correlationId", correlationId);

        try {
            if (!request.getBody().isPresent()) {
                
                log.info("Unable to read request body. Is empty");
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Unable to read request body.").build();
            } else  {

                String requestString = request.getBody().get();
                log.info("batchApiJobProducer function hit. Request body is {}", requestString);

                BatchHttpScraperRequest routeRequest = objectMapper.readValue(requestString, BatchHttpScraperRequest.class);

                List<ScraperRequest> scraperRequests = batchApiJobHandler.apply(routeRequest);
                if (scraperRequests.size() > 0)
                     serviceBusBatchSender.sendBatch(scraperRequests, correlationId);
                
                return request.createResponseBuilder(HttpStatus.OK).body("Successfully added scraper batch scraper requests for country: " + routeRequest.countryName() + " with searchDate: " + routeRequest.searchDate() + " to queue: " + sb_pre_processed_queue_name).build(); 
            }
        } catch (JsonProcessingException e) {
            log.error("A JsonProcessingException exception was thrown when trying to either serialize/desirialize.", e);
            context.getLogger().log(Level.SEVERE, "A JsonProcessingException exception was thrown when trying to either serialize/desirialize. CorrelationId " + correlationId + " Exception: " + e.getMessage(), e);
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("A JsonProcessingException exception was thrown when trying to either serialize/desirialize. Error: " + e.getMessage()).build();
        } catch (Exception e) {
            log.error("There was an error in the batchApiJobProducer.", e);
            context.getLogger().log(Level.SEVERE, "An exception was thrown when trying to create ScraperRequests within the batchApiJobProducer. CorrelationId " + correlationId + " Exception: " + e.getMessage(), e);
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("An exception was thrown when trying to create ScraperRequests within the batchApiJobProducer. Error: " + e.getMessage()).build();
        } finally {
            ThreadContext.remove("correlationId");
        }
        
    }
}