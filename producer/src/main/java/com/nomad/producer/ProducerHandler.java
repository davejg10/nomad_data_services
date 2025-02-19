package com.nomad.producer;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.TimerTrigger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@Slf4j
@Component
public class ProducerHandler {

    public static String EXECUTION_CONTEXT = "executionContext";

    private static AtomicInteger count = new AtomicInteger();

    @Autowired
    private Consumer<Message<String>> processAndShutdown;

    @FunctionName("processAndShutdown")
    public void execute(@TimerTrigger(name = "keepAliveTrigger", schedule = "0 */5 * * * *") String timerInfo,
                        ExecutionContext context) {

        Message<String> message = MessageBuilder
                .withPayload(timerInfo)
                .setHeader(EXECUTION_CONTEXT, context)
                .build();

        this.processAndShutdown.accept(message);
    }

}