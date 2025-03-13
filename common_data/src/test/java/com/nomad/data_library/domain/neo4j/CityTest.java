package com.nomad.data_library.domain.neo4j;

import com.nomad.data_library.Neo4jTestGenerator;
import com.nomad.data_library.domain.CityMetrics;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
@ExtendWith(MockitoExtension.class)
public class CityTest {

    @Mock
    Neo4jCountry country;

    @Test
    void addRoute_shouldAddRoute_whenRouteDoesntExist() {
        Neo4jCity city = Neo4jTestGenerator.neo4jCityNoRoutes("CityA", country);
        Neo4jCity targetCity = Neo4jTestGenerator.neo4jCityNoRoutes("CityB", country);

        assertThat(city.getRoutes()).isEmpty();

        Neo4jRoute route = Neo4jTestGenerator.neo4jRoute(targetCity);
        city = city.addRoute(route);

        assertThat(city.getRoutes().size()).isEqualTo(1);
        assertThat(city.getRoutes()).contains(route);
    }

    @Test
    void addRoute_shouldNotAddRoute_whenRouteAlreadyExists() {
        Neo4jCity city = Neo4jTestGenerator.neo4jCityNoRoutes("CityA", country);
        Neo4jCity targetCity = Neo4jTestGenerator.neo4jCityNoRoutes("CityB", country);

        assertThat(city.getRoutes()).isEmpty();

        Neo4jRoute route = Neo4jTestGenerator.neo4jRoute(targetCity);
        city = city.addRoute(route);

        assertThat(city.getRoutes().size()).isEqualTo(1);
        assertThat(city.getRoutes()).contains(route);

        city = city.addRoute(route);
        assertThat(city.getRoutes().size()).isEqualTo(1);
        assertThat(city.getRoutes()).contains(route);
    }

    @Test
    void addRoute_shouldOverwriteRoute_whenRouteToAddHasDifferentMetricsButSameTargetAndSameTransportType() {
        Neo4jCity city = Neo4jTestGenerator.neo4jCityNoRoutes("CityA", country);
        Neo4jCity targetCity = Neo4jTestGenerator.neo4jCityNoRoutes("CityB", country);

        assertThat(city.getRoutes()).isEmpty();

        Neo4jRoute route = Neo4jTestGenerator.neo4jRoute(targetCity);
        city = city.addRoute(route);

        assertThat(city.getRoutes().size()).isEqualTo(1);
        assertThat(city.getRoutes()).contains(route);

        Neo4jRoute lessPopularRoute = new Neo4jRoute(route.getId(), route.getTargetCity(), route.getPopularity() - 1, route.getTime(), route.getCost(), route.getTransportType());
        city = city.addRoute(lessPopularRoute);
        assertThat(city.getRoutes().size()).isEqualTo(1);
        assertThat(city.getRoutes()).contains(lessPopularRoute);

        Neo4jRoute longerRoute = new Neo4jRoute(lessPopularRoute.getId(), lessPopularRoute.getTargetCity(), lessPopularRoute.getPopularity(), lessPopularRoute.getTime() + 4, lessPopularRoute.getCost(), lessPopularRoute.getTransportType());
        city = city.addRoute(longerRoute);
        assertThat(city.getRoutes().size()).isEqualTo(1);
        assertThat(city.getRoutes()).contains(longerRoute);

        Neo4jRoute moreExpensiveRoute = new Neo4jRoute(longerRoute.getId(), longerRoute.getTargetCity(), longerRoute.getPopularity(), longerRoute.getTime(), longerRoute.getCost() + 10.0, longerRoute.getTransportType());
        city = city.addRoute(moreExpensiveRoute);
        assertThat(city.getRoutes().size()).isEqualTo(1);
        assertThat(city.getRoutes()).contains(moreExpensiveRoute);
    }

}
