package com.nomad.admin_api.functions.put_route_popularity;

import java.util.UUID;

public record RoutePopularityDTO(UUID sourceCityId, UUID targetCityId, double popularity) {}
