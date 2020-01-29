import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;

/**
 * Task to create a binary-dictionary readable by AnySoftKeyboard
 */
public class MakeDictionaryTask extends DefaultTask {

    public MakeDictionaryTask() {
        setGroup("AnySoftKeyboard");
        setDescription("Creating AnySoftKeyboard binary dictionary");
    }

    @TaskAction
    public void makeDictionary() throws Exception {
        if (resourcesFolder == null) resourcesFolder = new File(getProject().getProjectDir(), "/src/main/res/");

        if (!getResourcesFolder().exists() && !getResourcesFolder().mkdirs()) {
            throw new IllegalArgumentException("Failed to create output folder " + getResourcesFolder().getAbsolutePath());
        }

        MainClass.buildDictionary(getInputWordsListFile(), getResourcesFolder(), getPrefix());
    }

    @InputFile
    public File getInputWordsListFile() {
        return inputWordsListFile;
    }

    public void setInputWordsListFile(File inputWordsListFile) {
        this.inputWordsListFile = inputWordsListFile;
    }

    @OutputDirectory
    public File getResourcesFolder() {
        return resourcesFolder;
    }

    public void setResourcesFolder(File resourcesFolder) {
        this.resourcesFolder = resourcesFolder;
    }

    @Input
    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    private File inputWordsListFile;
    private File resourcesFolder;
    private String prefix;
}
