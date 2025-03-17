package com.nomad.admin_api.domain;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.neo4j.types.GeographicPoint3d;

import java.io.IOException;

/*
This is not a Neo4j deserializer. This is simply used when converting from jsonPayload in create_city trigger
 */
public class CoordinateDeserializer extends JsonDeserializer<GeographicPoint3d> {

    public CoordinateDeserializer() {}

    @Override
    public GeographicPoint3d deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        // Read JSON as a tree
        JsonNode node = parser.getCodec().readTree(parser);

        // Extract fields from JSON
        double longitude = node.get("longitude").asDouble();
        double latitude = node.get("latitude").asDouble();
        double height = node.has("height") ? node.get("height").asDouble() : 0.0;

        // Return a Neo4j Point object
        return new GeographicPoint3d(latitude, longitude, height);
    }
}