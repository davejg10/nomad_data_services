package com.nomad.job_orchestrator.functions.processed_queue_consumer;

import java.util.Map;
import java.util.function.Consumer;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomad.job_orchestrator.Neo4jRepository;
import com.nomad.library.domain.CityDTO;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class ProcessedQueueHandler implements Consumer<String> {
    
    private ObjectMapper objectMapper;
    private Neo4jRepository neo4jRepository;

    public ProcessedQueueHandler(Neo4jRepository neo4jRepository, ObjectMapper objectMapper) {
        this.neo4jRepository = neo4jRepository;
        this.objectMapper = objectMapper;
    }

    public void accept(String message) {

        try {
            CityDTO cityDTO = objectMapper.readValue(message, CityDTO.class);
            Map<String, Object> cityAsMap = objectMapper.convertValue(cityDTO,  Map.class);
            neo4jRepository.saveCityDTOWithDepth0(cityAsMap);

        } catch (JsonProcessingException e) {
            log.error("Error when trying to map message to CityDTO. Error: {}", e.getMessage());
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
    }
}

