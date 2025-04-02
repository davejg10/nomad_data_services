package com.nomad.scraping_library.domain;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nomad.common_utils.domain.TransportType;

import lombok.Getter;

@Getter
public class ScraperResponse extends ScraperMessage {

    private ScraperIdentifier scraperIdentifier;
    private TransportType transportType;
    private List<RouteDTO> routes;
    
    @JsonCreator
    public ScraperResponse(@JsonProperty("scraperRequestSource") ScraperRequestSource scraperRequestSource,
                           @JsonProperty("scraperRequestType") ScraperRequestType scraperRequestType,
                           @JsonProperty("scraperIdentifier") ScraperIdentifier scraperIdentifier,
                           @JsonProperty("transportType") TransportType transportType,
                           @JsonProperty("sourceCity") CityDTO sourceCity,
                           @JsonProperty("targetCity") CityDTO targetCity,
                           @JsonProperty("routes") List<RouteDTO> routes,
                           @JsonProperty("searchDate") LocalDate searchDate) {
        super(scraperRequestSource, scraperRequestType, sourceCity, targetCity, searchDate);
        this.scraperIdentifier = scraperIdentifier;
        this.transportType = transportType;
        this.routes = routes;
    }

    @Override
    public String toString() {
        return "ScraperResponse [scraperRequestSource=" + scraperRequestSource + ", scraperRequestType=" + scraperRequestType + "," + ", scraperIdentifier=" + scraperIdentifier  +
                " transportType=" + transportType + ", sourceCity=" + sourceCity + ", routes=" + routes + ", targetCity=" + targetCity + ", searchDate=" + searchDate + "]";
    }

}   
