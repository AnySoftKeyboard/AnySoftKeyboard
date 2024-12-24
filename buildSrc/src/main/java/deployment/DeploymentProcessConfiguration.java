package deployment;

import java.util.ArrayList;
import java.util.List;

public class DeploymentProcessConfiguration {
  public final String name;

  public List<String> environmentSteps = new ArrayList<>();

  public DeploymentProcessConfiguration(String name) {
    this.name = name;
  }
}
