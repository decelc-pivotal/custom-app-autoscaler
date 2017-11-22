package io.pivotal.pa.appautoscaler.repositories;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.repository.CrudRepository;

import io.pivotal.pa.appautoscaler.domain.ServiceInstance;

@Transactional
public interface ServiceInstanceRepository extends CrudRepository<ServiceInstance, String> {
		
	List<ServiceInstance> findByOrganizationGUID(String organizationGUID);
	List<ServiceInstance> findBySpaceGUID(String spaceGUID);
	List<ServiceInstance> findByServiceID(String serviceID);
	List<ServiceInstance> findByOrganizationGUIDAndSpaceGUIDAndServiceID(String organizationGUID, String spaceGUID, String serviceID);
}
