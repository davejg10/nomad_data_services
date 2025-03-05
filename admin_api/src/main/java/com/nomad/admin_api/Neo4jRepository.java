package com.nomad.admin_api;

import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.neo4j.core.mapping.Neo4jMappingContext;
import org.springframework.stereotype.Repository;

import com.nomad.library.domain.Neo4jCity;
import com.nomad.library.domain.Neo4jCountry;
import com.nomad.library.domain.SqlCity;
import com.nomad.library.domain.SqlCountry;
import com.nomad.library.exceptions.Neo4jGenericException;
import com.nomad.library.repositories.Neo4jCommonRepository;

import lombok.extern.log4j.Log4j2;

@Repository
@Log4j2
public class Neo4jRepository extends Neo4jCommonRepository {

    public Neo4jRepository(Neo4jClient neo4jClient, Neo4jMappingContext schema) {
        super(neo4jClient, schema);
    }

    public Neo4jCountry syncCountry(SqlCountry country) throws Neo4jGenericException {
        try {
            Neo4jCountry neo4jCountry = neo4jClient
            .query("""
                MERGE (country:Country {id: $id})
                ON CREATE SET country.name = $name
                RETURN country
            """)
            .bind(country.getId().toString()).to("id")
            .bind(country.getName()).to("name")
            .fetchAs(Neo4jCountry.class)
            .mappedBy((typeSystem, record) -> {
                return countryMapper.apply(typeSystem, record.get("country").asNode());
            })
            .first()
            .get();
            return neo4jCountry;
        } catch (Exception e) {
            log.info("Exception when trying to sync country; {}", e );
            throw new Neo4jGenericException("Issue when trying to syncCountry: " + e.getMessage());
        }
    }

    public Neo4jCity syncCity(SqlCity city) throws Neo4jGenericException {
        try {
            Neo4jCity neo4jCity = neo4jClient
            .query("""
                MERGE (c:City {id: $id})
                ON CREATE SET c.name = $name
                
                WITH c
                MATCH(country:Country {id: $countryId})
                MERGE (country)-[fromCountry:HAS_CITY]->(c)
                ON CREATE SET fromCountry.id = randomUUID()
                MERGE (c)-[toCountry:OF_COUNTRY]->(country)
                ON CREATE SET toCountry.id = randomUUID()
                RETURN c
            """)
            .bind(city.getId().toString()).to("id")
            .bind(city.getName()).to("name")
            .bind(city.getCountryId().toString()).to("countryId")
            .fetchAs(Neo4jCity.class)
            .mappedBy((typeSystem, record) -> {
                return cityMapper.apply(typeSystem, record.get("c").asNode());
            })
            .first()
            .get();
            return neo4jCity;
        } catch (Exception e) {
            log.info("Exception when trying to sync city; {}", e.getMessage());
            throw new Neo4jGenericException("Issue when trying to syncCity: " + e.getMessage());
        }
    }

    
}
