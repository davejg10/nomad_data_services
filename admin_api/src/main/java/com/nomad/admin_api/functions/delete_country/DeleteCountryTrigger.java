package com.nomad.admin_api.functions.delete_country;

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

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class DeleteCountryTrigger {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DeleteCountryHandler deleteCountryHandler;

    @FunctionName("deleteCountry")
    public HttpResponseMessage execute(@HttpTrigger(name = "req", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS)
        HttpRequestMessage<Optional<String>> request, 
        ExecutionContext context) throws Exception {
        
        String correlationId = UUID.randomUUID().toString();
        ThreadContext.put("correlationID", correlationId);

        try {
            if (!request.getBody().isPresent()) {
                log.info("Unable to read request body. Is empty");
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Unable to read request body.").build();
            } else {
                
                try {
                    String countryToBeDeleted = objectMapper.readTree(request.getBody().get()).get("name").asText();
                    log.info("deleteCountry function hit. Request body is {}", countryToBeDeleted);
                
                    deleteCountryHandler.accept(countryToBeDeleted);
    
                    return request.createResponseBuilder(HttpStatus.OK).body("Successfully deleted Country " + countryToBeDeleted + " in PostgreSQl flexible server & deleted in Neo4j.").build();
    
                } catch(JsonMappingException e) {    
                    log.error("An error was thrown when trying to map message to String.", e);
                    context.getLogger().log(Level.SEVERE, "An error was thrown when trying to map message to String. CorrelationId: " + correlationId + " Exception: " + e.getMessage(), e);
                    return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Json mapping error. Please ensure you have the correct payload. Issue: " + e.getMessage()).build();
                } catch (Exception e) {
                    context.getLogger().log(Level.SEVERE, "There was an issue deleting the country. CorrelationId: " + correlationId + " Exception: " + e.getMessage(), e);
                    return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("Issue deleting Country. Issue: " + e.getMessage()).build();
                }
            }
        } finally {
            ThreadContext.clearAll();
        }
        
    }
}