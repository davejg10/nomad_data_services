package com.nomad.admin_api.functions.create_country;

import java.util.Set;
import java.util.function.Consumer;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nomad.admin_api.Neo4jCountryRepository;
import com.nomad.admin_api.domain.CountryDTO;
import com.nomad.admin_api.exceptions.DatabaseSyncException;
import com.nomad.data_library.domain.neo4j.Neo4jCountry;
import com.nomad.data_library.domain.sql.SqlCountry;
import com.nomad.data_library.exceptions.Neo4jGenericException;
import com.nomad.data_library.repositories.SqlCountryRepository;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class CreateCountryHandler implements Consumer<CountryDTO> {

    private SqlCountryRepository sqlCountryRepository;
    private Neo4jCountryRepository neo4jCountryRepository;

    public CreateCountryHandler(SqlCountryRepository sqlCountryRepository, Neo4jCountryRepository neo4jCountryRepository) {
        this.sqlCountryRepository = sqlCountryRepository;
        this.neo4jCountryRepository = neo4jCountryRepository;
    }
 
    @Transactional
    public void accept(CountryDTO countryToBeCreated) {
        String countryName = countryToBeCreated.name();

        try {
            SqlCountry sqlCountry = SqlCountry.of(countryName, countryToBeCreated.description());
            sqlCountry = sqlCountryRepository.save(sqlCountry);
            log.info("Created country in PostgreSQL flexible server with id: {}, and name: {}", sqlCountry.getId(), countryName);
            
            Neo4jCountry neo4jCountry = new Neo4jCountry(sqlCountry.getId().toString(), countryName, countryToBeCreated.shortDescription(), countryToBeCreated.primaryBlobUrl(), Set.of());
            neo4jCountry = neo4jCountryRepository.save(neo4jCountry);
            log.info("Created country in Neo4j with id {}, and name: {}", neo4jCountry.getId(), countryName);
            
        } catch (Neo4jGenericException e) {
            log.error("Failed to create country: {} in Neo4j. Transaction will be rolled back. Error: ", countryName, e.getMessage());
            throw new DatabaseSyncException("Failed to create country: " + countryName + " in Neo4j. ", e);
        } catch (Exception e) {
            log.error("Unexpected error while creating country: " + countryName, e);
            throw new DatabaseSyncException("Unexpected error while creating country: " + countryName, e);
        }
        
    }
}
