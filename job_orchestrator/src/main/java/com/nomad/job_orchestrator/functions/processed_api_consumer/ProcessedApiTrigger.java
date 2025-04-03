package com.nomad.job_orchestrator.functions.processed_api_consumer;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
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
import com.nomad.job_orchestrator.functions.processed_queue_consumer.ProcessedQueueHandler;
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
        
        Map<String, String> headers = request.getHeaders();

        String correlationId = headers.get("correlationId");
        ThreadContext.put("correlationId", correlationId);

        try {
            if (!request.getBody().isPresent()) {
                
                log.info("Unable to read request body. Is empty");
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Unable to read request body.").build();
            } else {

                String requestString = request.getBody().get();
                log.info("apiJobProducer function hit. Request body is {}", requestString);

                List<ScraperResponse> scraperResponses = objectMapper.readValue(requestString, new TypeReference<List<ScraperResponse>>(){});

                for (ScraperResponse response : scraperResponses) {
                    processedQueueHandler.accept(response);
                }

                return request.createResponseBuilder(HttpStatus.OK).body("Successfully processed message...").build(); 
            }
        } catch (Exception e) {
            log.error("An unexpected exception was thrown in processedApiConsumer. Error: {}", e.getMessage());
            context.getLogger().log(Level.SEVERE, "An unexpected exception was thrown in processedApiConsumer. CorrelationId " + correlationId + " Exception: " + e.getMessage(), e);
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("An exception was thrown when trying to queue the job. Error: " + e.getMessage()).build();
        } finally {
            ThreadContext.remove("correlationId");
        }
        
    }
}
