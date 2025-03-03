package com.nomad.admin_api.domain;

import org.springframework.data.annotation.Id;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "country")
@Getter
public class SqlCountry {

    @Id
    private final String id;

    private final String name;

    private final String description;

    @JsonCreator
    public SqlCountry of(@JsonProperty("name") String name,
                         @JsonProperty("description") String description) {
        return new SqlCountry(null, name, description);
    }

    public SqlCountry(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

}
