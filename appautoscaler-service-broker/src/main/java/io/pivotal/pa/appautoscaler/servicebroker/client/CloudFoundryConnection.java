package io.pivotal.pa.appautoscaler.servicebroker.client;

import java.net.URL;

import org.cloudfoundry.client.lib.CloudCredentials;
import org.cloudfoundry.client.lib.CloudFoundryClient;

public class CloudFoundryConnection {
	
	private final CloudCredentials credentials;

	private final URL targetUrl;
	
	private final String organization;
	
	private final String space;
	
	private final boolean skipSSLVerification;

	public CloudFoundryConnection(URL targetUrl, String username, String password,
								  String organization, String space, boolean skipSSLVerification) {
		this.targetUrl = targetUrl;
		this.organization = organization;
		this.space = space;
		this.credentials = new CloudCredentials(username, password);
		this.skipSSLVerification = true;
	}


	public CloudFoundryClient createClient() {
		return new CloudFoundryClient(credentials, targetUrl, organization, space, skipSSLVerification);
	}
	
	public URL getTargetUrl() {
		return targetUrl;
	}
}
