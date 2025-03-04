package com.nomad.admin_api.functions.create_country;

import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nomad.admin_api.Neo4jRepository;
import com.nomad.library.domain.Neo4jCountry;
import com.nomad.library.domain.SqlCountry;
import com.nomad.library.exceptions.Neo4jGenericException;
import com.nomad.library.repositories.SqlCountryRepository;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class CreateCountryHandler implements Consumer<SqlCountry> {

    private SqlCountryRepository sqlCountryRepository;
    private Neo4jRepository neo4jRepository;

    public CreateCountryHandler(SqlCountryRepository sqlCountryRepository, Neo4jRepository neo4jRepository) {
        this.sqlCountryRepository = sqlCountryRepository;
        this.neo4jRepository = neo4jRepository;
    }
 
    public void accept(SqlCountry countryToBeCreated) {
        SqlCountry sqlCountry = null;
        try {
            sqlCountry = sqlCountryRepository.save(countryToBeCreated);
            log.info("Created country in PostgreSQL flexible server with id: {}, and name: {}", sqlCountry.getId(), sqlCountry.getName());

            Neo4jCountry neo4jCountry = neo4jRepository.syncCountry(sqlCountry);
            log.info("Synced country to Neo4j database with id {}, and name: {}", neo4jCountry.getId(), neo4jCountry.getName());
        } catch (Neo4jGenericException e) {
            log.error("Failed to sync country: {} to Neo4j. Rolling backing transactions. Error: {}", countryToBeCreated.getName(), e);
            sqlCountryRepository.delete(countryToBeCreated);
            throw new RuntimeException("Failed to save country: " + countryToBeCreated.getName() + " to Postgres OR Neo4j.", e);

        } catch (Exception e) {
            throw new RuntimeException("Failed to save country: " + countryToBeCreated.getName() + " to Postgres OR Neo4j.", e);
        }
    }
}
