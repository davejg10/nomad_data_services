package com.nomad.data_library.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.*;
import java.util.stream.Collectors;

import com.nomad.common_utils.domain.TransportType;
import com.nomad.data_library.GenericTestGenerator;
import com.nomad.data_library.config.Neo4jConfig;
import com.nomad.data_library.domain.neo4j.Neo4jCity;
import com.nomad.data_library.domain.neo4j.Neo4jCountry;
import com.nomad.data_library.domain.neo4j.Neo4jRoute;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.neo4j.core.mapping.Neo4jMappingContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomad.data_library.Neo4jTestConfiguration;
import com.nomad.data_library.Neo4jTestGenerator;
import com.nomad.data_library.TestConfig;
import com.nomad.data_library.exceptions.Neo4jGenericException;

import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;

@Log4j2
@DataNeo4jTest
@Import({TestConfig.class, Neo4jTestConfiguration.class, Neo4jConfig.class})
@Transactional
public class Neo4jCommonCityRepositoryTest {

    private Neo4jCommonCityRepository cityRepository;
    private Neo4jCommonCountryRepository countryRepository;

    Neo4jCountry countryA =  Neo4jTestGenerator.neo4jCountryNoCities("CountryA");
    Neo4jCountry countryB =  Neo4jTestGenerator.neo4jCountryNoCities("CountryB");

    Neo4jCountry savedCountryA;
    Neo4jCountry savedCountryB;

    String cityAName = "CityA";
    String cityBName = "CityB";
    Neo4jCity cityA = Neo4jTestGenerator.neo4jCityNoRoutes(cityAName, countryA);
    Neo4jCity cityB = Neo4jTestGenerator.neo4jCityNoRoutes(cityBName, countryA);

    Neo4jRoute routeAToB = Neo4jTestGenerator.neo4jRoute(cityB);

    @BeforeEach
    void setup(@Autowired Neo4jClient neo4jClient, @Autowired Neo4jMappingContext schema, @Autowired ObjectMapper objectMapper) throws Neo4jGenericException {
        Neo4jCommonCityMappers neo4jCityMappers = new Neo4jCommonCityMappers(schema);
        Neo4jCommonCountryMappers neo4jCountryMappers = new Neo4jCommonCountryMappers(schema);

        cityRepository = new Neo4jCommonCityRepository(neo4jClient, objectMapper, neo4jCityMappers);
        countryRepository = new Neo4jCommonCountryRepository(neo4jClient, objectMapper, neo4jCountryMappers);

        savedCountryA = countryRepository.createCountry(countryA);
        savedCountryB = countryRepository.createCountry(countryB);
    }

    @Test
    void findById_shouldReturnEmptyOptionalOfCity_WhenCityDoesntExist() {
        String cityId = "notfound";

        Optional<Neo4jCity> fetchedCity = cityRepository.findById(cityId);
        assertThat(fetchedCity).isEmpty();
    }

    @Test
    void findById_shouldReturnCity_whenCityExists() {
        Neo4jCity createdCity = cityRepository.createCity(cityA);

        Neo4jCity fetchedCity = cityRepository.findById(createdCity.getId()).get();
        assertThat(fetchedCity).isEqualTo(cityA);
    }

    @Test
    void findById_shouldNotPopulateRoutesRelationship_whenCityHasRoutes() {
        Neo4jCity cityABeforeRoutes = cityA;
        cityRepository.createCity(cityA);
        cityRepository.createCity(cityB);
        cityA = cityA.addRoute(routeAToB);
        Neo4jCity createdCity = cityRepository.saveRoute(cityA);

        Neo4jCity fetchedCity = cityRepository.findById(createdCity.getId()).get();
        assertThat(fetchedCity).isEqualTo(cityABeforeRoutes);
        assertThat(fetchedCity.getRoutes()).isEmpty();
    }

    @Test
    void findById_shouldPopulateCountryRelationship_always() {
        Neo4jCity createdCity = cityRepository.createCity(cityA);

        Neo4jCity fetchedCity = cityRepository.findById(createdCity.getId()).get();

        assertThat(fetchedCity.getCountry())
                .usingRecursiveComparison()
                .ignoringFields("id", "cities")
                .isEqualTo(countryA);
    }

    @Test
    void findByIdFetchRoutes_shouldReturnEmptyOptionalOfCity_whenCityDoesntExist() {
        String cityId = "notfound";

        Optional<Neo4jCity> fetchedCity = cityRepository.findByIdFetchRoutes(cityId);

        assertThat(fetchedCity).isEmpty();
    }

    @Test
    void findByIdFetchRoutes_shouldReturnCity_whenCityExists() {
        Neo4jCity createdCity = cityRepository.createCity(cityA);

        Neo4jCity fetchedCity = cityRepository.findByIdFetchRoutes(createdCity.getId()).get();

        assertThat(fetchedCity).isEqualTo(cityA);
    }

    @Test
    void findByIdFetchRoutes_shouldPopulateCountryRelationship_always() {
        Neo4jCity createdCityA = cityRepository.createCity(cityA);
        Neo4jCity createdCityB = cityRepository.createCity(cityB);

        Neo4jCity fetchedCityA = cityRepository.findByIdFetchRoutes(createdCityA.getId()).get();

        Neo4jCity fetchedCityB = cityRepository.findByIdFetchRoutes(createdCityB.getId()).get();

        assertThat(fetchedCityA.getCountry())
                .usingRecursiveComparison()
                .ignoringFields("id", "cities")
                .isEqualTo(countryA);
        assertThat(fetchedCityB.getCountry())
                .usingRecursiveComparison()
                .ignoringFields("id", "cities")
                .isEqualTo(countryA);
    }

    @Test
    void findByIdFetchRoutes_shouldPopulateTargetCitiesCountryRelationship_always() {
        String cityCName = "CityC";
        Neo4jCity cityC = Neo4jTestGenerator.neo4jCityNoRoutes(cityCName, countryB);
        Neo4jCity createdCityA = cityRepository.createCity(cityA);
        cityRepository.createCity(cityB);
        cityRepository.createCity(cityC);

        cityA = cityA.addRoute(routeAToB).addRoute(Neo4jTestGenerator.neo4jRoute(cityC));
        cityRepository.saveRoute(cityA);

        Neo4jCity fetchedCityA = cityRepository.findByIdFetchRoutes(createdCityA.getId()).get();

        Neo4jCity fetchedCityB = fetchedCityA.getRoutes().stream().filter(route -> route.getTargetCity().getName().equals(cityBName)).findFirst().get().getTargetCity();
        Neo4jCity fetchedCityC = fetchedCityA.getRoutes().stream().filter(route -> route.getTargetCity().getName().equals(cityCName)).findFirst().get().getTargetCity();

        assertThat(fetchedCityB.getCountry())
                .usingRecursiveComparison()
                .ignoringFields("id", "cities")
                .isEqualTo(countryA);
        assertThat(fetchedCityC.getCountry())
                .usingRecursiveComparison()
                .ignoringFields("id", "cities")
                .isEqualTo(countryB);
    }

    @Test
    void findByIdFetchRoutes_shouldPopulateRoutesRelationshipWithAllTargetCities_whenCityHasRoutes() {
        Neo4jCity cityC = Neo4jTestGenerator.neo4jCityNoRoutes("CityC", countryB);
        cityRepository.createCity(cityA);
        cityRepository.createCity(cityB);
        cityRepository.createCity(cityC);

        cityA = cityA.addRoute(routeAToB);
        cityA = cityA.addRoute(Neo4jTestGenerator.neo4jRoute(cityC));
        Neo4jCity cityWithRoutes = cityRepository.saveRoute(cityA);

        Neo4jCity fetchedCity = cityRepository.findByIdFetchRoutes(cityWithRoutes.getId()).get();

        Set<String> routeTargetNames = fetchedCity.getRoutes().stream().map((route) -> route.getTargetCity().getName()).collect(Collectors.toSet());
        assertThat(fetchedCity).isEqualTo(cityA);
        assertThat(fetchedCity.getRoutes()).isNotEmpty();
        assertThat(fetchedCity.getRoutes().size()).isEqualTo(2);
        assertThat(routeTargetNames).containsAll(Set.of("CityB", "CityC"));
    }

    @Test
    void findByIdFetchRoutes_shouldNotPopulateRoutesRelationship_whenCityDoesntHaveAnyRoutes() {
        cityRepository.createCity(cityA);
        Neo4jCity createdCity = cityRepository.saveRoute(cityA);

        Neo4jCity fetchedCity = cityRepository.findByIdFetchRoutes(createdCity.getId()).get();

        assertThat(fetchedCity).isEqualTo(cityA);
        assertThat(fetchedCity.getRoutes()).isEmpty();
    }

    @Test
    void findAllCities_shouldReturnEmptyList_whenNoCitiesExist() {
        Set<Neo4jCity> allCities = cityRepository.findAllCities();

        assertThat(allCities).isEmpty();
    }

     @Test
     void findAllCities_shouldReturnListContainingAllCities_ifCitiesExist() {
         cityRepository.createCity(cityA);
         cityRepository.createCity(cityB);

         Set<Neo4jCity> allCities = cityRepository.findAllCities();
         List<String> allCityNames = allCities.stream().map(Neo4jCity::getName).toList();
         assertThat(allCities.size()).isEqualTo(2);
         assertThat(allCityNames.size()).isEqualTo(2);
         assertThat(allCityNames).containsAll(List.of("CityA", "CityB"));
     }

     @Test
     void findAllCities_shouldPopulateRoutesRelationship_ifRoutesExist() {
         cityRepository.createCity(cityA);
         cityRepository.createCity(cityB);
         cityA = cityA.addRoute(routeAToB);
         cityRepository.saveRoute(cityA);

         Set<Neo4jCity> allCities = cityRepository.findAllCities();

         Neo4jCity createdCityA = allCities.stream().filter((city -> city.getName().equals(cityAName))).findFirst().get();
         Neo4jCity createdCityB = allCities.stream().filter((city -> city.getName().equals(cityBName))).findFirst().get();

         assertThat(createdCityA).isEqualTo(cityA);
         assertThat(createdCityB.getRoutes()).isEmpty();
     }

     @Test
     void findAllCities_shouldPopulateCountriesRelationship_always() {
         cityRepository.createCity(cityA);
         cityRepository.createCity(cityB);

         cityA = cityA.addRoute(routeAToB);
         cityRepository.saveRoute(cityA);

         Set<Neo4jCity> allCities = cityRepository.findAllCities();
         assertThat(allCities.size()).isEqualTo(2);
         for (Neo4jCity city : allCities) {

             assertThat(city.getCountry())
                     .usingRecursiveComparison()
                     .ignoringFields("id", "cities")
                     .isEqualTo(countryA);
         }
     }

     @Test
     void findAllCities_shouldPopulateTargetCitiesCountriesRelationship_always() {
         cityRepository.createCity(cityA);
         cityRepository.createCity(cityB);

         cityA = cityA.addRoute(routeAToB);
         cityRepository.saveRoute(cityA);
         cityRepository.saveRoute(cityB);

         Set<Neo4jCity> allCities = cityRepository.findAllCities();
         assertThat(allCities.size()).isEqualTo(2);
         for (Neo4jCity city : allCities) {

             assertThat(city.getCountry())
                     .usingRecursiveComparison()
                     .ignoringFields("id", "cities")
                     .isEqualTo(countryA);

             for (Neo4jRoute route : city.getRoutes()) {
                 assertThat(route.getTargetCity().getCountry()).isNotNull();
             }
         }
     }

     @Test
     void createCity_createsCityNode_ifNotExist() {
         Set<Neo4jCity> allCities = cityRepository.findAllCities();
         Neo4jCity createdCity = cityRepository.createCity(cityA);

         assertThat(allCities).isEmpty();
         assertThat(createdCity).isEqualTo(cityA);
     }
     
    @Test
    void createCity_doesntRecreateCityNode_ifExist() {
        Neo4jCity cityAAfterFirstSave = cityRepository.createCity(cityA);

        Neo4jCity cityAAfterSecondSave = cityRepository.createCity(cityA);

        Set<Neo4jCity> allCities = cityRepository.findAllCities();

        assertThat(cityAAfterFirstSave).isEqualTo(cityAAfterSecondSave);
        assertThat(allCities).isEqualTo(Set.of(cityAAfterFirstSave));
    }

    @Test
    void createCity_overwritesAllPropertiesExceptName_ifExist() {

        Neo4jCity cityAAfterFirstSave = cityRepository.createCity(cityA);

        cityA = new Neo4jCity(cityA.getId(), "new name", "newdescription", "new blob url", Neo4jTestGenerator.generateCoords(), GenericTestGenerator.cityMetrics(), cityA.getRoutes(), cityA.getCountry());

        Neo4jCity cityAAfterSecondSave = cityRepository.createCity(cityA);

        assertThat(cityAAfterSecondSave.getCityMetrics()).isNotEqualTo(cityAAfterFirstSave.getCityMetrics());
        assertThat(cityAAfterSecondSave.getCoordinate()).isNotEqualTo(cityAAfterFirstSave.getCoordinate());
        assertThat(cityAAfterSecondSave.getShortDescription()).isNotEqualTo(cityAAfterFirstSave.getShortDescription());
        assertThat(cityAAfterSecondSave.getPrimaryBlobUrl()).isNotEqualTo(cityAAfterFirstSave.getPrimaryBlobUrl());
        assertThat(cityAAfterSecondSave.getName()).isEqualTo(cityAAfterFirstSave.getName());

        assertThat(cityAAfterFirstSave.getId()).isEqualTo(cityAAfterSecondSave.getId());
    }

    @Test
    void createCity_createsCityBiDirectionalRelationshipToCountry_ifNotExist() {

        Neo4jCity createdCity = cityRepository.createCity(cityA);

        Neo4jCountry dbCountry = countryRepository.findByIdFetchCities(savedCountryA.getId()).get();

        assertThat(createdCity.getCountry().getName()).isEqualTo(dbCountry.getName());
        assertThat(dbCountry.getCities().stream().findFirst().get())
                .usingRecursiveComparison()
                .ignoringFields("routes", "cityMetrics", "country")
                .isEqualTo(createdCity);
    }

     @Test
     void saveRoute_doesntOverwriteTargetCityNodeProperties_ifExist() {
         cityRepository.createCity(cityA);
         Neo4jCity cityBAfterFirstSave = cityRepository.createCity(cityB);


         cityB = new Neo4jCity(cityB.getId(), cityB.getName(), "different", "different url", Neo4jTestGenerator.generateCoords(), GenericTestGenerator.cityMetrics(), cityB.getRoutes(), cityB.getCountry());
         cityA = cityA.addRoute(Neo4jTestGenerator.neo4jRoute(cityB));

         cityRepository.saveRoute(cityA);
         Neo4jCity cityBAfterSecondSave = cityRepository.findByName(cityBName).get();

         assertThat(cityBAfterFirstSave.getCityMetrics()).isEqualTo(cityBAfterSecondSave.getCityMetrics());
         assertThat(cityBAfterFirstSave.getId()).isEqualTo(cityBAfterSecondSave.getId());
     }

     @Test
     void saveRoute_doesntRecreateTargetCityNode_ifExist() {
         cityRepository.createCity(cityA);
         Neo4jCity cityBAfterFirstSave = cityRepository.createCity(cityB);

         cityA = cityA.addRoute(routeAToB);
         Neo4jCity cityASave = cityRepository.saveRoute(cityA);

         Neo4jCity cityBAfterSecondSave = cityRepository.findByIdFetchRoutes(cityBAfterFirstSave.getId()).get();

         Set<Neo4jCity> allCities = cityRepository.findAllCities();

         assertThat(cityBAfterFirstSave.getId()).isEqualTo(cityBAfterSecondSave.getId());
         assertThat(cityBAfterFirstSave).isEqualTo(cityBAfterSecondSave);
         // Order doesnt matter here
         assertThat(allCities).containsAll(List.of(cityASave, cityBAfterFirstSave));
         assertThat(List.of(cityASave, cityBAfterFirstSave)).containsAll(allCities);
     }

     @Test
     void saveRoute_createRouteRelationshipToTargetCityNode_ifNotExist() {
         cityRepository.createCity(cityA);
         cityRepository.createCity(cityB);

         cityA = cityA.addRoute(routeAToB);
         Neo4jCity createdCityA = cityRepository.saveRoute(cityA);

         Neo4jRoute createdRouteAToB = createdCityA.getRoutes().stream().findFirst().get();
         assertThat(createdCityA.getRoutes().size()).isEqualTo(1);
         assertThat(createdRouteAToB).isEqualTo(routeAToB);
     }

     @Test
     void saveRoute_createsARouteRelationshipToTargetCityForEachTransportType_ifThereAreTwoTransportTypeRoutes() {
         cityRepository.createCity(cityA);
         cityRepository.createCity(cityB);
         cityA = cityA.addRoute(UUID.randomUUID().toString(), cityB, 4, Neo4jTestGenerator.calculateRandomDuration(), Neo4jTestGenerator.calculateRandomCost(), TransportType.BUS);
         cityA = cityA.addRoute(UUID.randomUUID().toString(), cityB, 4, Neo4jTestGenerator.calculateRandomDuration(), Neo4jTestGenerator.calculateRandomCost(), TransportType.FLIGHT);
         Neo4jCity createdCityA = cityRepository.saveRoute(cityA);

         List<String> allTargetCities = createdCityA.getRoutes().stream().map(route -> route.getTargetCity().getName()).distinct().toList();
         List<TransportType> allTransportTypes = createdCityA.getRoutes().stream().map(route -> route.getTransportType()).distinct().toList();

         assertThat(createdCityA.getRoutes().size()).isEqualTo(2);
         assertThat(allTargetCities.size()).isEqualTo(1);
         assertThat(allTargetCities).isEqualTo(List.of("CityB"));
         assertThat(allTransportTypes.size()).isEqualTo(2);
         assertThat(allTransportTypes).containsAll(List.of(TransportType.BUS, TransportType.FLIGHT));
     }

     @Test
     void saveRoute_doesntTouchTargetCityRouteRelationships_ever() {
         String cityCName = "CityC";
         Neo4jCity cityC = Neo4jTestGenerator.neo4jCityNoRoutes(cityCName, countryA);
         cityRepository.createCity(cityA);
         cityRepository.createCity(cityB);
         cityRepository.createCity(cityC);

         cityB = cityB.addRoute(UUID.randomUUID().toString(), cityC, 4, Neo4jTestGenerator.calculateRandomDuration(), Neo4jTestGenerator.calculateRandomCost(), TransportType.BUS);

         Neo4jCity cityBAfterFirstSave = cityRepository.saveRoute(cityB);
         Neo4jCity cityCAfterFirstSave = cityRepository.findByIdFetchRoutes(cityC.getId()).get();

         Neo4jCity cityBResetRoutes = new Neo4jCity(cityB.getId(), cityBName, cityB.getShortDescription(), cityB.getPrimaryBlobUrl(), cityB.getCoordinate(), cityB.getCityMetrics(), Set.of(), cityB.getCountry());
         cityA = cityA.addRoute(UUID.randomUUID().toString(), cityBResetRoutes, 4, Neo4jTestGenerator.calculateRandomDuration(), Neo4jTestGenerator.calculateRandomCost(), TransportType.BUS);
         Neo4jCity cityAAfterSecondSave = cityRepository.saveRoute(cityA);

         Neo4jCity cityBAfterSecondSave = cityRepository.findByIdFetchRoutes(cityB.getId()).get();

         Set<Neo4jCity> allCities = cityRepository.findAllCities();

         assertThat(allCities.size()).isEqualTo(3);
         assertThat(allCities).containsAll(Set.of(cityBAfterFirstSave, cityCAfterFirstSave, cityAAfterSecondSave));
         assertThat(Set.of(cityBAfterFirstSave, cityCAfterFirstSave, cityAAfterSecondSave)).containsAll(allCities);

         assertThat(cityBAfterSecondSave.getRoutes()).isEqualTo(cityBAfterFirstSave.getRoutes());
         assertThat(cityBResetRoutes.getRoutes()).isNotEqualTo(cityB.getRoutes());
     }
}
