package com.penguineering.moss.wb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.Async;

@SpringBootApplication
@Async
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
