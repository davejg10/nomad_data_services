package com.nomad.job_orchestrator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = {"com.nomad.library.domain"})
// @EnableJpaRepositories({"com.nomad.library.repositories"})
@ComponentScan({"com.nomad.library.config", "com.nomad.job_orchestrator"})
public class JobOrchestratorApplication {

	public static void main(String[] args) {

		SpringApplication.run(JobOrchestratorApplication.class, args);
	}
}
