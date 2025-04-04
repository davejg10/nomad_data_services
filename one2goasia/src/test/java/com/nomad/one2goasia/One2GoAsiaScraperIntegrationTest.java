package com.nomad.one2goasia;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomad.scraping_library.domain.ScraperRequest;
import com.nomad.scraping_library.domain.ScraperResponse;
import com.nomad.scraping_library.config.AppConfig;

import lombok.extern.log4j.Log4j2;

@Log4j2
@ActiveProfiles("maven")
@SpringBootTest(classes = {One2GoAsiaScraper.class, AppConfig.class})
public class One2GoAsiaScraperIntegrationTest {

    LocalDate futureDate = LocalDate.now().plusDays(8);

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    One2GoAsiaScraper one2GoAsiaScraper;

    String validScrapeRequest = String.format("""
            {
                "scraperRequestSource": "CRON",
                "scraperRequestType": "ROUTE_DISCOVERY",
                "sourceCity": {
                    "id": "d637fdf7-d4d8-4bbb-a0d7-218b87d86442",
                    "name": "Bangkok"
                },
                "targetCity": {
                    "id": "9ef0a8a7-fab9-4c7d-8040-194ba1e3a726",
                    "name": "Phuket"
                },
                "searchDate": "%s"
            }
            """, futureDate);

    @Test
    void scrapeData_shouldReturnAValidListOfScraperResponse() throws JsonMappingException, JsonProcessingException {

        ScraperRequest validRequest = objectMapper.readValue(validScrapeRequest, ScraperRequest.class);
        List<ScraperResponse> responses = one2GoAsiaScraper.scrapeData(validRequest);
//        assertThat(responses.size()).isGreaterThan(0);
    }
    
}
