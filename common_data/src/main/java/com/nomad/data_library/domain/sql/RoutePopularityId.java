package com.nomad.data_library.domain.sql;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;

// This class allows us to have our composite primary key in the RoutePopularity table.
@Embeddable // Marks this class as embeddable within another entity
@Getter
@EqualsAndHashCode
public class RoutePopularityId implements Serializable {

    // Serialzable because JPA requires it (or strongly recommends it as best practice) for managing entity identity
    private static final long serialVersionUID = 1L;

    @Column(name = "source_city_id", nullable = false)
    private UUID sourceCityId;

    @Column(name = "target_city_id", nullable = false)
    private UUID targetCityId;

    public RoutePopularityId() {}

    public RoutePopularityId(UUID sourceCityId, UUID targetCityId) {
        this.sourceCityId = sourceCityId;
        this.targetCityId = targetCityId;
    }
}