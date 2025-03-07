package com.nomad.job_orchestrator.functions.cron_job_producer;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.TimerTrigger;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.nomad.library.connectors.ServiceBusBatchSender;
import com.nomad.library.messages.ScraperRequest;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Log4j2
@Component
public class CronJobTrigger {

    private static final String cronJobConfigFile = "jobs-config.yml";
    
    // Note that no matter the cron schedule on the jobs within jobs-config, the maximumum frequency with 
    // which they can be called is down to this cron schedule. 
    private final String cronTriggerSchedule = "0 */5 * * * *";


    @Autowired
    private CronJobHandler cronJobHandler;

    @Autowired 
    private ServiceBusBatchSender<ScraperRequest> serviceBusBatchSender;

    /*
     * This Azure Function is scheduled to queue scraping jobs at various intervals. These are posted to the same
     * queue as apiJobTrigger Function.
     */
    @FunctionName("cronJobProducer")
    public void execute(@TimerTrigger(name = "keepAliveTrigger", schedule = cronTriggerSchedule) String timerInfo,
                        ExecutionContext context) throws StreamReadException, DatabindException, IOException {
        
        LocalDateTime now = LocalDateTime.now();

        CronJobs cronJobs = cronJobHandler.readCronJobs(cronJobConfigFile);
        List<CronJob> filteredCronJobs = cronJobHandler.filterCronJobs(now, cronTriggerSchedule, cronJobs);

        for(CronJob filteredCronJob : filteredCronJobs) {
            List<ScraperRequest> scraperRequests = cronJobHandler.createScraperRequests(filteredCronJob);
            serviceBusBatchSender.sendBatch(scraperRequests);           
        }
    }
}