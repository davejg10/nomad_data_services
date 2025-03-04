package com.nomad.library.domain;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.ToString;

// public record Country(String id, String name) {}
@Node("Country")
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class Neo4jCountry {
    @Id
    private final String id;
    private final String name;

    @JsonCreator
    public Neo4jCountry(@JsonProperty("id") String id,
                   @JsonProperty("name") String name) {

        this.id = id;
        this.name = name;
    }
}

