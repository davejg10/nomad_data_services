package com.nomad.scraping_library.json;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import com.nomad.scraping_library.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.nomad.common_utils.domain.TransportType;
import com.nomad.scraping_library.config.AppConfig;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootTest(classes = {AppConfig.class})
public class ScraperResponseTest {
    
    @Autowired
    ObjectMapper objectMapper;

    LocalDate futureDate = LocalDate.now().plusDays(2);
    LocalDateTime departEasy = LocalDateTime.of(futureDate, LocalTime.parse("10:00:01"));
    LocalDateTime arrivalEasy = LocalDateTime.of(futureDate, LocalTime.parse("14:00:01"));
    LocalDateTime departWizz = LocalDateTime.of(futureDate, LocalTime.parse("11:00:01"));
    LocalDateTime arrivalWizz = LocalDateTime.of(futureDate, LocalTime.parse("13:00:01"));

    CityDTO sourceCity = new CityDTO("d637fdf7-d4d8-4bbb-a0d7-218b87d86442", "CityA");
    CityDTO targetCity = new CityDTO("9ef0a8a7-fab9-4c7d-8040-194ba1e3a726", "CityB");

    RouteDTO routeDTOEasyjet = new RouteDTO(TransportType.FLIGHT, "Easyjet", departEasy, arrivalEasy, new BigDecimal("12.99"), "someurl");
    RouteDTO routeDTOWizz = new RouteDTO(TransportType.FLIGHT, "WizzAir", departWizz, arrivalWizz, new BigDecimal("4.22"), "someurl");

    ScraperResponse scraperResponseObject = new ScraperResponse("httpTrigger", ScraperRequestType.ROUTE_DISCOVERY, ScraperIdentifier.ONE2GOASIA, TransportType.FLIGHT, sourceCity, targetCity, List.of(routeDTOEasyjet, routeDTOWizz), futureDate);
    String scraperResponseJson = String.format("""
            {
                "scraperRequestSource": "httpTrigger",
                "type": "ROUTE_DISCOVERY",
                "scraperIdentifier": "ONE2GOASIA",
                "transportType": "FLIGHT",
                "sourceCity": {
                    "id": "d637fdf7-d4d8-4bbb-a0d7-218b87d86442",
                    "name": "CityA"
                },
                "targetCity": {
                    "id": "9ef0a8a7-fab9-4c7d-8040-194ba1e3a726",
                    "name": "CityB"
                },
                "routes": [
                    {
                        "transportType": "FLIGHT",
                        "operator": "Easyjet",
                        "depart": "%s",
                        "arrival": "%s",
                        "cost": 12.99,
                        "url": "someurl"
                    },
                    {
                        "transportType": "FLIGHT",
                        "operator": "WizzAir",
                        "depart": "%s",
                        "arrival": "%s",
                        "cost": 4.22,
                        "url": "someurl"
                    }
                ],
                "searchDate": "%s"
            }
            """, departEasy, arrivalEasy, departWizz, arrivalWizz, futureDate).replaceAll("\\s+", "");

    @Test
    public void serialize() throws Exception {

        String serialized = objectMapper.writeValueAsString(scraperResponseObject);
        assertThat(serialized).isEqualTo(scraperResponseJson);
    }

    @Test
    public void deserialize() throws Exception {

        ScraperResponse deserialized = objectMapper.readValue(scraperResponseJson, ScraperResponse.class);
        String jsonDeserialzied = objectMapper.writeValueAsString(deserialized);
        assertThat(jsonDeserialzied).isEqualTo(scraperResponseJson);
    }
}
