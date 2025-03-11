package com.nomad.admin_api.functions.create_country;

import java.util.Optional;

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
import com.nomad.library.domain.sql.SqlCountry;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class CreateCountryTrigger {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CreateCountryHandler createCountryHandler;

    @FunctionName("createCountry")
    public HttpResponseMessage execute(@HttpTrigger(name = "req", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS)
        HttpRequestMessage<Optional<String>> request, ExecutionContext context) throws Exception {
        
        if (!request.getBody().isPresent()) {
            log.info("Unable to read request body. Is empty");
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Unable to read request body.").build();
        } else {
            
            try {
                SqlCountry countryToBeCreated = objectMapper.readValue(request.getBody().get(), SqlCountry.class);
                log.info("createCountry function hit. Request body is {}", countryToBeCreated);
            
                // createAndSyncCountry(countryToBeCreated);
                createCountryHandler.accept(countryToBeCreated);

                return request.createResponseBuilder(HttpStatus.OK).body("Successfully created Country " + countryToBeCreated.getName() + " in PostgreSQl flexible server & synced to Neo4j.").build();

            } catch(JsonMappingException e) {
                log.error("An error was thrown when trying to map message to SqlCountry. Error: {}", e);
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Json mapping error. Please ensure you have the correct payload. Issue: " + e.getMessage()).build();
            } catch (Exception  e) {
                log.error("There was an issue saving the country {} in the Postgres Flexible server. Likely a bad requst. Message; {}", e.getMessage());
                return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("Issue creating Country. Issue: " + e.getMessage()).build();
            }
        }
    }
}