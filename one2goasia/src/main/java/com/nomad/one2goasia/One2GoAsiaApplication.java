package com.nomad.one2goasia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.nomad.library.config", "com.nomad.one2goasia"})
public class One2GoAsiaApplication {

	public static void main(String[] args) {
		SpringApplication.run(One2GoAsiaApplication.class, args);
	}

}
