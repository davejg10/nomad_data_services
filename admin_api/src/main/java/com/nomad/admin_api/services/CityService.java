package com.nomad.admin_api.services;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nomad.admin_api.domain.CityDTO;
import com.nomad.admin_api.domain.CityToDeleteDTO;
import com.nomad.admin_api.exceptions.DatabaseSyncException;
import com.nomad.admin_api.exceptions.DuplicateEntityException;
import com.nomad.admin_api.exceptions.GeoEntity;
import com.nomad.admin_api.repositories.Neo4jCityRepository;
import com.nomad.data_library.domain.neo4j.Neo4jCity;
import com.nomad.data_library.domain.neo4j.Neo4jCountry;
import com.nomad.data_library.domain.sql.SqlCity;
import com.nomad.data_library.domain.sql.SqlCountry;
import com.nomad.data_library.exceptions.Neo4jGenericException;
import com.nomad.data_library.repositories.SqlCityRepository;
import com.nomad.data_library.repositories.SqlCountryRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class CityService {

    private final Neo4jCityRepository neo4jCityRepository;
    private final SqlCityRepository sqlCityRepository;
    private final SqlCountryRepository sqlCountryRepository;
    
    public CityService(Neo4jCityRepository neo4jCityRepository, SqlCityRepository sqlCityRepository, SqlCountryRepository sqlCountryRepository) {
        this.neo4jCityRepository = neo4jCityRepository;
        this.sqlCityRepository = sqlCityRepository;
        this.sqlCountryRepository = sqlCountryRepository;
    }

    @Transactional
    public void createCity(CityDTO cityToCreate) {
        String cityName = cityToCreate.name();

        SqlCountry sqlCountry = sqlCountryRepository.findByName(cityToCreate.countryName())
                .orElseThrow(() -> new EntityNotFoundException("Couldnt find country: " + cityToCreate.countryName()));

        Optional<SqlCity> existingCity = sqlCityRepository.findByCountryIdAndName(sqlCountry.getId(), cityName);

        if (existingCity.isPresent()) {
            throw new DuplicateEntityException(
                "Entity with name '" + cityToCreate.name() + "' already exists.",
                GeoEntity.CITY,
                existingCity.get().getId().toString(),
                cityToCreate.name()
            );
        }

        try {
            SqlCity sqlCity = SqlCity.of(cityToCreate.name(), cityToCreate.description(), cityToCreate.cityMetrics(), sqlCountry.getId());

            sqlCity = sqlCityRepository.save(sqlCity);
            log.info("Created city in PostgreSQL flexible server with id: {}, and name: {}", sqlCity.getId(), cityName);
            
            Neo4jCity neo4jCity = new Neo4jCity(sqlCity.getId().toString(), cityName, cityToCreate.shortDescription(), cityToCreate.primaryBlobUrl(), cityToCreate.coordinate(), cityToCreate.cityMetrics(), Set.of(), new Neo4jCountry(sqlCity.getCountryId().toString(), "", "", "", Set.of()));
            neo4jCity = neo4jCityRepository.save(neo4jCity);
            log.info("Created city in Neo4j with id {}, and name: {}", neo4jCity.getId(), cityName);

        } catch (Neo4jGenericException e) {
            log.error("Failed to create city: {} in Neo4j. Transaction will be rolled back. Error: {}", cityName, e.getMessage());
            throw new DatabaseSyncException("Failed to create city: " + cityName + " in Neo4j.", e);
        } catch (Exception e) {
            log.error("Unexpected error while creating city: {}, Transaction will be rolled back. Error: {}", cityName, e.getMessage());
            throw new DatabaseSyncException("Unexpected error while creating city: " + cityName, e);
        }
    }

    @Transactional
    public void updateCity(String cityId, CityDTO cityToUpdate) {
        SqlCountry sqlCountryRetrieved = sqlCountryRepository.findByName(cityToUpdate.countryName())
                .orElseThrow(() -> new EntityNotFoundException("Couldnt find country: " + cityToUpdate.countryName()));
        SqlCity sqlCityRetreieved = sqlCityRepository.findById(UUID.fromString(cityId))
                .orElseThrow(() -> new EntityNotFoundException("Couldnt find city: " + cityToUpdate.name()));

        try {
            
            SqlCity sqlCityToBeUpdated = new SqlCity(sqlCityRetreieved.getId(), cityToUpdate.name(), cityToUpdate.description(), cityToUpdate.cityMetrics(), sqlCountryRetrieved.getId());
            SqlCity sqlCityUpdated = sqlCityRepository.save(sqlCityToBeUpdated);
            log.info("Updated city in PostgreSQL flexible server with id: {}, and name: {}", sqlCityUpdated.getId(), sqlCityUpdated.getName());

            Neo4jCity neo4jCityToBeUpdated = new Neo4jCity(sqlCityRetreieved.getId().toString(), cityToUpdate.name(), cityToUpdate.shortDescription(), cityToUpdate.primaryBlobUrl(), cityToUpdate.coordinate(), cityToUpdate.cityMetrics(), Set.of(), new Neo4jCountry(sqlCountryRetrieved.getId().toString(), "", "", "", Set.of()));
            Neo4jCity neo4jCityUpdated = neo4jCityRepository.update(neo4jCityToBeUpdated);
            log.info("Updated city in Neo4j with id: {}, and name: {}", neo4jCityUpdated.getId(), neo4jCityUpdated.getName());
            
        } catch (Neo4jGenericException e) {
            log.error("Failed to update city: {} in Neo4j. Transaction will be rolled back. Error: {}", cityToUpdate.name(), e.getMessage());
            throw new DatabaseSyncException("Failed to update city: " + cityToUpdate.name() + " in Neo4j.", e);
        } catch (Exception e) {
            log.error("Unexpected error while update city: {}, Transaction will be rolled back. Error: {}", cityToUpdate.name(), e.getMessage());
            throw new DatabaseSyncException("Unexpected error while update city: " + cityToUpdate.name(), e);
        }
    }

    @Transactional
    public void deleteCity(CityToDeleteDTO cityToBeDeleted) {

        SqlCountry sqlCountry = sqlCountryRepository.findByName(cityToBeDeleted.countryName())
            .orElseThrow(() -> new EntityNotFoundException("Country not found: " + cityToBeDeleted.countryName()));

        SqlCity sqlCityToBeDeleted = sqlCityRepository.findByCountryIdAndName(sqlCountry.getId(), cityToBeDeleted.name())
            .orElseThrow(() -> new EntityNotFoundException("City not found: " + cityToBeDeleted.name() + " in country: " + cityToBeDeleted.countryName()));

    
        UUID cityId = sqlCityToBeDeleted.getId();
        String cityName = sqlCityToBeDeleted.getName();

        try {
            sqlCityRepository.delete(sqlCityToBeDeleted);
            log.info("Deleted city in PostgreSQL with id: {}, and name: {}", cityId, cityName);
            
            neo4jCityRepository.delete(sqlCityToBeDeleted);
            log.info("Deleted city in Neo4j with id: {}, and name: {}", cityId, cityName);
            
        } catch (Neo4jGenericException e) {
            log.error("Failed to delete city: {} in Neo4j. Transaction will be rolled back. Error: {}", cityName, e.getMessage());
            throw new DatabaseSyncException("Failed to delete city: " + cityName + " in Neo4j", e);
        } catch (Exception e) {
            log.error("Unexpected error while deleting city: {}. Transaction will be rolled back. Error: {}", cityName, e.getMessage());
            throw new DatabaseSyncException("Unexpected error while deleting city: " + cityName, e);
        }
    }
    
}
