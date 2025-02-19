package com.nomad.producer.nomad;

import lombok.Getter;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node
@Getter
public class Country {
    @Id
    private final String id;
    private final String name;

    public Country(String id, String name) {
        this.id = id;
        this.name = name;
    }
}
