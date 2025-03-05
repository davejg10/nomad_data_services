package com.nomad.library.domain.neo4j;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.nomad.library.domain.CityMetrics;

import lombok.extern.log4j.Log4j2;

import java.io.IOException;

//This class is used to convert the CityMetrics object to a JSON string so it can be stored as a single Neo4j value
// (rather than a map of maps which object mapper would usually do)
@Log4j2
public class CityMetricsSerializer extends JsonSerializer<CityMetrics> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public CityMetricsSerializer() {
    }

    @Override
    public void serialize(CityMetrics value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(OBJECT_MAPPER.writeValueAsString(value));
    }
}