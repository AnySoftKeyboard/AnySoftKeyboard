package github;

import com.google.gson.Gson;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;

public class Deployment {

    private final Gson mGson;

    private final String username;
    private final String password;

    public Deployment(String username, String password) {
        this.username = username;
        this.password = password;
        mGson = GsonCreator.create();
    }

    public Response requestDeployment(Request request) throws Exception {
        final String requestJson = mGson.toJson(request);
        System.out.println("Request: " + requestJson);

        try (CloseableHttpClient client = HttpClientCreator.create(username, password)) {
            HttpPost httpPost =
                    new HttpPost(
                            "https://api.github.com/repos/AnySoftKeyboard/AnySoftKeyboard/deployments");
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setEntity(new StringEntity(requestJson, StandardCharsets.UTF_8));
            try (CloseableHttpResponse httpResponse = client.execute(httpPost)) {
                System.out.println("Response status: " + httpResponse.getStatusLine());
                return mGson.fromJson(
                        new InputStreamReader(httpResponse.getEntity().getContent()),
                        Response.class);
            }
        }
    }

    public static class Request {
        public final String ref;
        public final String task;
        public final boolean auto_merge;
        public final String environment;
        public final String description;
        public final List<String> required_contexts;
        public final RequestPayloadField payload;

        public Request(
                String ref,
                String task,
                boolean auto_merge,
                String environment,
                String description,
                List<String> required_contexts,
                RequestPayloadField payload) {
            this.ref = ref;
            this.task = task;
            this.auto_merge = auto_merge;
            this.environment = environment;
            this.description = description;
            this.required_contexts = required_contexts;
            this.payload = payload;
        }
    }

    public static class RequestPayloadField {
        public final List<String> environments_to_kill;

        public RequestPayloadField(List<String> environmentsToKill) {
            environments_to_kill = environmentsToKill;
        }
    }

    public static class Response {
        public final String id;
        public final String sha;
        public final String ref;
        public final String task;
        public final RequestPayloadField payload;
        public final String environment;

        public Response(
                String id,
                String sha,
                String ref,
                String task,
                RequestPayloadField payload,
                String environment) {
            this.id = id;
            this.sha = sha;
            this.ref = ref;
            this.task = task;
            this.payload = payload;
            this.environment = environment;
        }
    }
}
