package com.pharmacy.drug_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class DrugApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(DrugApiApplication.class, args);
	}

}
