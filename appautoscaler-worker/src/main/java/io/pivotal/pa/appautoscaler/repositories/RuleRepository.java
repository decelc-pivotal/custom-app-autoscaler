package io.pivotal.pa.appautoscaler.repositories;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.repository.CrudRepository;

import io.pivotal.pa.appautoscaler.domain.Rule;

@Transactional
public interface RuleRepository extends CrudRepository<Rule, String> {
		
	List<Rule> findByAppGUID(String appGUID);
	List<Rule> findBySpaceGUID(String spaceGUID);
}
