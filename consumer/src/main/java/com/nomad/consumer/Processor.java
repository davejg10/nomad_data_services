package com.nomad.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomad.consumer.messages.DataCollectionJob;
import com.nomad.consumer.nomad.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Service
@Log4j2
public class Processor implements Function<DataCollectionJob, List<CityDTO>> {

   private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
   private final WebScraper scraper;
   
   public Processor(WebScraper scraper) {
    this.scraper = scraper;
   }

   @Override
   public List<CityDTO> apply(DataCollectionJob job) {
       log.info("The job is {}, for route {} -> {}", job.jobId(), job.sourceCity().name(), job.destinationCity().name());

       List<RouteInfo> routeInfoList = scraper.scrapeData(job.sourceCity().name(), job.destinationCity().name(), job.dateToCollectResults());
       routeInfoList.sort(Comparator.comparing(RouteInfo::price));

       List<RouteInfo> uniqueTransportRouteList = new ArrayList<>();
       for (TransportType transportType : TransportType.values()) {
           Optional<RouteInfo> uniqueRoute = routeInfoList.stream().filter(route -> route.transportType().equals(transportType)).findFirst();
           uniqueRoute.ifPresent(uniqueTransportRouteList::add);
       }
       log.info("All unique routes found: {}", uniqueTransportRouteList);

       List<CityDTO> cityDTOs = new ArrayList<>();
       for (RouteInfo routeInfo  : uniqueTransportRouteList) {
           Duration duration;

           if (routeInfo.depart().contains("Any time")) {
               continue;
           } else {
               LocalTime arrivalTime = LocalTime.parse(routeInfo.arrival());
               LocalTime departureTime = LocalTime.parse(routeInfo.depart());
               if (arrivalTime.isBefore(departureTime)) {

                   // If arrival is before departure, add 24 hours
                   duration = Duration.between(departureTime, arrivalTime);
                   duration = duration.isNegative() ? duration.plusHours(24) : duration;
               } else {
                   duration = Duration.between(departureTime, arrivalTime);
               }
           }

           String routeTime = duration.toHoursPart() + "." + (duration.toMinutesPart());

           RouteDTO routeDTO = new RouteDTO(job.destinationCity(), 1.0, Double.parseDouble(routeTime), routeInfo.price(), routeInfo.transportType());
           CityDTO cityDTO = new CityDTO(job.sourceCity().id(), job.sourceCity().name(), routeDTO);
           cityDTOs.add(cityDTO);
            
       }
       return cityDTOs;
   }

//    public static Duration parseDuration(String timeStr) {
//        // Regex pattern to match hours and minutes
//        Pattern pattern = Pattern.compile("([+-]?\\d+)h\\s*(\\d+)m");
//        Matcher matcher = pattern.matcher(timeStr);

//        if (matcher.matches()) {
//            int hours = Integer.parseInt(matcher.group(1));
//            int minutes = Integer.parseInt(matcher.group(2));
//            return Duration.ofHours(hours).plusMinutes(minutes);
//        } else {
//            throw new IllegalArgumentException("Invalid time format: " + timeStr);
//        }
//    }
}
