package com.nomad.job_orchestrator.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.data.repository.CrudRepository;

import com.nomad.common_utils.domain.TransportType;
import com.nomad.data_library.domain.sql.RouteDefinition;

@Repository
public interface RouteDefinitionRepository extends CrudRepository<RouteDefinition, UUID> {

    Optional<RouteDefinition> findByTransportTypeAndSourceCityIdAndTargetCityId(TransportType transportType, UUID sourceCityId, UUID targetCityId);
    
}
