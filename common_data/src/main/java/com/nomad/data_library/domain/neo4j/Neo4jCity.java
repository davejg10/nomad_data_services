package com.nomad.data_library.domain.neo4j;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.neo4j.core.convert.ConvertWith;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.nomad.data_library.domain.CityMetrics;
import com.nomad.data_library.domain.TransportType;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Node("City")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Neo4jCity {
    
    @Id
    @Getter private final String id;
    @Getter private final String name;

    @JsonSerialize(using = CityMetricsSerializer.class) // Conversion TO Neo4j.Value (called by mapifyCity())
    @ConvertWith(converterRef = "cityMetricsDeserializer") // Conversion TO CityMetrics (when being read) (requires a bean in context with name cityMetricsDeserializer)
    @Getter private final CityMetrics cityMetrics;

    @Relationship(type = "ROUTE", direction = Relationship.Direction.OUTGOING)
    private final Set<Neo4jRoute> routes;
    
    @Relationship(type = "OF_COUNTRY", direction = Relationship.Direction.OUTGOING)
    @Getter private final Neo4jCountry country;

    @JsonCreator // This is used by Spring Data for object mapping
    public Neo4jCity(String id, String name, CityMetrics cityMetrics, Set<Neo4jRoute> routes, Neo4jCountry country) {
        this.id = id;
        this.name = name;
        this.cityMetrics = cityMetrics;
        this.routes = routes;
        this.country = country;
    }

    public static Neo4jCity of(String name, CityMetrics cityMetrics, Set<Neo4jRoute> routes, Neo4jCountry country) {
        return new Neo4jCity(null, name, cityMetrics, Set.copyOf(routes), country);
    }

    public Neo4jCity withCountry(Neo4jCountry country) {
        return new Neo4jCity(this.id, this.name, this.cityMetrics, this.routes, country);
    }

    // This is used by Neo4j for object mapping
    public Neo4jCity withId(String id) {
        return new Neo4jCity(id, this.name, this.cityMetrics, this.routes, this.country);
    }

    // Ensure mutable field 'routes' remains immutable
    public Set<Neo4jRoute> getRoutes() {
        return new HashSet<>(routes);
    }

    public Neo4jCity addRoute(Neo4jRoute route) {
        return addRoute(route.getTargetCity(), route.getPopularity(), route.getTime(), route.getCost(), route.getTransportType());
    }

    public Neo4jCity addRoute(Neo4jCity targetCity, double popularity, double time, double cost, TransportType transportType) {
        Set<Neo4jRoute> existingRoutes = getRoutes();
        Neo4jRoute routeToAdd = Neo4jRoute.of(targetCity, popularity, time, cost, transportType);
        log.info("Adding route: {}", routeToAdd);

        Optional<Neo4jRoute> route = existingRoutes.stream()
                .filter(r -> Objects.equals(r.getTargetCity().getName(), targetCity.getName()) && r.getTransportType() == transportType)
                .findFirst();

        if (route.isPresent()) {
            Neo4jRoute existingRoute = route.get();
            if (!Objects.equals(existingRoute.getPopularity(), popularity) || !Objects.equals(existingRoute.getTime(), time) || !Objects.equals(existingRoute.getCost(), cost)) {
                existingRoutes.remove(existingRoute);
                existingRoutes.add(routeToAdd);
            }
        } else {
            existingRoutes.add(routeToAdd);
        }
        return new Neo4jCity(this.id, this.name, this.cityMetrics, existingRoutes, this.country);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Neo4jCity city = (Neo4jCity) o;
        return Objects.equals(id, city.id) && Objects.equals(name, city.name) && Objects.equals(cityMetrics, city.cityMetrics) && city.getRoutes().containsAll(routes) && routes.containsAll(city.getRoutes()) && Objects.equals(country, city.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, cityMetrics, routes, country);
    }

    @Override
    public String toString() {
        return "Neo4jCity{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", cityMetrics=" + cityMetrics +
                ", routes=" + routes +
                ", country=" + country +
                '}';
    }
}
