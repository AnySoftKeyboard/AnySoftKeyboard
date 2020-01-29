import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

class WordListWriter implements Closeable {

    private final XmlWriter mXmlWriter;
    private long mWordsWritten;

    WordListWriter(File outputWordsListFile) throws IOException {
        final File parentFile = outputWordsListFile.getParentFile();
        if (!parentFile.exists() && !parentFile.mkdirs()) {
            throw new IllegalArgumentException("Failed to create output folder " + parentFile.getAbsolutePath());
        }
        OutputStreamWriter outputWriter = new OutputStreamWriter(new FileOutputStream(outputWordsListFile), Charset.forName("UTF-8"));
        mXmlWriter = new XmlWriter(outputWriter, true, 0, true);
        mXmlWriter.writeEntity("wordlist");
    }

    public static void writeWordWithRuntimeException(WordListWriter writer, String word, int frequency) {
        try {
            writer.addEntry(word, frequency);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void addEntry(String word, int frequency) throws IOException {
        mXmlWriter
                .writeEntity("w")
                .writeAttribute("f", Integer.toString(frequency))
                .writeText(word)
                .endEntity();
        mWordsWritten++;
    }

    @Override
    public void close() throws IOException {
        mXmlWriter.endEntity();
        System.out.println("Wrote " + mWordsWritten + " words.");
        mXmlWriter.close();
    }

    private static int calcActualFreq(double wordIndex, double wordsCount) {
        return Math.min(255, 1 + (int) (255 * (wordsCount - wordIndex) / wordsCount));
    }

}
