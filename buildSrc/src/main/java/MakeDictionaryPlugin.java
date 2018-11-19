import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MakeDictionaryPlugin implements Plugin<Project> {

    private static <T> T getExtValue(Project project, String key, T defaultValue) {
        if (project.hasProperty(key)) {
            return (T) project.getProperties().get(key);
        } else {
            return defaultValue;
        }
    }

    private static Object getExtValue(Project proj, String key) {
        return getExtValue(proj, key, null);
    }

    private <T> T[] arrayPlus(T[] source, T... addition) {
        List<T> list = new ArrayList<>(Arrays.asList((T[]) source));
        list.addAll(Arrays.asList((T[]) addition));
        return list.toArray(source);
    }

    @Override
    public void apply(Project project) {
        final String languageName = project.getParent().getName();
        final File dictionaryOutputDir = new File(project.getBuildDir(), "dictionary");

        //adding dictionary making tasks
        TaskProvider<MergeWordsListTask> mergingTask = project.getTasks().register("mergeAllWordLists", MergeWordsListTask.class, task -> {
            task.setInputWordsListFiles(new File[0]);
            task.setOutputWordsListFile(new File(dictionaryOutputDir, "words_merged.xml"));
            task.setMaxWordsInList(300000);
        });

        TaskProvider<MakeDictionaryTask> makeTask = project.getTasks().register("makeDictionary", MakeDictionaryTask.class, task -> {
            task.dependsOn(mergingTask);
            task.setInputWordsListFile(new File(dictionaryOutputDir, "words_merged.xml"));
            task.setPrefix(languageName);
            task.setResourcesFolder(project.file("src/main/res/"));
        });

        //if AOSP file exists (under language/pack/dictionary/aosp.combined)
        //we'll create the generation task
        //download the words-list from AOSP at https://android.googlesource.com/platform/packages/inputmethods/LatinIME/+/master/dictionaries/
        // make sure that you are using an unzipped file. The XX_wordlist.combined file should be a plain text file.
        if (project.file("dictionary/aosp.combined").exists()) {
            TaskProvider<GenerateWordsListFromAOSPTask> aosp = project.getTasks().register("parseAospDictionary", GenerateWordsListFromAOSPTask.class, task -> {
                System.out.println("Found ASOP words file for " + task.getPath());
                task.setInputFile(project.file("dictionary/aosp.combined"));
                task.setOutputWordsListFile(new File(dictionaryOutputDir, "aosp.xml"));
                task.setMaxWordsInList(300000);
            });

            mergingTask.configure(task -> {
                task.dependsOn(aosp);
                task.setInputWordsListFiles(arrayPlus(task.getInputWordsListFiles(), aosp.get().getOutputWordsListFile()));
            });
        }

        //we can also parse text files and generate word-list based on that.
        if (project.file("dictionary/inputs").exists()) {
            TaskProvider<GenerateWordsListTask> inputs = project.getTasks().register("parseTextInputFiles", GenerateWordsListTask.class, task -> {
                task.setInputFiles(project.file("dictionary/inputs").listFiles());
                task.setOutputWordsListFile(new File(dictionaryOutputDir, "inputs.xml"));

                System.out.println("Found text inputs for " + project.getPath() + " with " + task.getInputFiles().length + " files.");

                char[] dictionaryInputPossibleCharacters = getExtValue(project, "dictionaryInputPossibleCharacters", null);
                if (dictionaryInputPossibleCharacters != null) {
                    task.setWordCharacters(dictionaryInputPossibleCharacters);
                    System.out.println("Overriding input-text files possible characters to " + new String(dictionaryInputPossibleCharacters));
                }
                char[] dictionaryInputAdditionalInnerCharacters = getExtValue(project, "dictionaryInputAdditionalInnerCharacters", null);
                if (dictionaryInputAdditionalInnerCharacters != null) {
                    task.setAdditionalInnerCharacters(dictionaryInputAdditionalInnerCharacters);
                    System.out.println("Overriding input-text files possible additional inner characters to " + new String(dictionaryInputAdditionalInnerCharacters));
                }
            });

            mergingTask.configure(task -> {
                task.dependsOn(inputs);
                task.setInputWordsListFiles(arrayPlus(task.getInputWordsListFiles(), inputs.get().getOutputWordsListFile()));
            });
        }

        //you can also provide pre-built word-list XMLs
        if (project.file("dictionary/prebuilt").exists()) {
            File[] prebuiltFiles = project.file("dictionary/prebuilt").listFiles((dir, name) -> name.endsWith(".xml"));
            if (prebuiltFiles != null && prebuiltFiles.length > 0) {
                mergingTask.configure(task -> {
                    task.setInputWordsListFiles(arrayPlus(task.getInputWordsListFiles(), prebuiltFiles));
                    System.out.println("Found prebuilt word-list folder for " + project.getPath() + " with " + prebuiltFiles.length + " files.");
                });
            }
        }

        project.afterEvaluate(evalProject -> {
            if (getExtValue(project, "shouldGenerateDictionary", true)) {
                project.getTasks().named("preBuild").configure(preBuildTask -> preBuildTask.dependsOn(makeTask));
            } else {
                makeTask.configure(task -> task.setEnabled(false));
                mergingTask.configure(task -> task.setEnabled(false));
            }
        });
    }

}
