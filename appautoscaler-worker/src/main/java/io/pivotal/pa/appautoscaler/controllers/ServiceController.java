package io.pivotal.pa.appautoscaler.controllers;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.pivotal.pa.appautoscaler.domain.ServiceBindInfo;
import io.pivotal.pa.appautoscaler.domain.ServiceBinding;
import io.pivotal.pa.appautoscaler.domain.ServiceInstance;
import io.pivotal.pa.appautoscaler.domain.ServiceInstanceBindInfo;
import io.pivotal.pa.appautoscaler.repositories.RuleRepository;
import io.pivotal.pa.appautoscaler.repositories.ServiceBindingRepository;
import io.pivotal.pa.appautoscaler.repositories.ServiceInstanceRepository;


@RestController
public class ServiceController {

	@Autowired
	RuleRepository ruleRepository;

	@Autowired
	ServiceBindingRepository serviceBindingRepository;

	@Autowired
	ServiceInstanceRepository serviceInstanceRepository;

	@RequestMapping(value = "/v2/service_instances/{instance_id}", method = RequestMethod.GET)
	public JSONObject getSIInfo(@PathVariable String instance_id, HttpServletResponse response) throws IOException {
		System.out.println("[ServiceController] getSIInfo() instance_id=" + instance_id);
		JSONObject json = new JSONObject();
		json.put("success", true);
		return json;
	}

	@RequestMapping(value = "/v2/service_instances/{instance_id}", method = RequestMethod.PUT)
	public JSONObject putSIInfo(@PathVariable String instance_id, @RequestBody ServiceInstanceBindInfo serviceBindInfo,
			HttpServletResponse response) throws IOException {
		System.out.println("[ServiceController] putSIInfo() instance_id=" + instance_id);
		System.out.println(
				"[ServiceController] putSIInfo() getOrganization_guid=" + serviceBindInfo.getOrganization_guid());
		System.out.println("[ServiceController] putSIInfo() getSpace_guid=" + serviceBindInfo.getSpace_guid());
		System.out.println("[ServiceController] putSIInfo() getService_id=" + serviceBindInfo.getService_id());
		System.out.println("[ServiceController] putSIInfo() getPlan_id=" + serviceBindInfo.getPlan_id());

		ServiceInstance s = new ServiceInstance(serviceBindInfo.getService_id(), serviceBindInfo.getPlan_id(),
				serviceBindInfo.getOrganization_guid(), serviceBindInfo.getSpace_guid());

		List<ServiceInstance> bindings = serviceInstanceRepository.findByOrganizationGUIDAndSpaceGUIDAndServiceID(
				serviceBindInfo.getOrganization_guid(), serviceBindInfo.getSpace_guid(),
				serviceBindInfo.getService_id());
		if (!bindings.isEmpty()) {
			// delete all old ones
			for (ServiceInstance bs : bindings) {
				serviceInstanceRepository.delete(bs);
			}
		}

		serviceInstanceRepository.save(s);
		JSONObject json = new JSONObject();
		json.put("success", true);
		return json;
	}

	@RequestMapping(value = "/v2/service_instances/{instance_id}", method = RequestMethod.DELETE)
	public JSONObject deleteSIInfo(@PathVariable String instance_id, @RequestParam("service_id") String service_id,
			@RequestParam("plan_id") String plan_id, HttpServletResponse response) throws IOException {
		System.out.println("[ServiceController] putSIInfo() instance_id=" + instance_id);
		System.out.println("[ServiceController] putSIInfo() service_id=" + service_id);
		System.out.println("[ServiceController] putSIInfo() plan_id=" + plan_id);

		List<ServiceInstance> bindings = serviceInstanceRepository.findByServiceID(instance_id);
		if (!bindings.isEmpty()) {
			// delete all old ones
			for (ServiceInstance bs : bindings) {
				serviceInstanceRepository.delete(bs);
			}
		}

		return new JSONObject();
	}

	@RequestMapping(value = "/v2/service_instances/{instance_id}/service_bindings/{binding_id}", method = RequestMethod.PUT)
	public JSONObject putSIBInfo(@PathVariable String instance_id, @PathVariable String binding_id,
			@RequestBody ServiceBindInfo serviceBindInfo, HttpServletResponse response) throws IOException {
		System.out.println("[ServiceController] putSIBInfo() instance_id=" + instance_id);
		System.out.println("[ServiceController] putSIBInfo() getService_id=" + serviceBindInfo.getService_id());
		System.out.println("[ServiceController] putSIBInfo() plan_id=" + serviceBindInfo.getPlan_id());

		System.out.println("[ServiceController] putSIInfo() getBindResource.getApp_guid="
				+ serviceBindInfo.getBind_resource().getApp_guid());

		ServiceBinding b = new ServiceBinding(binding_id, serviceBindInfo.getBind_resource().getApp_guid(),
				serviceBindInfo.getService_id(), serviceBindInfo.getPlan_id());
		List<ServiceBinding> bindings = serviceBindingRepository.findByServiceIDAndAppGUID(
				serviceBindInfo.getService_id(), serviceBindInfo.getBind_resource().getApp_guid());
		if (!bindings.isEmpty()) {
			// delete all old ones
			for (ServiceBinding bs : bindings) {
				serviceBindingRepository.delete(bs);
			}
		}

		serviceBindingRepository.save(b);
		return new JSONObject();
	}

	@RequestMapping(value = "/v2/service_instances/{instance_id}/service_bindings/{binding_id}", method = RequestMethod.DELETE)
	public JSONObject deleteSIBInfo(@PathVariable String instance_id, @PathVariable String binding_id,
			@RequestParam("service_id") String service_id, @RequestParam("plan_id") String plan_id,
			HttpServletResponse response) throws IOException {
		System.out.println("[ServiceController] deleteSIBInfo() instance_id=" + instance_id);
		System.out.println("[ServiceController] deleteSIBInfo() binding_id=" + binding_id);
		System.out.println("[ServiceController] deleteSIBInfo() service_id=" + service_id);
		System.out.println("[ServiceController] deleteSIBInfo() plan_id=" + plan_id);
		List<ServiceBinding> bindings = serviceBindingRepository.findByServiceIDAndBindingID(instance_id, binding_id);
		if (!bindings.isEmpty()) {
			// delete all old ones
			for (ServiceBinding bs : bindings) {
				serviceBindingRepository.delete(bs);
			}
		}
		JSONObject json = new JSONObject();
		json.put("success", true);
		return json;
	}

	@GetMapping("/v2/catalog")
	public JSONObject catalog() {
		return getServiceCatalog();
	}

	@RequestMapping(value = "/serviceInstances", method = RequestMethod.GET)
	public Iterable<ServiceInstance> getServiceInstances() throws IOException {
		return serviceInstanceRepository.findAll();
	}

	@RequestMapping(value = "/serviceBindings", method = RequestMethod.GET)
	public Iterable<ServiceBinding> getServiceBindings() throws IOException {
		return serviceBindingRepository.findAll();
	}

	@SuppressWarnings("unchecked")
	private JSONObject getServiceCatalog() {
		JSONObject serviceCatalog = new JSONObject();
		JSONArray services = new JSONArray();

		JSONObject serviceObject = new JSONObject();
		serviceObject.put("id", "acb56d7c-XXXX-XXXX-XXXX-feb140a59a66");
		serviceObject.put("name", "custom-app-autoscaler");
		serviceObject.put("description", "Scales bound applications in response to user provided metric/rule");
		serviceObject.put("plan_updateable", true);
		serviceObject.put("bindable", true);

		JSONObject metaDataObject = new JSONObject();

		metaDataObject.put("displayName", "Custom App Autoscaler");
		metaDataObject.put("documentationUrl", "http://docs.gopivotal.com/pivotalcf/");
		metaDataObject.put("imageUrl", "https://d1nwne6cc3e7hn.cloudfront.net/autoscaler_icon.png");
		metaDataObject.put("longDescription",
				"Instances of this service are scaled based on a custom rule. The data for the rule is from any REST API endpoint.");
		metaDataObject.put("providerDisplayName", "Pivotal-CJD");
		metaDataObject.put("supportUrl", "");

		JSONArray planData = new JSONArray();
		JSONObject planDataObject = new JSONObject();

		planDataObject.put("id", "d3031751-XXXX-XXXX-XXXX-a42377d3320e");
		planDataObject.put("name", "standard");
		planDataObject.put("description",
				"This plan monitors and scales applications based on scaling rules every 30 seconds.");
		planData.add(planDataObject);

		JSONObject planMetaDataObject = new JSONObject();

		planMetaDataObject.put("displayName", "Standard");

		JSONArray bullets = new JSONArray();
		bullets.add("Scales your app up and down according to user-provided rules");
		bullets.add("Waits 30 seconds after every scale event before other scale decisions.");

		planMetaDataObject.put("bullets", bullets);

		planDataObject.put("metadata", planMetaDataObject);
		JSONObject costDataObject = new JSONObject();

		JSONObject cost = new JSONObject();
		JSONArray costs = new JSONArray();

		cost.put("usd", 0);
		costDataObject.put("amount", cost);
		costDataObject.put("unit", "MONTHLY");
		costs.add(costDataObject);
		planMetaDataObject.put("costs", costs);

		serviceObject.put("metadata", metaDataObject);
		serviceObject.put("plans", planData);

		services.add(serviceObject);
		serviceCatalog.put("services", services);

		System.out.println("#######################################################");
		System.out.println("### serviceCatalog =" + serviceCatalog);
		System.out.println("#######################################################");

		return serviceCatalog;
	}

}
