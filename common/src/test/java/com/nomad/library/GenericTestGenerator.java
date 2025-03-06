package com.nomad.library;

import java.util.concurrent.ThreadLocalRandom;

import com.nomad.library.domain.CityCriteria;
import com.nomad.library.domain.CityMetric;
import com.nomad.library.domain.CityMetrics;

public class GenericTestGenerator {
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
}
