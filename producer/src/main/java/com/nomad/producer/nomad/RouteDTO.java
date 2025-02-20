package com.nomad.producer.nomad;

public record RouteDTO(City targetCity, double popularity, double time, double cost, TransportType transportType) { }
