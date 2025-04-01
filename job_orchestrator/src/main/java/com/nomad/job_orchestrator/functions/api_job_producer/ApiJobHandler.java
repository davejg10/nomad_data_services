package com.nomad.job_orchestrator.functions.api_job_producer;

import java.util.function.Function;

import org.springframework.stereotype.Service;

import com.nomad.scraping_library.domain.CityDTO;
import com.nomad.scraping_library.domain.ScraperRequest;
import com.nomad.scraping_library.domain.ScraperRequestType;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class ApiJobHandler implements Function<HttpScraperRequest, ScraperRequest> {


    public ScraperRequest apply(HttpScraperRequest routeRequest) {
        
        CityDTO sourceCity = new CityDTO(routeRequest.sourceCity().id(), routeRequest.sourceCity().name());
        CityDTO targetCity = new CityDTO(routeRequest.targetCity().id(), routeRequest.targetCity().name());

        ScraperRequest scraperRequest = new ScraperRequest("httpTrigger", ScraperRequestType.ROUTE_UPDATE, sourceCity, targetCity, routeRequest.searchDate());
        return scraperRequest;
    }
    
}
