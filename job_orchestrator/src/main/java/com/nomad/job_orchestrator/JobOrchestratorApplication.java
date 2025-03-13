package com.nomad.job_orchestrator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import lombok.extern.log4j.Log4j2;

@Log4j2
// @EntityScan(basePackages = {"com.nomad.data_library.domain"})
// @EnableJpaRepositories({"com.nomad.data_library.repositories"})
@SpringBootApplication
// @ComponentScan({"com.nomad.data_library.config", "com.nomad.scraping_library.config", "com.nomad.job_orchestrator"})
@ComponentScan({"com.nomad.data_library.config", "com.nomad.scraping_library.config", "com.nomad.job_orchestrator"})
public class JobOrchestratorApplication {

	public static void main(String[] args) {

		SpringApplication.run(JobOrchestratorApplication.class, args);

	}	
}
