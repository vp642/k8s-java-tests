package vlad;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.google.gson.reflect.TypeToken;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.AppsV1Api;
import io.kubernetes.client.apis.AppsV1beta2Api;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1Deployment;
import io.kubernetes.client.models.V1Namespace;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.Watch;

public class DeploymentWatcher {
	 public static void main(String[] args) throws IOException, ApiException {
		    ApiClient client = Config.defaultClient();
		    client.getHttpClient().setReadTimeout(0, TimeUnit.SECONDS); // infinite timeout
		    Configuration.setDefaultApiClient(client);

	//	    CoreV1Api api = new CoreV1Api();
		    AppsV1Api appsV1api = new AppsV1Api();

		    Watch<V1Deployment> deploymentWatch = Watch.createWatch(client, 
		    		appsV1api.listNamespacedDeploymentCall("sb-dte-jenkins-ppeline-ns", null, null, null, null, 10, null, null, Boolean.TRUE, null, null), 
		    		new TypeToken<Watch.Response<V1Deployment>>() {}.getType());
		    /*
		    Watch<V1Namespace> watch =
		        Watch.createWatch(
		            client,
		            api.listNamespaceCall(null, null, null, null, 5, null, null, Boolean.TRUE, null, null),
		            new TypeToken<Watch.Response<V1Namespace>>() {}.getType());
*/
		    try {
		    	System.out.println("Starting watch");
		    	for( Watch.Response<V1Deployment> item : deploymentWatch ) {
		    		System.out.printf( "%s : %s", item.type, item.object.getMetadata().getName());
		    	}
		    } finally {
		    	deploymentWatch.close();
		    }
		    /*
		    try {
		      for (Watch.Response<V1Namespace> item : watch) {
		        System.out.printf("%s : %s%n", item.type, item.object.getMetadata().getName());
		      }
		    } finally {
		      watch.close();
		    }*/
		  }

}
