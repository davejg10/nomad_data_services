package com.nomad.job_orchestrator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import lombok.extern.log4j.Log4j2;

@Log4j2
@EntityScan(basePackages = {"com.nomad.data_library.domain"})
@EnableJpaRepositories({"com.nomad.data_library.repositories", "com.nomad.job_orchestrator.repositories"})
@SpringBootApplication
@ComponentScan({"com.nomad.data_library.config", "com.nomad.scraping_library.config", "com.nomad.job_orchestrator"})
public class JobOrchestratorApplication {

	public static void main(String[] args) {

		SpringApplication.run(JobOrchestratorApplication.class, args);

	}	
}
