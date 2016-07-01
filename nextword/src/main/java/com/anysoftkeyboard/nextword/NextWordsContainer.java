package com.anysoftkeyboard.nextword;

import android.support.v4.util.ArrayMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class NextWordsContainer {

    private static final NextWord.NextWordComparator msNextWordComparator = new NextWord.NextWordComparator();

    public final CharSequence word;
    private final List<NextWord> mOrderedNextWord = new ArrayList<>();
    private final Map<CharSequence, NextWord> mNextWordLookup = new ArrayMap<>();

    public NextWordsContainer(CharSequence word) {
        this.word = word;
    }

    public NextWordsContainer(String word, List<String> nextWords) {
        this.word = word;
        int frequency = nextWords.size();
        for (String nextWordText : nextWords) {
            NextWord nextWord = new NextWord(nextWordText, frequency);
            mNextWordLookup.put(nextWordText, nextWord);
            mOrderedNextWord.add(nextWord);
        }
    }

    public void markWordAsUsed(CharSequence word) {
        NextWord nextWord = mNextWordLookup.get(word);
        if (nextWord == null) {
            nextWord = new NextWord(word);
            mNextWordLookup.put(word, nextWord);
            mOrderedNextWord.add(nextWord);
        } else {
            nextWord.markAsUsed();
        }
    }

    public List<NextWord> getNextWordSuggestions() {
        Collections.sort(mOrderedNextWord, msNextWordComparator);

        return mOrderedNextWord;
    }

    @Override
    public String toString() {
        return "("+word+") -> ["+mOrderedNextWord.toString()+"]";
    }
}
