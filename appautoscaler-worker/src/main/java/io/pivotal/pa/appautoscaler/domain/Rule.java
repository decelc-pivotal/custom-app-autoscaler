package io.pivotal.pa.appautoscaler.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import org.hibernate.annotations.GenericGenerator;

@Entity
public class Rule {

	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "uuid")
	private String id;

	private String appGUID;
	private String spaceGUID;

	private String ruleURL;
	private String ruleAPIKey;
	private String ruleExpression;
	private String minInstances;
	private String maxInstances;

	@Column(insertable = true, updatable = false)
	private Date created;

	private Date modified;

	public Rule() {
		this.id = java.util.UUID.randomUUID().toString();
		this.created = new Date();
		this.modified = new Date();
	}

	public String getRuleURL() {
		return ruleURL;
	}

	public void setRuleURL(String ruleURL) {
		this.ruleURL = ruleURL;
	}

	public String getRuleAPIKey() {
		return ruleAPIKey;
	}

	public void setRuleAPIKey(String ruleAPIKey) {
		this.ruleAPIKey = ruleAPIKey;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getAppGUID() {
		return appGUID;
	}

	public void setAppGUID(String appGUID) {
		this.appGUID = appGUID;
	}

	public String getSpaceGUID() {
		return spaceGUID;
	}

	public void setSpaceGUID(String spaceGUID) {
		this.spaceGUID = spaceGUID;
	}

	public String getMinInstances() {
		return minInstances;
	}

	public void setMinInstances(String minInstances) {
		this.minInstances = minInstances;
	}

	public String getMaxInstances() {
		return maxInstances;
	}

	public void setMaxInstances(String maxInstances) {
		this.maxInstances = maxInstances;
	}

	public String getRuleExpression() {
		return ruleExpression;
	}

	public void setRuleExpression(String ruleExpression) {
		this.ruleExpression = ruleExpression;
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