package io.pivotal.pa.appautoscaler.servicebroker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
public class AppautoscalerServiceBrokerApplication {

	public static void main(String[] args) {
		SpringApplication.run(AppautoscalerServiceBrokerApplication.class, args);
	}
}
