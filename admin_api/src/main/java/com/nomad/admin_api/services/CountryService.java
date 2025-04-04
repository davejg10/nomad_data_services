package com.nomad.admin_api.services;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nomad.admin_api.domain.CountryDTO;
import com.nomad.admin_api.exceptions.DatabaseSyncException;
import com.nomad.admin_api.exceptions.DuplicateEntityException;
import com.nomad.admin_api.exceptions.GeoEntity;
import com.nomad.admin_api.repositories.Neo4jCountryRepository;
import com.nomad.data_library.domain.neo4j.Neo4jCountry;
import com.nomad.data_library.domain.sql.SqlCountry;
import com.nomad.data_library.exceptions.Neo4jGenericException;
import com.nomad.data_library.repositories.SqlCountryRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class CountryService {

    private final Neo4jCountryRepository neo4jCountryRepository;
    private final SqlCountryRepository sqlCountryRepository;
    
    public CountryService(Neo4jCountryRepository neo4jCountryRepository, SqlCountryRepository sqlCountryRepository) {
        this.neo4jCountryRepository = neo4jCountryRepository;
        this.sqlCountryRepository = sqlCountryRepository;
    }

    @Transactional
    public void createCountry(CountryDTO countryToCreate) {
        String countryName = countryToCreate.name();
        
        Optional<SqlCountry> existingCountry = sqlCountryRepository.findByName(countryName);
        if (existingCountry.isPresent()) {
            throw new DuplicateEntityException(
                "Entity with name '" + countryName + "' already exists.",
                GeoEntity.COUNTRY,
                existingCountry.get().getId().toString(),
                countryName
            );
        }

        try {
            SqlCountry sqlCountry = SqlCountry.of(countryName, countryToCreate.description());
            sqlCountry = sqlCountryRepository.save(sqlCountry);
            log.info("Created country in PostgreSQL flexible server with id: {}, and name: {}", sqlCountry.getId(), countryName);
            
            Neo4jCountry neo4jCountry = new Neo4jCountry(sqlCountry.getId().toString(), countryName, countryToCreate.shortDescription(), countryToCreate.primaryBlobUrl(), Set.of());
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

    @Transactional
    public void updateCountry(String countryId, CountryDTO countryToUpdate) {
        SqlCountry sqlCountryRetrieved = sqlCountryRepository.findById(UUID.fromString(countryId))
                .orElseThrow(() -> new EntityNotFoundException("Couldnt find country: " + countryToUpdate.name()));

        try {
            
            SqlCountry sqlCountryToBeUpdated = new SqlCountry(sqlCountryRetrieved.getId(), countryToUpdate.name(), countryToUpdate.description());
            SqlCountry sqlCountryUpdated = sqlCountryRepository.save(sqlCountryToBeUpdated);
            log.info("Updated country in PostgreSQL flexible server with id: {}, and name: {}", sqlCountryUpdated.getId(), sqlCountryUpdated.getName());

            Neo4jCountry neo4jCountryToBeUpdated = new Neo4jCountry(sqlCountryRetrieved.getId().toString(), countryToUpdate.name(), countryToUpdate.shortDescription(), countryToUpdate.primaryBlobUrl(), Set.of());
            Neo4jCountry neo4jCountryUpdated = neo4jCountryRepository.update(neo4jCountryToBeUpdated);
            log.info("Updated country in Neo4j with id: {}, and name: {}", neo4jCountryUpdated.getId(), neo4jCountryUpdated.getName());
            
        } catch (Neo4jGenericException e) {
            log.error("Failed to update country: {} in Neo4j. Transaction will be rolled back. Error: {}", countryToUpdate.name(), e.getMessage());
            throw new DatabaseSyncException("Failed to update country: " + countryToUpdate.name() + " in Neo4j.", e);
        } catch (Exception e) {
            log.error("Unexpected error while updating country: {}, Transaction will be rolled back. Error: {}", countryToUpdate.name(), e.getMessage());
            throw new DatabaseSyncException("Unexpected error while updating country: " + countryToUpdate.name(), e);
        }
    }

    @Transactional
    public void deleteCountry(String countryName) {
        SqlCountry sqlCountryToDelete = sqlCountryRepository.findByName(countryName)
            .orElseThrow(() -> new EntityNotFoundException("Country not found: " + countryName));

        UUID countryId = sqlCountryToDelete.getId();

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