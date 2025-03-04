package com.nomad.library.repositories;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.nomad.library.domain.SqlCountry;

@Repository
public interface SqlCountryRepository extends CrudRepository<SqlCountry, UUID> {

    Optional<SqlCountry> findByName(String name);

    Set<SqlCountry> findAll();
} 