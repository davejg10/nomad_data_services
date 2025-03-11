package com.nomad.admin_api.domain;

import com.nomad.data_library.domain.CityMetrics;

public record CityDTO(String name, String description, CityMetrics cityMetrics, String countryName) {}
