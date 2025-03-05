package com.nomad.library.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Value;

@Value
public class CityMetric {

    CityCriteria criteria;
    double metric;

    @JsonCreator
    public CityMetric(
        @JsonProperty("criteria") CityCriteria criteria,
        @JsonProperty("metric") double metric) {
            this.criteria = criteria;
            this.metric = metric;
        }
}
