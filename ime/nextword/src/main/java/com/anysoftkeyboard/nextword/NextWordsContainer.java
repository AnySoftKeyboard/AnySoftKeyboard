package com.anysoftkeyboard.nextword;

import androidx.collection.ArrayMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class NextWordsContainer {

  private static final NextWord.NextWordComparator msNextWordComparator =
      new NextWord.NextWordComparator();

  public final String word;
  private final List<NextWord> mOrderedNextWord = new ArrayList<>();
  private final Map<String, NextWord> mNextWordLookup = new ArrayMap<>();
  private boolean mNeedsSort = false;

  public NextWordsContainer(String word) {
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
    mNeedsSort = true;
  }

  public void markWordAsUsed(String word) {
    NextWord nextWord = mNextWordLookup.get(word);
    if (nextWord == null) {
      nextWord = new NextWord(word);
      mNextWordLookup.put(word, nextWord);
      mOrderedNextWord.add(nextWord);
    } else {
      nextWord.markAsUsed();
    }
    mNeedsSort = true;
  }

  public List<NextWord> getNextWordSuggestions() {
    if (mNeedsSort) {
      Collections.sort(mOrderedNextWord, msNextWordComparator);
      mNeedsSort = false;
    }

    return mOrderedNextWord;
  }

  @Override
  public String toString() {
    return "(" + word + ") -> [" + mOrderedNextWord.toString() + "]";
  }
}
