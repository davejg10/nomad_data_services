package com.nomad.data_library.domain.neo4j;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomad.data_library.domain.CityMetric;
import com.nomad.data_library.domain.CityMetrics;

import lombok.extern.log4j.Log4j2;
import org.neo4j.driver.Value;
import org.springframework.data.neo4j.core.convert.Neo4jPersistentPropertyConverter;

import java.util.Map;

// This class is used to convert a Neo4j Value to a CityMetrics object. I.e the reverse of CityMetricsSerializer
// Its automatically called by the cityMapper defined in CityRepository when we call cityMapper.apply(typeSystem, record.get("city").asNode());
@Log4j2
public class CityMetricsDeserializer implements Neo4jPersistentPropertyConverter<CityMetrics> {

    private final ObjectMapper objectMapper;

    public CityMetricsDeserializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Value write(CityMetrics source) { return null; } // CityMetricsSerializer is used instead.

    @Override
    public CityMetrics read(Value source) {
        try {
            String json = source.asString();
            Map<String, CityMetric> metricsMap = objectMapper.readValue(json,
                    objectMapper.getTypeFactory().constructMapType(Map.class, String.class, CityMetric.class));

            return new CityMetrics(metricsMap.get("sailing"), metricsMap.get("food"), metricsMap.get("nightlife"));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
