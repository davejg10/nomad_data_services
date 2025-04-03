package com.nomad.scraping_library.domain;

import java.time.LocalDate;


import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public abstract class ScraperMessage {

    ScraperRequestSource scraperRequestSource;
    ScraperRequestType scraperRequestType;
    CityDTO sourceCity;
    CityDTO targetCity;
    LocalDate searchDate;

    public ScraperMessage(ScraperRequestSource scraperRequestSource, ScraperRequestType scraperRequestType, CityDTO sourceCity, CityDTO targetCity, LocalDate searchDate) {
        this.scraperRequestSource = scraperRequestSource;
        this.scraperRequestType = scraperRequestType;
        this.sourceCity = sourceCity;
        this.targetCity = targetCity;
        this.searchDate = searchDate;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((scraperRequestSource == null) ? 0 : scraperRequestSource.hashCode());
        result = prime * result + ((scraperRequestType == null) ? 0 : scraperRequestType.hashCode());
        result = prime * result + ((sourceCity == null) ? 0 : sourceCity.hashCode());
        result = prime * result + ((targetCity == null) ? 0 : targetCity.hashCode());
        result = prime * result + ((searchDate == null) ? 0 : searchDate.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ScraperMessage other = (ScraperMessage) obj;
        if (scraperRequestSource == null) {
            if (other.scraperRequestSource != null)
                return false;
        } else if (!scraperRequestSource.equals(other.scraperRequestSource))
            return false;
        if (scraperRequestType != other.scraperRequestType)
            return false;
        if (sourceCity == null) {
            if (other.sourceCity != null)
                return false;
        } else if (!sourceCity.equals(other.sourceCity))
            return false;
        if (targetCity == null) {
            if (other.targetCity != null)
                return false;
        } else if (!targetCity.equals(other.targetCity))
            return false;
        if (searchDate == null) {
            if (other.searchDate != null)
                return false;
        } else if (!searchDate.equals(other.searchDate))
            return false;
        return true;
    }
    
}
