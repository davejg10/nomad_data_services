package com.nomad.job_orchestrator;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.nomad.job_orchestrator.config.ServiceBusBatchSender;

@SpringBootTest
public class JobOrchestratorApplicationTest {
	
    @MockitoBean
    private ServiceBusBatchSender serviceBusBatchSender;

    @Test
	void  contextLoads() {

	}

}