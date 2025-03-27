package com.nomad.data_library.domain.neo4j;

import com.nomad.common_utils.domain.TransportType;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.neo4j.core.schema.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Objects;

@Getter
@RelationshipProperties
@Log4j2
public class Neo4jRoute {

    @RelationshipId
    private final String id;

    private final double popularity;
    private final Duration averageDuration;
    private final BigDecimal averageCost;
    private final TransportType transportType;

    @TargetNode
    private final Neo4jCity targetCity;

    // This factory is used by us
    public static Neo4jRoute of(Neo4jCity targetCity, double popularity, Duration averageDuration, BigDecimal averageCost, TransportType transportType) {
        return new Neo4jRoute(null, targetCity, popularity, averageDuration, averageCost, transportType);
    }

    // This is used by Spring data for object mapping
    public Neo4jRoute(String id, Neo4jCity targetCity, double popularity, Duration averageDuration, BigDecimal averageCost, TransportType transportType) {
        this.id = id;
        this.targetCity = targetCity;
        this.popularity = popularity;
        this.averageDuration = averageDuration;
        this.averageCost = averageCost;
        this.transportType = transportType;
    }

    // This is used by Neo4j for object mapping
    public Neo4jRoute withId(String id) {
        return new Neo4jRoute(id, this.targetCity, this.popularity, this.averageDuration, this.averageCost, this.transportType);
    }

    @Override
    public String toString() {
        return "Neo4jRoute{" +
                "id=" + id +
                ", popularity=" + popularity +
                ", averageDuration=" + averageDuration +
                ", averageCost=" + averageCost +
                ", transportType=" + transportType +
                ", targetCity=" + targetCity +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Neo4jRoute route = (Neo4jRoute) o;
        return Objects.equals(id, route.id) && popularity == route.popularity && averageDuration.compareTo(route.averageDuration) == 0 && averageCost.compareTo(route.averageCost) == 0 && transportType == route.transportType && Objects.equals(targetCity.getName(), route.getTargetCity().getName()) && Objects.equals(targetCity.getId(), route.getTargetCity().getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, popularity, averageDuration, averageCost, transportType, targetCity.getName(), targetCity.getId());
    }
}
