package com.nomad.job_orchestrator.functions.cron_job_producer;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.nomad.data_library.config.Neo4jConfig;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.microsoft.applicationinsights.TelemetryClient;
import com.nomad.job_orchestrator.config.ServiceBusConnector;
import com.nomad.job_orchestrator.repositories.Neo4jCityRepository;
import com.nomad.job_orchestrator.repositories.Neo4jCountryRepository;
import com.nomad.data_library.Neo4jTestGenerator;
import com.nomad.data_library.TestConfig;
import com.nomad.data_library.domain.neo4j.Neo4jCity;
import com.nomad.data_library.domain.neo4j.Neo4jCountry;
import com.nomad.data_library.exceptions.Neo4jGenericException;

import com.nomad.scraping_library.connectors.ServiceBusBatchSender;
import com.nomad.scraping_library.domain.ScraperRequest;
import com.nomad.scraping_library.domain.ScraperRequestType;

import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;

@Log4j2
@DataNeo4jTest
@ActiveProfiles("maven")
@Import({com.nomad.data_library.Neo4jTestConfiguration.class})
@Transactional  // Ensures test changes do not persist
public class CronJobHandlerTest {

    @MockitoBean
    private ServiceBusBatchSender<ScraperRequest> serviceBusBatchSender;
    @MockitoBean
    private ServiceBusSenderClient serviceBusSenderClient;

    @Autowired
    private Neo4jCityRepository cityRepository;

    @Autowired
    private Neo4jCountryRepository countryRepository;

    @Autowired
    private CronJobHandler cronJobHandler;

    @Test
    public void jobsConfigCloud_isValid() throws StreamReadException, DatabindException, IOException {

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
        Neo4jCountry countryA = Neo4jTestGenerator.neo4jCountryNoCities("CountryA");
        Neo4jCountry countryB = Neo4jTestGenerator.neo4jCountryNoCities("CountryB");
        Neo4jCity city1 = Neo4jTestGenerator.neo4jCityNoRoutes("CityA", countryA);
        Neo4jCity city2 = Neo4jTestGenerator.neo4jCityNoRoutes("CityB", countryA);
        Neo4jCity city3 = Neo4jTestGenerator.neo4jCityNoRoutes("CityC", countryA);
        Neo4jCity city4 = Neo4jTestGenerator.neo4jCityNoRoutes("CityD", countryB);

        countryRepository.createCountry(countryA);
        countryRepository.createCountry(countryB);

        cityRepository.createCity(city1);
        cityRepository.createCity(city2);
        cityRepository.createCity(city3);
        cityRepository.createCity(city4);
        
        String cronJobSchedule = "0 */10 * * * *"; 
        CronJob routeDiscoveryCronJob = new CronJob("test-route", ScraperRequestType.ROUTE_DISCOVERY, cronJobSchedule, "CountryA", true, LocalDate.of(2025, 10, 3));

        List<ScraperRequest> scraperRequests = cronJobHandler.createScraperRequests(routeDiscoveryCronJob);

        List<List<String>> allCityPairs = scraperRequests.stream().map(request -> List.of(request.getSourceCity().name(), request.getTargetCity().name())).toList();
        List<String> allCityNames = allCityPairs.stream().flatMap(list -> list.stream()).toList();
        assertThat(allCityPairs.size()).isEqualTo(6);
        assertThat(allCityNames).containsOnly("CityA", "CityB", "CityC");
    }
}
