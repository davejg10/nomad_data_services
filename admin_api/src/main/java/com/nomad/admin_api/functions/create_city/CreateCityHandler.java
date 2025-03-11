package com.nomad.admin_api.functions.create_city;

import java.util.function.Consumer;

import org.springframework.stereotype.Component;

import com.nomad.admin_api.Neo4jCityRepository;
import com.nomad.admin_api.domain.CityDTO;
import com.nomad.data_library.domain.neo4j.Neo4jCity;
import com.nomad.data_library.domain.sql.SqlCity;
import com.nomad.data_library.domain.sql.SqlCountry;
import com.nomad.data_library.exceptions.Neo4jGenericException;
import com.nomad.data_library.repositories.SqlCityRepository;
import com.nomad.data_library.repositories.SqlCountryRepository;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class CreateCityHandler implements Consumer<CityDTO> {

    private SqlCountryRepository sqlCountryRepository;
    private SqlCityRepository sqlCityRepository;
    private Neo4jCityRepository neo4jCityRepository;

    public CreateCityHandler(SqlCountryRepository sqlCountryRepository, SqlCityRepository sqlCityRepository, Neo4jCityRepository neo4jCityRepository) {
        this.sqlCountryRepository = sqlCountryRepository;
        this.sqlCityRepository = sqlCityRepository;
        this.neo4jCityRepository = neo4jCityRepository;
    }
    
    public void accept(CityDTO cityToBeCreated) {
        SqlCity sqlCity = null;
        try {
            SqlCountry citiesCountry = sqlCountryRepository.findByName(cityToBeCreated.countryName()).get();
            sqlCity = SqlCity.of(cityToBeCreated.name(), cityToBeCreated.description(), cityToBeCreated.cityMetrics(), citiesCountry.getId());

            sqlCity = sqlCityRepository.save(sqlCity);

            log.info("Created city in PostgreSQL flexible server with id: {}, and name: {}", sqlCity.getId(), sqlCity.getName());

            Neo4jCity neo4jCity = neo4jCityRepository.syncCity(sqlCity);

            log.info("Synced city to Neo4j database with id {}, and name: {}", neo4jCity.getId(), neo4jCity.getName());

        } catch (Neo4jGenericException e) {
            log.error("Failed to sync city: {} to Neo4j. Rolling backing transactions. Error: {}", cityToBeCreated.name(), e);
            sqlCityRepository.delete(sqlCity);
            throw new RuntimeException("Failed to save city: " + cityToBeCreated.name() + " to Postgres OR Neo4j. Rolling backing transactions.", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save city: " + cityToBeCreated.name() + " to Postgres OR Neo4j. Rolling backing transactions.", e);
        }
    }
}
