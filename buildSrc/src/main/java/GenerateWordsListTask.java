import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.jsoup.Jsoup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Task to generate words-list XML file from a input
 */
public class GenerateWordsListTask extends DefaultTask {
    @TaskAction
    public void generateWordsList() throws Exception {
        final List<File> inputTextFiles = new ArrayList<>();
        for (File it : inputFiles) {
            if (it.getName().endsWith(".html") || it.getName().endsWith(".htm")) {
                File wordsInputFile = File.createTempFile(it.getName() + "_stripped_html_", ".txt");
                String inputText = Jsoup.parse(it, "UTF-8").text();

                Writer writer = new OutputStreamWriter(new FileOutputStream(wordsInputFile), Charset.forName("UTF-8"));
                writer.write(inputText);
                writer.flush();
                writer.close();
                inputTextFiles.add(wordsInputFile);
            } else if (it.getName().endsWith(".txt")) {
                inputTextFiles.add(it);
            } else {
                System.out.println("Skipping file " + it.getAbsolutePath() + ", since it's not txt or html file.");
            }
        }

        final File parentFile = outputWordsListFile.getParentFile();
        if (!parentFile.exists() && !parentFile.mkdirs()) {
            throw new IllegalArgumentException("Failed to create output folder " + parentFile.getAbsolutePath());
        }
        Parser parser = new Parser(inputTextFiles, outputWordsListFile, wordCharacters, locale, additionalInnerCharacters, maxWordsInList);
        parser.parse();
    }

    @InputFiles
    public File[] getInputFiles() {
        return inputFiles;
    }

    public void setInputFiles(File[] inputFiles) {
        this.inputFiles = inputFiles;
    }

    @OutputFile
    public File getOutputWordsListFile() {
        return outputWordsListFile;
    }

    public void setOutputWordsListFile(File outputWordsListFile) {
        this.outputWordsListFile = outputWordsListFile;
    }

    @Input
    public char[] getWordCharacters() {
        return wordCharacters;
    }

    public void setWordCharacters(char[] wordCharacters) {
        this.wordCharacters = wordCharacters;
    }

    @Input
    public char[] getAdditionalInnerCharacters() {
        return additionalInnerCharacters;
    }

    public void setAdditionalInnerCharacters(char[] additionalInnerCharacters) {
        this.additionalInnerCharacters = additionalInnerCharacters;
    }

    @Input
    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    @Input
    public int getMaxWordsInList() {
        return maxWordsInList;
    }

    public void setMaxWordsInList(int maxWordsInList) {
        this.maxWordsInList = maxWordsInList;
    }

    private File[] inputFiles;
    private File outputWordsListFile;
    private char[] wordCharacters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
    private char[] additionalInnerCharacters = "'".toCharArray();
    private Locale locale = Locale.US;
    private int maxWordsInList = Integer.MAX_VALUE;
}
