package io.pivotal.pa.appautoscaler.domain;

public class ServiceBindInfo {

	private String service_id;
	private String plan_id;
	private BindResource bind_resource;

	public String getService_id() {
		return service_id;
	}

	public void setService_id(String service_id) {
		this.service_id = service_id;
	}

	public String getPlan_id() {
		return plan_id;
	}

	public void setPlan_id(String plan_id) {
		this.plan_id = plan_id;
	}

	public BindResource getBind_resource() {
		return bind_resource;
	}

	public void setBind_resource(BindResource bindResource) {
		this.bind_resource = bindResource;
	}


	public class BindResource {

		private String app_guid;
		
		public BindResource() {
			super();
		}

		public BindResource(String app_guid) {
			super();
			this.app_guid = app_guid;
		}

		public String getApp_guid() {
			return app_guid;
		}

		public void setApp_guid(String app_guid) {
			this.app_guid = app_guid;
		}   
    }

}
