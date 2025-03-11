package com.nomad.admin_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = {"com.nomad.data_library.domain"})
@EnableJpaRepositories({"com.nomad.data_library.repositories"})
@ComponentScan({"com.nomad.data_library.config", "com.nomad.admin_api"})
public class AdminApiApplication {
    
    public static void main(String[] args) {

		SpringApplication.run(AdminApiApplication.class, args);
	}
}