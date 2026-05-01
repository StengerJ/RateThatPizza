package com.pghpizza.api;

import org.springframework.boot.SpringApplication;

public class TestPghPizzaApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(PghPizzaApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
