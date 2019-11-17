package vlad;

import java.util.Map;

public class K8sClient {

	public static K8sClient getClient() {
		
		Map<String,String> env = System.getenv();
		
		return null;
	}
	
	public K8sClient( String KUBERNETES_PORT, byte[] caCert, byte[] token ) {
		
	}
	
	
}
