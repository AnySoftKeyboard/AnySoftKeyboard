package github;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

class HttpClientCreator {
    public static CloseableHttpClient create(String username, String password) {
        BasicCredentialsProvider creds = new BasicCredentialsProvider();
        creds.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
        return HttpClientBuilder.create().setDefaultCredentialsProvider(creds).build();
    }
}
