package com.nomad.admin_api.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomad.admin_api.config.AppConfig;
import com.nomad.data_library.domain.CityCriteria;
import com.nomad.data_library.domain.CityMetric;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.neo4j.types.GeographicPoint3d;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {AppConfig.class})
public class CityDTOTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void deserialize() throws Exception {
        Set<CityMetric> newCityMetrics = Set.of(
                new CityMetric(CityCriteria.SAILING, 4.0),
                new CityMetric(CityCriteria.FOOD, 8.0),
                new CityMetric(CityCriteria.NIGHTLIFE, 4.0));
        CityDTO jsonAsObject = new CityDTO(
                "CityA",
                "Short description",
                "Example description",
                "url:xx",
                new GeographicPoint3d(13.7563, 100.5018, 0.0),
                newCityMetrics,
                "CountryA");

        String requestBody = Files.readString(Path.of("src/main/java/com/nomad/admin_api/functions/create_city/payload.json"));
        CityDTO deserialized = objectMapper.readValue(requestBody, CityDTO.class);

        assertThat(deserialized).isEqualTo(jsonAsObject);
    }
}
