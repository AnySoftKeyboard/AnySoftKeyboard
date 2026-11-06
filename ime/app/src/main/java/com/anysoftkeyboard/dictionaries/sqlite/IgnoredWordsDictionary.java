package com.anysoftkeyboard.dictionaries.sqlite;

import android.content.Context;
import androidx.annotation.Nullable;

public class IgnoredWordsDictionary extends SQLiteUserDictionaryBase {
  public static final String IGNORED_WORDS_DB = "ignored_words.db";

  public IgnoredWordsDictionary(Context context, String locale) {
    super("IgnoredWordsDictionary", context, locale);
  }

  @Override
  protected WordsSQLiteConnection createStorage(String locale) {
    return new WordsSQLiteConnection(mContext, IGNORED_WORDS_DB, locale);
  }

  public boolean isIgnored(@Nullable CharSequence word) {
    return isValidWord(word);
  }

  public boolean isIgnored(char[] word, int offset, int length) {
    return isValidWord(word, offset, length);
  }
}
