package com.nomad.job_orchestrator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.TelemetryConfiguration;

@Profile("!maven")
@Configuration
public class TelemetryConnector {

    // @Value("${APPLICATIONINSIGHTS_CONNECTION_STRING:local}")
    // private String APPLICATIONINSIGHTS_CONNECTION_STRING;

    @Bean
    public TelemetryClient telemetryClient() {
        TelemetryConfiguration config = TelemetryConfiguration.createDefault();
        config.setConnectionString("InstrumentationKey=52c31484-0b48-4b47-8bc0-16aa9bf3bffd;IngestionEndpoint=https://uksouth-1.in.applicationinsights.azure.com/;LiveEndpoint=https://uksouth.livediagnostics.monitor.azure.com/;ApplicationId=cb84c331-1d0f-4d24-97a9-20006f4f7adb");
        config.setInstrumentationKey("52c31484-0b48-4b47-8bc0-16aa9bf3bffd");
        config.setRoleName("job_orchestraotr");
        TelemetryClient client = new TelemetryClient(config);
        return client;
    }
    
}
