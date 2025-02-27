package com.nomad.job_orchestrator.functions.processed_queue_consumer;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.ServiceBusQueueTrigger;
import com.nomad.library.domain.CityDTO;
import com.nomad.job_orchestrator.CityRepository;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class ProcessedQueueTrigger {

    private final String sb_processed_queue_name = "nomad_processed";

    private final CityRepository cityRepository;

    @Autowired
    private ObjectMapper objectMapper;

    public ProcessedQueueTrigger(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    @FunctionName("processedQueueConsumer")
    public void execute(@ServiceBusQueueTrigger(name = "msg", queueName = sb_processed_queue_name, connection = "nomadservicebus") String message,
                        ExecutionContext context) throws JsonProcessingException {

        log.info("processedQueueConsumer Azure Function. Triggered with following message {} Service Bus Queue : {}", message, sb_processed_queue_name);
      
        CityDTO cityDTO = objectMapper.readValue(message, CityDTO.class);
        Map<String, Object> cityAsMap = objectMapper.convertValue(cityDTO,  Map.class);
        cityRepository.saveCityDTOWithDepth0(cityAsMap);
    }

}