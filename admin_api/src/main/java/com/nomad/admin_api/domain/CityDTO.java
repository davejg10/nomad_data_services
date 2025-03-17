package com.nomad.admin_api.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.springframework.data.neo4j.types.GeographicPoint3d;

import com.nomad.data_library.domain.CityMetrics;

public record CityDTO(String name, String shortDescription, String description, String primaryBlobUrl, @JsonDeserialize(using = CoordinateDeserializer.class) GeographicPoint3d coordinate, CityMetrics cityMetrics, String countryName) {}
