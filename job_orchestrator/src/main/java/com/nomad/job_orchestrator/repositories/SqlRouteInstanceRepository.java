package com.nomad.job_orchestrator.repositories;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.nomad.data_library.domain.sql.RouteDefinition;
import com.nomad.data_library.domain.sql.RouteInstance;

@Repository
public interface SqlRouteInstanceRepository extends CrudRepository<RouteInstance, UUID>{
    
    @Transactional
    public void deleteAllByRouteDefinitionAndSearchDate(RouteDefinition routeDefinition, LocalDate searchDate);
    
}
