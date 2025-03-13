package com.nomad.admin_api.functions.create_country;

import java.util.function.Consumer;

import org.springframework.stereotype.Component;

import com.nomad.admin_api.Neo4jCountryRepository;
import com.nomad.data_library.domain.neo4j.Neo4jCountry;
import com.nomad.data_library.domain.sql.SqlCountry;
import com.nomad.data_library.exceptions.Neo4jGenericException;
import com.nomad.data_library.repositories.SqlCountryRepository;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class CreateCountryHandler implements Consumer<SqlCountry> {

    private SqlCountryRepository sqlCountryRepository;
    private Neo4jCountryRepository neo4jCountryRepository;

    public CreateCountryHandler(SqlCountryRepository sqlCountryRepository, Neo4jCountryRepository neo4jCountryRepository) {
        this.sqlCountryRepository = sqlCountryRepository;
        this.neo4jCountryRepository = neo4jCountryRepository;
    }
 
    public void accept(SqlCountry countryToBeCreated) {
        SqlCountry sqlCountry = null;
        try {
            sqlCountry = sqlCountryRepository.save(countryToBeCreated);
            log.info("Created country in PostgreSQL flexible server with id: {}, and name: {}", sqlCountry.getId(), sqlCountry.getName());

            Neo4jCountry neo4jCountry = neo4jCountryRepository.syncCountry(sqlCountry);
            log.info("Synced country to Neo4j database with id {}, and name: {}", neo4jCountry.getId(), neo4jCountry.getName());
        } catch (Neo4jGenericException e) {
            log.error("Failed to sync country: {} to Neo4j. Rolling backing transactions. Error: {}", countryToBeCreated.getName(), e);
            sqlCountryRepository.delete(countryToBeCreated);
            throw new RuntimeException("Failed to save country to Neo4j: " + countryToBeCreated.getName() + " Error: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("some error with postgres", e);
            throw new RuntimeException("Failed to save country to Postgres " + countryToBeCreated.getName() + " Error: " + e.getMessage(), e);
        }
    }
}
