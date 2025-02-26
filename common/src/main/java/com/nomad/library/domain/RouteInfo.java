package com.nomad.library.domain;

public record RouteInfo(TransportType transportType, String operator, String depart, String arrival, double price) {}
