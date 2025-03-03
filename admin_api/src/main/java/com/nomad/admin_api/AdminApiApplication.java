package com.nomad.admin_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan(basePackages = {"com.nomad.library.domain", "com.nomad.admin_api.domain" }) // Required for the Neo4j Entities
public class AdminApiApplication {
    
    public static void main(String[] args) {

		SpringApplication.run(AdminApiApplication.class, args);
	}
}