package com.nomad.producer;

import com.microsoft.azure.functions.ExecutionContext;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;

import java.util.Locale;
import java.util.function.Consumer;

@Log4j2
@SpringBootApplication
public class TimeTriggerDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(TimeTriggerDemoApplication.class, args);
    }

    @Bean
    public Consumer<Message<String>> uppercase() {
        return message -> {
            String timeInfo = message.getPayload();
            String value = timeInfo.toUpperCase(Locale.ROOT);

            log.info("This is a log message from Sl4j");
            // (Optionally) access and use the Azure function context.
            ExecutionContext context = (ExecutionContext) message.getHeaders().get(UppercaseHandler.EXECUTION_CONTEXT);

            // No response.
        };
    }

}
