package vlad;

import static io.kubernetes.client.KubernetesConstants.V1STATUS_CAUSE_REASON_EXITCODE;
import static io.kubernetes.client.KubernetesConstants.V1STATUS_REASON_NONZEROEXITCODE;
import static io.kubernetes.client.KubernetesConstants.V1STATUS_SUCCESS;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import com.google.gson.reflect.TypeToken;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.models.V1Status;
import io.kubernetes.client.models.V1StatusCause;
import io.kubernetes.client.models.V1StatusDetails;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.WebSocketStreamHandler;
import io.kubernetes.client.util.WebSockets;

/**
 * {@link https://blog.openshift.com/executing-commands-in-pods-using-k8s-api/ }
 */

public class K8sExec {
	private static final Logger logger = LogManager.getLogger();
	/*
	 * public K8sExec() throws IOException {
	 * 
	 * apiClient = Config.defaultClient();
	 * Configuration.setDefaultApiClient(apiClient);
	 * 
	 * }
	 * 
	 * 
	 * static public int exec(String namespace, String podName, String
	 * container, String[] commands) throws ApiException, IOException,
	 * InterruptedException { // Exec exec = new Exec();
	 * 
	 * boolean tty = System.console() != null;
	 * 
	 * logger.trace("Process exec: namespace:{} pod:{} command:{}", namespace,
	 * podName, String.join(" ", commands));
	 * 
	 * // final Process proc = exec.exec(namespace, podName, commands,
	 * container, // false, false);
	 * 
	 * logger.trace("Process exec completed");
	 * 
	 * // Flux<String> stdout = Flux.from( proc.getInputStream() );
	 * 
	 * // { // // InputStreamReader isr = new
	 * InputStreamReader(proc.getInputStream() ); // BufferedReader br = new
	 * BufferedReader( isr ); // String line; // while( (line = br.readLine())
	 * != null ) // System.out.printf( "%s\n", line ); //
	 * ByteStreams.copy(proc.getInputStream(), System.out);
	 * 
	 * // } Thread err = new Thread(new Runnable() { public void run() { try {
	 * ByteStreams.copy(proc.getErrorStream(), System.err); } catch (IOException
	 * ex) { ex.printStackTrace(); } } }); err.start();
	 * 
	 * logger.trace("err reader thread started");
	 * 
	 * Thread out = new Thread(new Runnable() { public void run() { try {
	 * 
	 * InputStreamReader isr = new InputStreamReader( proc.getInputStream());
	 * BufferedReader br = new BufferedReader(isr); String line; while ((line =
	 * br.readLine()) != null) System.out.printf("%s\n", line); //
	 * ByteStreams.copy(proc.getInputStream(), System.out); } catch (IOException
	 * ex) { ex.printStackTrace(); } } });
	 * 
	 * out.start();
	 * 
	 * // logger.trace("out reader thread started"); // proc.waitFor();
	 * 
	 * // logger.trace("process exited");
	 * 
	 * // wait for any last output; no need to wait for input thread out.join();
	 * err.join();
	 * 
	 * logger.trace("reader threads joined");
	 * 
	 * proc.destroy(); logger.trace("process destroyed");
	 * 
	 * return (proc.exitValue()); }
	 */

}
