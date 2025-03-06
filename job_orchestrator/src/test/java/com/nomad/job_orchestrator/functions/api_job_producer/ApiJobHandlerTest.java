package com.nomad.job_orchestrator.functions.api_job_producer;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.AutoClose;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomad.library.config.AppConfig;
import com.nomad.library.messages.CityDTO;
import com.nomad.library.messages.ScraperRequest;
import com.nomad.library.messages.ScraperRequestType;

import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootTest(classes = {AppConfig.class, ApiJobHandler.class})
public class ApiJobHandlerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApiJobHandler apiJobHandler;

    @Test
    public void apply_shouldReturnScraperRequest_whenValidPayloadPassed() throws IOException {
        String requestBody = Files.readString(Path.of("src/main/java/com/nomad/job_orchestrator/functions/api_job_producer/payload-in.json"));
        Optional<ScraperRequest> scraperRequest = apiJobHandler.apply(requestBody);

        CityDTO sourceCity = new CityDTO("d637fdf7-d4d8-4bbb-a0d7-218b87d86442", "CityA");
        CityDTO targetCity = new CityDTO("9ef0a8a7-fab9-4c7d-8040-194ba1e3a726", "CityB");
        ScraperRequest expected = new ScraperRequest("httpTrigger", ScraperRequestType.ROUTE_DISCOVERY, sourceCity, targetCity, LocalDate.of(2025, 03, 10));

        assertThat(scraperRequest.get()).isEqualTo(expected);
    }

    @Test
    public void apply_shouldReturnEmptyOptional_whenMappingExceptionOccurs() throws IOException {
        String requestBody = "";
        Optional<ScraperRequest> scraperRequest = apiJobHandler.apply(requestBody);

        assertThat(scraperRequest.isPresent()).isFalse();
    }
    
}
