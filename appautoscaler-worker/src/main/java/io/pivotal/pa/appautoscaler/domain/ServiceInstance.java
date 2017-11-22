package io.pivotal.pa.appautoscaler.domain;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

@Entity
public class ServiceInstance {

	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "uuid")
	private String id;
	@Column(insertable = true, updatable = false)
	private Date created;
	private Date modified;

	private String serviceID;
	private String planID;
	private String organizationGUID;
	private String spaceGUID;

	public ServiceInstance() {
		this.id = java.util.UUID.randomUUID().toString();
		this.created = new Date();
		this.modified = new Date();
	}
	
	public ServiceInstance(String serviceID, String planID, String organizationGUID, String spaceGUID) {
		super();
		this.id = java.util.UUID.randomUUID().toString();
		this.created = new Date();
		this.modified = new Date();
		this.serviceID = serviceID;
		this.planID = planID;
		this.organizationGUID = organizationGUID;
		this.spaceGUID = spaceGUID;
	}



	public String getServiceID() {
		return serviceID;
	}

	public void setServiceID(String serviceID) {
		this.serviceID = serviceID;
	}

	public String getPlanID() {
		return planID;
	}

	public void setPlanID(String planID) {
		this.planID = planID;
	}

	public String getOrganizationGUID() {
		return organizationGUID;
	}

	public void setOrganizationGUID(String organizationGUID) {
		this.organizationGUID = organizationGUID;
	}

	public String getSpaceGUID() {
		return spaceGUID;
	}

	public void setSpaceGUID(String spaceGUID) {
		this.spaceGUID = spaceGUID;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Date getModified() {
		return modified;
	}

	public void setModified(Date modified) {
		this.modified = modified;
	}

	@PrePersist
	void onCreate() {
		this.setCreated(new Date());
		this.setModified(new Date());
	}

	@PreUpdate
	void onUpdate() {
		this.setModified(new Date());
	}
}