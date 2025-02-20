package com.nomad.consumer;

import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.ServiceBusQueueTrigger;
import com.nomad.consumer.messages.DataCollectionJob;

@Component
public class ConsumerHandler {

    // @Value("${sb_pre_processed_queue_name}")
    private final String sb_pre_processed_queue_name = "nomad_pre_processed";
    // @Value("${sb_processed_queue_name}")
    private final String sb_processed_queue_name = "nomad_processed";

    @Autowired
    private Function<Message<String>, Message<String>> scrape;

    @FunctionName("scrape")
    public void execute(@ServiceBusQueueTrigger(name = "msg", queueName = sb_pre_processed_queue_name, connection = "nomadservicebus") DataCollectionJob message,
                        ExecutionContext context) {

        

        this.scrape.apply(message);
    }

}