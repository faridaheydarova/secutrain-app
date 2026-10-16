package com.securityawareness.secutrainapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class
SecutrainAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(SecutrainAppApplication.class, args);
	}

}
