package com.nomad.job_orchestrator.functions.cron_job_producer;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import com.nomad.job_orchestrator.domain.CityPair;
import com.nomad.scraping_library.domain.CityDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;

import com.nomad.job_orchestrator.repositories.Neo4jCityRepository;
import com.nomad.data_library.exceptions.Neo4jGenericException;

import com.nomad.scraping_library.domain.ScraperRequest;
import com.nomad.scraping_library.domain.ScraperRequestType;

import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootTest(classes = CronJobHandler.class)
@ActiveProfiles("maven")
public class CronJobHandlerTest {

    @MockitoBean
    private Neo4jCityRepository cityRepository;

    @Autowired
    private CronJobHandler cronJobHandler;

    @Test
    public void jobsConfigCloud_isValid() throws StreamReadException, DatabindException, IOException {
        String id = UUID.randomUUID().toString();
        String cityA = "CityA", cityB = "CityB";

        when(cityRepository.routeDiscoveryGivenCountry("Thailand")).thenReturn(List.of(
                new CityPair(new CityDTO(id, cityA), new CityDTO(id, cityB)),
                new CityPair(new CityDTO(id, cityB), new CityDTO(id, cityA))
        ));

        String jobConfigFileName = "jobs-config-cloud.yml";
        String longTimeCronSchedule = "0 0 0 * * 0"; //every Sunday at midnight

        CronJobs cronJobs = cronJobHandler.readCronJobs(jobConfigFileName);
        
        List<CronJob> filteredCronJobs = cronJobHandler.filterCronJobs(LocalDateTime.now(), longTimeCronSchedule, cronJobs);
        
        for(CronJob filteredCronJob : filteredCronJobs) {
            List<ScraperRequest> scraperRequests = cronJobHandler.createScraperRequests(filteredCronJob);
        }
    }

    @Test
    public void filterCronJobs_shouldRemoveAllInactiveJobs() throws StreamReadException, DatabindException, IOException, ParseException {
        String cronTriggerSchedule = "0 */10 * * * *";

        String cronJobSchedule = "0 */1 * * * *"; // Set this more frequent to crontRigger so the job wont be filtered out for cron reasons 
  

        CronJob cronJobActive = new CronJob("active", ScraperRequestType.ROUTE_DISCOVERY,  cronJobSchedule, "CountryA", true, LocalDate.of(2025, 10, 3));
        CronJob cronJobInactive = new CronJob("inactive", ScraperRequestType.ROUTE_DISCOVERY,  cronJobSchedule, "CountryA", false, LocalDate.of(2025, 10, 3));

        CronJobs cronJobs = new CronJobs(List.of(cronJobActive, cronJobInactive));
       
        List<CronJob> cronJobsList = cronJobHandler.filterCronJobs(LocalDateTime.now(), cronTriggerSchedule, cronJobs);

        assertThat(cronJobsList.size()).isEqualTo(1);
        assertThat(cronJobsList.get(0).id()).isEqualTo("active");
    }

    @Test
    public void filterCronJobs_shouldFilterCronJobs_whenTheirCronScheduleIsNotWithinNextTriggerSchedule() throws StreamReadException, DatabindException, IOException, ParseException {
        LocalDateTime dateTimeAt10AM = LocalDateTime.now().withHour(10).withMinute(0).withSecond(0).withNano(0);

        String cronTriggerSchedule = "0 */10 * * * *"; //every 10 mins

        String cronJobScheduleWithin1 = "0 */5 * * * *"; //every 5mins
        String cronJobScheduleWithin2 = "0 */9 * * * *"; //every 9mins
        String cronJobScheduleOutside = "0 */15 * * * *"; //every 15mins

        CronJob cronJobWithin1 = new CronJob("within1", ScraperRequestType.ROUTE_DISCOVERY,  cronJobScheduleWithin1, "CountryA", true, LocalDate.of(2025, 10, 3));
        CronJob cronJobWithin2 = new CronJob("within2", ScraperRequestType.ROUTE_DISCOVERY,  cronJobScheduleWithin2, "CountryA", true, LocalDate.of(2025, 10, 3));
        CronJob cronJobOutside = new CronJob("within3", ScraperRequestType.ROUTE_DISCOVERY,  cronJobScheduleOutside, "CountryA", true, LocalDate.of(2025, 10, 3));

        CronJobs cronJobs = new CronJobs(List.of(cronJobWithin1, cronJobWithin2, cronJobOutside));
        

        List<CronJob> cronJobsList = cronJobHandler.filterCronJobs(dateTimeAt10AM, cronTriggerSchedule, cronJobs);
        List<String> filtered =  cronJobsList.stream().map(cronJob -> cronJob.id()).toList();
        log.info(cronJobsList);

        assertThat(cronJobsList.size()).isEqualTo(2);
        assertThat(filtered).isEqualTo(List.of("within1", "within2"));
    }

    @Test
    public void createScraperRequests_whenGivenARouteDiscoveryJob_shouldReturnListScraperRequestWithAllPermuationsOfCitiesForGivenCountry() throws StreamReadException, DatabindException, IOException, ParseException, Neo4jGenericException {
        String countryName = "CountryA";
        String id = UUID.randomUUID().toString();
        String cityA = "CityA", cityB = "CityB", cityC = "CityC";

        when(cityRepository.routeDiscoveryGivenCountry(countryName)).thenReturn(List.of(
                new CityPair(new CityDTO(id, cityA), new CityDTO(id, cityB)),
                new CityPair(new CityDTO(id, cityA), new CityDTO(id, cityC)),
                new CityPair(new CityDTO(id, cityB), new CityDTO(id, cityA)),
                new CityPair(new CityDTO(id, cityB), new CityDTO(id, cityC)),
                new CityPair(new CityDTO(id, cityC), new CityDTO(id, cityA)),
                new CityPair(new CityDTO(id, cityC), new CityDTO(id, cityB))
        ));
        
        String cronJobSchedule = "0 */10 * * * *"; 
        CronJob routeDiscoveryCronJob = new CronJob("test-route", ScraperRequestType.ROUTE_DISCOVERY, cronJobSchedule, countryName, true, LocalDate.of(2025, 10, 3));

        List<ScraperRequest> scraperRequests = cronJobHandler.createScraperRequests(routeDiscoveryCronJob);

        List<List<String>> allCityPairs = scraperRequests.stream().map(request -> List.of(request.getSourceCity().name(), request.getTargetCity().name())).toList();
        List<String> allCityNames = allCityPairs.stream().flatMap(list -> list.stream()).toList();
        assertThat(allCityPairs.size()).isEqualTo(6);
        assertThat(allCityNames).containsOnly("CityA", "CityB", "CityC");
    }
}
