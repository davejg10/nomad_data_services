package com.nomad.admin_api.functions.create_city;

import java.util.Set;
import java.util.function.Consumer;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nomad.admin_api.Neo4jCityRepository;
import com.nomad.admin_api.domain.CityDTO;
import com.nomad.admin_api.exceptions.DatabaseSyncException;
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
@Component
public class CreateCityHandler implements Consumer<CityDTO> {

    private final SqlCountryRepository sqlCountryRepository;
    private final SqlCityRepository sqlCityRepository;
    private final Neo4jCityRepository neo4jCityRepository;

    public CreateCityHandler(SqlCountryRepository sqlCountryRepository, SqlCityRepository sqlCityRepository, Neo4jCityRepository neo4jCityRepository) {
        this.sqlCountryRepository = sqlCountryRepository;
        this.sqlCityRepository = sqlCityRepository;
        this.neo4jCityRepository = neo4jCityRepository;
    }
    
    @Transactional
    public void accept(CityDTO cityToBeCreated) {
        SqlCountry sqlCountry = sqlCountryRepository.findByName(cityToBeCreated.countryName())
                .orElseThrow(() -> new EntityNotFoundException("Couldnt find country: " + cityToBeCreated.countryName()));

        String cityName = cityToBeCreated.name();
        SqlCity sqlCity = SqlCity.of(cityToBeCreated.name(), cityToBeCreated.description(), cityToBeCreated.cityMetrics(), sqlCountry.getId());

        try {
            sqlCity = sqlCityRepository.save(sqlCity);
            log.info("Created city in PostgreSQL flexible server with id: {}, and name: {}", sqlCity.getId(), cityName);
            
            Neo4jCity neo4jCity = new Neo4jCity(sqlCity.getId().toString(), cityName, cityToBeCreated.shortDescription(), cityToBeCreated.primaryBlobUrl(), cityToBeCreated.coordinate(), cityToBeCreated.cityMetrics(), Set.of(), new Neo4jCountry(sqlCity.getCountryId().toString(), "", "", "", Set.of()));
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
}
