package com.nomad.data_library.domain.sql;

import java.util.Set;
import java.util.UUID;

import com.nomad.data_library.domain.CityMetric;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "city_metrics", columnDefinition = "jsonb")
    @Convert(converter = CityMetricsConverter.class) // Used to convert to/from Postgres
    private Set<CityMetric> cityMetrics;

    @Column(name = "country_id")
    private UUID countryId;

    public static SqlCity of(String name, String description, Set<CityMetric> cityMetrics, UUID countryId) {
        return new SqlCity(null, name, description, cityMetrics, countryId);
    }

    public SqlCity(UUID id, String name, String description, Set<CityMetric> cityMetrics, UUID countryId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.cityMetrics = cityMetrics;
        this.countryId = countryId;
    }

}
