package com.nomad.library.domain.sql;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomad.library.domain.CityMetrics;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class CityMetricsConverter implements AttributeConverter<CityMetrics, String> {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(CityMetrics attribute) {
        try {
            return attribute == null ? null : objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting CityMetrics to JSON", e);
        }
    }

    @Override
    public CityMetrics convertToEntityAttribute(String dbData) {
        try {
            return dbData == null ? null : objectMapper.readValue(dbData, CityMetrics.class);
        } catch (IOException e) {
            throw new RuntimeException("Error converting JSON to CityMetrics", e);
        }
    }
}