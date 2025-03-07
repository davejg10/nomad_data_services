package com.nomad.job_orchestrator.functions.processed_queue_consumer;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Map;
import java.util.function.Consumer;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomad.job_orchestrator.Neo4jCityRepository;
import com.nomad.library.messages.ScraperResponse;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class ProcessedQueueHandler implements Consumer<String> {
    
    private ObjectMapper objectMapper;
    private Neo4jCityRepository neo4jRepository;

    public ProcessedQueueHandler(Neo4jCityRepository neo4jRepository, ObjectMapper objectMapper) {
        this.neo4jRepository = neo4jRepository;
        this.objectMapper = objectMapper;
    }

    public void accept(String scraperResponseString) {

        try {
            ScraperResponse scraperResponse = objectMapper.readValue(scraperResponseString, ScraperResponse.class);
            Map<String, Object> cityAsMap = objectMapper.convertValue(scraperResponse,  Map.class);
            log.info("Message recieved: {}", scraperResponse.getScraperRequestSource());
            // neo4jRepository.saveCityDTOWithDepth0(cityAsMap);

        } catch (JsonProcessingException e) {
            log.error("Error when trying to map message to CityDTO. Error: {}", e.getMessage());
            throw new RuntimeException(e);
        }

    }

    // Duration duration;

    //         if (routeInfo.depart().contains("Any time")) {
    //             continue;
    //         } else {
    //             LocalTime arrivalTime = LocalTime.parse(routeInfo.arrival());
    //             LocalTime departureTime = LocalTime.parse(routeInfo.depart());
    //             if (arrivalTime.isBefore(departureTime)) {

    //                 // If arrival is before departure, add 24 hours
    //                 duration = Duration.between(departureTime, arrivalTime);
    //                 duration = duration.isNegative() ? duration.plusHours(24) : duration;
    //             } else {
    //                 duration = Duration.between(departureTime, arrivalTime);
    //             }
    //         }

    //         String routeTime = duration.toHoursPart() + "." + (duration.toMinutesPart());
}

