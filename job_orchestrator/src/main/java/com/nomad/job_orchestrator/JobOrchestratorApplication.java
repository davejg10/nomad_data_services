package com.nomad.job_orchestrator;

import java.util.Objects;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;

import com.microsoft.applicationinsights.attach.ApplicationInsights;

import lombok.extern.log4j.Log4j2;

@Log4j2
@EntityScan(basePackages = {"com.nomad.library.domain"})
// @EnableJpaRepositories({"com.nomad.library.repositories"})
@SpringBootApplication
@ComponentScan({"com.nomad.library.config", "com.nomad.job_orchestrator"})
public class JobOrchestratorApplication {

	public static void main(String[] args) {

		Environment environment = SpringApplication.run(JobOrchestratorApplication.class, args).getEnvironment();

		String profile = environment.getProperty("spring.profiles.active", "local");

		if (!Objects.equals(profile, "local")) {
			log.info("Attaching application insights");
			// Note; the application insights connection string is set as `APPLICATIONINSIGHTS_CONNECTION_STRING` environment variable.
			ApplicationInsights.attach();
		}
	}	
}
