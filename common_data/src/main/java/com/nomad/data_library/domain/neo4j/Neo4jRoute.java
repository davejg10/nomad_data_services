package com.nomad.data_library.domain.neo4j;

import com.nomad.data_library.domain.TransportType;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.neo4j.core.schema.*;

import java.util.Objects;

@Getter
@RelationshipProperties
@Log4j2
public class Neo4jRoute {

    @RelationshipId
    private final String id;

    private final double popularity;
    private final double time;
    private final double cost;
    private final TransportType transportType;

    @TargetNode
    private final Neo4jCity targetCity;

    // This factory is used by us
    public static Neo4jRoute of(Neo4jCity targetCity, double popularity, double time, double cost, TransportType transportType) {
        return new Neo4jRoute(null, targetCity, popularity, time, cost, transportType);
    }

    // This is used by Spring data for object mapping
    public Neo4jRoute(String id, Neo4jCity targetCity, double popularity, double time, double cost, TransportType transportType) {
        this.id = id;
        this.targetCity = targetCity;
        this.popularity = popularity;
        this.time = time;
        this.cost = cost;
        this.transportType = transportType;
    }

    // This is used by Neo4j for object mapping
    public Neo4jRoute withId(String id) {
        return new Neo4jRoute(id, this.targetCity, this.popularity, this.time, this.cost, this.transportType);
    }

    @Override
    public String toString() {
        return "Neo4jRoute{" +
                "id=" + id +
                ", popularity=" + popularity +
                ", time=" + time +
                ", cost=" + cost +
                ", transportType=" + transportType +
                ", targetCity=" + targetCity +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Neo4jRoute route = (Neo4jRoute) o;
        return Objects.equals(id, route.id) && popularity == route.popularity && time == route.time && cost == route.cost && transportType == route.transportType && Objects.equals(targetCity.getName(), route.getTargetCity().getName()) && Objects.equals(targetCity.getId(), route.getTargetCity().getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, popularity, time, cost, transportType, targetCity.getName(), targetCity.getId());
    }
}
