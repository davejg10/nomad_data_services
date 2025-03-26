package com.nomad.data_library.domain.sql;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "route_instance")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RouteInstance {
    
    @Id
    @GeneratedValue
    private UUID id;

    private BigDecimal cost;

    private LocalDateTime departure;

    private LocalDateTime arrival;

    private String operator;

    @Column(name = "departure_location")
    private String departureLocation;

    @Column(name = "arrival_location")
    private String arrivalLocation;

    private String url;
    
    @JdbcTypeCode(SqlTypes.INTERVAL_SECOND)
    @Column(name = "travel_time")
    private Duration travelTime;

    @Column(name = "search_date")
    private LocalDate searchDate;

    @Column(name = "last_check")
    private LocalDateTime lastCheck;

    @ManyToOne
    @JoinColumn(name = "route_definition_id", referencedColumnName = "id")
    private RouteDefinition routeDefinition;


    public static RouteInstance of(BigDecimal cost, LocalDateTime departure, LocalDateTime arrival, String operator, String departureLocation, String arrivalLocation, String url, LocalDate searchDate, RouteDefinition routeDefinition) {
        return new RouteInstance(null, cost, departure, arrival, operator, departureLocation, arrivalLocation, url, Duration.between(departure, arrival), searchDate, LocalDateTime.now(), routeDefinition);
    }
}
