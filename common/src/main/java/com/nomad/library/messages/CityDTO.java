package com.nomad.library.messages;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.ToString;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CityDTO(String id, String name) {}
