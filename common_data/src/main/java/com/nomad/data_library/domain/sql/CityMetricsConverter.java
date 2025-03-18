package com.nomad.data_library.domain.sql;

import java.io.IOException;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomad.data_library.domain.CityMetric;
import com.nomad.data_library.domain.CityMetrics;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class CityMetricsConverter implements AttributeConverter<Set<CityMetric>, String> {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Set<CityMetric> cityMetrics) {
        try {
            return cityMetrics == null ? null : objectMapper.writeValueAsString(cityMetrics);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting CityMetrics to JSON", e);
        }
    }

    @Override
    public Set<CityMetric> convertToEntityAttribute(String dbData) {
        try {
            JsonNode root = objectMapper.readTree(dbData);
            return dbData == null ? null : objectMapper.readValue(root.toString(), new TypeReference<Set<CityMetric>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Error converting JSON to CityMetrics", e);
        }
    }
}