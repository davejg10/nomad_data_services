 package com.nomad.job_orchestrator.functions.cron_job_producer;

 import com.fasterxml.jackson.core.exc.StreamReadException;
 import com.fasterxml.jackson.databind.DatabindException;
 import com.microsoft.azure.functions.ExecutionContext;
 import com.microsoft.azure.functions.annotation.FunctionName;
 import com.microsoft.azure.functions.annotation.TimerTrigger;

 import org.apache.logging.log4j.ThreadContext;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Component;

 import com.nomad.scraping_library.connectors.ServiceBusBatchSender;
 import com.nomad.scraping_library.domain.ScraperRequest;

 import lombok.extern.log4j.Log4j2;

 import java.io.IOException;
 import java.time.LocalDateTime;
 import java.util.List;
 import java.util.UUID;
 import java.util.logging.Level;

 @Log4j2
 @Component
 public class CronJobTrigger {

     @Value("${app_settings.cron_job_config_file}")
     private String CRON_JOB_CONFIG_FILE;
    
     // Note that no matter the cron schedule on the jobs within jobs-config, the maximumum frequency with
     // which they can be called is down to this cron schedule.
     private final String cronTriggerSchedule = "0 */50 * * * *";

     @Autowired
     private CronJobHandler cronJobHandler;

     @Autowired
     private ServiceBusBatchSender<ScraperRequest> serviceBusBatchSender;

     /*
      * This Azure Function is scheduled to queue scraping jobs at various intervals. These are posted to the same
      * queue as apiJobTrigger Function.
      */
     @FunctionName("cronJobProducer")
     public void execute(@TimerTrigger(name = "keepAliveTrigger", schedule = cronTriggerSchedule) String timerInfo, ExecutionContext context) throws StreamReadException, DatabindException, IOException, InterruptedException {
         String correlationId = UUID.randomUUID().toString();
         try {
             ThreadContext.put("correlationId", correlationId);

             CronJobs cronJobs = cronJobHandler.readCronJobs(CRON_JOB_CONFIG_FILE);
             List<CronJob> filteredCronJobs = cronJobHandler.filterCronJobs(LocalDateTime.now(), cronTriggerSchedule, cronJobs);
    
             for(CronJob filteredCronJob : filteredCronJobs) {
                 List<ScraperRequest> scraperRequests = cronJobHandler.createScraperRequests(filteredCronJob);
    
                 if (scraperRequests.size() > 0)
                     serviceBusBatchSender.sendBatch(scraperRequests, correlationId);
             }
    
         } catch (Exception e) {
             log.error("An exception was thrown when trying to create ScraperRequests within the cronJobProducer.", e);
             context.getLogger().log(Level.SEVERE, "An exception was thrown when trying to create ScraperRequests within the cronJobProducer. CorrelationId " + correlationId + " Exception: " + e.getMessage(), e);
         } finally {
            ThreadContext.remove("correlationId");
         }
        
     }
 }