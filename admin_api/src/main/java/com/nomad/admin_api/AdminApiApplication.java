package com.nomad.admin_api;

import java.util.Objects;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.microsoft.applicationinsights.attach.ApplicationInsights;

import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootApplication
@EntityScan(basePackages = {"com.nomad.library.domain"})
@EnableJpaRepositories({"com.nomad.library.repositories"})
@ComponentScan({"com.nomad.library.config", "com.nomad.admin_api"})
public class AdminApiApplication {
    
    public static void main(String[] args) {

		Environment environment = SpringApplication.run(AdminApiApplication.class, args).getEnvironment();

		String profile = environment.getProperty("spring.profiles.active", "local");

		if (!Objects.equals(profile, "local")) {
			log.info("Attaching application insights");
			// Note; the application insights connection string is set as `APPLICATIONINSIGHTS_CONNECTION_STRING` environment variable.
			ApplicationInsights.attach();
		}
	}
}