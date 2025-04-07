package com.nomad.scraping_library.domain;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


public class ScraperRequest extends ScraperMessage {
    
    @JsonCreator
    public ScraperRequest(@JsonProperty("scraperRequestSource") ScraperRequestSource scraperRequestSource,
                          @JsonProperty("scraperRequestType") ScraperRequestType scraperRequestType,
                          @JsonProperty("sourceCity") CityDTO sourceCity,
                          @JsonProperty("targetCity") CityDTO targetCity,
                          @JsonProperty("searchDate") LocalDate searchDate) {
        super(scraperRequestSource, scraperRequestType, sourceCity, targetCity, searchDate);
    }

    @Override
    public String toString() {
        return "ScraperRequest{" +
                "scraperRequestSource='" + scraperRequestSource +
                ", scraperRequestType='" + scraperRequestType +
                ", sourceCity='" + sourceCity +
                ", targetCity='" + targetCity +
                ", searchDate='" + searchDate +
                '}';
    }
    
    
}