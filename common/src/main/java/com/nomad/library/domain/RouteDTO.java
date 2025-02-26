package com.nomad.library.domain;

public record RouteDTO(City targetCity, double popularity, double time, double cost, TransportType transportType) { }
