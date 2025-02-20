package com.nomad.producer;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.ServiceBusQueueTrigger;
import com.microsoft.azure.functions.annotation.TimerTrigger;
import com.nomad.producer.messages.DataCollectionJob;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@Log4j2
@Component
public class ProducerHandler {

    public static String EXECUTION_CONTEXT = "executionContext";

    private static AtomicInteger count = new AtomicInteger();

    @Autowired
    private Consumer<Message<String>> processAndShutdown;

    @FunctionName("processAndShutdown")
    public void execute(@TimerTrigger(name = "keepAliveTrigger", schedule = "0 10 * * * *") String timerInfo,
                        ExecutionContext context) {

        Message<String> message = MessageBuilder
                .withPayload(timerInfo)
                .setHeader(EXECUTION_CONTEXT, context)
                .build();

        this.processAndShutdown.accept(message);
    }
}