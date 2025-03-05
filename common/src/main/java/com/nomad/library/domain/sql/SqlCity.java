package com.nomad.library.domain.sql;

import java.util.UUID;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.nomad.library.domain.CityMetrics;
import com.nomad.library.domain.neo4j.CityMetricsSerializer;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "city")
@Getter
@NoArgsConstructor
@ToString
public class SqlCity {

    @Id
    @GeneratedValue
    private UUID id;

    private String name;

    private String description;
    
    @Column(name = "city_metrics", columnDefinition = "TEXT")
    @JsonSerialize(using = CityMetricsSerializer.class) // Conversion TO JSON String (called by mapifyCity())
    @Convert(converter = CityMetricsConverter.class) // Used to convert to/from Postgres
    private CityMetrics cityMetrics;

    @Column(name = "country_id")
    private UUID countryId;

    public static SqlCity of(String name, String description, CityMetrics cityMetrics, UUID countryId) {
        return new SqlCity(null, name, description, cityMetrics, countryId);
    }

    public SqlCity(UUID id, String name, String description, CityMetrics cityMetrics, UUID countryId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.cityMetrics = cityMetrics;
        this.countryId = countryId;
    }

}
