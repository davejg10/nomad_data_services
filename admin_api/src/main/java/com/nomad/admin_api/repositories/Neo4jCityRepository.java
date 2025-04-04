package com.nomad.admin_api.repositories;

import java.util.Map;

import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomad.data_library.domain.neo4j.Neo4jCity;
import com.nomad.data_library.domain.sql.SqlCity;
import com.nomad.data_library.exceptions.Neo4jGenericException;
import com.nomad.data_library.repositories.Neo4jCommonCityRepository;

import lombok.extern.log4j.Log4j2;

@Repository
@Log4j2
public class Neo4jCityRepository extends Neo4jCommonCityRepository {

    public Neo4jCityRepository(Neo4jClient neo4jClient, ObjectMapper objectMapper, Neo4jCityMappers neo4jCityMappers) {
        super(neo4jClient, objectMapper, neo4jCityMappers);
    }

    public Neo4jCity save(Neo4jCity city) throws Neo4jGenericException {
        return super.createCity(city);
    }

    public void delete(SqlCity city) throws Neo4jGenericException {
        try {
            neo4jClient
            .query("""
                MATCH (city:City {id: $id}) DETACH DELETE city;
            """)
            .bind(city.getId().toString()).to("id")
            .run();
        } catch (Exception e) {
            throw new Neo4jGenericException("Exception when trying to delete City.", e);
        }
    }

    public Neo4jCity update(Neo4jCity city) throws Neo4jGenericException {
        Map<String, Object> cityAsMap = mapifyCity(city);

        try {
            Neo4jCity neo4jCity = neo4jClient
            .query("""
                MERGE (city:City {id: $id})
                SET city.name = $name,
                    city.shortDescription = $shortDescription,
                    city.primaryBlobUrl = $primaryBlobUrl,
                    city.coordinate = point($coordinate)

                WITH city
                MATCH(country:Country {id: $countryId})
                
                WITH city, country
                UNWIND $cityMetrics AS cityMetric
                MERGE (city)-[:HAS_METRIC]->(m:Metric {criteria: cityMetric.criteria})
                SET m.metric = cityMetric.metric
                    
                RETURN city, country, collect(m) as cityMetrics
            """)
            .bind(city.getCountry().getId()).to("countryId")
            .bindAll(cityAsMap)
            .fetchAs(Neo4jCity.class)
            .mappedBy(cityWithNoRoutesMapper)
            .first()
            .get();
            return neo4jCity;
        } catch (Exception e) {
            throw new Neo4jGenericException("Exception when trying to update City.", e);
        }
    }

    public void updateAllRoutesPopularity(String sourceCityId, String targetCityId, double popularity) throws Neo4jGenericException {
        try {
            neo4jClient
            .query("""
                MATCH (sourceCity:City {id: $sourceCityId}) -[routes:ROUTE]-> (targetCity:City {id: $targetCityId})
                SET routes.popularity = $popularity
            """)
            .bind(sourceCityId).to("sourceCityId")
            .bind(targetCityId).to("targetCityId")
            .bind(popularity).to("popularity")
            .run();
        } catch (Exception e) {
            throw new Neo4jGenericException("Exception when trying to update routes popularity in Neo4j.", e);
        }
    }

}
