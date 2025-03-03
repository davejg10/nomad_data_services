package com.nomad.admin_api.functions.create_city;

import java.util.Optional;

import javax.net.ssl.SSLEngineResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import com.nomad.admin_api.Neo4jRepository;
import com.nomad.admin_api.SqlCityRepository;
import com.nomad.admin_api.SqlCountryRepository;
import com.nomad.admin_api.domain.CityDTO;
import com.nomad.admin_api.domain.SqlCity;
import com.nomad.admin_api.domain.SqlCountry;
import com.nomad.library.domain.City;
import com.nomad.library.domain.Country;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class CreateCity {
    
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SqlCountryRepository countryRepository;

    @Autowired
    private SqlCityRepository cityRepository;

    @Autowired
    private Neo4jRepository neo4jRepository;

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

                SqlCountry citiesCountry = countryRepository.findByName(cityToBeCreated.countryName()).get();
                SqlCity city = SqlCity.of(cityToBeCreated.name(), cityToBeCreated.description(), citiesCountry.getId());
                city = cityRepository.save(city);

                log.info("Created city in PostgreSQL flexible server with id: {}, and name: {}", city.getId(), city.getName());

                City neo4jCity = neo4jRepository.syncCity(city);

                log.info("Synced city to Neo4j database with id {}, and name: {}", neo4jCity.getId(), neo4jCity.getName());

                return request.createResponseBuilder(HttpStatus.OK).body("Successfully created City " + cityToBeCreated.name() + " in PostgreSQl flexible server & synced to Neo4j.").build();

            } catch (DataIntegrityViolationException  e) {
                log.error("There was an issue saving the country {} in the Postgres Flexible server. Likely a bad requst. Message; {}", e.getMessage());
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Issue creating City. Issue: " + e.getMessage()).build();
            }

        }
    } 
    
}
