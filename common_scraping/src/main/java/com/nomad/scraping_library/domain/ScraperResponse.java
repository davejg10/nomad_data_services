package com.nomad.scraping_library.domain;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nomad.common_utils.domain.TransportType;

import lombok.Getter;

@Getter
public class ScraperResponse extends ScraperMessage {

    private TransportType transportType;
    private List<RouteDTO> routes;
    
    @JsonCreator
    public ScraperResponse(@JsonProperty("scraperRequestSource") String scraperRequestSource,
                           @JsonProperty("type") ScraperRequestType type,
                           @JsonProperty("transportType") TransportType transportType,
                           @JsonProperty("sourceCity") CityDTO sourceCity,
                           @JsonProperty("targetCity") CityDTO targetCity,
                           @JsonProperty("routes") List<RouteDTO> routes,
                           @JsonProperty("searchDate") LocalDate searchDate) {
        super(scraperRequestSource, type, sourceCity, targetCity, searchDate);
        this.transportType = transportType;
        this.routes = routes;
    }

    @Override
    public String toString() {
        return "ScraperResponse [scraperRequestSource=" + scraperRequestSource + ", type=" + type + ", transportType=" + transportType + ", sourceCity="
                + sourceCity + ", routes=" + routes + ", targetCity=" + targetCity + ", searchDate=" + searchDate + "]";
    }

}   
