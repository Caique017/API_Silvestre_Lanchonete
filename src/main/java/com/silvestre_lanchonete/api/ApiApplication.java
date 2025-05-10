package com.silvestre_lanchonete.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ApiApplication {

	public static void main(String[] args) {
		System.out.println("TOKEN_SECRET: " + System.getenv("TOKEN_SECRET"));
		SpringApplication.run(ApiApplication.class, args);
	}

}
