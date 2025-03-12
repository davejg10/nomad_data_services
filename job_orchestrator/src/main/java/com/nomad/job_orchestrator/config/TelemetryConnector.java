package com.nomad.job_orchestrator.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.TelemetryConfiguration;

@Profile("cloud")
@Configuration
public class TelemetryConnector {

    @Value("${APPLICATIONINSIGHTS_CONNECTION_STRING:local}")
    private String APPLICATIONINSIGHTS_CONNECTION_STRING;

    @Bean
    public TelemetryClient telemetryClient() {
        TelemetryConfiguration config = TelemetryConfiguration.getActiveWithoutInitializingConfig();
        config.setConnectionString(APPLICATIONINSIGHTS_CONNECTION_STRING);
        config.setRoleName("job_orchestraotr");
        TelemetryClient client = new TelemetryClient();
        return client;
    }
    
}
