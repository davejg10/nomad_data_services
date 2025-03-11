package com.nomad.scraping_library.domain;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public class ScraperResponse extends ScraperMessage {
    
    private RouteDTO route;
    
    @JsonCreator
    public ScraperResponse(@JsonProperty("scraperRequestSource") String scraperRequestSource,
                           @JsonProperty("type") ScraperRequestType type,
                           @JsonProperty("sourceCity") CityDTO sourceCity,
                           @JsonProperty("targetCity") CityDTO targetCity,
                           @JsonProperty("route") RouteDTO route,
                           @JsonProperty("searchDate") LocalDate searchDate) {
        super(scraperRequestSource, type, sourceCity, targetCity, searchDate);
        this.route = route;
    }

    @Override
    public String toString() {
        return "ScraperResponse [scraperRequestSource=" + scraperRequestSource + ", type=" + type + ", sourceCity="
                + sourceCity + ", route=" + route + ", targetCity=" + targetCity + ", searchDate=" + searchDate + "]";
    }

    @Override
    public int hashCode() {
        super.hashCode();
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((route == null) ? 0 : route.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        super.equals(obj);
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        ScraperResponse other = (ScraperResponse) obj;
        if (route == null) {
            if (other.route != null)
                return false;
        } else if (!route.equals(other.route))
            return false;
        return true;
    }


}   
