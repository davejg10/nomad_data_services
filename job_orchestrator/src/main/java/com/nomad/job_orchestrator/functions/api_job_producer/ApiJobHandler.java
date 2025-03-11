package com.nomad.job_orchestrator.functions.api_job_producer;

import java.util.Optional;
import java.util.function.Function;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomad.scraping_library.domain.CityDTO;
import com.nomad.scraping_library.domain.ScraperRequest;
import com.nomad.scraping_library.domain.ScraperRequestType;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class ApiJobHandler implements Function<String, Optional<ScraperRequest>> {

    private ObjectMapper objectMapper;

    public ApiJobHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Optional<ScraperRequest> apply(String request) {
        try {
            HttpScraperRequest routeRequest = objectMapper.readValue(request, HttpScraperRequest.class);
            CityDTO sourceCity = new CityDTO(routeRequest.sourceCity().id(), routeRequest.sourceCity().name());
            CityDTO targetCity = new CityDTO(routeRequest.targetCity().id(), routeRequest.targetCity().name());

            ScraperRequest scraperRequest = new ScraperRequest("httpTrigger", ScraperRequestType.ROUTE_DISCOVERY, sourceCity, targetCity, routeRequest.searchDate());
            return Optional.of(scraperRequest);
        } catch (JsonProcessingException e) {
            log.error("There was an issue when trying to map the request to a HttpScraperRequest. Error: {}", e.getMessage());
            return Optional.empty();
        } 
    }
    
}
