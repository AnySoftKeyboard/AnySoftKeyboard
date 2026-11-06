package com.anysoftkeyboard.dictionaries.sqlite;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class IgnoredWordsDictionary extends SQLiteUserDictionaryBase {
  public static final String IGNORED_WORDS_DB = "ignored_words.db";

  private final Set<String> mIgnoredWords = Collections.newSetFromMap(new ConcurrentHashMap<>());
  private volatile char[][] mIgnoredWordsChars = new char[0][];
  private final Locale mLocale;
  private final boolean mIsTurkishOrAzeri;

  public IgnoredWordsDictionary(Context context, String locale) {
    super("IgnoredWordsDictionary", context, locale);
    mLocale = locale != null ? new Locale(locale) : Locale.ROOT;
    mIsTurkishOrAzeri =
        locale != null
            && ("tr".equalsIgnoreCase(locale)
                || locale.toLowerCase(Locale.ROOT).startsWith("tr_")
                || "az".equalsIgnoreCase(locale)
                || locale.toLowerCase(Locale.ROOT).startsWith("az_"));
  }

  private Locale currentLocale() {
    return mLocale != null ? mLocale : Locale.ROOT;
  }

  @Override
  protected WordsSQLiteConnection createStorage(String locale) {
    return new WordsSQLiteConnection(mContext, IGNORED_WORDS_DB, locale);
  }

  @Override
  protected void addWordFromStorageToMemory(String word, int frequency) {
    if (mIgnoredWords != null && word != null) {
      mIgnoredWords.add(word.toLowerCase(currentLocale()));
    }
  }

  @Override
  protected void loadAllResources() {
    super.loadAllResources();
    updateCachedArray();
  }

  private void updateCachedArray() {
    final String[] words = mIgnoredWords.toArray(new String[0]);
    final char[][] chars = new char[words.length][];
    for (int i = 0; i < words.length; i++) {
      chars[i] = words[i].toCharArray();
    }
    mIgnoredWordsChars = chars;
  }

  @Override
  public boolean addWord(String word, int frequency) {
    synchronized (mResourceMonitor) {
      if (isClosed() || word == null) return false;
      mInAddWord = true;
      try {
        deleteWord(word);
        mIgnoredWords.add(word.toLowerCase(currentLocale()));
        updateCachedArray();
        addWordToStorage(word, frequency);
        return true;
      } finally {
        mInAddWord = false;
      }
    }
  }

  @Override
  public void deleteWord(String word) {
    synchronized (mResourceMonitor) {
      if (isClosed() || word == null) return;
      mIgnoredWords.remove(word.toLowerCase(currentLocale()));
      updateCachedArray();
      deleteWordFromStorage(word);
    }
  }

  @Override
  protected void resetDictionary() {
    super.resetDictionary();
    if (mIgnoredWords != null) {
      mIgnoredWords.clear();
      updateCachedArray();
    }
  }

  private char toLowerCaseChar(char c) {
    if (mIsTurkishOrAzeri) {
      if (c == 'I') return '\u0131';
      if (c == '\u0130') return 'i';
    }
    return Character.toLowerCase(c);
  }

  public boolean isIgnored(@Nullable CharSequence word) {
    if (isLoading() || isClosed() || word == null || word.length() == 0) return false;
    final char[][] cached = mIgnoredWordsChars;
    final int length = word.length();
    for (int i = 0; i < cached.length; i++) {
      final char[] target = cached[i];
      if (target.length != length) continue;
      boolean match = true;
      for (int j = 0; j < length; j++) {
        if (toLowerCaseChar(word.charAt(j)) != target[j]) {
          match = false;
          break;
        }
      }
      if (match) return true;
    }
    return false;
  }

  public boolean isIgnored(char[] word, int offset, int length) {
    if (isLoading()
        || isClosed()
        || word == null
        || length <= 0
        || offset < 0
        || offset + length > word.length) {
      return false;
    }
    final char[][] cached = mIgnoredWordsChars;
    for (int i = 0; i < cached.length; i++) {
      final char[] target = cached[i];
      if (target.length != length) continue;
      boolean match = true;
      for (int j = 0; j < length; j++) {
        if (toLowerCaseChar(word[offset + j]) != target[j]) {
          match = false;
          break;
        }
      }
      if (match) return true;
    }
    return false;
  }

  public boolean isEmpty() {
    return mIgnoredWords.isEmpty();
  }

  @NonNull
  @VisibleForTesting
  /*package*/ Set<String> getIgnoredWords() {
    return mIgnoredWords;
  }
}
