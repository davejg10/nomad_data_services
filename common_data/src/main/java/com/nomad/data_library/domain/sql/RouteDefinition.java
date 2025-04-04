package com.nomad.data_library.domain.sql;

import java.util.UUID;

import com.nomad.common_utils.domain.TransportType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Entity
@Table(name = "route_definition")
@Getter
@EqualsAndHashCode
public class RouteDefinition {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "transport_type")
    @Enumerated(EnumType.STRING)
    private TransportType transportType;
    
    @Column(name = "source_city_id")
    private UUID sourceCityId;

    @Column(name = "target_city_id")
    private UUID targetCityId;

    // Used by Hibernate
    public RouteDefinition() {}

    public static RouteDefinition of(TransportType transportType, UUID sourceCityId, UUID targetCityId) {
        return new RouteDefinition(null, transportType, sourceCityId, targetCityId);
    }

    public RouteDefinition(UUID id, TransportType transportType, UUID sourceCityId, UUID targetCityId) {
        this.id = id;
        this.transportType = transportType;
        this.sourceCityId = sourceCityId;
        this.targetCityId = targetCityId;
    }

}