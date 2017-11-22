package io.pivotal.pa.appautoscaler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class AppautoscalerWorkerApplication {

	public static void main(String[] args) {
		SpringApplication.run(AppautoscalerWorkerApplication.class, args);
	}
}
