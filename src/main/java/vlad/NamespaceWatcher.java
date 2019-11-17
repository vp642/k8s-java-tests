package vlad;

import com.google.gson.reflect.TypeToken;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1Namespace;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.Watch;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class NamespaceWatcher {
	 public static void main(String[] args) throws IOException, ApiException {
		    ApiClient client = Config.defaultClient();
		    client.getHttpClient().setReadTimeout(0, TimeUnit.SECONDS); // infinite timeout
		    Configuration.setDefaultApiClient(client);

		    CoreV1Api api = new CoreV1Api();

		    Watch<V1Namespace> watch =
		        Watch.createWatch(
		            client,
		            api.listNamespaceCall(null, null, null, null, 5, null, null, Boolean.TRUE, null, null),
		            new TypeToken<Watch.Response<V1Namespace>>() {}.getType());

		    try {
		      for (Watch.Response<V1Namespace> item : watch) {
		        System.out.printf("%s : %s%n", item.type, item.object.getMetadata().getName());
		      }
		    } finally {
		      watch.close();
		    }
		  }
}
