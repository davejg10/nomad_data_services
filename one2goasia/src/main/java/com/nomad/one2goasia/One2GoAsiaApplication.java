package com.nomad.one2goasia;

import org.springframework.core.env.Environment;

import com.microsoft.applicationinsights.attach.ApplicationInsights;

import lombok.extern.log4j.Log4j2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@Log4j2
@SpringBootApplication
@ComponentScan({"com.nomad.scraping_library.config", "com.nomad.one2goasia"})
public class One2GoAsiaApplication {

	public static void main(String[] args) {
		Environment environment = SpringApplication.run(One2GoAsiaApplication.class, args).getEnvironment();

		log.info("in the main method");

		String profile = environment.getProperty("spring.profiles.active", "local");

		if (!profile.equals("local")) {
			log.info("Attaching application insights");
			// Note; the application insights connection string is set as `APPLICATIONINSIGHTS_CONNECTION_STRING` environment variable.
			ApplicationInsights.attach();
		}
	}

}
