package com.capstone.kakas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class KakasApplication {

	public static void main(String[] args) {
		SpringApplication.run(KakasApplication.class, args);
	}

}
