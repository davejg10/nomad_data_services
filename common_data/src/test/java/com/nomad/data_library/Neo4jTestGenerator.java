package com.nomad.data_library;

import com.nomad.data_library.domain.TransportType;
import com.nomad.data_library.domain.neo4j.Neo4jCity;
import com.nomad.data_library.domain.neo4j.Neo4jCountry;
import com.nomad.data_library.domain.neo4j.Neo4jRoute;
import com.nomad.data_library.domain.sql.SqlCity;
import com.nomad.data_library.domain.sql.SqlCountry;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.data.neo4j.types.GeographicPoint3d;

public class Neo4jTestGenerator {
    
    public static Neo4jRoute neo4jRoute(Neo4jCity targetCity) {
        double min = 0.0;
        double popularity = ThreadLocalRandom.current().nextDouble(min, 10.0);
        double time = ThreadLocalRandom.current().nextDouble(min, 20.0);
        double cost = ThreadLocalRandom.current().nextDouble(min, 100.0);
        TransportType[] allTransports = TransportType.values();
        TransportType randomTransportType = allTransports[ThreadLocalRandom.current().nextInt(allTransports.length)];

        return new Neo4jRoute(UUID.randomUUID().toString(), targetCity, popularity, time, cost, randomTransportType);
    }

    public static GeographicPoint3d generateCoords() {
        double latitude = ThreadLocalRandom.current().nextDouble(-90, 90);
        double longitude = ThreadLocalRandom.current().nextDouble(-180, 180);
        double height = 0.0;
        return new GeographicPoint3d(latitude, longitude, height);
    }

    public static Neo4jCountry neo4jCountryNoCities(String countryName) {

        return new Neo4jCountry(UUID.randomUUID().toString(), countryName, "a country short desscription", "url:blob", Set.of());
    }

    public static Neo4jCountry neo4jCountryFromSql(SqlCountry country) {

        return new Neo4jCountry(country.getId().toString(), country.getName(), "a country short desscription", "url:blob", Set.of());
    }

    public static Neo4jCity neo4jCityNoRoutes(String cityName, Neo4jCountry country) {

        return new Neo4jCity(UUID.randomUUID().toString(), cityName, "some short description", "url:blob", generateCoords(), GenericTestGenerator.cityMetrics(), Set.of(), country);
    }

    public static Neo4jCity neo4jCityNoRoutesWithId(String id, String cityName, Neo4jCountry country) {

        return new Neo4jCity(id, cityName, "some short description", "url:blob", generateCoords(), GenericTestGenerator.cityMetrics(), Set.of(), country);
    }

    public static Neo4jCity neo4jCityFromSql(SqlCity city, SqlCountry country) {

        return new Neo4jCity(city.getId().toString(), city.getName(), "some short description", "url:blob", generateCoords(), city.getCityMetrics(), Set.of(), neo4jCountryFromSql(country));
    }
}
