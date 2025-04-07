package com.nomad.job_orchestrator.functions.batch_api_job_producer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.springframework.stereotype.Service;

import com.nomad.job_orchestrator.domain.CityPair;
import com.nomad.job_orchestrator.repositories.Neo4jCityRepository;
import com.nomad.scraping_library.domain.ScraperRequest;
import com.nomad.scraping_library.domain.ScraperRequestSource;
import com.nomad.scraping_library.domain.ScraperRequestType;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class BatchApiJobHandler implements Function<BatchHttpScraperRequest, List<ScraperRequest>> {

    private final Neo4jCityRepository neo4jCityRepository;

    public BatchApiJobHandler(Neo4jCityRepository neo4jCityRepository) {
        this.neo4jCityRepository = neo4jCityRepository;
    }

    public List<ScraperRequest> apply(BatchHttpScraperRequest routeRequest) {
        
        List<CityPair> cityPairs = neo4jCityRepository.routeDiscoveryGivenCountry(routeRequest.countryName());
        List<ScraperRequest> scraperRequests = new ArrayList<>();
        for (CityPair cityPair : cityPairs) {
            ScraperRequest scraperRequest = new ScraperRequest(ScraperRequestSource.BATCH_API, ScraperRequestType.ROUTE_DISCOVERY, cityPair.sourceCity(), cityPair.targetCity(), routeRequest.searchDate());
            log.info("Scraper request created. Request is: {}", scraperRequest);
            scraperRequests.add(scraperRequest);
        }

        return scraperRequests;
    }
    
}
