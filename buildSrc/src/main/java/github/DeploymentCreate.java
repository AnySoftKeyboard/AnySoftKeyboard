package github;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;

public class DeploymentCreate
    extends RestRequestPerformer<DeploymentCreate.Request, DeploymentCreate.Response> {

  public DeploymentCreate(String username, String password) {
    super(username, password, DeploymentCreate.Response.class);
  }

  @Override
  protected HttpUriRequest createHttpRequest(Request request, String requestJsonAsString) {
    final HttpPost httpPost =
        new HttpPost("https://api.github.com/repos/AnySoftKeyboard/AnySoftKeyboard/deployments");
    httpPost.setEntity(new StringEntity(requestJsonAsString, StandardCharsets.UTF_8));
    return httpPost;
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
    public final String previous_environment;

    public RequestPayloadField(List<String> environmentsToKill, String previousEnvironment) {
      environments_to_kill = environmentsToKill;
      previous_environment = previousEnvironment;
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
