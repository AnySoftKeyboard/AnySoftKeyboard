package com.anysoftkeyboard.nextword;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * File structure:
 * [1 byte VERSION (HAS TO BE 1]
 * [ENTRIES]
 * [1 byte Word length] [n bytes UTF8 word]
 * [1 byte count of next words]
 * [1 byte Next word length] [n bytes UTF8 word], if n==0 no more next-words
 * ... more entries
 */
public class NextWordsFileParserV1 implements NextWordsFileParser {

    @NonNull
    @Override
    public Iterable<NextWordsContainer> loadStoredNextWords(@NonNull InputStream inputStream) throws IOException {
        final byte[] buffer = new byte[256];
        //assuming that VERSION was read, and InputStream points to the next byte
        LinkedList<NextWordsContainer> loadedEntries = new LinkedList<>();
        String word;
        while (null != (word = readWord(buffer, inputStream))) {
            final int nextWordsCount = inputStream.read();
            if (nextWordsCount <= 0) break;
            final ArrayList<String> nextWords = new ArrayList<>(nextWordsCount);
            String nextWord;
            while (nextWordsCount > nextWords.size() && null != (nextWord = readWord(buffer, inputStream))) {
                nextWords.add(nextWord);
            }
            loadedEntries.add(new NextWordsContainer(word, nextWords));
        }

        return loadedEntries;
    }

    @Nullable
    private String readWord(@NonNull byte[] buffer, @NonNull InputStream inputStream) throws IOException {
        final int bytesToRead = inputStream.read();
        if (bytesToRead < 1) return null;
        final int actualReadBytes = inputStream.read(buffer, 0, bytesToRead);
        if (bytesToRead == actualReadBytes) {
            return new String(buffer, 0, bytesToRead, Charset.forName("UTF-8"));
        } else {
            return null;
        }
    }

    @Override
    public void storeNextWords(@NonNull Iterable<NextWordsContainer> nextWords, @NonNull OutputStream outputStream) throws IOException {
        //assuming output stream is pointing to the start of the file
        outputStream.write(1/*VERSION*/);
        for (NextWordsContainer nextWordsContainer : nextWords) {
            writeWord(outputStream, nextWordsContainer.word);
            int maxWordsToStore = Math.min(12/*the maximum words we want to store*/, nextWordsContainer.getNextWordSuggestions().size());
            outputStream.write(maxWordsToStore);
            for (NextWord nextWord : nextWordsContainer.getNextWordSuggestions()) {
                writeWord(outputStream, nextWord.nextWord);
                maxWordsToStore--;
                if (maxWordsToStore == 0) break;
            }
        }
    }

    private void writeWord(OutputStream outputStream, CharSequence word) throws IOException {
        byte[] buffer = word.toString().getBytes("UTF-8");
        if (buffer.length == 0) return;
        outputStream.write(buffer.length);
        outputStream.write(buffer);
    }
}
