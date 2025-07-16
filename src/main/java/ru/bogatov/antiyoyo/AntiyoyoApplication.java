package ru.bogatov.antiyoyo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@EnableAsync
public class AntiyoyoApplication {

	public static void main(String[] args) {
		SpringApplication.run(AntiyoyoApplication.class, args);
	}

}
