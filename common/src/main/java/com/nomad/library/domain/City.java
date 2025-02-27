package com.nomad.library.domain;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
// public record City(String id, String name, Country country) {}
@Node
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class City {
    @Id
    private final String id;
    private final String name;
    private Country country;
    
    @JsonCreator
    public City(@JsonProperty("id") String id,
                @JsonProperty("name") String name,
                @JsonProperty("country") Country country) {
        this.id = id;
        this.name = name;
        this.country = country;
    }

    public City withCountry(Country country) {
        return new City(this.id, this.name, country);
    }
}
