package com.nomad.scraping_library.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

import com.nomad.scraping_library.exceptions.ScrapingDataSchemaException;

import lombok.extern.log4j.Log4j2;

@Log4j2
public record RouteDTO(TransportType transportType, String operator, LocalDateTime depart, LocalDateTime arrival, double cost) {

    public static RouteDTO createWithSchema(
        String transportType,
        String operator,
        String depart,
        String arrival,
        double cost,
        LocalDate searchDate) throws ScrapingDataSchemaException
        {
            LocalDateTime departDateTime, arrivalDateTime = null;
            TransportType transportTypeEnum = null;
            try {
                departDateTime = LocalDateTime.of(searchDate, LocalTime.parse(depart));

                arrivalDateTime = LocalDateTime.of(searchDate, LocalTime.parse(arrival));
    
                if (arrivalDateTime.isBefore(departDateTime)) {
                    arrivalDateTime = arrivalDateTime.plusDays(1);
                }

                transportTypeEnum = TransportType.valueOf(transportType.toUpperCase());

                
            } catch (DateTimeParseException e) {
                log.error("Error when passing depart/arrival time to LocalTime. Error: {}", e.getMessage());
                throw new ScrapingDataSchemaException("In RouteDTO createWithSchema: " + e.getMessage());
            } catch (Exception e ) {
                log.error("Error most likely when mapping TransportType. TransportType string given: {}", transportType);
                throw new ScrapingDataSchemaException("In RouteDTO createWithSchema: " + e.getMessage());

            }
            
            if (cost < 0) {
                log.error("Cost was less than 0. Rejecting");
                throw new ScrapingDataSchemaException("Cost cannot be less than 0 when creating RouteDTO object");
            }

            return new RouteDTO(transportTypeEnum, operator, departDateTime, arrivalDateTime, cost);
        }
    
}
