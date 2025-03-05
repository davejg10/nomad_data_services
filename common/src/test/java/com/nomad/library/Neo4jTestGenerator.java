package com.nomad.library;

import com.nomad.library.domain.CityCriteria;
import com.nomad.library.domain.CityMetric;
import com.nomad.library.domain.CityMetrics;
import com.nomad.library.domain.TransportType;
import com.nomad.library.domain.neo4j.Neo4jCity;
import com.nomad.library.domain.neo4j.Neo4jCountry;
import com.nomad.library.domain.neo4j.Neo4jRoute;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class Neo4jTestGenerator {
    
    public static CityMetrics cityMetrics() {
        double min = 0.0;
        double max = 10.0;
        double sailing = ThreadLocalRandom.current().nextDouble(min, max);
        double food = ThreadLocalRandom.current().nextDouble(min, max);
        double nightlife = ThreadLocalRandom.current().nextDouble(min, max);

        CityMetrics cityMetrics = new CityMetrics(
            new CityMetric(CityCriteria.SAILING, sailing),
            new CityMetric(CityCriteria.FOOD, food),
            new CityMetric(CityCriteria.NIGHTLIFE, nightlife)
        );
        return cityMetrics;
    }

    public static Neo4jRoute neo4jRoute(Neo4jCity targetCity) {
        double min = 0.0;
        double popularity = ThreadLocalRandom.current().nextDouble(min, 10.0);
        double time = ThreadLocalRandom.current().nextDouble(min, 20.0);
        double cost = ThreadLocalRandom.current().nextDouble(min, 100.0);
        TransportType[] allTransports = TransportType.values();
        TransportType randomTransportType = allTransports[ThreadLocalRandom.current().nextInt(allTransports.length)];

        return new Neo4jRoute(UUID.randomUUID().toString(), targetCity, popularity, time, cost, randomTransportType);
    }

    public static Neo4jCountry neo4jCountryNoCities(String countryName) {

        return new Neo4jCountry(UUID.randomUUID().toString(), countryName, "some desc", Set.of());
    }

    public static Neo4jCity neo4jCityNoRoutes(String cityName, Neo4jCountry country) {

        return new Neo4jCity(UUID.randomUUID().toString(), cityName, cityMetrics(), Set.of(), country);
    }
}
