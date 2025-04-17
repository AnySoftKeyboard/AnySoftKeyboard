package github;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;

class HttpClientCreator {
  public static CloseableHttpClient create(String username, String password) {
    BasicCredentialsProvider creds = new BasicCredentialsProvider();
    creds.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

    return HttpClientBuilder.create()
        .setRedirectStrategy(new LaxRedirectStrategy())
        .setDefaultCredentialsProvider(creds)
        .build();
  }

  static HttpClientContext createContext(String username, String password) {
    BasicCredentialsProvider creds = new BasicCredentialsProvider();
    creds.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

    AuthCache authCache = new BasicAuthCache();
    authCache.put(new HttpHost("api.github.com", 443, "https"), new BasicScheme());
    // Add AuthCache to the execution context
    HttpClientContext context = HttpClientContext.create();
    context.setCredentialsProvider(creds);
    context.setAuthCache(authCache);

    return context;
  }
}
