import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Task to merge several word-list files into one
 */
public class MergeWordsListTask extends DefaultTask {
    @TaskAction
    public void mergeWordsLists() throws IOException, ParserConfigurationException, SAXException {
        if (inputWordsListFiles == null || inputWordsListFiles.length == 0)
            throw new IllegalArgumentException("Must specify at least one inputWordsListFiles");
        if (outputWordsListFile == null)
            throw new IllegalArgumentException("Must supply outputWordsListFile");

        System.out.println("Merging " + inputWordsListFiles.length + " files for maximum " + maxWordsInList + " words, and writing into \'" + outputWordsListFile.getName() + "\'. Discarding " + wordsToDiscard.length + " words.");
        final HashMap<String, WordWithCount> allWords = new HashMap<>();

        for (File inputFile : inputWordsListFiles) {
            System.out.println("Reading " + inputFile.getName() + "...");
            if (!inputFile.exists()) throw new FileNotFoundException(inputFile.getAbsolutePath());
            SAXParserFactory parserFactor = SAXParserFactory.newInstance();
            SAXParser parser = parserFactor.newSAXParser();
            final InputStreamReader inputStream = new InputStreamReader(new FileInputStream(inputFile), Charset.forName("UTF-8"));
            InputSource inputSource = new InputSource(inputStream);
            parser.parse(inputSource, new MySaxHandler(allWords));
            System.out.println("Loaded " + allWords.size() + " words in total...");
            inputStream.close();
        }


        //discarding unwanted words
        if (wordsToDiscard.length > 0) {
            System.out.print("Discarding words...");
            Arrays.stream(wordsToDiscard).forEach(word -> {
                if (allWords.remove(word) != null) System.out.print(".");
            });
            System.out.println();
        }


        System.out.println("Sorting list...");
        List<WordWithCount> sortedList = new ArrayList<WordWithCount>(allWords.values());
        Collections.sort(sortedList);

        System.out.println("Creating output XML file...");
        final File parentFile = outputWordsListFile.getParentFile();
        if (!parentFile.exists() && !parentFile.mkdirs()) {
            throw new IllegalArgumentException("Failed to create output folder " + parentFile.getAbsolutePath());
        }
        Writer output = new OutputStreamWriter(new FileOutputStream(outputWordsListFile), Charset.forName("UTF-8"));
        Parser.createXml(sortedList, output, maxWordsInList, true);

        output.flush();
        output.close();

        System.out.println("Done.");
    }

    @InputFiles
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
        public MySaxHandler(HashMap<String, WordWithCount> allWords) {
            this.allWords = allWords;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
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
        public void unparsedEntityDecl(String name, String publicId, String systemId, String notationName) throws SAXException {
            System.out.print("unparsedEntityDecl! " + name);
            super.unparsedEntityDecl(name, publicId, systemId, notationName);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
            if (qName.equals("w") && inWord) {
                WordWithCount wordWithCount = new WordWithCount(word.toString(), freq);
                if (allWords.containsKey(wordWithCount.getKey())) {
                    allWords.get(wordWithCount.getKey()).addOtherWord(wordWithCount);
                } else {
                    allWords.put(wordWithCount.getKey(), wordWithCount);
                }

            }

            inWord = false;
        }

        private HashMap<String, WordWithCount> allWords;
        private boolean inWord;
        private StringBuilder word = new StringBuilder();
        private int freq;
    }
}
