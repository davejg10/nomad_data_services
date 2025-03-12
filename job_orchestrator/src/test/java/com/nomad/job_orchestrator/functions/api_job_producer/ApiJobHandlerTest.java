package com.nomad.job_orchestrator.functions.api_job_producer;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.nomad.scraping_library.domain.CityDTO;
import com.nomad.scraping_library.domain.ScraperRequest;
import com.nomad.scraping_library.domain.ScraperRequestType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomad.scraping_library.config.AppConfig;

import lombok.extern.log4j.Log4j2;

@Log4j2
@ActiveProfiles("maven")
@SpringBootTest(classes = {AppConfig.class, ApiJobHandler.class})
public class ApiJobHandlerTest {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private ApiJobHandler apiJobHandler;

    CityDTO sourceCity = new CityDTO("d637fdf7-d4d8-4bbb-a0d7-218b87d86442", "CityA");
    CityDTO targetCity = new CityDTO("9ef0a8a7-fab9-4c7d-8040-194ba1e3a726", "CityB");
    LocalDate date = LocalDate.parse("2025-03-10");
    HttpScraperRequest requestAsObject = new HttpScraperRequest(sourceCity, targetCity, date);

    @Test
    public void deserialize() throws Exception {
        String requestBody = Files.readString(Path.of("src/main/java/com/nomad/job_orchestrator/functions/api_job_producer/payload-in.json"));
        HttpScraperRequest deserialized = objectMapper.readValue(requestBody, HttpScraperRequest.class);

        assertThat(deserialized).isEqualTo(requestAsObject);
    }

    @Test
    public void apply_shouldReturnScraperRequest_whenValidPayloadPassed() throws IOException {
        ScraperRequest scraperRequest = apiJobHandler.apply(requestAsObject);

        CityDTO sourceCity = new CityDTO("d637fdf7-d4d8-4bbb-a0d7-218b87d86442", "CityA");
        CityDTO targetCity = new CityDTO("9ef0a8a7-fab9-4c7d-8040-194ba1e3a726", "CityB");
        ScraperRequest expected = new ScraperRequest("httpTrigger", ScraperRequestType.ROUTE_DISCOVERY, sourceCity, targetCity, LocalDate.of(2025, 03, 10));

        assertThat(scraperRequest).isEqualTo(expected);
    }
    
}
