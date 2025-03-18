package com.nomad.data_library.repositories;

import com.nomad.data_library.domain.CityCriteria;
import com.nomad.data_library.domain.CityMetric;
import com.nomad.data_library.domain.TransportType;
import com.nomad.data_library.domain.neo4j.Neo4jCity;
import com.nomad.data_library.domain.neo4j.Neo4jCountry;
import com.nomad.data_library.domain.neo4j.Neo4jRoute;
import com.nomad.data_library.exceptions.Neo4jMissingKeyException;
import lombok.Getter;
import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.MapAccessor;
import org.neo4j.driver.types.TypeSystem;
import org.springframework.data.neo4j.core.mapping.Neo4jMappingContext;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

public class Neo4jCityMappers {

    @Getter public final BiFunction<TypeSystem, MapAccessor, Neo4jCity> cityMapper;
    @Getter public final BiFunction<TypeSystem, MapAccessor, Neo4jCountry> countryMapper;

    public Neo4jCityMappers(Neo4jMappingContext schema) {
        this.cityMapper = schema.getRequiredMappingFunctionFor(Neo4jCity.class);
        this.countryMapper = schema.getRequiredMappingFunctionFor(Neo4jCountry.class);
    }

    /*
    Returns a city with all ROUTE, OF_COUNTRY, HAS_METRIC relationships mapped
     */
    public BiFunction<TypeSystem, Record, Neo4jCity> cityWithAllRelationshipsMapper() {
        return (typeSystem, record) -> {
            if (!record.containsKey("city") || !record.containsKey("country") || !record.containsKey("cityMetrics") || !record.containsKey("routes") || !record.containsKey("targetCities") ||
                    !record.containsKey("targetCityCountries") || !record.containsKey("cityMetrics") || !record.containsKey("targetCityMetrics"))
            {
                throw new Neo4jMissingKeyException("cityWithAllRelationships");
            }

            Neo4jCity fetchedCity = cityMapper.apply(typeSystem, record.get("city").asNode());
            Neo4jCountry fetchedCitiesCountry = countryMapper.apply(typeSystem, record.get("country").asNode());
            fetchedCity = fetchedCity.withCityMetrics(mapCityMetrics(record.get("cityMetrics")));

            Set<Neo4jRoute> routes = Set.of();
            if (!record.get("routes").asList().isEmpty()) {
                ArrayList<Neo4jCity> targetCities = mapTargetCityCountries(typeSystem, record.get("targetCities"), record.get("targetCityCountries"));
                targetCities = mapTargetCityMetrics(typeSystem, record.get("targetCityMetrics"), targetCities);
                routes = mapRoutes(record.get("routes"), targetCities);
            }
            return new Neo4jCity(fetchedCity.getId(), fetchedCity.getName(), fetchedCity.getShortDescription(), fetchedCity.getPrimaryBlobUrl(), fetchedCity.getCoordinate(), fetchedCity.getCityMetrics(), routes, fetchedCitiesCountry);
        };
    }

    /*
    Returns a city with all OF_COUNTRY, HAS_METRIC relationships mapped
     */
    public BiFunction<TypeSystem, Record, Neo4jCity> cityWithNoRoutesMapper() {
        return (typeSystem, record) -> {
            if (!record.containsKey("city") || !record.containsKey("country") || !record.containsKey("cityMetrics")) {
                throw new Neo4jMissingKeyException("cityWithNoRoutes");
            }
            Neo4jCity fetchedCity = cityMapper.apply(typeSystem, record.get("city").asNode());
            Neo4jCountry fetchedCitiesCountry = countryMapper.apply(typeSystem, record.get("country").asNode());
            fetchedCity = fetchedCity.withCityMetrics(mapCityMetrics(record.get("cityMetrics")));

            return new Neo4jCity(fetchedCity.getId(), fetchedCity.getName(), fetchedCity.getShortDescription(), fetchedCity.getPrimaryBlobUrl(), fetchedCity.getCoordinate(), fetchedCity.getCityMetrics(), Set.of(), fetchedCitiesCountry);

        };
    }

    private ArrayList<Neo4jCity> mapTargetCityCountries(TypeSystem typeSystem, Value targetCities, Value targetCityCountries) {
        AtomicInteger index = new AtomicInteger(0);
        return new ArrayList<>(targetCities.asList(targetCity -> {
            Neo4jCity city = cityMapper.apply(typeSystem, targetCity.asNode());
            Neo4jCountry country = countryMapper.apply(typeSystem, targetCityCountries.get(index.getAndIncrement()));
            return city.withCountry(country);
        }));
    }

    private ArrayList<Neo4jCity> mapTargetCityMetrics(TypeSystem typeSystem, Value targetCityMetrics, ArrayList<Neo4jCity> targetCities) {
        AtomicInteger index = new AtomicInteger(0);
        return new ArrayList<>(targetCityMetrics.asList(targetCityMetric -> {
            Neo4jCity city = targetCities.get(index.getAndIncrement());
            return city.withCityMetrics(mapCityMetrics(targetCityMetric));
        }));
    }

    private Set<CityMetric> mapCityMetrics(Value cityMetrics) {
        return new HashSet<>(cityMetrics.asList(targetCityMetric -> new CityMetric(
                CityCriteria.valueOf(targetCityMetric.get("criteria").asString()),
                targetCityMetric.get("metric").asDouble()
        )));
    }

    private Set<Neo4jRoute> mapRoutes(Value routes, ArrayList<Neo4jCity> targetCities) {
        AtomicInteger index = new AtomicInteger(0);
        return new HashSet<>(routes.asList(route -> new Neo4jRoute(
                    route.get("id").asString(),
                    targetCities.get(index.getAndIncrement()),
                    route.get("popularity").asDouble(),
                    route.get("time").asDouble(),
                    route.get("cost").asDouble(),
                    TransportType.valueOf(route.get("transportType").asString())
        )));
    }
}
