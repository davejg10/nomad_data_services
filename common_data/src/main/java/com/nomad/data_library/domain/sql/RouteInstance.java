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


    public static RouteInstance of(BigDecimal cost, LocalDateTime departure, LocalDateTime arrival, String url, LocalDate searchDate, RouteDefinition routeDefinition) {
        return new RouteInstance(null, cost, departure, arrival, url, Duration.between(departure, arrival), searchDate, LocalDateTime.now(), routeDefinition);
    }

    // @PostLoad
    // protected void convertMillisToDuration() {
    //     if ("org.h2.Driver".equals(System.getProperty("database.driver"))) {
    //         this.travelTime = Duration.between(departure, arrival);
    //     }
    // }
}
