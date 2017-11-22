package io.pivotal.pa.appautoscaler.ui;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

//@EnableScheduling
@SpringBootApplication
public class AppAutoScalerUIApplication {

	public static void main(String[] args) {
		SpringApplication.run(AppAutoScalerUIApplication.class, args);
	}

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurerAdapter() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/nyse").allowedOrigins("http://localhost:8080");
				registry.addMapping("/loginTest").allowedOrigins("http://localhost:8080");
				registry.addMapping("/orgsTest").allowedOrigins("http://localhost:8080");
				registry.addMapping("/nyse").allowedOrigins("http://localhost:3000");
				registry.addMapping("/loginTest").allowedOrigins("http://localhost:3000");
				registry.addMapping("/orgsTest").allowedOrigins("http://localhost:3000");
			}
		};
	}
}
