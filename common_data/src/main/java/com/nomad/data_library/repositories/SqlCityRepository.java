package com.nomad.data_library.repositories;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.nomad.data_library.domain.sql.SqlCity;


@Repository
public interface SqlCityRepository extends CrudRepository<SqlCity, UUID> {

    Set<SqlCity> findAll();

    Optional<SqlCity> findByCountryIdAndName(UUID countryId, String name);
} 
