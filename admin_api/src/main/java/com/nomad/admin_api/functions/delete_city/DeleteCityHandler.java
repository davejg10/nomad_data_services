package com.nomad.admin_api.functions.delete_city;

import java.util.UUID;
import java.util.function.Consumer;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nomad.admin_api.Neo4jCityRepository;
import com.nomad.admin_api.domain.CityToDeleteDTO;
import com.nomad.admin_api.exceptions.DatabaseSyncException;
import com.nomad.data_library.domain.sql.SqlCity;
import com.nomad.data_library.domain.sql.SqlCountry;
import com.nomad.data_library.exceptions.Neo4jGenericException;
import com.nomad.data_library.repositories.SqlCityRepository;
import com.nomad.data_library.repositories.SqlCountryRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class DeleteCityHandler implements Consumer<CityToDeleteDTO> {

    private final SqlCountryRepository sqlCountryRepository;
    private final SqlCityRepository sqlCityRepository;
    private final Neo4jCityRepository neo4jCityRepository;

    public DeleteCityHandler(SqlCountryRepository sqlCountryRepository, SqlCityRepository sqlCityRepository, Neo4jCityRepository neo4jCityRepository) {
        this.sqlCountryRepository = sqlCountryRepository;
        this.sqlCityRepository = sqlCityRepository;
        this.neo4jCityRepository = neo4jCityRepository;
    }
    
    @Transactional
    public void accept(CityToDeleteDTO cityToBeDeleted) {

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
