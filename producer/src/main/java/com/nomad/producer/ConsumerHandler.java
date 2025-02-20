package com.nomad.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.ServiceBusQueueTrigger;
import com.nomad.producer.nomad.CityDTO;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class ConsumerHandler {

    private final String sb_pre_processed_queue_name = "nomad_processed";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private CityRepository cityRepository;

    @FunctionName("processCity")
    public void execute(@ServiceBusQueueTrigger(name = "msg", queueName = sb_pre_processed_queue_name, connection = "nomadservicebus") String message,
                        ExecutionContext context) throws JsonMappingException, JsonProcessingException {

        log.info("message: {}", message);
        CityDTO CityDTO = OBJECT_MAPPER.readValue(message, CityDTO.class);
        cityRepository.saveCityDTOWithDepth0(CityDTO);
    }

}