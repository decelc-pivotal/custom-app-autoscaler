package io.pivotal.pa.appautoscaler.servicebroker.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.cloud.servicebroker.model.Catalog;
import org.springframework.cloud.servicebroker.model.Plan;
import org.springframework.cloud.servicebroker.model.ServiceDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CatalogConfig {

	@Bean
	public Catalog catalog() {
		return new Catalog(Collections.singletonList(
				new ServiceDefinition("custom-appautoscaler-service-broker", "custom-appautoscaler",
						"Custom App Autoscaler", true,
						false,
						Collections.singletonList(new Plan("custom-appautoscaler-plan", "default",
								"This is a default custom app autoscaler service plan.  All services are created equally.",
								getPlanMetadata())),
						Arrays.asList("custom-appautoscaler", "document"), getServiceDefinitionMetadata(), null,
						null)));
	}

	private Map<String, Object> getServiceDefinitionMetadata() {
		Map<String, Object> sdMetadata = new HashMap<>();
		sdMetadata.put("displayName", "Custom App Autoscaler");
		sdMetadata.put("imageUrl",
				"http://workshop-content.cfapps.io/static/resources/images/custom_appautoscaler_icon.png");
		sdMetadata.put("longDescription", "Custom App Autoscaler");
		sdMetadata.put("providerDisplayName", "Pivotal");
		sdMetadata.put("documentationUrl", "https://github.com/decelc-pivotal/custom-app-autoscaler");
		sdMetadata.put("supportUrl", "https://github.com/decelc-pivotal/custom-app-autoscaler");
		sdMetadata.put("bindable", true);

		return sdMetadata;
	}

	private Map<String, Object> getPlanMetadata() {
		Map<String, Object> planMetadata = new HashMap<>();
		planMetadata.put("costs", getCosts());
		planMetadata.put("bullets", getBullets());
		return planMetadata;
	}

	private List<Map<String, Object>> getCosts() {
		Map<String, Object> costsMap = new HashMap<>();

		Map<String, Object> amount = new HashMap<>();
		amount.put("usd", 0.0);

		costsMap.put("amount", amount);
		costsMap.put("unit", "MONTHLY");

		return Collections.singletonList(costsMap);
	}

	private List<String> getBullets() {
		return Arrays.asList("Custom App Autoscaler 1.0.0", "Scale every 30 seconds.");
	}
}
