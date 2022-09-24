package contributors;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class ContributorsPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        var fetchTask =
                project.getTasks()
                        .register(
                                "fetchContributorsList",
                                FetchContributorsListTask.class,
                                task -> {
                                    task.setDescription("Fetches contributors list from GitHub.");
                                    task.setUsername(
                                            propertyOrDefault(project, "Request.apiUsername", ""));
                                    task.setPassword(
                                            propertyOrDefault(project, "Request.apiUserToken", ""));
                                    task.setRepositorySha(
                                            propertyOrDefault(project, "Request.sha", ""));
                                });
        project.getTasks()
                .register(
                        "generateContributorsFile",
                        GenerateContributorsFileTask.class,
                        task -> {
                            task.setDescription("Generates CONTRIBUTORS.ms file from GitHub data.");
                            task.dependsOn(fetchTask);
                            task.setRawContributorsFile(fetchTask.get().getContributorsListFile());
                            task.setMaxContributors(
                                    Integer.parseInt(
                                            propertyOrDefault(
                                                    project, "Request.maxContributors", "20")));
                        });
    }

    private String propertyOrDefault(Project project, String key, String defaultValue) {
        Object value = project.findProperty(key);
        if (value == null) return defaultValue;
        else return value.toString();
    }
}
