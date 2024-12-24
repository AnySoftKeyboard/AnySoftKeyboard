package github;

import java.nio.charset.StandardCharsets;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;

public class DeploymentStatus
    extends RestRequestPerformer<DeploymentStatus.Request, DeploymentStatus.Response> {

  public DeploymentStatus(String username, String password) {
    super(username, password, Response.class);
  }

  @Override
  protected HttpUriRequest createHttpRequest(Request request, String requestJsonAsString) {
    final HttpPost httpPost =
        new HttpPost(
            "https://api.github.com/repos/AnySoftKeyboard/AnySoftKeyboard/deployments/"
                + request.id
                + "/statuses");
    httpPost.setEntity(new StringEntity(requestJsonAsString, StandardCharsets.UTF_8));
    httpPost.addHeader("Accept", "application/vnd.github.flash-preview+json");
    httpPost.addHeader("Accept", "application/vnd.github.ant-man-preview+json");
    return httpPost;
  }

  public static class Request {
    public final String id;
    public final String environment;
    public final String state;
    public final boolean auto_inactive;

    public Request(String id, String environment, String state) {
      this.id = id;
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
