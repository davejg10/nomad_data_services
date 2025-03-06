package com.nomad.library.messages;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.ToString;

@ToString
public class ScraperRequest extends ScraperMessage {
    
    @JsonCreator
    public ScraperRequest(@JsonProperty("scraperRequestSource") final String scraperRequestSource,
                          @JsonProperty("type") final ScraperRequestType type,
                          @JsonProperty("sourceCity") final CityDTO sourceCity,
                          @JsonProperty("targetCity") final CityDTO targetCity,
                          @JsonProperty("searchDate") final LocalDate searchDate) {
        super(scraperRequestSource, type, sourceCity, targetCity, searchDate);
    }
    
    
}