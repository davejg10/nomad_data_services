package com.nomad.scraping_library.domain;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


public class ScraperRequest extends ScraperMessage {
    
    @JsonCreator
    public ScraperRequest(@JsonProperty("scraperRequestSource") String scraperRequestSource,
                          @JsonProperty("type") ScraperRequestType type,
                          @JsonProperty("sourceCity") CityDTO sourceCity,
                          @JsonProperty("targetCity") CityDTO targetCity,
                          @JsonProperty("searchDate") LocalDate searchDate) {
        super(scraperRequestSource, type, sourceCity, targetCity, searchDate);
    }

    public String toString() {
        StringBuilder string = new StringBuilder();
        string.append(this.scraperRequestSource).append(" ").append(this.scraperRequestType).append(" ").append(sourceCity);
        return string.toString();
    }
    
    
}