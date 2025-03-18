package com.nomad.data_library.repositories;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;

import com.nomad.data_library.exceptions.Neo4jMissingKeyException;
import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.MapAccessor;
import org.neo4j.driver.types.TypeSystem;
import org.springframework.data.neo4j.core.mapping.Neo4jMappingContext;

import com.nomad.data_library.domain.neo4j.Neo4jCity;
import com.nomad.data_library.domain.neo4j.Neo4jCountry;

public class Neo4jCountryMappers {

    protected final BiFunction<TypeSystem, MapAccessor, Neo4jCity> cityMapper;
    protected final BiFunction<TypeSystem, MapAccessor, Neo4jCountry> countryMapper;

    public Neo4jCountryMappers(Neo4jMappingContext schema) {
        this.cityMapper = schema.getRequiredMappingFunctionFor(Neo4jCity.class);
        this.countryMapper = schema.getRequiredMappingFunctionFor(Neo4jCountry.class);
    }

    public BiFunction<TypeSystem, Record, Neo4jCountry> countryWithCitiesMapper() {
        return ((typeSystem, record) -> {
            if (!record.containsKey("country") || !record.containsKey("cities"))
            {
                throw new Neo4jMissingKeyException("cityWithAllRelationships");
            }

            Neo4jCountry fetchedCountry = countryMapper.apply(typeSystem, record.get("country").asNode());
            Set<Neo4jCity> cities = new HashSet<>();

            if (!record.get("cities").asList().isEmpty()) {
                cities = mapCountryCities(typeSystem, record.get("cities"), fetchedCountry);
            }

            return new Neo4jCountry(fetchedCountry.getId(), fetchedCountry.getName(), fetchedCountry.getShortDescription(), fetchedCountry.getPrimaryBlobUrl(), cities);
        });
    }

    public BiFunction<TypeSystem, Record, Neo4jCountry> countryNoCitiesMapper() {
        return ((typeSystem, record) -> {
            if (!record.containsKey("country"))
            {
                throw new Neo4jMissingKeyException("cityWithAllRelationships");
            }
            Neo4jCountry theCountry = countryMapper.apply(typeSystem, record.get("country").asNode());
            return theCountry;
        });
    }

    private Set<Neo4jCity> mapCountryCities(TypeSystem typeSystem, Value cities, Neo4jCountry fetchedCountry) {
        return new HashSet<>(cities.asList(city -> {
            Neo4jCity neo4jCity = cityMapper.apply(typeSystem, city.asNode());
            return neo4jCity.withCountry(fetchedCountry);
        }));
    }
}
