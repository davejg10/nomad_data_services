package com.nomad.data_library.domain.sql;

import java.util.UUID;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "route_popularity")
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RoutePopularity {
    
    @EmbeddedId // Specifies that the primary key is embedded from another class
    @EqualsAndHashCode.Include
    private RoutePopularityId id;

    private double popularity = 0.0;

    public RoutePopularity() {}

    public RoutePopularity(RoutePopularityId popularityId) {
        this.id = popularityId;
    }

    public RoutePopularity(UUID sourceCityId, UUID targetCityId) {
        this.id = new RoutePopularityId(sourceCityId, targetCityId);
    }

    public RoutePopularity(UUID sourceCityId, UUID targetCityId, double popularity) {
        this.id = new RoutePopularityId(sourceCityId, targetCityId);
        this.popularity = popularity;
    }
}
