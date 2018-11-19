import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Task to generate words-list XML file from a AOSP words-list file.
 * https://android.googlesource.com/platform/packages/inputmethods/LatinIME/+/master/dictionaries/
 */
public class GenerateWordsListFromAOSPTask extends DefaultTask {
    private static final Pattern mWordLineRegex = Pattern.compile("^\\s*word=([\\w\\p{L}'\"-]+),f=(\\d+).*$");

    private File inputFile;
    private File outputWordsListFile;
    private int maxWordsInList = 200000;

    @TaskAction
    public void generateWordsList() throws IOException {
        if (inputFile == null)
            throw new IllegalArgumentException("Please provide inputFile value.");
        if (!inputFile.isFile()) throw new IllegalArgumentException("inputFile must be a file!");
        if (outputWordsListFile == null)
            throw new IllegalArgumentException("Please provide outputWordsListFile value.");

        final long inputSize = inputFile.length();
        System.out.println("Reading input file " + inputFile.getName() + " (size " + inputSize + ")...");
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), Charset.forName("UTF-8")));
        String wordDataLine;
        List<WordWithCount> parsedWords = new ArrayList<>();
        int read = 0;
        while (null != (wordDataLine = reader.readLine())) {
            read += wordDataLine.length();
            //word=heh,f=0,flags=,originalFreq=53,possibly_offensive=true
            Matcher matcher = mWordLineRegex.matcher(wordDataLine);
            if (matcher.matches()) {
                String word = matcher.group(1);
                int frequency = Integer.parseInt(matcher.group(2));
                parsedWords.add(new WordWithCount(word, frequency));
                if ((parsedWords.size() % 50000) == 0) {
                    System.out.print("." + ((100 * read) / inputSize) + "%.");
                }
            }
        }

        System.out.print(".100%.");

        System.out.println("Sorting list of " + parsedWords.size() + " words...");
        Collections.sort(parsedWords);

        final File parentFile = outputWordsListFile.getParentFile();
        if (!parentFile.exists() && !parentFile.mkdirs()) {
            throw new IllegalArgumentException("Failed to create output folder " + parentFile.getAbsolutePath());
        }

        OutputStreamWriter output = new OutputStreamWriter(new FileOutputStream(outputWordsListFile), Charset.forName("UTF-8"));
        Parser.createXml(parsedWords, output, maxWordsInList, true);

        output.flush();
        output.close();

        System.out.println("Done.");
    }

    @InputFile
    public File getInputFile() {
        return inputFile;
    }

    public void setInputFile(File inputFile) {
        this.inputFile = inputFile;
    }

    @OutputFile
    public File getOutputWordsListFile() {
        return outputWordsListFile;
    }

    public void setOutputWordsListFile(File outputWordsListFile) {
        this.outputWordsListFile = outputWordsListFile;
    }

    @Input
    public int getMaxWordsInList() {
        return maxWordsInList;
    }

    public void setMaxWordsInList(int maxWordsInList) {
        this.maxWordsInList = maxWordsInList;
    }
}
