package com.nomad.consumer;

import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.ServiceBusQueueTrigger;
import com.nomad.consumer.messages.DataCollectionJob;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class ConsumerHandler {

    // @Value("")
    private final String sb_pre_processed_queue_name = "nomad_pre_processed";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private Consumer<DataCollectionJob> scrape;

    @FunctionName("scrape")
    public void execute(@ServiceBusQueueTrigger(name = "msg", queueName = sb_pre_processed_queue_name, connection = "nomadservicebus") String message,
                        ExecutionContext context) throws JsonMappingException, JsonProcessingException {

        log.info("message: {}", message);
        DataCollectionJob job = OBJECT_MAPPER.readValue(message, DataCollectionJob.class);
        this.scrape.accept(job);
    }

}