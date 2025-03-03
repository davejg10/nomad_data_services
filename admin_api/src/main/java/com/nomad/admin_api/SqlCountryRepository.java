package com.nomad.admin_api;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.nomad.admin_api.domain.SqlCountry;

@Repository
public interface SqlCountryRepository extends CrudRepository<SqlCountry, String> {

} 