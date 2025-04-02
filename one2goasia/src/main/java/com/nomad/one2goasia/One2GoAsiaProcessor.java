package com.nomad.one2goasia;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomad.scraping_library.connectors.ServiceBusBatchSender;
import com.nomad.scraping_library.domain.ScraperResponse;
import com.nomad.scraping_library.scraper.ScraperProcessor;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class One2GoAsiaProcessor extends ScraperProcessor<One2GoAsiaScraper> {

    private One2GoAsiaScraper one2GoAsiaScraper;
    private ObjectMapper objectMapper;
    
    public One2GoAsiaProcessor(@Value("${app_settings.TIMEOUT_IN_SECONDS}") int TIMEOUT_IN_SECONDS,
                               @Value("${job_orchestrator_processed_api_url}") String jobOrchestratorProcessedApiUrl, 
                                One2GoAsiaScraper one2GoAsiaScraper,
                                ServiceBusBatchSender<ScraperResponse> serviceBusBatchSender,
                                ServiceBusReceiverClient receiver, 
                                ApplicationContext applicationContext, 
                                ObjectMapper objectMapper) {
        super(one2GoAsiaScraper, jobOrchestratorProcessedApiUrl, serviceBusBatchSender, receiver, applicationContext, TIMEOUT_IN_SECONDS, objectMapper);
    }

}