package com.nomad.producer;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.OutputBinding;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpOutput;
import com.microsoft.azure.functions.annotation.ServiceBusQueueOutput;
import com.microsoft.azure.functions.annotation.TimerTrigger;
import com.nomad.producer.messages.DataCollectionJob;
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

    private static final String QUEUE_NAME = "nomad_12goasia";

    @Autowired
    private Consumer<OutputBinding<DataCollectionJob>> processAndShutdown;

    @FunctionName("processAndShutdown")
    @ServiceBusQueueOutput(name = "message", queueName = QUEUE_NAME, connection = "OneToGoAsiaQueue")
    public void execute(@TimerTrigger(name = "keepAliveTrigger", schedule = "*/60 * * * * *") String timerInfo,
                        @HttpOutput(name = "response") final OutputBinding<DataCollectionJob> result) {

//        Message<String> message = MessageBuilder
//                .withPayload(timerInfo)
//                .setHeader(EXECUTION_CONTEXT, context)
//                .build();

        this.processAndShutdown.accept(result);
    }

}