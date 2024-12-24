package deployment;

import java.util.Map;

class RequestCommandLineArgs {
  static final String PROP_KEY_API_USERNAME = "Request.apiUsername";
  static final String PROP_KEY_API_TOKEN = "Request.apiUserToken";
  final String apiUsername;
  final String apiUserToken;

  RequestCommandLineArgs(Map<String, ?> properties) {
    this.apiUsername = properties.get(PROP_KEY_API_USERNAME).toString();
    this.apiUserToken = properties.get(PROP_KEY_API_TOKEN).toString();
  }
}
