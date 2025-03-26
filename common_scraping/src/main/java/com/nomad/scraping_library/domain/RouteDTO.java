package com.nomad.scraping_library.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

import com.nomad.common_utils.domain.TransportType;
import com.nomad.scraping_library.exceptions.ScrapingDataSchemaException;

import lombok.extern.log4j.Log4j2;

@Log4j2
public record RouteDTO(TransportType transportType, String operator, LocalDateTime depart, LocalDateTime arrival, String departureLocation, String arrivalLocation, BigDecimal cost, String url) {

    public static RouteDTO createWithSchema(
        String transportType,
        String operator,
        String departure,
        String arrival,
        String departureLocation,
        String arrivalLocation,
        String cost,
        String url,
        LocalDate searchDate) throws ScrapingDataSchemaException
        {
            LocalDateTime departDateTime, arrivalDateTime = null;
            TransportType transportTypeEnum = null;
            try {
                departDateTime = LocalDateTime.of(searchDate, LocalTime.parse(departure));

                arrivalDateTime = LocalDateTime.of(searchDate, LocalTime.parse(arrival));
    
                if (arrivalDateTime.isBefore(departDateTime)) {
                    arrivalDateTime = arrivalDateTime.plusDays(1);
                }

                if (transportType.equals("Bus, Ferry")) {
                    transportTypeEnum = TransportType.BUS_FERRY;
                } else if (transportType.equals("Van, Bus")) {
                    transportTypeEnum = TransportType.VAN_BUS;
                } else if (transportType.equals("Van, Ferry")) {
                    transportTypeEnum = TransportType.VAN_FERRY;
                } else if (transportType.equals("Bus, Bus, Ferry")) {
                    transportTypeEnum = TransportType.BUS_BUS_FERRY;
                } else if (transportType.equals("Bus, Van")) {
                    transportTypeEnum = TransportType.BUS_VAN;
                } else if (transportType.equals("Ferry, Bus")) {
                    transportTypeEnum = TransportType.FERRY_BUS;
                } else if (transportType.equals("Ferry, Van")) {
                    transportTypeEnum = TransportType.FERRY_VAN;
                } else if (transportType.equals("Bus, Bus")) {
                    transportTypeEnum = TransportType.BUS_BUS;
                } else {
                    transportTypeEnum = TransportType.valueOf(transportType.toUpperCase());
                }



            } catch (DateTimeParseException e) {
                log.error("Error when passing depart/arrival time to LocalTime. Error: {}", e.getMessage());
                throw new ScrapingDataSchemaException("In RouteDTO createWithSchema", e);
            } catch (Exception e ) {
                log.error("Error most likely when mapping TransportType. TransportType string given: {}. Error: {}", transportType, e.getMessage());
                throw new ScrapingDataSchemaException("In RouteDTO createWithSchema", e);

            }

            BigDecimal costConverted = new BigDecimal(cost);
            
            if (costConverted.compareTo(BigDecimal.ZERO) < 0) {
                log.error("Cost was less than 0. Rejecting");
                throw new ScrapingDataSchemaException("Cost cannot be less than 0 when creating RouteDTO object");
            }

            return new RouteDTO(transportTypeEnum, operator, departDateTime, arrivalDateTime, departureLocation, arrivalLocation, new BigDecimal(cost), url);
        }
    
}
