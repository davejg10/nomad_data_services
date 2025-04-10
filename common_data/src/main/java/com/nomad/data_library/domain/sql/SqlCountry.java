package com.nomad.data_library.domain.sql;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "country")
@Getter
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class SqlCountry {

    @Id
    @GeneratedValue
    private UUID id;

    private String name;

    private String description;

    @JsonCreator
    public static SqlCountry of(@JsonProperty("name") String name,
                                @JsonProperty("description") String description) {
        return new SqlCountry(null, name, description);
    }

    public SqlCountry(UUID id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }
}
