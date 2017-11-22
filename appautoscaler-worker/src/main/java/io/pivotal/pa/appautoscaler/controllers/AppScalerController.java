package io.pivotal.pa.appautoscaler.controllers;

import java.util.Date;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.pivotal.pa.appautoscaler.domain.Rule;
import io.pivotal.pa.appautoscaler.domain.ServiceBinding;
import io.pivotal.pa.appautoscaler.repositories.RuleRepository;
import io.pivotal.pa.appautoscaler.repositories.ServiceBindingRepository;



@RestController
public class AppScalerController {

	@Autowired
	MailSender mailSender;

	@Autowired
	RuleRepository ruleRepository;
	
	@Autowired
	ServiceBindingRepository serviceBindingRepository;

	@RequestMapping("/email")
	public String email() {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setText("Hello from Spring Boot Application");
		message.setTo("cdecelles@pivotal.io");
		message.setFrom("cdecelles@pivotal.io");
		try {

			mailSender.send(message);
			return "{\"message\": \"OK\"}";
		} catch (Exception e) {
			e.printStackTrace();
			return "{\"message\": \"Error\"}";
		}

	}

	@PostMapping("/rules")
	public ResponseEntity<?> add(@RequestBody Rule rule) {

		List<Rule> rules = ruleRepository.findByAppGUID(rule.getAppGUID());
		JSONParser parser = new JSONParser();
		JSONObject vcap = null;
		
		Iterable<ServiceBinding> bindings = serviceBindingRepository.findByAppGUID(rule.getAppGUID());

		if (!bindings.iterator().hasNext()) {

			ServiceBinding serviceBinding = new ServiceBinding();
			serviceBinding.setAppGUID(rule.getAppGUID());
			serviceBinding.setPlanID("_not_defined");
			serviceBinding.setServiceID("_not_defined");
			serviceBinding.setBindingID("_not_defined");
			serviceBinding.setCreated(new Date());
			serviceBinding.setModified(new Date());

			ServiceBinding _sb = serviceBindingRepository.save(serviceBinding);
			assert _sb != null;
		}

		String id = "";
		Rule savedRule;

		if (rules.size() == 0) {

			Rule _rule = ruleRepository.save(rule);
			assert _rule != null;
			id = _rule.getId();
			savedRule = _rule;
		} else {
			Rule r = rules.get(0);
			savedRule = rule;
			id = r.getId();
		}

		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders
				.setLocation(ServletUriComponentsBuilder.fromCurrentRequest().path("/" + id).buildAndExpand().toUri());

		return new ResponseEntity<>(savedRule, httpHeaders, HttpStatus.CREATED);
	}

	@GetMapping("/rules/{id}")
	public Rule snippet(@PathVariable("id") String id) {
		return ruleRepository.findOne(id);
	}

	@GetMapping("/rules")
	public Iterable<Rule> rules() {
		return ruleRepository.findAll();
	}

	@DeleteMapping("/rules")
	public void deleteRules() {
		ruleRepository.deleteAll();
	}

	@GetMapping("/bindings")
	public Iterable<ServiceBinding> serviceBindings() {
		return serviceBindingRepository.findAll();
	}
}
