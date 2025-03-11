package com.nomad.data_library.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Value;

@Value
public class CityMetrics {

    CityMetric sailing;
    CityMetric food;
    CityMetric nightlife;

    @JsonCreator
    public CityMetrics(
        @JsonProperty("sailing") CityMetric sailing,
        @JsonProperty("food") CityMetric food,
        @JsonProperty("nightlife") CityMetric nightlife) {
            this.sailing = sailing;
            this.food = food;
            this.nightlife = nightlife;
        }
    
}
