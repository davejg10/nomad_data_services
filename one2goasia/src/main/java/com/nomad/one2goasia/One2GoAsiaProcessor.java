package com.nomad.one2goasia;


import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.nomad.library.connectors.ServiceBusBatchSender;
import com.nomad.library.messages.ScraperResponse;
import com.nomad.library.scraper.ScraperProcessor;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class One2GoAsiaProcessor extends ScraperProcessor<One2GoAsiaScraper> {

    private One2GoAsiaScraper one2GoAsiaScraper;
    
    public One2GoAsiaProcessor(One2GoAsiaScraper one2GoAsiaScraper, ServiceBusBatchSender<ScraperResponse> serviceBusBatchSender, ServiceBusReceiverClient receiver, ApplicationContext applicationContext) {
        super(one2GoAsiaScraper, serviceBusBatchSender, receiver, applicationContext, 30);
    }

}