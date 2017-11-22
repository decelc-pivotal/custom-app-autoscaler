package io.pivotal.pa.appautoscaler.domain;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

@Entity
public class ServiceBinding {

	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "uuid")
	private String id;
	@Column(insertable = true, updatable = false)
	private Date created;
	private Date modified;

	private String bindingID;
	private String appGUID;
	private String serviceID;
	private String planID;

	public ServiceBinding() {
		this.id = java.util.UUID.randomUUID().toString();
		this.created = new Date();
		this.modified = new Date();
	}

	public ServiceBinding(String bindingID, String appGUID, String serviceID, String planID) {
		super();
		this.id = java.util.UUID.randomUUID().toString();
		this.created = new Date();
		this.modified = new Date();
		this.bindingID = bindingID;
		this.appGUID = appGUID;
		this.serviceID = serviceID;
		this.planID = planID;
	}

	public String getBindingID() {
		return bindingID;
	}

	public void setBindingID(String bindingID) {
		this.bindingID = bindingID;
	}

	public String getAppGUID() {
		return appGUID;
	}

	public void setAppGUID(String appGUID) {
		this.appGUID = appGUID;
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

	public String toString() {
		return "[SERVICEBINDING] AppGUID=" + this.appGUID + " ServiceID=" + this.serviceID + " PlanID=" + this.planID;
	}
}