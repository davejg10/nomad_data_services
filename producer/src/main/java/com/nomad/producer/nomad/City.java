package com.nomad.producer.nomad;

import lombok.Getter;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Node
@Getter
public class City {
    @Id
    private final String id;
    private final String name;
    private Country country;

//    public City(String id, String name) {
//        this.id = id;
//        this.name = name;
//    }
    
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
