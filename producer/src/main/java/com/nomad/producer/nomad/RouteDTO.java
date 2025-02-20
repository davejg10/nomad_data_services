package com.nomad.consumer.nomad;

public record RouteDTO(City targetCity, double popularity, double time, double cost, TransportType transportType) { }
