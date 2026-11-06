package com.anysoftkeyboard.dictionaries.sqlite;

import android.content.Context;
import androidx.annotation.Nullable;

import java.util.Locale;

public class IgnoredWordsDictionary extends SQLiteUserDictionaryBase {
  public static final String IGNORED_WORDS_DB = "ignored_words.db";

  public IgnoredWordsDictionary(Context context, String locale) {
    super("IgnoredWordsDictionary", context, locale);
  }

  @Override
  protected WordsSQLiteConnection createStorage(String locale) {
    return new WordsSQLiteConnection(mContext, IGNORED_WORDS_DB, locale);
  }

  @Override
  public boolean addWord(String word, int frequency) {
    return super.addWord(word == null ? null : word.toLowerCase(Locale.ROOT), frequency);
  }

  @Override
  public void deleteWord(String word) {
    if (word != null) {
      super.deleteWord(word.toLowerCase(Locale.ROOT));
    }
  }

  @Override
  protected void addWordFromStorageToMemory(String word, int frequency) {
    if (word != null) {
      super.addWordFromStorageToMemory(word.toLowerCase(Locale.ROOT), frequency);
    }
  }

  public boolean isIgnored(@Nullable CharSequence word) {
    return isValidWord(word);
  }

  public boolean isIgnored(char[] word, int offset, int length) {
    return isValidWord(word, offset, length);
  }
}
