package com.leets7th.job_is_be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
public class JobIsBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(JobIsBeApplication.class, args);
	}

}
