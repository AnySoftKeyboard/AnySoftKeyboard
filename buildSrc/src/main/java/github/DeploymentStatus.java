package github;

import com.google.gson.Gson;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Scanner;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;

public class DeploymentStatus {

    private final Gson mGson;

    private final String username;
    private final String password;

    public DeploymentStatus(String username, String password) {
        this.username = username;
        this.password = password;
        mGson = GsonCreator.create();
    }

    public Response requestDeploymentStatus(String deploymentId, Request request) throws Exception {
        final String requestJson = mGson.toJson(request);
        System.out.println("Request: " + requestJson);

        try (CloseableHttpClient client = HttpClientCreator.create(username, password)) {
            HttpPost httpPost =
                    new HttpPost(
                            "https://api.github.com/repos/AnySoftKeyboard/AnySoftKeyboard/deployments/"
                                    + deploymentId
                                    + "/statuses");
            httpPost.setEntity(new StringEntity(requestJson, StandardCharsets.UTF_8));
            httpPost.addHeader("Accept", "application/vnd.github.flash-preview+json");
            httpPost.addHeader("Accept", "application/vnd.github.ant-man-preview+json");
            try (CloseableHttpResponse httpResponse =
                    client.execute(httpPost, HttpClientCreator.createContext(username, password))) {
                System.out.println("Response status: " + httpResponse.getStatusLine());
                final Scanner scanner =
                        new Scanner(httpResponse.getEntity().getContent(), StandardCharsets.UTF_8)
                                .useDelimiter("\\A");
                final String responseString = scanner.hasNext() ? scanner.next() : "";
                System.out.println("Response content: " + responseString);
                if (httpResponse.getStatusLine().getStatusCode() > 299
                        || httpResponse.getStatusLine().getStatusCode() < 200) {
                    throw new IOException(
                            String.format(
                                    Locale.ROOT,
                                    "Got non-OK response status '%s' with content: %s",
                                    httpResponse.getStatusLine(),
                                    responseString));
                }
                return mGson.fromJson(responseString, Response.class);
            }
        }
    }

    public static class Request {
        public final String environment;
        public final String state;
        public final boolean auto_inactive;

        public Request(String environment, String state) {
            this.environment = environment;
            this.state = state;
            this.auto_inactive = "success".equals(state);
        }
    }

    public static class Response {
        public final String id;
        public final String state;
        public final String description;
        public final String environment;

        public Response(String id, String state, String description, String environment) {
            this.id = id;
            this.state = state;
            this.description = description;
            this.environment = environment;
        }
    }
}
