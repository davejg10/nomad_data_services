package com.nomad.admin_api.services;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.neo4j.core.Neo4jClient;

import com.nomad.admin_api.functions.put_route_popularity.RoutePopularityDTO;
import com.nomad.admin_api.repositories.Neo4jCityRepository;
import com.nomad.admin_api.repositories.Neo4jCountryRepository;
import com.nomad.data_library.GenericTestGenerator;
import com.nomad.data_library.Neo4jTestGenerator;
import com.nomad.data_library.domain.neo4j.Neo4jCity;
import com.nomad.data_library.domain.neo4j.Neo4jCountry;
import com.nomad.data_library.domain.neo4j.Neo4jRoute;
import com.nomad.data_library.domain.sql.RoutePopularity;
import com.nomad.data_library.domain.sql.RoutePopularityId;
import com.nomad.data_library.domain.sql.SqlCity;
import com.nomad.data_library.domain.sql.SqlCountry;
import com.nomad.data_library.repositories.RoutePopularityRepository;
import com.nomad.data_library.repositories.SqlCityRepository;
import com.nomad.data_library.repositories.SqlCountryRepository;

import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootTest
@Import({com.nomad.data_library.Neo4jTestConfiguration.class})
public class RoutePopularityServiceTest {

    @Autowired
    private RoutePopularityRepository routePopularityRepository;

    @Autowired
    private SqlCountryRepository sqlCountryRepository;
    
    @Autowired
    private SqlCityRepository sqlCityRepository;

    @Autowired
    private Neo4jCityRepository neo4jCityRepository;

    @Autowired
    private Neo4jCountryRepository neo4jCountryRepository;

    @Autowired
    private RoutePopularityService routePopularityService;

    private String countryAName = "CountryA";
    private SqlCountry sqlCountry;
    private SqlCity sqlCityA;
    private SqlCity sqlCityB;
    private Neo4jCity neo4jCityA;
    private Neo4jCity neo4jCityB;

    @BeforeEach
    void setupDB() {
        sqlCountry = SqlCountry.of(countryAName, "A description of countryA");
        sqlCountry = sqlCountryRepository.save(sqlCountry);
        Neo4jCountry neo4jCountry = Neo4jTestGenerator.neo4jCountryFromSql(sqlCountry);
        neo4jCountryRepository.save(neo4jCountry);

        sqlCityA = SqlCity.of("CityA", "desc", GenericTestGenerator.cityMetrics(), sqlCountry.getId());
        sqlCityB = SqlCity.of("CityB", "desc", GenericTestGenerator.cityMetrics(), sqlCountry.getId());
        sqlCityA = sqlCityRepository.save(sqlCityA);
        sqlCityB = sqlCityRepository.save(sqlCityB);
        neo4jCityA = neo4jCityRepository.save(Neo4jTestGenerator.neo4jCityFromSql(sqlCityA, sqlCountry));
        neo4jCityB = neo4jCityRepository.save(Neo4jTestGenerator.neo4jCityFromSql(sqlCityB, sqlCountry));
    }

    @AfterEach
    void clearDB(@Autowired Neo4jClient neo4jClient) {
        routePopularityRepository.deleteAll();
        sqlCityRepository.deleteAll();
        sqlCountryRepository.deleteAll();
        neo4jClient.query("MATCH (n) DETACH DELETE n;").run();
    }

    @Test
    void updateRoutePopularity_createsRoutePopularityIfNotExist() {
        UUID cityAId = sqlCityA.getId();
        UUID cityBId = sqlCityB.getId();

        List<RoutePopularity> allRoutePopularities = (List<RoutePopularity>) routePopularityRepository.findAll();
        
        RoutePopularityDTO routePopularityDTO = new RoutePopularityDTO(cityAId, cityBId, 4);
        routePopularityService.updateRoutePopularity(routePopularityDTO);

        List<RoutePopularity> allRoutePopularitiesAfterSave = (List<RoutePopularity>) routePopularityRepository.findAll();
        RoutePopularity createdRoutePopularity = allRoutePopularitiesAfterSave.get(0);
        assertThat(allRoutePopularities.size()).isEqualTo(0);
        assertThat(allRoutePopularitiesAfterSave.size()).isEqualTo(1);
        assertThat(createdRoutePopularity.getId()).isEqualTo(new RoutePopularityId(cityAId, cityBId));
        assertThat(createdRoutePopularity.getPopularity()).isEqualTo(4);
    }

    @Test
    void updateRoutePopularity_updatesRoutePopularityIfExist() {
        UUID cityAId = sqlCityA.getId();
        UUID cityBId = sqlCityB.getId();

        RoutePopularity routePopularityfirstSave = routePopularityRepository.save(new RoutePopularity(cityAId, cityBId, 2));

        
        RoutePopularityDTO routePopularityDTO = new RoutePopularityDTO(cityAId, cityBId, 4);
        routePopularityService.updateRoutePopularity(routePopularityDTO);

        List<RoutePopularity> allRoutePopularitiesAfterSave = (List<RoutePopularity>) routePopularityRepository.findAll();
        RoutePopularity createdRoutePopularity = allRoutePopularitiesAfterSave.get(0);

        assertThat(allRoutePopularitiesAfterSave.size()).isEqualTo(1);
        assertThat(createdRoutePopularity.getId()).isEqualTo(routePopularityfirstSave.getId());
        assertThat(createdRoutePopularity.getPopularity()).isEqualTo(4);
    }

    @Test
    void updateRoutePopularity_updatesAnyNeo4jRoutesFromSourceCityToTargetCity_withNewPopularity() {
        UUID cityAId = sqlCityA.getId();
        UUID cityBId = sqlCityB.getId();

        Neo4jRoute routeAToB1 = Neo4jTestGenerator.neo4jRoute(neo4jCityB);
        Neo4jRoute routeAtToB2 = Neo4jTestGenerator.neo4jRoute(neo4jCityB);
        neo4jCityA = neo4jCityA.addRoute(routeAToB1).addRoute(routeAtToB2);
        neo4jCityA = neo4jCityRepository.saveRoute(neo4jCityA);
        
        RoutePopularityDTO routePopularityDTO = new RoutePopularityDTO(cityAId, cityBId, 4.0);
        routePopularityService.updateRoutePopularity(routePopularityDTO);

        Neo4jCity neo4jCityAAfterSave = neo4jCityRepository.findByIdFetchRoutes(neo4jCityA.getId()).get();

        List<Double> allOutboundRoutePopularitiesBeforeSave = neo4jCityA.getRoutes().stream().map((route) -> route.getPopularity()).distinct().toList();
        List<Double> allOutboundRoutePopularitiesAfterSave = neo4jCityAAfterSave.getRoutes().stream().map((route) -> route.getPopularity()).distinct().toList();

        assertThat(allOutboundRoutePopularitiesBeforeSave.size()).isEqualTo(1);
        assertThat(allOutboundRoutePopularitiesBeforeSave.get(0)).isEqualTo(0.0);
        assertThat(allOutboundRoutePopularitiesAfterSave.size()).isEqualTo(1);
        assertThat(allOutboundRoutePopularitiesAfterSave.get(0)).isEqualTo(4.0);
    }

    @Test
    void updateRoutePopularity_doesntChangePopularity_forAnyNeo4jRoutesFromTargetCityToSourceCity() {
        UUID cityAId = sqlCityA.getId();
        UUID cityBId = sqlCityB.getId();

        Neo4jRoute routeBToA = Neo4jTestGenerator.neo4jRoute(neo4jCityA);
        neo4jCityB = neo4jCityB.addRoute(routeBToA);
        neo4jCityB = neo4jCityRepository.saveRoute(neo4jCityB);
        
        RoutePopularityDTO routePopularityDTO = new RoutePopularityDTO(cityAId, cityBId, 4.0);
        routePopularityService.updateRoutePopularity(routePopularityDTO);

        Neo4jCity neo4jCityBAfterSave = neo4jCityRepository.findByIdFetchRoutes(neo4jCityB.getId()).get();

        List<Double> allOutboundRoutePopularitiesBeforeSave = neo4jCityB.getRoutes().stream().map((route) -> route.getPopularity()).distinct().toList();
        List<Double> allOutboundRoutePopularitiesAfterSave = neo4jCityBAfterSave.getRoutes().stream().map((route) -> route.getPopularity()).distinct().toList();

        assertThat(allOutboundRoutePopularitiesBeforeSave.size()).isEqualTo(1);
        assertThat(allOutboundRoutePopularitiesAfterSave.get(0)).isEqualTo(0.0);
        assertThat(allOutboundRoutePopularitiesAfterSave.size()).isEqualTo(1);
        assertThat(allOutboundRoutePopularitiesAfterSave.get(0)).isEqualTo(0.0);
    }
    
}
