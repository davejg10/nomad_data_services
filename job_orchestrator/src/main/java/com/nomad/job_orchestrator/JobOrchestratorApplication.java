package com.nomad.job_orchestrator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan(basePackages = {"com.nomad.library.domain"}) // Required for the Neo4j Entities
public class JobOrchestratorApplication {

	public static void main(String[] args) {

		SpringApplication.run(JobOrchestratorApplication.class, args);
	}
}
