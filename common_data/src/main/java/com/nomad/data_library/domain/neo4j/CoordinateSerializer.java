package com.nomad.data_library.domain.neo4j;

import com.fasterxml.jackson.databind.*;
import org.springframework.data.neo4j.types.GeographicPoint3d;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/*
Weirdly Neo4j is fine deserializing (reading from db and creating a GeographicPoint3d BUT it doesnt like being passed a GeographicPoint3d when
writing to the DB. Therefore here we convert the 3d point into a map. This is called by mapifyCity().
 */
public class CoordinateSerializer extends JsonSerializer<GeographicPoint3d> {

    public CoordinateSerializer() {
    }

    @Override
    public void serialize(GeographicPoint3d value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }

        // Create a proper Neo4j point value
        Map<String, Object> pointMap = new HashMap<>();
        pointMap.put("x", value.getLongitude());
        pointMap.put("y", value.getLatitude());
        pointMap.put("z", value.getHeight());
        pointMap.put("srid", value.getSrid());

        // Write the point value directly
        gen.writeObject(pointMap);
    }
}


