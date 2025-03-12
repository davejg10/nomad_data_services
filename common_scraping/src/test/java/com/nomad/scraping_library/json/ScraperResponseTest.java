package com.nomad.scraping_library.json;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.nomad.scraping_library.config.AppConfig;
import com.nomad.scraping_library.domain.CityDTO;
import com.nomad.scraping_library.domain.RouteDTO;
import com.nomad.scraping_library.domain.ScraperRequestType;
import com.nomad.scraping_library.domain.ScraperResponse;
import com.nomad.scraping_library.domain.TransportType;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootTest(classes = {AppConfig.class})
public class ScraperResponseTest {
    
    @Autowired
    ObjectMapper objectMapper;

    LocalDate futureDate = LocalDate.now().plusDays(2);
    LocalDateTime depart = LocalDateTime.of(futureDate, LocalTime.parse("10:00:01"));
    LocalDateTime arrival = LocalDateTime.of(futureDate, LocalTime.parse("14:00:01"));


    CityDTO sourceCity = new CityDTO("d637fdf7-d4d8-4bbb-a0d7-218b87d86442", "CityA");
    CityDTO targetCity = new CityDTO("9ef0a8a7-fab9-4c7d-8040-194ba1e3a726", "CityB");

    RouteDTO routeDTO = new RouteDTO(TransportType.FLIGHT, "Easyjet", depart, arrival, 12.99);
    ScraperResponse scraperResponseObject = new ScraperResponse("httpTrigger", ScraperRequestType.ROUTE_DISCOVERY, sourceCity, targetCity, routeDTO, futureDate);
    String scraperResponseJson = String.format("""
            {
                "scraperRequestSource": "httpTrigger",
                "type": "ROUTE_DISCOVERY",
                "sourceCity": {
                    "id": "d637fdf7-d4d8-4bbb-a0d7-218b87d86442",
                    "name": "CityA"
                },
                "targetCity": {
                    "id": "9ef0a8a7-fab9-4c7d-8040-194ba1e3a726",
                    "name": "CityB"
                },
                "route": {
                    "transportType": "FLIGHT",
                    "operator": "Easyjet",
                    "depart": "%s",
                    "arrival": "%s",
                    "cost": 12.99
                },
                "searchDate": "%s"
            }
            """, depart, arrival, futureDate).replaceAll("\\s+", "");

    @Test
    public void serialize() throws Exception {

        String serialized = objectMapper.writeValueAsString(scraperResponseObject);
        log.info(routeDTO);
        System.out.println(routeDTO);
        assertThat(serialized).isEqualTo(scraperResponseJson);
    }

    @Test
    public void deserialize() throws Exception {

        ScraperResponse deserialized = objectMapper.readValue(scraperResponseJson, ScraperResponse.class);
        String jsonDeserialzied = objectMapper.writeValueAsString(deserialized);
        assertThat(jsonDeserialzied).isEqualTo(scraperResponseJson);
    }
}
