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
  private boolean mIsSorted = true;

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
    // we don't know if the input list is sorted, so we mark it as unsorted
    mIsSorted = false;
  }

  public void markWordAsUsed(String word) {
    mIsSorted = false;
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
    if (!mIsSorted) {
      Collections.sort(mOrderedNextWord, msNextWordComparator);
      mIsSorted = true;
    }

    return mOrderedNextWord;
  }

  @Override
  public String toString() {
    return "(" + word + ") -> [" + mOrderedNextWord.toString() + "]";
  }
}
