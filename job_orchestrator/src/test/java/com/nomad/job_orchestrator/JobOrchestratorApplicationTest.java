// package com.nomad.job_orchestrator;

// import org.junit.jupiter.api.Test;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.test.context.bean.override.mockito.MockitoBean;

// import com.azure.messaging.servicebus.ServiceBusSenderClient;
// import com.microsoft.applicationinsights.TelemetryClient;
// import com.nomad.scraping_library.connectors.ServiceBusBatchSender;
// import com.nomad.scraping_library.domain.ScraperRequest;

// @SpringBootTest
// public class JobOrchestratorApplicationTest {
	
//     @MockitoBean
//     private ServiceBusBatchSender<ScraperRequest> serviceBusBatchSender;
    
//     @MockitoBean
//     private ServiceBusSenderClient serviceBusSenderClient;
    
//     @MockitoBean
//     private TelemetryClient telemetryClient;

//     @Test
// 	void  contextLoads() {

// 	}

// }