package com.nomad.producer.nomad;

import lombok.Getter;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

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

    public City(String id, String name, Country country) {
        this.id = id;
        this.name = name;
        this.country = country;
    }

    public City withCountry(Country country) {
        return new City(this.id, this.name, country);
    }
}
