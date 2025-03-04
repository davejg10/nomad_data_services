package com.nomad.library.domain;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.ToString;
// public record City(String id, String name, Country country) {}
@Node("City")
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class Neo4jCity {
    @Id
    private final String id;
    private final String name;
    private Neo4jCountry country;
    
    @JsonCreator
    public Neo4jCity(@JsonProperty("id") String id,
                @JsonProperty("name") String name,
                @JsonProperty("country") Neo4jCountry country) {
        this.id = id;
        this.name = name;
        this.country = country;
    }

    public Neo4jCity withCountry(Neo4jCountry country) {
        return new Neo4jCity(this.id, this.name, country);
    }
}
