package com.pghpizza.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.pghpizza.api.config.AppProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class PghPizzaApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(PghPizzaApiApplication.class, args);
	}

}
