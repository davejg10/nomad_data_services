package com.nomad.data_library.domain.neo4j;

import static org.assertj.core.api.Assertions.assertThat;

import com.nomad.data_library.config.Neo4jConfig;
import com.nomad.data_library.domain.CityMetrics;
import org.junit.jupiter.api.Test;

import com.nomad.data_library.Neo4jTestConfiguration;
import com.nomad.data_library.TestConfig;
import org.neo4j.driver.Value;
import org.neo4j.driver.Values;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;
import org.springframework.context.annotation.Import;

@DataNeo4jTest
@Import({TestConfig.class, Neo4jTestConfiguration.class, Neo4jConfig.class})
public class CityMetricsDeserializerTest {

    @Autowired
    private CityMetricsDeserializer cityMetricsDeserializer;

    @Test
    void read_shouldConvertNeo4jValue_toCityMetricsObject() {
        Value neo4jCityMetricsProperty = Values.value("{\"sailing\":{\"criteria\":\"SAILING\",\"metric\":0},\"food\":{\"criteria\":\"FOOD\",\"metric\":6},\"nightlife\":{\"criteria\":\"NIGHTLIFE\",\"metric\":5}}\"");

        CityMetrics cityMetrics = cityMetricsDeserializer.read(neo4jCityMetricsProperty);

        assertThat(cityMetrics.getSailing().getMetric()).isEqualTo(0);
        assertThat(cityMetrics.getFood().getMetric()).isEqualTo(6);
        assertThat(cityMetrics.getNightlife().getMetric()).isEqualTo(5);

    }
}
