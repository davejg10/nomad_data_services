package com.nomad.scraping_library.scraper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

import com.azure.core.util.IterableStream;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.models.DeadLetterOptions;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomad.scraping_library.connectors.ServiceBusBatchSender;
import com.nomad.scraping_library.domain.ScraperRequest;
import com.nomad.scraping_library.domain.ScraperRequestType;
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
    private final ObjectMapper objectMapper;

    public final HttpClient httpClient;

    public ScraperProcessor(
        T scraper,
        ServiceBusBatchSender<ScraperResponse> serviceBusBatchSender,
        ServiceBusReceiverClient receiver,
        ApplicationContext applicationContext,
        int timeoutInSeconds,
        ObjectMapper objectMapper) {
            this.scraper = scraper;
            this.serviceBusBatchSender = serviceBusBatchSender;
            this.receiver = receiver;
            this.applicationContext = applicationContext;
            this.timeoutInSeconds = timeoutInSeconds;
            this.objectMapper = objectMapper;
            this.httpClient = HttpClient.newHttpClient();
        }
    
    @Value("${spring.profiles.active}")
    private String ACTIVE_PROFILE;
    
    @Override
    public void run(String... args) throws JsonMappingException, JsonProcessingException {

        try {
            Duration timeout = Duration.ofSeconds(timeoutInSeconds);
            long endTime = System.currentTimeMillis() + timeout.toMillis();

            boolean alwaysOn = timeoutInSeconds == -1; // This is set to -1 for Hertzner runner
            
            while (System.currentTimeMillis() < endTime || alwaysOn) {
                IterableStream<ServiceBusReceivedMessage> messages = 
                receiver.receiveMessages(5, Duration.ofSeconds(45));
            
                Iterator<ServiceBusReceivedMessage> iterator = messages.iterator();

                while (iterator.hasNext()) {
                    ServiceBusReceivedMessage message = iterator.next();
                    String correlationId = message.getCorrelationId();
                    ThreadContext.put("correlationId", correlationId);

                    log.info("Started processing ScraperRequest. Agent is running with active.profile: {}", ACTIVE_PROFILE);
                    ScraperRequest request = null;
                    try {
                        request = message.getBody().toObject(ScraperRequest.class);
        
                        log.info("ScraperRequestType: {}. The job is {}, for route {} -> {}", request.getScraperRequestType(), request.getScraperRequestSource(), request.getSourceCity().name(), request.getTargetCity().name());

                        List<ScraperResponse> scraperResponses = scraper.scrapeData(request);
                        
                        if (scraperResponses.size() == 0) {
                            throw new NoRoutesFoundException("Scraper found no route instances for route " +  request.getSourceCity().name() + " -> " +  request.getTargetCity().name());
                        }

                        if (request.getScraperRequestSource().contains("http")) {
                            log.info("ScraperRequestSource was http therefore hitting processedApiConsumer");
                            String url = "https://fa-dev-uks-nomad-02-job-orchestrator.azurewebsites.net/api/processedApiConsumer";
                            String jsonRequest = objectMapper.writeValueAsString(scraperResponses);
                            HttpRequest httpRequest = HttpRequest.newBuilder()
                                .uri(URI.create(url))
                                .header("Content-Type", "application/json")
                                .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                                .build();
                            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

                            if (response.statusCode() == 200) {
                                log.info("Response from processedApiConsumer was 200, continuing with loop");
                                continue;
                            }
                        }
                        
                        log.info("Preparing to send scraperResponses as batch");
                        serviceBusBatchSender.sendBatch(scraperResponses, correlationId); 

                        log.info("Successfully sent message");
                        receiver.complete(message);
                        // Reset timeout
                        endTime = System.currentTimeMillis() + timeout.toMillis();
    
                    } catch (NoRoutesFoundException e)  {
                    
                        ScraperRequestType type = request.getScraperRequestType();
                        if (type.equals(ScraperRequestType.ROUTE_UPDATE)) { // We do this because we know we've found a route last time
                            log.info("{}. ScraperRequestType was {}. Therefore abandoning message (putting back on queue)", e.getMessage(), type);
                            receiver.abandon(message);
                        } else {
                            log.warn("{}. ScraperRequestType is {}. Therefore DLQing the following message.", e.getMessage(), type);
                            DeadLetterOptions reason = new DeadLetterOptions().setDeadLetterReason(e.getMessage());
                            receiver.deadLetter(message, reason);
                        }

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
