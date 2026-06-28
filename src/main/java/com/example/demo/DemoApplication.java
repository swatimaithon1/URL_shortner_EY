package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
// Enables periodic jobs such as expired-link cleanup.
@EnableScheduling
public class DemoApplication {

	public static void main(String[] args) {
		// Bootstraps the Spring context and starts the embedded server.
		SpringApplication.run(DemoApplication.class, args);
	}

}
