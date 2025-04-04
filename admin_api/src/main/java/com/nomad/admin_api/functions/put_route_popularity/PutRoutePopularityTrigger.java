package com.nomad.admin_api.functions.put_route_popularity;

import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
import com.nomad.admin_api.services.RoutePopularityService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class PutRoutePopularityTrigger {
    
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoutePopularityService routePopularityService;

    @FunctionName("putRoutePopularity")
    public HttpResponseMessage execute(
        @HttpTrigger(
            name = "req", 
            methods = {HttpMethod.POST}, 
            authLevel = AuthorizationLevel.ANONYMOUS)
        HttpRequestMessage<Optional<String>> request,
        ExecutionContext context) throws Exception {

        String correlationId = UUID.randomUUID().toString();
        ThreadContext.put("correlationId", correlationId);

        try {
            if (!request.getBody().isPresent()) {
                log.info("Unable to read request body. Is empty");
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Unable to read request body.").build();
            } else {
    
                try {
                    RoutePopularityDTO routePopularityDTO = objectMapper.readValue(request.getBody().get(), RoutePopularityDTO.class);
                    log.info("putRoutePopularity function hit. Request body is {}", routePopularityDTO);
                    
                    routePopularityService.updateRoutePopularity(routePopularityDTO);

                    log.info("RoutePopularity and updated Neo4jRoutes have been successfully saved.");

                    return request.createResponseBuilder(HttpStatus.OK).body("Successfully updated all routes out of city with id: " + routePopularityDTO.sourceCityId() + " to have popularity of: " + routePopularityDTO.popularity()).build();
    
                } catch(JsonMappingException e) {
                    context.getLogger().log(Level.SEVERE, "An error was thrown when trying to map the request to RoutePopularityDTO. CorrelationId: " + correlationId + " Exception: " + e.getMessage(), e);
                    return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Json mapping error. Please ensure you have the correct payload. Issue: " + e.getMessage()).build();
                
                } catch (Exception e) {
                    context.getLogger().log(Level.SEVERE, "There was an issue trying to update the popularity of all outbound routes from the city. CorrelationId: " + correlationId + " Exception: " + e.getMessage(), e);
                    return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("Issue updating popularity of all outbound routes from the city. Issue: " + e.getMessage()).build();
                } 
            }
        } finally {
            ThreadContext.remove("correlationId");
        }
    }
}