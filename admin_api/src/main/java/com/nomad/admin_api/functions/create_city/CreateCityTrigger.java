package com.nomad.admin_api.functions.create_city;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import com.nomad.admin_api.domain.CityDTO;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class CreateCityTrigger {
    
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CreateCityHandler createCityHandler;

    @FunctionName("createCity")
    public HttpResponseMessage execute(@HttpTrigger(name = "req", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS)
        HttpRequestMessage<Optional<String>> request) throws Exception {
        
        if (!request.getBody().isPresent()) {
            log.info("Unable to read request body. Is empty");
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Unable to read request body.").build();
        } else {

            try {
                CityDTO cityToBeCreated = objectMapper.readValue(request.getBody().get(), CityDTO.class);
                log.info("createCity function hit. Request body is {}", cityToBeCreated);

                createCityHandler.accept(cityToBeCreated);

                return request.createResponseBuilder(HttpStatus.OK).body("Successfully created City " + cityToBeCreated.name() + " in PostgreSQl flexible server & synced to Neo4j.").build();

            } catch (Exception  e) {
                log.error("There was an issue saving the country {} in the Postgres Flexible server. Likely a bad requst. Message; {}", e.getMessage());
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Issue creating City. Issue: " + e.getMessage()).build();
            } 
        }
    }
    
}
