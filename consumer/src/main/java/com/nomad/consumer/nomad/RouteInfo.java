package com.nomad.consumer.nomad;

public record RouteInfo(TransportType transportType, String operator, String depart, String arrival, double price) {}
