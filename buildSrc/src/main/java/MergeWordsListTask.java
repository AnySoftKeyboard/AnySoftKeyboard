import static org.gradle.api.tasks.PathSensitivity.RELATIVE;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.TaskAction;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/** Task to merge several word-list files into one */
@CacheableTask
public class MergeWordsListTask extends DefaultTask {
    @TaskAction
    public void mergeWordsLists() throws IOException, ParserConfigurationException, SAXException {
        if (inputWordsListFiles == null || inputWordsListFiles.length == 0) {
            throw new IllegalArgumentException("Must specify at least one inputWordsListFiles");
        }
        if (outputWordsListFile == null) {
            throw new IllegalArgumentException("Must supply outputWordsListFile");
        }

        System.out.printf(
                Locale.ENGLISH,
                "Merging %d files for maximum %d words, and writing into '%s'. Discarding %d words.%n",
                inputWordsListFiles.length,
                maxWordsInList,
                outputWordsListFile.getName(),
                wordsToDiscard.length);
        final HashMap<String, Integer> allWords = new HashMap<>();
        final List<String> inputFilesWithDuplicates = new ArrayList<>();
        for (File inputFile : inputWordsListFiles) {
            System.out.printf(Locale.ENGLISH, "Reading %s...%n", inputFile.getName());
            if (!inputFile.exists()) throw new FileNotFoundException(inputFile.getAbsolutePath());
            SAXParserFactory parserFactor = SAXParserFactory.newInstance();
            SAXParser parser = parserFactor.newSAXParser();

            Set<String> duplicateWords = new HashSet<>();
            try (final InputStreamReader inputStream =
                    new InputStreamReader(new FileInputStream(inputFile), StandardCharsets.UTF_8)) {
                InputSource inputSource = new InputSource(inputStream);
                parser.parse(inputSource, new MySaxHandler(allWords, duplicateWords));
                System.out.printf(Locale.ENGLISH, "Loaded %d words in total...%n", allWords.size());
            }
            if (duplicateWords.size() > 0 && !inputFile.getAbsolutePath().contains("/build/")) {
                inputFilesWithDuplicates.add(inputFile.getAbsolutePath());
                filterWordsFromInputFile(inputFile, duplicateWords);
            }
        }

        // discarding unwanted words
        if (wordsToDiscard.length > 0) {
            System.out.print("Discarding words...");
            Arrays.stream(wordsToDiscard)
                    .forEach(
                            word -> {
                                if (allWords.remove(word) != null) System.out.print(".");
                            });
            System.out.println();
        }

        System.out.println("Creating output XML file...");
        try (WordListWriter writer = new WordListWriter(outputWordsListFile)) {
            for (Map.Entry<String, Integer> entry : allWords.entrySet()) {
                WordListWriter.writeWordWithRuntimeException(
                        writer, entry.getKey(), entry.getValue());
            }
            System.out.println("Done.");
        }

        if (inputFilesWithDuplicates.size() > 0) {
            throw new RuntimeException(
                    "Found duplicate words in: " + String.join(",", inputFilesWithDuplicates));
        }
    }

    private static final Pattern WORD_LIST_ENTRY =
            Pattern.compile("\\s*<w\\s+f=\"\\d+\">(.+)</w>\\s*");

    private static void filterWordsFromInputFile(File inputFile, Set<String> duplicateWords)
            throws IOException {
        File tempFile = Files.createTempFile("filtered_word_list", "tmp").toFile();

        System.out.printf(
                Locale.ENGLISH,
                "Removing duplicate words from '%s': ",
                inputFile.getAbsolutePath());
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                String currentLine;
                while ((currentLine = reader.readLine()) != null) {
                    Matcher matcher = WORD_LIST_ENTRY.matcher(currentLine);
                    if (matcher.find()) {
                        String word = matcher.group(1);
                        if (duplicateWords.contains(word)) {
                            System.out.printf(Locale.ENGLISH, "%s, ", word);
                            continue;
                        }
                    }

                    writer.write(currentLine);
                    writer.newLine();
                }
            }
        }

        Files.copy(tempFile.toPath(), inputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        System.out.println("Done!");
    }

    @InputFiles
    @PathSensitive(RELATIVE)
    public File[] getInputWordsListFiles() {
        return inputWordsListFiles;
    }

    public void setInputWordsListFiles(File[] inputWordsListFiles) {
        this.inputWordsListFiles = inputWordsListFiles;
    }

    @OutputFile
    public File getOutputWordsListFile() {
        return outputWordsListFile;
    }

    public void setOutputWordsListFile(File outputWordsListFile) {
        this.outputWordsListFile = outputWordsListFile;
    }

    @Input
    public String[] getWordsToDiscard() {
        return wordsToDiscard;
    }

    public void setWordsToDiscard(String[] wordsToDiscard) {
        this.wordsToDiscard = wordsToDiscard;
    }

    @Input
    public int getMaxWordsInList() {
        return maxWordsInList;
    }

    public void setMaxWordsInList(int maxWordsInList) {
        this.maxWordsInList = maxWordsInList;
    }

    private File[] inputWordsListFiles;
    private File outputWordsListFile;
    private String[] wordsToDiscard = new String[0];
    private int maxWordsInList = Integer.MAX_VALUE;

    private static class MySaxHandler extends DefaultHandler {

        private final HashMap<String, Integer> allWords;
        private final Set<String> seenBeforeWords;
        private final Set<String> duplicateWords;
        private boolean inWord;
        private final StringBuilder word = new StringBuilder();
        private int freq;

        public MySaxHandler(HashMap<String, Integer> allWords, Set<String> duplicateWords) {
            this.allWords = allWords;
            this.seenBeforeWords = Set.copyOf(allWords.keySet());
            this.duplicateWords = duplicateWords;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes)
                throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            if (qName.equals("w")) {
                inWord = true;
                freq = Integer.parseInt(attributes.getValue("f"));
                word.setLength(0);
            } else {
                inWord = false;
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length);
            if (inWord) {
                word.append(ch, start, length);
            }
        }

        @Override
        public void skippedEntity(String name) throws SAXException {
            System.out.print("Skipped " + name);
            super.skippedEntity(name);
        }

        @Override
        public void warning(SAXParseException e) throws SAXException {
            System.out.print("Warning! " + e);
            super.warning(e);
        }

        @Override
        public void error(SAXParseException e) throws SAXException {
            System.out.print("Error! " + e);
            super.error(e);
        }

        @Override
        public void fatalError(SAXParseException e) throws SAXException {
            System.out.print("Fatal-Error! " + e);
            super.fatalError(e);
        }

        @Override
        public void unparsedEntityDecl(
                String name, String publicId, String systemId, String notationName)
                throws SAXException {
            System.out.print("unparsed-Entity-Decl! " + name);
            super.unparsedEntityDecl(name, publicId, systemId, notationName);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
            if (qName.equals("w") && inWord) {
                String word = this.word.toString();
                if (seenBeforeWords.contains(word)) {
                    duplicateWords.add(word);
                }
                allWords.compute(word, (key, value) -> Math.max(value == null ? 0 : value, freq));
            }

            inWord = false;
        }
    }
}
