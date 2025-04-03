package com.nomad.data_library.domain.neo4j;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.nomad.data_library.domain.CityMetric;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.types.GeographicPoint3d;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.nomad.common_utils.domain.TransportType;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Node("City")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Neo4jCity {
    
    @Id
    @Getter private final String id;
    @Getter private final String name;
    @Getter private final String shortDescription;
    @Getter private final String primaryBlobUrl;

    @JsonSerialize(using = CoordinateSerializer.class) // Converts GeographicPoint3d into a map that Neo4js `point()` Cypher function can use
    @Getter private final GeographicPoint3d coordinate;

    @Relationship(type = "HAS_METRIC", direction = Relationship.Direction.OUTGOING)
    @Getter private final Set<CityMetric> cityMetrics;

    @Relationship(type = "ROUTE", direction = Relationship.Direction.OUTGOING)
    private final Set<Neo4jRoute> routes;
    
    @Relationship(type = "OF_COUNTRY", direction = Relationship.Direction.OUTGOING)
    @Getter private final Neo4jCountry country;

    @JsonCreator // This is used by Spring Data for object mapping
    public Neo4jCity(String id, String name, String shortDescription, String primaryBlobUrl, GeographicPoint3d coordinate, Set<CityMetric> cityMetrics, Set<Neo4jRoute> routes, Neo4jCountry country) {
        this.id = id;
        this.name = name;
        this.shortDescription = shortDescription;
        this.primaryBlobUrl = primaryBlobUrl;
        this.coordinate = coordinate;
        this.cityMetrics = cityMetrics;
        this.routes = routes;
        this.country = country;
    }

    public static Neo4jCity of(String name, String shortDescription, String primaryBlobUrl, GeographicPoint3d coordinate, Set<CityMetric> cityMetrics, Set<Neo4jRoute> routes, Neo4jCountry country) {
        return new Neo4jCity(null, name, shortDescription, primaryBlobUrl, coordinate, cityMetrics, Set.copyOf(routes), country);
    }

    public Neo4jCity withCountry(Neo4jCountry country) {
        return new Neo4jCity(this.id, this.name, this.shortDescription, this.primaryBlobUrl, this.coordinate, this.cityMetrics, this.routes, country);
    }

    // This is used by Neo4j for object mapping
    public Neo4jCity withId(String id) {
        return new Neo4jCity(id, this.name, this.shortDescription, this.primaryBlobUrl, this.coordinate, this.cityMetrics, this.routes, this.country);
    }

    public Neo4jCity withCityMetrics(Set<CityMetric> cityMetrics) {
        return new Neo4jCity(this.id, this.name, this.shortDescription, this.primaryBlobUrl, this.coordinate, new HashSet<>(cityMetrics), this.routes, this.country);
    }

    // Ensure mutable field 'routes' remains immutable
    public Set<Neo4jRoute> getRoutes() {
        return new HashSet<>(routes);
    }

    public Neo4jCity addRoute(Neo4jRoute route) {
        return addRoute(route.getId(), route.getTargetCity(), route.getPopularity(), route.getAverageDuration(), route.getAverageCost(), route.getTransportType());
    }

    public Neo4jCity addRoute(String id, Neo4jCity targetCity, double popularity, Duration averageDuration, BigDecimal averageCost, TransportType transportType) {
        Set<Neo4jRoute> existingRoutes = getRoutes();
        Neo4jRoute routeToAdd = new Neo4jRoute(id, targetCity, popularity, averageDuration, averageCost, transportType);
        log.info("Adding route: {}", routeToAdd);

        Optional<Neo4jRoute> route = existingRoutes.stream()
                .filter(r -> Objects.equals(r.getTargetCity().getName(), targetCity.getName()) && r.getTransportType() == transportType)
                .findFirst();

        if (route.isPresent()) {
            Neo4jRoute existingRoute = route.get();
            if (!Objects.equals(existingRoute.getPopularity(), popularity) || !Objects.equals(existingRoute.getAverageDuration(), averageDuration) || !Objects.equals(existingRoute.getAverageCost(), averageCost)) {
                existingRoutes.remove(existingRoute);
                existingRoutes.add(routeToAdd);
            }
        } else {
            existingRoutes.add(routeToAdd);
        }
        return new Neo4jCity(this.id, this.name, this.shortDescription, this.primaryBlobUrl, this.coordinate, this.cityMetrics, existingRoutes, this.country);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Neo4jCity city = (Neo4jCity) o;
        return Objects.equals(id, city.id) && Objects.equals(name, city.name) && Objects.equals(shortDescription, city.shortDescription) && Objects.equals(coordinate, city.coordinate) && cityMetrics.containsAll(city.cityMetrics) && city.cityMetrics.containsAll(cityMetrics) && city.getRoutes().containsAll(routes) && routes.containsAll(city.getRoutes()) && Objects.equals(country, city.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, shortDescription, primaryBlobUrl, coordinate, cityMetrics, routes, country);
    }

    @Override
    public String toString() {
        return "Neo4jCity{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", shortDescription='" + shortDescription + '\'' +
                ", primaryBlobUrl='" + primaryBlobUrl + '\'' +
                ", coordinate='" + coordinate + '\'' +
                ", cityMetrics=" + cityMetrics +
                ", routes=" + routes +
                ", country=" + country +
                '}';
    }
}
