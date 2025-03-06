package com.nomad.library.json;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.nomad.library.config.AppConfig;
import com.nomad.library.messages.CityDTO;
import com.nomad.library.messages.ScraperRequest;
import com.nomad.library.messages.ScraperRequestType;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootTest(classes = {AppConfig.class})
public class ScraperRequestJsonTest {
    
    @Autowired
    ObjectMapper objectMapper;

    LocalDate futureDate = LocalDate.now().plusDays(2);

    CityDTO sourceCity = new CityDTO("d637fdf7-d4d8-4bbb-a0d7-218b87d86442", "CityA");
    CityDTO targetCity = new CityDTO("9ef0a8a7-fab9-4c7d-8040-194ba1e3a726", "CityB");
    ScraperRequest scraperRequestObject = new ScraperRequest("httpTrigger", ScraperRequestType.ROUTE_DISCOVERY, sourceCity, targetCity, futureDate);

    String scraperRequestJson = String.format("""
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
                "searchDate": "%s"
            }
            """, futureDate).replaceAll("\\s+", "");

    @Test
    public void serialize() throws Exception {
        String serialized = objectMapper.writeValueAsString(scraperRequestObject);
        assertThat(serialized).isEqualTo(scraperRequestJson);
    }

    @Test
    public void deserialize() throws Exception {
        ScraperRequest deserialized = objectMapper.readValue(scraperRequestJson, ScraperRequest.class);

        assertThat(deserialized).isEqualTo(scraperRequestObject);
    }
}
