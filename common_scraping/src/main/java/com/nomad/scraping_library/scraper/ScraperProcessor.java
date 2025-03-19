package com.nomad.scraping_library.scraper;

import java.time.Duration;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

import com.azure.core.util.IterableStream;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.nomad.scraping_library.connectors.ServiceBusBatchSender;
import com.nomad.scraping_library.domain.ScraperRequest;
import com.nomad.scraping_library.domain.ScraperResponse;
import com.nomad.scraping_library.exceptions.NoRoutesFoundException;

import lombok.extern.log4j.Log4j2;

@Log4j2
public abstract class ScraperProcessor<T extends WebScraperInterface> implements CommandLineRunner {
 
    public final T scraper;
    public final ServiceBusBatchSender<ScraperResponse> serviceBusBatchSender;
    public final ServiceBusReceiverClient receiver;
    public final ApplicationContext applicationContext;
    public final int timeoutInSeconds;

    public ScraperProcessor(
        T scraper,
        ServiceBusBatchSender<ScraperResponse> serviceBusBatchSender,
        ServiceBusReceiverClient receiver,
        ApplicationContext applicationContext,
        int timeoutInSeconds) {
            this.scraper = scraper;
            this.serviceBusBatchSender = serviceBusBatchSender;
            this.receiver = receiver;
            this.applicationContext = applicationContext;
            this.timeoutInSeconds = timeoutInSeconds;
        }

    @Override
    public void run(String... args) throws JsonMappingException, JsonProcessingException {

        try {
            Duration timeout = Duration.ofSeconds(timeoutInSeconds);
            long endTime = System.currentTimeMillis() + timeout.toMillis();
            
            while (System.currentTimeMillis() < endTime) {

                IterableStream<ServiceBusReceivedMessage> messages = 
                receiver.receiveMessages(1, Duration.ofSeconds(5));
            
                Iterator<ServiceBusReceivedMessage> iterator = messages.iterator();

                if (iterator.hasNext()) {
                    ServiceBusReceivedMessage message = iterator.next();
                    
                    String correlationId = message.getCorrelationId();
                    ThreadContext.put("correlationId", correlationId);

                    log.info("Started processing ScraperRequest");

                    try {
                        ScraperRequest request = message.getBody().toObject(ScraperRequest.class);
        
                        log.info("The job is {}, for route {} -> {}", request.getScraperRequestSource(), request.getSourceCity().name(), request.getTargetCity().name());

                        List<ScraperResponse> scraperResponses = scraper.scrapeData(request);
                        
                        if (scraperResponses.size() == 0) {
                            log.warn("WebScraper found no route instances for route {} -> {}. DLQing.", request.getSourceCity().name(), request.getTargetCity().name());
                            throw new NoRoutesFoundException("Scraper found no route instances for route " +  request.getSourceCity().name() + " -> " +  request.getTargetCity().name() + ". DLQing");
                        }

                        serviceBusBatchSender.sendBatch(scraperResponses, correlationId); 

                        log.info("Successfully sent message");
                        receiver.complete(message);

                        // Reset timeout
                        endTime = System.currentTimeMillis() + timeout.toMillis();
    
                    } catch(NoRoutesFoundException e)  {
                        log.error("DLQing the following message. Reason: {}", e.getMessage());
                        receiver.deadLetter(message);
                    } catch (Exception e) {
                        log.error("Abandoning the following message. Reason: {}", e.getMessage());
                        receiver.abandon(message);
                    }
                }
            }
            log.info("Timeout exeeceded, no messages in {} seconds.", timeoutInSeconds);
            
        } finally {
            log.info("Closing client connections...");
            receiver.close();
            serviceBusBatchSender.getSenderClient().close();

            log.info("Shutting down application...");

            ThreadContext.clearAll();
            SpringApplication.exit(applicationContext, () -> 0);
            System.exit(0);
        }
    }
}
