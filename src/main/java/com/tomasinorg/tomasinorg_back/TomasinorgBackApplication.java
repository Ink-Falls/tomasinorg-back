package com.tomasinorg.tomasinorg_back;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TomasinorgBackApplication {

	public static void main(String[] args) {
		SpringApplication.run(TomasinorgBackApplication.class, args);
	}

}
