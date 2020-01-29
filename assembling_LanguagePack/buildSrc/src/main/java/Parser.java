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
import java.util.stream.Collectors;

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
    private final int mMaxWordFrequency;

    public Parser(List<File> inputFiles, File outputFile, char[] wordCharacters, Locale locale, char[] additionalInnerWordCharacters, int maxListSize, int maxFrequency) throws IOException {
        if (inputFiles.size() == 0) {
            throw new IllegalArgumentException("Files list should be at least 1 size.");
        }
        for (File inputFile : inputFiles) {
            if (!inputFile.exists()) {
                throw new IOException("Could not file input file " + inputFile);
            }
            if (!inputFile.isFile()) throw new IOException("Input must be a file.");
        }
        if (maxFrequency > 255) {
            throw new IllegalArgumentException("max-word-frequency can not be more than 255");
        }
        mMaxWordFrequency = maxFrequency;
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

        System.out.println(
                String.format(Locale.US, "Parsing %d files for a maximum of %d words (with max-frequency %d), and writing into '%s'.", mInputFiles.size(), mMaxListSize, maxFrequency, outputFile));
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

        if (mMaxListSize < sortedList.size()) {
            System.out.println("Removing over-the-limit words...");
            while (mMaxListSize > sortedList.size()) sortedList.remove(mMaxListSize - 1);
        }

        final double maxFrequencyFactor = sortedList.stream()
                .max((w1, w2) -> w1.getFreq() - w2.getFreq())
                .map(WordWithCount::getFreq)
                .map(currentMaxFreq -> (double) currentMaxFreq)
                .map(currentMaxFreq -> ((double) mMaxWordFrequency) / currentMaxFreq)
                .orElseThrow(() -> new IllegalStateException("could not find max-frequency word. No words provided?"));
        System.out.println(String.format(Locale.US, "Adjusting frequencies with factor %.4f...", maxFrequencyFactor));
        sortedList = sortedList.stream()
                .map(word -> new WordWithCount(word.getWord(), 1 + (int) (word.getFreq() * maxFrequencyFactor)))
                .collect(Collectors.toList());

        System.out.println("Creating output XML file...");

        try (WordListWriter wordListWriter = new WordListWriter(mOutputFile)) {
            sortedList.forEach(word -> WordListWriter.writeWordWithRuntimeException(wordListWriter, word.getWord(), word.getFreq()));
            System.out.println("Done.");
        }
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
            char currentChar = fixup(intChar);
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

    private char fixup(int intChar) {
        switch (intChar) {
            case '’':
                return '\'';
            case '”':
            case '“':
                return '\"';
            default:
                return (char) intChar;
        }
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
