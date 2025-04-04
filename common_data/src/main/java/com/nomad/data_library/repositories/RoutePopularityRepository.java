package com.nomad.data_library.repositories;

import org.springframework.stereotype.Repository;
import org.springframework.data.repository.CrudRepository;

import com.nomad.data_library.domain.sql.RoutePopularity;
import com.nomad.data_library.domain.sql.RoutePopularityId;

@Repository
public interface RoutePopularityRepository extends CrudRepository<RoutePopularity, RoutePopularityId> {
    
}
