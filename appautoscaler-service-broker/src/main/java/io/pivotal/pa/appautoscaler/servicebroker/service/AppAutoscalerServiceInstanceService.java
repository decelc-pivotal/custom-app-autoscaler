package io.pivotal.pa.appautoscaler.servicebroker.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.cloudfoundry.client.lib.StartingInfo;
import org.cloudfoundry.client.lib.domain.Staging;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceResponse;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceResponse;
import org.springframework.cloud.servicebroker.model.GetLastServiceOperationRequest;
import org.springframework.cloud.servicebroker.model.GetLastServiceOperationResponse;
import org.springframework.cloud.servicebroker.model.OperationState;
import org.springframework.cloud.servicebroker.model.UpdateServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.UpdateServiceInstanceResponse;
import org.springframework.cloud.servicebroker.service.ServiceInstanceService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;

import io.pivotal.pa.appautoscaler.servicebroker.client.CloudFoundryConnection;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AppAutoscalerServiceInstanceService implements ServiceInstanceService {

	@Autowired
	private ResourceLoader resourceLoader;

	private final String ORGANIZATION = "appautoscaler-org";
	private final String SPACE = "appautoscaler-space";
	private final String APP_PREFIX = "appautoscaler-";

	private long appStartupPollIntervalMs;
	private long appStartupTimeoutMs;

	@Override
	public CreateServiceInstanceResponse createServiceInstance(CreateServiceInstanceRequest arg0) {

		log.info("createServiceInstance CreateServiceInstanceRequest=" + arg0);
		log.info("createServiceInstance CreateServiceInstanceRequest resourceLoader=" + resourceLoader);

		CreateServiceInstanceResponse response = new CreateServiceInstanceResponse();

		Map<String, Object> params = arg0.getParameters();

		log.info("createServiceInstance params=" + params);

		if (params != null) {
			for (String key : params.keySet()) {
				log.info("createServiceInstance key=" + key + " value=" + params.get(key));
			}
		}

		pushApp(arg0.getServiceInstanceId());

		return response;
	}

	@Override
	public DeleteServiceInstanceResponse deleteServiceInstance(DeleteServiceInstanceRequest arg0) {

		log.info("eleteServiceInstance start");
		deleteApp(arg0.getServiceInstanceId());
		log.info("deleteServiceInstance end");
		return new DeleteServiceInstanceResponse();
	}

	@Override
	public GetLastServiceOperationResponse getLastOperation(GetLastServiceOperationRequest arg0) {
		// TODO Auto-generated method stub
		return new GetLastServiceOperationResponse().withOperationState(OperationState.SUCCEEDED);
	}

	@Override
	public UpdateServiceInstanceResponse updateServiceInstance(UpdateServiceInstanceRequest arg0) {

		log.info("updateServiceInstance=" + arg0.getParameters());// +
																					// response.toString());
		Map<String, Object> params = arg0.getParameters();

		log.info("updateServiceInstance params=" + params);

		if (params != null) {
			for (String key : params.keySet()) {
				log.info("updateServiceInstance key=" + key + " value=" + params.get(key));
			}
		}

		return new UpdateServiceInstanceResponse();
	}

	private String deleteApp(String serviceInstanceID) {

		log.info("deleteApp serviceInstanceID=" + serviceInstanceID);

		try {

			CloudFoundryClient client = getCloudFoundryClient();

			String appName = getAppName(serviceInstanceID);

			// delete the old one
			if (client.getApplication(appName) != null) {
				client.deleteApplication(appName);
			}

			log.info("deleteApp client=" + client);

		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();

		}

		return "";
	}

	private String pushApp(String serviceInstanceID) {

		log.info("pushApp serviceInstanceID=" + serviceInstanceID);
		log.info("pushApp resourceLoader=" + resourceLoader);

		CloudFoundryClient client = null;

		String buildpack = "https://github.com/cloudfoundry/java-buildpack";

		try {
			client = getCloudFoundryClient();

			// connection.
			log.info("pushApp client=" + client);

		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();

		}

		String startCommand = "CALCULATED_MEMORY=$($PWD/.java-buildpack/open_jdk_jre/bin/java-buildpack-memory-calculator-3.8.0_RELEASE -totMemory=$MEMORY_LIMIT -stackThreads=300 -loadedClasses=25088 -poolType=metaspace) &&  JAVA_HOME=$PWD/.java-buildpack/open_jdk_jre JAVA_OPTS=\"-Djava.io.tmpdir=$TMPDIR $CALCULATED_MEMORY -agentpath:$PWD/.java-buildpack/open_jdk_jre/bin/jvmkill-1.8.0_RELEASE=printHeapHistogram=1 -Djavax.net.ssl.trustStore=$PWD/.java-buildpack/container_certificate_trust_store/truststore.jks -Djavax.net.ssl.trustStorePassword=java-buildpack-trust-store-password -Djava.endorsed.dirs=$PWD/.java-buildpack/tomcat/endorsed -Daccess.logging.enabled=false -Dhttp.port=$PORT\" exec $PWD/.java-buildpack/tomcat/bin/catalina.sh run";
		Staging staging = new Staging(null, buildpack);

		// Staging staging = new Staging();

		String domain = ".apps.decelles.io";

		ArrayList<String> urls = new ArrayList<String>();

		String appName = getAppName(serviceInstanceID);

		String routeURL = appName + domain;

		urls.add(routeURL);
		log.info("routeURL= " + routeURL);

		client.createApplication(appName, staging, 1024, urls, new ArrayList<String>());
		String jarResourcePath = "classpath:jar-files/appautoscaler-worker-0.0.1-SNAPSHOT.jar";
		log.info("jarResourcePath=" + jarResourcePath);

		try {
			Resource resource = resourceLoader.getResource(jarResourcePath);
			log.info("resource = " + resource);
			if (resource != null) {
				InputStream stream = resource.getInputStream();
				log.info("stream = " + stream);
				String filename = resource.getFilename();
				log.info("filename = " + filename);
				client.uploadApplication(appName, resource.getFilename(), stream);
				List<String> env = new ArrayList<String>();

				env.add("CC_HOST=" + System.getenv("CC_HOST"));
				env.add("LOGIN_HOST=" + System.getenv("LOGIN_HOST"));
				env.add("CF_ADMIN_USER=" + System.getenv("SECURITY_USER_NAME"));
				env.add("CF_ADMIN_PASSWORD=" + System.getenv("SECURITY_USER_PASSWORD"));
				client.updateApplicationEnv(appName, env);
			}

		} catch (IOException e) {
			IllegalStateException ex = new IllegalStateException(
					"Error getting application archive from resource " + jarResourcePath, e);
			log.error(ex.getMessage());
			throw ex;
		}

		StartingInfo info = client.startApplication(appName);

		log.info("startingINfo = " + info.getStagingFile());
		return "";
	}

	private CloudFoundryClient getCloudFoundryClient() throws MalformedURLException {
		log.info("getCloudFoundryClient start");

		CloudFoundryConnection connection;
		CloudFoundryClient client = null;
		URL targetUrl;

		String client_id = System.getenv("SECURITY_USER_NAME");
		String client_secret = System.getenv("SECURITY_USER_PASSWORD");
		String cf_target = System.getenv("CC_HOST");
		log.info("pushApp SECURITY_USER_NAME=" + client_id);
		log.info("pushApp SECURITY_USER_PASSWORD=" + client_secret);
		log.info("pushApp CC_HOST=" + cf_target);
		log.info("getCloudFoundryClient BUILDPACK=" + System.getenv("BUILDPACK"));

		try {
			targetUrl = new URL(cf_target);
			log.info("targetUrl=" + targetUrl);
			connection = new CloudFoundryConnection(targetUrl, client_id, client_secret, ORGANIZATION, SPACE, true);
			log.info("pushApp connection=" + connection);

			client = connection.createClient();
			log.info("getCloudFoundryClient client=" + client);

			return client;

		} catch (MalformedURLException e1) {
			log.error(e1.getMessage());

		}

		return client;
	}

	private String getAppName(String serviceInstanceID) {

		return APP_PREFIX + serviceInstanceID;
	}

}
