package com.nomad.admin_api.functions.create_country;

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
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import com.nomad.admin_api.Neo4jRepository;
import com.nomad.admin_api.PostgresRepository;
import com.nomad.admin_api.domain.Country;
import com.nomad.admin_api.domain.CountryDTO;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class CreateCountry {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PostgresRepository postgresRepository;

    @Autowired
    private Neo4jRepository neo4jRepository;

    @FunctionName("createCountry")
    public HttpResponseMessage execute(@HttpTrigger(name = "req", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS)
        HttpRequestMessage<Optional<String>> request) throws Exception {
        
        if (!request.getBody().isPresent()) {
            log.info("Unable to read request body. Is empty");
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Unable to read request body.").build();
        } else {
            CountryDTO countryToBeCreated = objectMapper.readValue(request.getBody().get(), CountryDTO.class);
            log.info("createCountry function hit. Request body is {}", countryToBeCreated);

            Country country = postgresRepository.createCountry(countryToBeCreated);
            log.info("Created country in PostgreSQL flexible server with id: {}, and name: {}", country.id(), country.name());

            Country neo4jCountry = neo4jRepository.syncCountry(country);

            log.info("Synced country to Neo4j database with id {}, and name: {}", neo4jCountry.id(), neo4jCountry.name());
            return request.createResponseBuilder(HttpStatus.OK).body("Successfully created Country " + countryToBeCreated.name() + " in PostgreSQl flexible server & synced to Neo4j.").build();
        }
    }
}