package com.nomad.job_orchestrator.functions.processed_queue_consumer;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.ServiceBusQueueTrigger;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class ProcessedQueueTrigger {

    private final String sb_processed_queue_name = "nomad_processed";

    @Autowired
    private ProcessedQueueHandler processedQueueHandler;

    /*
     * This Azure Function reads messages from the Queue that the scapers post to. See payload.json for an example message.
     */
    @FunctionName("processedQueueConsumer")
    public void execute(@ServiceBusQueueTrigger(name = "msg", queueName = sb_processed_queue_name, connection = "nomadservicebus") String message,
                        ExecutionContext context) throws JsonProcessingException {

        log.info("processedQueueConsumer Azure Function. Triggered with following message {} Service Bus Queue : {}", message, sb_processed_queue_name);
        processedQueueHandler.accept(message);
    }

}