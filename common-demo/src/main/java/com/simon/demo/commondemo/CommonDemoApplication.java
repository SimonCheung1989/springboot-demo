package com.simon.demo.commondemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CommonDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(CommonDemoApplication.class, args);
	}
}
