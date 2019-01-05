import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

class Parser {

    private final static int LOOKING_FOR_WORD_START = 1;
    private final static int LOOKING_FOR_WORD_END = 2;
    private final List<File> mInputFiles;
    private final File mOutputFile;
    private final HashSet<Character> mLangChars;
    private final HashSet<Character> mLangInnerChars;
    private final HashMap<String, WordWithCount> mWords;
    private final int mMaxListSize;
    private final Locale mLocale;

    public Parser(List<File> inputFiles, File outputFile, char[] wordCharacters, Locale locale, char[] additionalInnerWordCharacters, int maxListSize) throws IOException {
        if (inputFiles.size() == 0) {
            throw new IllegalArgumentException("Files list should be at least 1 size.");
        }
        for (File inputFile : inputFiles) {
            if (!inputFile.exists()) {
                throw new IOException("Could not file input file " + inputFile);
            }
            if (!inputFile.isFile()) throw new IOException("Input must be a file.");
        }
        mOutputFile = outputFile;
        mInputFiles = Collections.unmodifiableList(inputFiles);
        mLocale = locale;
        mMaxListSize = maxListSize;

        mLangInnerChars = new HashSet<>(additionalInnerWordCharacters.length + wordCharacters.length);
        mLangChars = new HashSet<>(wordCharacters.length);
        for (char c : wordCharacters) {
            mLangChars.add(c);
            mLangInnerChars.add(c);
        }

        for (char c : additionalInnerWordCharacters) {
            mLangInnerChars.add(c);
        }

        mWords = new HashMap<>();

        System.out.println(String.format(Locale.US, "Parsing %d files for a maximum of %d words, and writing into '%s'.", mInputFiles.size(), mMaxListSize, outputFile));
    }

    public void parse() throws IOException {
        for (File inputFile : mInputFiles) {
            System.out.println(String.format(Locale.US, "Reading input file %s...", inputFile));
            InputStreamReader inputStream = new FileReader(inputFile);
            addWordsFromInputStream(inputFile.length(), inputStream);
            inputStream.close();
            System.out.println(String.format(Locale.US, "Have %d words so far.", mWords.size()));
        }

        System.out.println("Sorting list...");
        List<WordWithCount> sortedList = new ArrayList<>(mWords.values());
        Collections.sort(sortedList);

        System.out.println("Creating output XML file...");

        try (WordListWriter wordListWriter = new WordListWriter(mOutputFile)) {
            sortedList.forEach(word -> WordListWriter.writeWordWithRuntimeException(wordListWriter, word.getWord(), word.getFreq()));
            System.out.println("Done.");
        }
    }

//    public static void createXml(List<WordWithCount> sortedList, Writer outputWriter, int maxListSize, boolean takeFrequencyFromWordObject) throws IOException {
//        final int wordsCount = Math.min(maxListSize, sortedList.size());
//
//        XmlWriter writer = new XmlWriter(outputWriter, false, 0, true);
//        writer.writeEntity("wordlist");
//        for (int wordIndex = 0; wordIndex < wordsCount; wordIndex++) {
//            WordWithCount word = sortedList.get(wordIndex);
//
//            writer.writeEntity("w").writeAttribute("f", Integer.toString(takeFrequencyFromWordObject? word.getFreq() : calcActualFreq(wordIndex, wordsCount))).writeText(word.getWord()).endEntity();
//        }
//        System.out.println("Wrote " + wordsCount + " words.");
//        writer.endEntity();
//    }

    private static int calcActualFreq(double wordIndex, double wordsCount) {
        return Math.min(255, 1 + (int) (255 * (wordsCount - wordIndex) / wordsCount));
    }

    private void addWordsFromInputStream(final long inputSize, InputStreamReader input) throws IOException {
        StringBuilder word = new StringBuilder();
        int intChar;

        int state = LOOKING_FOR_WORD_START;
        long read = 0;
        while ((intChar = input.read()) > 0) {
            if ((read % 50000) == 0 || read == inputSize) {
                System.out.print("." + ((100 * read) / inputSize) + "%.");
            }
            char currentChar = (char) intChar;
            read++;
            switch (state) {
                case LOOKING_FOR_WORD_START:
                    if (mLangChars.contains(currentChar)) {
                        word.append(currentChar);
                        state = LOOKING_FOR_WORD_END;
                    }
                    break;
                case LOOKING_FOR_WORD_END:
                    if (mLangInnerChars.contains(currentChar)) {
                        word.append(currentChar);
                    } else {
                        addWord(word);
                        word.setLength(0);
                        state = LOOKING_FOR_WORD_START;
                    }
            }
        }
        //last word?
        if (word.length() > 0) {
            addWord(word);
        }
        System.out.println("Done.");
    }

    private void addWord(StringBuilder word) {
        //removing all none chars from the end.
        String typedWord = word.toString();
        String wordKey = typedWord.toLowerCase(mLocale);
        if (mWords.containsKey(wordKey)) {
            mWords.get(wordKey).addFreq(typedWord);
        } else {
            mWords.put(wordKey, new WordWithCount(typedWord));
        }
    }

}
