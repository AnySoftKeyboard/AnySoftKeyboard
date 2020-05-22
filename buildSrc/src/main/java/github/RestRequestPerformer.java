package github;

import com.google.gson.Gson;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Scanner;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;

public abstract class RestRequestPerformer<R, A> {
    private final Gson mGson;

    private final String username;
    private final String password;
    private final Class<A> responseClass;

    public RestRequestPerformer(String username, String password, Class<A> responseClass) {
        this.username = username;
        this.password = password;
        this.responseClass = responseClass;
        mGson = GsonCreator.create();
    }

    public A request(R request) throws IOException {
        final String requestAsJsonString = mGson.toJson(request);
        System.out.println("Request: " + requestAsJsonString);

        try (CloseableHttpClient client = HttpClientCreator.create(username, password)) {
            HttpUriRequest httpRequest = createHttpRequest(request, requestAsJsonString);
            try (CloseableHttpResponse httpResponse =
                    client.execute(
                            httpRequest, HttpClientCreator.createContext(username, password))) {
                System.out.println("Response status: " + httpResponse.getStatusLine());
                final Scanner scanner =
                        new Scanner(httpResponse.getEntity().getContent(), StandardCharsets.UTF_8.toString())
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
                return mGson.fromJson(responseString, responseClass);
            }
        }
    }

    protected abstract HttpUriRequest createHttpRequest(R request, String requestJsonAsString);
}
