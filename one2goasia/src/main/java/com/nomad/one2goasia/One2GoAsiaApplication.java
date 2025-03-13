package com.nomad.one2goasia;

import lombok.extern.log4j.Log4j2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@Log4j2
@SpringBootApplication
@ComponentScan({"com.nomad.scraping_library.config", "com.nomad.one2goasia"})
public class One2GoAsiaApplication {

	public static void main(String[] args) {
    	SpringApplication.run(One2GoAsiaApplication.class, args);
	}

}
