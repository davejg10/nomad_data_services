package com.nomad.job_orchestrator.functions.cron_job_producer;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.TimerTrigger;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.nomad.library.messages.ScraperJob;
import com.nomad.job_orchestrator.config.ServiceBusBatchSender;

import java.io.IOException;
import java.util.List;

@Log4j2
@Component
public class CronJobTrigger {

    @Autowired
    private CronJobHandler cronJobHandler;

    @Autowired 
    private ServiceBusBatchSender serviceBusBatchSender;

    /*
     * This Azure Function is scheduled to queue scraping jobs at various intervals. These are posted to the same
     * queue as apiJobTrigger Function.
     */
    @FunctionName("cronJobProducer")
    public void execute(@TimerTrigger(name = "keepAliveTrigger", schedule = "0 */10 * * *") String timerInfo,
                        ExecutionContext context) throws StreamReadException, DatabindException, IOException {

        List<ScraperJob> scraperJobs = cronJobHandler.generateScraperJobs();
        serviceBusBatchSender.sendBatch(scraperJobs);           
    }
}