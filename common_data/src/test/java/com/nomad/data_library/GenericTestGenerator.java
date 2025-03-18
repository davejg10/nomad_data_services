package com.nomad.data_library;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import com.nomad.data_library.domain.CityCriteria;
import com.nomad.data_library.domain.CityMetric;
import com.nomad.data_library.domain.CityMetrics;

public class GenericTestGenerator {

    public static Set<CityMetric> cityMetrics() {
        double min = 0.0;
        double max = 10.0;

        Set<CityMetric> cityMetrics = new HashSet<CityMetric>();
        for (CityCriteria criteria : CityCriteria.values()) {
            double metric = ThreadLocalRandom.current().nextDouble(min, max);
            cityMetrics.add(new CityMetric(criteria, metric));
        }

        return cityMetrics;
    }
}
