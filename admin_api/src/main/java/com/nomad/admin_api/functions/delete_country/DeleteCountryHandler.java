package com.nomad.admin_api.functions.delete_country;

import java.util.UUID;
import java.util.function.Consumer;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nomad.admin_api.Neo4jCountryRepository;
import com.nomad.admin_api.exceptions.DatabaseSyncException;
import com.nomad.data_library.domain.sql.SqlCountry;
import com.nomad.data_library.exceptions.Neo4jGenericException;
import com.nomad.data_library.repositories.SqlCountryRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class DeleteCountryHandler implements Consumer<String> {

    private final SqlCountryRepository sqlCountryRepository;
    private final Neo4jCountryRepository neo4jCountryRepository;

    public DeleteCountryHandler(SqlCountryRepository sqlCountryRepository, Neo4jCountryRepository neo4jCountryRepository) {
        this.sqlCountryRepository = sqlCountryRepository;
        this.neo4jCountryRepository = neo4jCountryRepository;
    }
    
    @Transactional
    public void accept(String countryToBeDeleted) {

        SqlCountry sqlCountryToDelete = sqlCountryRepository.findByName(countryToBeDeleted)
            .orElseThrow(() -> new EntityNotFoundException("Country not found: " + countryToBeDeleted));

        UUID countryId = sqlCountryToDelete.getId();
        String countryName = sqlCountryToDelete.getName();

        try {
            sqlCountryRepository.delete(sqlCountryToDelete);
            log.info("Deleted country in PostgreSQL flexible server with id: {}, and name: {}", countryId, countryName);

            neo4jCountryRepository.delete(sqlCountryToDelete);
            log.info("Deleted country in Neo4j database with id {}, and name: {}", countryId, countryName);
            
        } catch (Neo4jGenericException e) {
            log.error("Failed to delete country: {} in Neo4j. Transaction will be rolled back. Error: {}", countryName, e.getMessage());
            throw new DatabaseSyncException("Failed to delete country: " + countryName + " in Neo4j", e);
        } catch (Exception e) {
            log.error("Unexpected error while deleting country: {}. Transaction will be rolled back. Error: {}", countryName, e.getMessage());
            throw new DatabaseSyncException("Unexcpeted error while deleting country: " + countryName, e);
        }
    }
}
