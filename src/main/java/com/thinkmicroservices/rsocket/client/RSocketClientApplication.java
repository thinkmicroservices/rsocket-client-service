package com.thinkmicroservices.rsocket.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.thinkmicroservices.rsocket.client")
public class RSocketClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(RSocketClientApplication.class, args);
	}

}
