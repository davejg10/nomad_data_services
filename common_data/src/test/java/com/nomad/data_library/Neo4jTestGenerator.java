package com.nomad.data_library;

import com.nomad.common_utils.domain.TransportType;
import com.nomad.data_library.domain.neo4j.Neo4jCity;
import com.nomad.data_library.domain.neo4j.Neo4jCountry;
import com.nomad.data_library.domain.neo4j.Neo4jRoute;
import com.nomad.data_library.domain.sql.SqlCity;
import com.nomad.data_library.domain.sql.SqlCountry;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.data.neo4j.types.GeographicPoint3d;

public class Neo4jTestGenerator {

    public static Duration calculateRandomDuration() {
        return Duration.between(LocalDateTime.now(), LocalDateTime.now().plusHours(ThreadLocalRandom.current().nextInt(0, 10)));
    }

    public static BigDecimal calculateRandomCost() {
        return new BigDecimal(ThreadLocalRandom.current().nextDouble(0.0, 100.0)).setScale(2, RoundingMode.HALF_UP);
    }
    
    public static Neo4jRoute neo4jRoute(Neo4jCity targetCity) {
        double min = 0.0;
        double popularity = 0.0;
        Duration averageDuration = calculateRandomDuration();
        BigDecimal averageCost = calculateRandomCost();
        TransportType[] allTransports = TransportType.values();
        TransportType randomTransportType = allTransports[ThreadLocalRandom.current().nextInt(allTransports.length)];

        return new Neo4jRoute(UUID.randomUUID().toString(), targetCity, popularity, averageDuration, averageCost, randomTransportType);
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
