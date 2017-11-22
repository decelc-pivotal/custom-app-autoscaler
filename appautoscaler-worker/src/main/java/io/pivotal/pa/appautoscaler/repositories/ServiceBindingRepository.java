package io.pivotal.pa.appautoscaler.repositories;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.repository.CrudRepository;

import io.pivotal.pa.appautoscaler.domain.ServiceBinding;

@Transactional
public interface ServiceBindingRepository extends CrudRepository<ServiceBinding, String> {
		
	List<ServiceBinding> findByBindingID(String bindingID);
	List<ServiceBinding> findByAppGUID(String appGUID);
	List<ServiceBinding> findByServiceID(String serviceID);
	List<ServiceBinding> findByServiceIDAndAppGUID(String serviceID, String appGUID);
	List<ServiceBinding> findByServiceIDAndBindingID(String serviceID, String bindingID);
}
