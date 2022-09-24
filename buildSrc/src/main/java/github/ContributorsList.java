package github;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;

public class ContributorsList
        extends RestRequestPerformer<ContributorsList.Request, ContributorsList.Response[]> {

    public ContributorsList(String username, String password) {
        super(username, password, Response[].class);
    }

    @Override
    protected HttpUriRequest createHttpRequest(Request request, String requestJsonAsString) {
        return new HttpGet(
                "https://api.github.com/repos/AnySoftKeyboard/AnySoftKeyboard/contributors?per_page=200&anon=0");
    }

    public static class Request {}

    public static class Response {
        public final String login;
        public final int contributions;

        public Response(String login, int contributions) {
            this.login = login;
            this.contributions = contributions;
        }
    }
}
