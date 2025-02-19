//package com.nomad.producer;
//
//import com.nomad.producer.nomad.City;
//import com.nomad.producer.nomad.Country;
//import com.nomad.producer.nomad.PotentialRoute;
//import lombok.extern.log4j.Log4j2;
//import org.neo4j.driver.types.MapAccessor;
//import org.neo4j.driver.types.TypeSystem;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.neo4j.core.Neo4jClient;
//import org.springframework.data.neo4j.core.mapping.Neo4jMappingContext;
//
//import java.util.*;
//import java.util.function.BiFunction;
//
//@Configuration
//@Log4j2
//public class CityRepository {
//
//    private Neo4jClient client;
//    private final BiFunction<TypeSystem, MapAccessor, City> cityMapper;
//    private final BiFunction<TypeSystem, MapAccessor, Country> countryMapper;
//
//    public CityRepository(Neo4jClient client, Neo4jMappingContext schema) {
//        this.client = client;
//        this.cityMapper = schema.getRequiredMappingFunctionFor(City.class);
//        this.countryMapper = schema.getRequiredMappingFunctionFor(Country.class);
//    }
//
//    public List<PotentialRoute> routeDiscoveryGivenCountry(String countryName) {
//        Collection<PotentialRoute> cities = client
//                .query("""
//                    MATCH (source:City)-[:OF_COUNTRY]->(country:Country {name: $countryName})
//                    MATCH (dest:City)-[:OF_COUNTRY]->(country)
//                    WHERE source <> dest
//                    RETURN source, dest, country
//                """)
//                .bind(countryName).to("countryName")
//                .fetchAs(PotentialRoute.class)
//                .mappedBy((typeSystem, record) -> {
//                    City sourceCity = cityMapper.apply(typeSystem, record.get("source").asNode());
//                    City destinationCity = cityMapper.apply(typeSystem, record.get("dest").asNode());
//                    Country country = countryMapper.apply(typeSystem, record.get("country").asNode());
//                    return new PotentialRoute(sourceCity.withCountry(country), destinationCity.withCountry(country));
//                })
//                .all();
//        return cities.stream().toList();
//    }
//}
