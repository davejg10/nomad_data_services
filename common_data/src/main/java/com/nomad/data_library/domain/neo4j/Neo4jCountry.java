package com.nomad.data_library.domain.neo4j;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;

@Node("Country")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Neo4jCountry {

    @Id @Getter private final String id;
    @Getter private final String name;
    @Getter private final String shortDescription;
    @Getter private final String primaryBlobUrl;

    @Relationship(type = "HAS_CITY", direction = Relationship.Direction.OUTGOING)
    private final Set<Neo4jCity> cities;

    public static Neo4jCountry of(String name, String shortDescription, String primaryBlobUrl, Set<Neo4jCity> cities) {
        return new Neo4jCountry(null, name, shortDescription, primaryBlobUrl, Set.copyOf(cities));
    }

    public Neo4jCountry(String id, String name, String shortDescription, String primaryBlobUrl, Set<Neo4jCity> cities) {
        this.id = id;
        this.name = name;
        this.shortDescription = shortDescription;
        this.primaryBlobUrl = primaryBlobUrl;
        this.cities = cities;
    }

    public Neo4jCountry withId(String id) {
        return new Neo4jCountry(id, this.name, this.shortDescription, this.primaryBlobUrl, this.cities);
    }

    // Ensure mutable field 'cities' remains immutable
    public Set<Neo4jCity> getCities() {
        return new HashSet<>(cities);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Neo4jCountry country = (Neo4jCountry) o;
        return Objects.equals(id, country.id) && Objects.equals(name, country.name) && Objects.equals(shortDescription, country.shortDescription) && Objects.equals(primaryBlobUrl, country.primaryBlobUrl) && Objects.equals(cities, country.cities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, cities);
    }

    @Override
    public String toString() {
        return "Neo4jCountry{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", shortDescription='" + shortDescription + '\'' +
                ", primaryBlobUrl='" + primaryBlobUrl + '\'' +
                ", cities=" + cities +
                '}';
    }
}

