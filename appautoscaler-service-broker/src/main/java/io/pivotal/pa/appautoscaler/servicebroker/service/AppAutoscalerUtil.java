package io.pivotal.pa.appautoscaler.servicebroker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public class AppAutoscalerUtil {

	public AppAutoscalerUtil() {
		super();
	}

	@Autowired
	private ResourceLoader resourceLoader;

	public ResourceLoader getResourceLoader() {
		return resourceLoader;
	}
}
