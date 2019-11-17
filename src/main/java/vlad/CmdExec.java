package vlad;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.util.Config;
import vlad.kubernetes.exec.Exec;

public class CmdExec {

	private static final Logger logger = LogManager.getLogger();

	private static void parseParams(String[] params,
			Map<String, Optional<String>> paramsMap, List<String> cmd)
			throws Exception {

		List<String> validParams = List.of("--namespace", "--pod",
				"--container");

		for (String param : validParams) {
			paramsMap.put(param, Optional.empty());
		}

		boolean cmdStarted = false;

		for (int i = 0; i < params.length; i++) {

			if (cmdStarted) {
				cmd.add(params[i]);
				continue;
			}

			if (params[i].charAt(0) != '-') {
				cmdStarted = true;
				cmd.add(params[i]);
				continue;
			}

			if (validParams.contains(params[i])) {
				if (i + 1 < params.length) {
					paramsMap.put(params[i], Optional.of(params[++i]));
				}
			} else {
				throw new Exception("Incorrect parameter " + params[i]);
			}

		}

		if (paramsMap.get("--namespace").isEmpty()
				|| paramsMap.get("--pod").isEmpty()) {
			System.out.println(
					"Usage: java -jar k8sexec --pod <pod-name> --namespace <namespace> [--container <container-name>] <command>");
			throw new Exception("One of the mandatory parameters is missing");
		}
	}

	public static void main(String[] args) throws InterruptedException {

		logger.traceEntry();
		if (args.length < 5) {
			System.out.println(
					"Usage: java -jar k8sexec --pod <pod-name> --namespace <namespace> [--container <container-name>] <command>");
			System.exit(1);
		}

		Map<String, Optional<String>> paramsMap = new HashMap<>();
		List<String> cmd = new ArrayList<>();

		try {
			parseParams(args, paramsMap, cmd);
		} catch (Exception e) {
			System.out.println(e);
			System.exit(-1);
		}

		Exec exec=null;
		ApiClient apiClient = null;

		try {
			apiClient = Config.defaultClient();
		} catch (IOException e) {
			System.err.println(e.toString());
			System.exit(1);
		}

		exec = new Exec( apiClient );
		
		try {
			Future<String> exitStatus = exec.exec(paramsMap.get("--namespace").get(),
					paramsMap.get("--pod").get(),
					paramsMap.get("--container").orElse(null),
					cmd.toArray(new String[cmd.size()]), System.out::println);

			System.out.println(exitStatus.get(1,TimeUnit.SECONDS));

		} catch (ApiException | IOException | ExecutionException|TimeoutException e) {
			System.err.println(e.toString());
		}

		exec.shutdown();
		
	}
}
