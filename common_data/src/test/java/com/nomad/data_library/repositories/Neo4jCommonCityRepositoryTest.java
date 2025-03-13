package com.nomad.data_library.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.*;
import java.util.stream.Collectors;

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
import com.nomad.data_library.domain.CityCriteria;
import com.nomad.data_library.domain.CityMetric;
import com.nomad.data_library.domain.CityMetrics;
import com.nomad.data_library.domain.TransportType;
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
        cityRepository = new Neo4jCommonCityRepository(neo4jClient, objectMapper, schema);
        countryRepository = new Neo4jCommonCountryRepository(neo4jClient, objectMapper, schema);

        savedCountryA = countryRepository.createCountry(countryA);
        savedCountryB = countryRepository.createCountry(countryB);
    }

    @Autowired
    Neo4jClient neo4jClient;

    String fetchId(String cityName) {
        Map<String, Object> cityId = neo4jClient
                .query(
                        "MATCH (city:City {name: $cityName}) " +
                                "RETURN city.id as id"
                )
                .bind(cityName).to("cityName")
                .fetch()
                .first()
                .get();
        return cityId.get("id").toString();
    }
    
    @Test
    void findById_shouldReturnEmptyOptionalOfCity_WhenCityDoesntExist() {
        String cityId = "notfound";

        Optional<Neo4jCity> fetchedCity = cityRepository.findById(cityId);
        assertThat(fetchedCity).isEmpty();
    }

    @Test
    void findById_shouldReturnCity_whenCityExists() {
        Neo4jCity createdCity = cityRepository.saveCityWithDepth0(cityA);

        Neo4jCity fetchedCity = cityRepository.findById(createdCity.getId()).get();
        assertThat(fetchedCity).isEqualTo(cityA);
    }

    @Test
    void findById_shouldNotPopulateRoutesRelationship_whenCityHasRoutes() {
        Neo4jCity cityABeforeRoutes = cityA;
        cityA = cityA.addRoute(routeAToB);
        Neo4jCity createdCity = cityRepository.saveCityWithDepth0(cityA);

        Neo4jCity fetchedCity = cityRepository.findById(createdCity.getId()).get();
        assertThat(fetchedCity).isEqualTo(cityABeforeRoutes);
        assertThat(fetchedCity.getRoutes()).isEmpty();
    }

    @Test
    void findById_shouldPopulateCountryRelationship_always() {
        Neo4jCity createdCity = cityRepository.saveCityWithDepth0(cityA);

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
        Neo4jCity createdCity = cityRepository.saveCityWithDepth0(cityA);

        Neo4jCity fetchedCity = cityRepository.findByIdFetchRoutes(createdCity.getId()).get();

        assertThat(fetchedCity).isEqualTo(cityA);
    }

    @Test
    void findByIdFetchRoutes_shouldPopulateCountryRelationship_always() {
        cityA = cityA.addRoute(routeAToB);
        Neo4jCity createdCityA = cityRepository.saveCityWithDepth0(cityA);
        Neo4jCity createdCityB = cityRepository.saveCityWithDepth0(cityB);

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
        cityA = cityA.addRoute(routeAToB);
        String cityCName = "CityC";
        Neo4jCity cityC = Neo4jTestGenerator.neo4jCityNoRoutes(cityCName, countryB);
        cityA = cityA.addRoute(Neo4jRoute.of(cityC, 4, 3, 30.0, TransportType.BUS));

        Neo4jCity createdCityA = cityRepository.saveCityWithDepth0(cityA);

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
        cityA = cityA.addRoute(routeAToB);
        cityA = cityA.addRoute(Neo4jTestGenerator.neo4jRoute(cityC));
        Neo4jCity createdCity = cityRepository.saveCityWithDepth0(cityA);

        Neo4jCity fetchedCity = cityRepository.findByIdFetchRoutes(createdCity.getId()).get();

        Set<String> routeTargetNames = fetchedCity.getRoutes().stream().map((route) -> route.getTargetCity().getName()).collect(Collectors.toSet());
        assertThat(fetchedCity).isEqualTo(cityA);
        assertThat(fetchedCity.getRoutes()).isNotEmpty();
        assertThat(fetchedCity.getRoutes().size()).isEqualTo(2);
        assertThat(routeTargetNames).containsAll(Set.of("CityB", "CityC"));
    }

    @Test
    void findByIdFetchRoutes_shouldNotPopulateRoutesRelationship_whenCityDoesntHaveAnyRoutes() {
        Neo4jCity createdCity = cityRepository.saveCityWithDepth0(cityA);

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
         cityA = cityA.addRoute(routeAToB);
         cityRepository.saveCityWithDepth0(cityA);
         cityRepository.saveCityWithDepth0(cityB);

         Set<Neo4jCity> allCities = cityRepository.findAllCities();
         List<String> allCityNames = allCities.stream().map(Neo4jCity::getName).toList();
         assertThat(allCities.size()).isEqualTo(2);
         assertThat(allCityNames.size()).isEqualTo(2);
         assertThat(allCityNames).containsAll(List.of("CityA", "CityB"));
     }

     @Test
     void findAllCities_shouldPopulateRoutesRelationship_ifRoutesExist() {
         cityA = cityA.addRoute(routeAToB);
         cityRepository.saveCityWithDepth0(cityA);
         cityRepository.saveCityWithDepth0(cityB);

         Set<Neo4jCity> allCities = cityRepository.findAllCities();

         Neo4jCity createdCityA = allCities.stream().filter((city -> city.getName().equals(cityAName))).findFirst().get();
         Neo4jCity createdCityB = allCities.stream().filter((city -> city.getName().equals(cityBName))).findFirst().get();

         assertThat(createdCityA).isEqualTo(cityA);
         assertThat(createdCityB.getRoutes()).isEmpty();
     }

     @Test
     void findAllCities_shouldPopulateCountriesRelationship_always() {
         cityA = cityA.addRoute(routeAToB);
         cityRepository.saveCityWithDepth0(cityA);
         cityRepository.saveCityWithDepth0(cityB);

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
         cityA = cityA.addRoute(routeAToB);
         cityRepository.saveCityWithDepth0(cityA);
         cityRepository.saveCityWithDepth0(cityB);

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
     void saveCityWithDepth0_createsCityNode_ifNotExist() {
         Set<Neo4jCity> allCities = cityRepository.findAllCities();
         String cityAId = cityRepository.saveCityWithDepth0(cityA).getId();

         Neo4jCity createdCity = cityRepository.findByIdFetchRoutes(cityAId).get();

         assertThat(allCities).isEmpty();
         assertThat(createdCity).isEqualTo(cityA);
     }

     @Test
     void saveCityWithDepth0_doesntRecreateCityNode_ifExist() {
         String firstSaveId = cityRepository.saveCityWithDepth0(cityA).getId();
         Neo4jCity cityAAfterFirstSave = cityRepository.findByIdFetchRoutes(firstSaveId).get();

         String secondSaveId = cityRepository.saveCityWithDepth0(cityA).getId();

         Set<Neo4jCity> allCities = cityRepository.findAllCities();

         assertThat(firstSaveId).isEqualTo(secondSaveId);
         assertThat(allCities).isEqualTo(Set.of(cityAAfterFirstSave));
     }

     @Test
     void saveCityWithDepth0_overwritesCityNodeMetrics_ifExist() {

         String firstSaveId = cityRepository.saveCityWithDepth0(cityA).getId();
         Neo4jCity cityAAfterFirstSave = cityRepository.findByIdFetchRoutes(firstSaveId).get();

         CityMetrics newCityMetrics = new CityMetrics(
                 new CityMetric(CityCriteria.SAILING, 3.9),
                 new CityMetric(CityCriteria.FOOD, 4.6),
                 new CityMetric(CityCriteria.NIGHTLIFE, 3.3)
         );
         cityA = new Neo4jCity(cityA.getId(), cityA.getName(), newCityMetrics, cityA.getRoutes(), cityA.getCountry());

         String cityASecondSaveId = cityRepository.saveCityWithDepth0(cityA).getId();
         Neo4jCity cityAAfterSecondSave = cityRepository.findById(cityASecondSaveId).get();

         assertThat(cityAAfterSecondSave.getCityMetrics()).isNotEqualTo(cityAAfterFirstSave.getCityMetrics());
         assertThat(firstSaveId).isEqualTo(cityASecondSaveId);
     }

     @Test
     void saveCityWithDepth0_createsCityBiDirectionalRelationshipToCountry_ifNotExist() {

         String cityAId = cityRepository.saveCityWithDepth0(cityA).getId();
         Neo4jCity createdCity = cityRepository.findByIdFetchRoutes(cityAId).get();

         Neo4jCountry dbCountry = countryRepository.findByIdFetchCities(savedCountryA.getId()).get();

         assertThat(createdCity.getCountry().getName()).isEqualTo(dbCountry.getName());
         assertThat(dbCountry.getCities().stream().findFirst().get())
                 .usingRecursiveComparison()
                 .ignoringFields("routes", "country")
                 .isEqualTo(createdCity);
     }

     @Test
     void saveCityWithDepth0_createsTargetCityNodeAndSetsProperties_ifNotExist() {
         cityA = cityA.addRoute(routeAToB);
         cityRepository.saveCityWithDepth0(cityA);

         String cityBId = fetchId(cityBName);

         Neo4jCity createdCityB = cityRepository.findByIdFetchRoutes(cityBId).get();

         assertThat(createdCityB).isNotNull();
         assertThat(createdCityB).isEqualTo(cityB);
     }

     @Test
     void saveCityWithDepth0_doesntOverwriteTargetCityNodeProperties_ifExist() {
         Neo4jCity cityBAfterFirstSave = cityRepository.saveCityWithDepth0(cityB);

         CityMetrics newCityMetrics = new CityMetrics(
                 new CityMetric(CityCriteria.SAILING, 3.4),
                 new CityMetric(CityCriteria.FOOD, 4.3),
                 new CityMetric(CityCriteria.NIGHTLIFE, 3.3)
         );
         cityB = new Neo4jCity(cityB.getId(), cityB.getName(), newCityMetrics, cityB.getRoutes(), cityB.getCountry());
         cityA = cityA.addRoute(Neo4jRoute.of(cityB, 3, 4, 10.0, TransportType.BUS));

         cityRepository.saveCityWithDepth0(cityA).getId();

         String cityBId = fetchId(cityBName);
         Neo4jCity cityBAfterSecondSave = cityRepository.findByIdFetchRoutes(cityBId).get();

         assertThat(cityBAfterFirstSave.getCityMetrics()).isEqualTo(cityBAfterSecondSave.getCityMetrics());
         assertThat(cityBAfterFirstSave.getId()).isEqualTo(cityBAfterSecondSave.getId());
     }

     @Test
     void saveCityWithDepth0_createsTargetCityBiDirectionalRelationshipToCountry_ifNotExist() {
         cityA = cityA.addRoute(routeAToB);
         cityRepository.saveCityWithDepth0(cityA).getId();

         String cityBId = fetchId(cityBName);
         Neo4jCity createdCityB = cityRepository.findByIdFetchRoutes(cityBId).get();
         Neo4jCountry dbCountry = countryRepository.findByIdFetchCities(savedCountryA.getId()).get();

         assertThat(createdCityB.getCountry().getName()).isEqualTo(dbCountry.getName());

         Set<Neo4jCity> dbCountryCityB = dbCountry.getCities().stream().filter(city -> Objects.equals(city.getName(), cityBName)).collect(Collectors.toSet());
         assertThat(dbCountryCityB)
                 .usingRecursiveComparison()
                 .ignoringFields("routes", "country")
                         .isEqualTo(Set.of(createdCityB));
         assertThat(createdCityB).isEqualTo(cityB);
     }

     @Test
     void saveCityWithDepth0_doesntRecreateTargetCityNode_ifExist() {

         Neo4jCity cityBAfterFirstSave = cityRepository.saveCityWithDepth0(cityB);

         cityA = cityA.addRoute(routeAToB);
         Neo4jCity cityASave = cityRepository.saveCityWithDepth0(cityA);

         Neo4jCity cityBAfterSecondSave = cityRepository.findByIdFetchRoutes(cityBAfterFirstSave.getId()).get();

         Set<Neo4jCity> allCities = cityRepository.findAllCities();

         assertThat(cityBAfterFirstSave.getId()).isEqualTo(cityBAfterSecondSave.getId());
         assertThat(cityBAfterFirstSave).isEqualTo(cityBAfterSecondSave);
         // Order doesnt matter here
         assertThat(allCities).containsAll(List.of(cityASave, cityBAfterFirstSave));
         assertThat(List.of(cityASave, cityBAfterFirstSave)).containsAll(allCities);
     }

     @Test
     void saveCityWithDepth0_createRouteRelationshipToTargetCityNode_ifNotExist() {
         cityA = cityA.addRoute(routeAToB);
         Neo4jCity createdCityA = cityRepository.saveCityWithDepth0(cityA);

         Neo4jRoute createdRouteAToB = createdCityA.getRoutes().stream().findFirst().get();
         assertThat(createdCityA.getRoutes().size()).isEqualTo(1);
         assertThat(createdRouteAToB)
                 .usingRecursiveComparison()
                 .ignoringFields("targetCity.country")
                 .isEqualTo(routeAToB);

     }

     @Test
     void saveCityWithDepth0_createsARouteRelationshipToTargetCityForEachTransportType_ifThereAreTwoTransportTypeRoutes() {
         cityA = cityA.addRoute(UUID.randomUUID().toString(), cityB, 4, 3, 16.0, TransportType.BUS);
         cityA = cityA.addRoute(UUID.randomUUID().toString(), cityB, 4, 3, 30.0, TransportType.FLIGHT);
         Neo4jCity createdCityA = cityRepository.saveCityWithDepth0(cityA);

         List<String> allTargetCities = createdCityA.getRoutes().stream().map(route -> route.getTargetCity().getName()).distinct().toList();
         List<TransportType> allTransportTypes = createdCityA.getRoutes().stream().map(route -> route.getTransportType()).distinct().toList();

         assertThat(createdCityA.getRoutes().size()).isEqualTo(2);
         assertThat(allTargetCities.size()).isEqualTo(1);
         assertThat(allTargetCities).isEqualTo(List.of("CityB"));
         assertThat(allTransportTypes.size()).isEqualTo(2);
         assertThat(allTransportTypes).containsAll(List.of(TransportType.BUS, TransportType.FLIGHT));
     }

     @Test
     void saveCityWithDepth0_doesntTouchTargetCityRouteRelationships_ever() {
         String cityCName = "CityC";
         Neo4jCity cityC = Neo4jTestGenerator.neo4jCityNoRoutes(cityCName, countryA);
         cityB = cityB.addRoute(UUID.randomUUID().toString(), cityC, 4, 3, 5.0, TransportType.BUS);

         Neo4jCity cityBAfterFirstSave = cityRepository.saveCityWithDepth0(cityB);
         String cityCId = fetchId(cityCName);
         Neo4jCity cityCAfterFirstSave = cityRepository.findByIdFetchRoutes(cityCId).get();

         Neo4jCity cityBResetRoutes = new Neo4jCity(cityB.getId(), cityBName, cityB.getCityMetrics(), Set.of(), cityB.getCountry());
         cityA = cityA.addRoute(UUID.randomUUID().toString(), cityBResetRoutes, 4, 3, 9.0, TransportType.BUS);
         Neo4jCity cityAAfterSecondSave = cityRepository.saveCityWithDepth0(cityA);

         String cityBId = fetchId(cityBName);
         Neo4jCity cityBAfterSecondSave = cityRepository.findByIdFetchRoutes(cityBId).get();

         Set<Neo4jCity> allCities = cityRepository.findAllCities();

         assertThat(allCities.size()).isEqualTo(3);
         assertThat(allCities).containsAll(Set.of(cityBAfterFirstSave, cityCAfterFirstSave, cityAAfterSecondSave));
         assertThat(Set.of(cityBAfterFirstSave, cityCAfterFirstSave, cityAAfterSecondSave)).containsAll(allCities);

         assertThat(cityBAfterSecondSave.getRoutes()).isEqualTo(cityBAfterFirstSave.getRoutes());
         assertThat(cityBResetRoutes.getRoutes()).isNotEqualTo(cityB.getRoutes());
     }

     @Test
     void mapifyCity_shouldRecursivelyStringifyCityMetricsFields() {
         CityMetrics cityAMetrics = new CityMetrics(
                 new CityMetric(CityCriteria.SAILING, 8.4),
                 new CityMetric(CityCriteria.FOOD, 5.4),
                 new CityMetric(CityCriteria.NIGHTLIFE, 4.3)
         );
         CityMetrics cityBMetrics = new CityMetrics(
                 new CityMetric(CityCriteria.SAILING, 4.3),
                 new CityMetric(CityCriteria.FOOD, 3.3),
                 new CityMetric(CityCriteria.NIGHTLIFE, 2.2)
         );
         Neo4jCity cityA =  Neo4jCity.of("CityA", cityAMetrics, Set.of(), countryA);
         Neo4jCity cityB =  Neo4jCity.of("CityB", cityBMetrics, Set.of(), countryA);

         cityA = cityA.addRoute(Neo4jTestGenerator.neo4jRoute(cityB));
         Map<String, Object> mapifiedCity = cityRepository.mapifyCity(cityA);

         Object cityACityMetrics = mapifiedCity.get("cityMetrics");

         ArrayList<LinkedHashMap<String, Object>> cityARoutes = (ArrayList<LinkedHashMap<String, Object>>) mapifiedCity.get("routes");
         LinkedHashMap<String, Object> routeToB = cityARoutes.get(0);
         LinkedHashMap<String, Object> cityBFetched = (LinkedHashMap<String, Object>) routeToB.get("targetCity");
         Object cityBCityMetrics = cityBFetched.get("cityMetrics");

         assertThat(cityACityMetrics).isEqualTo("{\"sailing\":{\"criteria\":\"SAILING\",\"metric\":8.4},\"food\":{\"criteria\":\"FOOD\",\"metric\":5.4},\"nightlife\":{\"criteria\":\"NIGHTLIFE\",\"metric\":4.3}}");
         assertThat(cityBCityMetrics).isEqualTo("{\"sailing\":{\"criteria\":\"SAILING\",\"metric\":4.3},\"food\":{\"criteria\":\"FOOD\",\"metric\":3.3},\"nightlife\":{\"criteria\":\"NIGHTLIFE\",\"metric\":2.2}}");
     }
}
