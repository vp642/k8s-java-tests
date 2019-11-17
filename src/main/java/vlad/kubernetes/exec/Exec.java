package vlad.kubernetes.exec;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.squareup.okhttp.ws.WebSocket;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.util.WebSockets;

/**
 * Kubernetes Exec.
 * 
 * <p>
 * Example:
 * </p>
 * 
 * <pre>
 * Exec exec = null;
 * ApiClient apiClient = null;
 * 
 * try {
 *     apiClient = Config.defaultClient();
 * } catch (IOException e) {
 *     System.err.println(e.toString());
 *     System.exit(1);
 * }
 * 
 * exec = new Exec(apiClient);
 * 
 * try {
 *     Future<String> exitStatus = exec.exec(paramsMap.get("--namespace").get(),
 *             paramsMap.get("--pod").get(),
 *             paramsMap.get("--container").orElse(null),
 *             cmd.toArray(new String[cmd.size()]), System.out::println);
 * 
 *     System.out.println(exitStatus.get(1, TimeUnit.SECONDS));
 * 
 * } catch (ApiException | IOException | ExecutionException
 *         | TimeoutException e) {
 *     System.err.println(e.toString());
 * }
 * 
 * exec.shutdown();
 * </pre>
 * 
 */
public class Exec {

    private static final Logger logger = LoggerFactory.getLogger(Exec.class);

    private ApiClient apiClient;

    public Exec() {
        this(Configuration.getDefaultApiClient());
    }

    public Exec(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    private String makePath(String namespace, String podName, String[] command,
            String container, boolean stdin, boolean tty) {
        for (int i = 0; i < command.length; i++) {
            try {
                command[i] = URLEncoder.encode(command[i], "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                throw new RuntimeException(
                        "some thing wrong happend: " + ex.getMessage());
            }
        }
        String path = "/api/v1/namespaces/" + namespace + "/pods/" + podName
                + "/exec?" + "stdin=" + stdin + "&stdout=true" + "&stderr=true"
                + "&tty=" + tty
                + (container != null ? "&container=" + container : "")
                + "&command=" + StringUtils.join(command, "&command=");
        return path;
    }

    /**
     * Execute a command in a Kuberntes pod.
     * 
     * @param namespace - Kubernetes namespace. Optional. Kubernetes client will
     * use namespace defined in the context, if omitted.
     * @param podName - Kubernetes pod name.
     * @param containerName - Container name inside the pod. Optional.
     * @param cmds - String array of commands and parameters. Every word between
     * spaces must be an array element. Example: { "mkdir","/mydir" }
     * @param consumer - Consumer will receive the command messages. The first
     * character of the message is the stream number: 1 - stdout, 2 - stderr
     * @throws ApiException
     * @throws IOException
     * 
     * @return exitStatus - {@code Future<String>}. Values: <em>Success</em> or
     * <em>Failure</em>
     */

    public Future<String> exec(String namespace, String podName,
            String containerName, String[] cmds, Consumer<String> consumer)
            throws ApiException, IOException {
        ExecWebSocketListener listener = new ExecWebSocketListener(consumer);

        WebSockets.stream(
                makePath(namespace, podName, cmds, containerName, false, false),
                "GET", apiClient, listener);

        return listener.getStatus();
    }

    /**
     * Shuts down OkHttp client executor service.
     * 
     * Kubernetes Java library WebSockets class uses the <i>enqueue</i> call to
     * run websockets clients in the background. When a task execution completes
     * the program wont' terminate immediately unless the Executor Service is
     * explicitly shutdown.
     */
    public void shutdown() {

        apiClient.getHttpClient().getDispatcher().getExecutorService()
                .shutdown();
    }

    protected class ExecWebSocketListener
            implements WebSockets.SocketListener, Closeable {

        private Gson gson = new Gson();
        private Consumer<String> consumer;
        private WebSocket socket;
        private boolean isCancelled = false;
        private String exitStatus;
        private final CountDownLatch latch = new CountDownLatch(1);

        public ExecWebSocketListener(Consumer<String> consumer) {
            this.consumer = consumer;
        }

        @Override
        public void open(String protocol, WebSocket socket) {
            this.socket = socket;
        }

        @Override
        public void bytesMessage(InputStream in) {
            try {

                String streamNum = String.valueOf((int) in.read());
                BufferedReader bufReader = new BufferedReader(
                        new InputStreamReader(in));

                String line;
                while ((line = bufReader.readLine()) != null) {
                    if ("3".matches(streamNum))
                        exitStatus = gson.fromJson(line, StatusMessage.class)
                                .toString();
                    else
                        consumer.accept(streamNum + line);
                }
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }

        @Override
        public void textMessage(Reader in) {
            try {
                BufferedReader bufReader = new BufferedReader(in);
                String line;
                while ((line = bufReader.readLine()) != null) {
                    logger.trace(line);
                    consumer.accept(line);
                }

            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }

        @Override
        public void close() {
            latch.countDown();
        }

        public Status getStatus() {

            return new Status();
        }

        protected class Status implements Future<String> {

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {

                if (isCancelled)
                    return true;

                if (isDone())
                    return false;

                if (mayInterruptIfRunning) {
                    // Closure codes
                    // https://tools.ietf.org/html/rfc6455#section-7.4
                    try {
                        if (socket != null) {
                            isCancelled = true;
                            socket.close(1001,
                                    "client requested to cancel the session");
                            return true;
                        }
                    } catch (IOException e) {
                        logger.error(e.toString());
                    }
                }

                return false;
            }

            @Override
            public boolean isCancelled() {
                return isCancelled;
            }

            @Override
            public boolean isDone() {

                if (isCancelled)
                    return true;

                return latch.getCount() == 0;
            }

            @Override
            public String get()
                    throws InterruptedException, ExecutionException {

                latch.await();
                return exitStatus;
            }

            @Override
            public String get(long timeout, TimeUnit unit)
                    throws ExecutionException, InterruptedException,
                    TimeoutException {

                if (!latch.await(timeout, unit)) {
                    cancel(true);
                    throw new TimeoutException(
                            "command execution has timed out");
                }

                return exitStatus;
            }

        }
    }

    public static class StatusMessage {
        public String status;

        @Override
        public String toString() {
            return status;
        }
    }
}
