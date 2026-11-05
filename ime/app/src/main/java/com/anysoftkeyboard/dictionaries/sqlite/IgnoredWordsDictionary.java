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
  private final Locale mLocale;

  public IgnoredWordsDictionary(Context context, String locale) {
    super("IgnoredWordsDictionary", context, locale);
    mLocale = locale != null ? new Locale(locale) : Locale.ROOT;
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
  public boolean addWord(String word, int frequency) {
    synchronized (mResourceMonitor) {
      if (isClosed() || word == null) return false;
      mIgnoredWords.add(word.toLowerCase(currentLocale()));
      addWordToStorage(word, frequency);
      return true;
    }
  }

  @Override
  public void deleteWord(String word) {
    synchronized (mResourceMonitor) {
      if (isClosed() || word == null) return;
      mIgnoredWords.remove(word.toLowerCase(currentLocale()));
      deleteWordFromStorage(word);
    }
  }

  @Override
  protected void resetDictionary() {
    super.resetDictionary();
    if (mIgnoredWords != null) {
      mIgnoredWords.clear();
    }
  }

  public boolean isIgnored(@Nullable CharSequence word) {
    if (isLoading() || isClosed() || word == null) return false;
    return mIgnoredWords.contains(word.toString().toLowerCase(currentLocale()));
  }

  public boolean isIgnored(char[] word, int offset, int length) {
    if (isLoading() || isClosed() || word == null || length <= 0) return false;
    return mIgnoredWords.contains(new String(word, offset, length).toLowerCase(currentLocale()));
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
